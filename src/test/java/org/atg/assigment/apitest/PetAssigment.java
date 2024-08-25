package org.atg.assigment.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import io.restassured.specification.RequestSpecification;

public class PetAssigment {
    private static JSONObject petCreationRequestBody(int petId, int categoryId, String categoryName, String name, String urls, int tagId, String tagName, String status) {
        JSONObject category = new JSONObject();
        category.put("id", categoryId);
        category.put("name", categoryName);

        List<String> photoUrls = new ArrayList<>();
        photoUrls.add(urls);

        List<JSONObject> tags = new ArrayList<>();
        JSONObject tagsObject = new JSONObject();
        tagsObject.put("id", tagId);
        tagsObject.put("name", tagName);
        tags.add(tagsObject);


        JSONObject newPetPayload = new JSONObject();
        newPetPayload.put("id", petId);
        newPetPayload.put("category", category);
        newPetPayload.put("name", name);
        newPetPayload.put("photoUrls", photoUrls);
        newPetPayload.put("tags", tags);
        newPetPayload.put("status", status);
        return newPetPayload;
    } // can be removed if we add the data to data provider

    @BeforeTest
    public static void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";
    }

    // 1. Upload image - passed
    @DataProvider(name = "uploadPic")
    public Object[][] uploadPicData() {
        return new Object[][]{
                {12, "metadata", "C:\\dog pic.png", 200}, // place your image path here
                {12, "", "C:\\dog pic.png", 200}
        };
    }

    @Test(dataProvider = "uploadPic", testName = "Uploading the picture for the pet")
    public void uploadPic(int petId, String metadata, String filePath, int expectedResponseCode) {
        Response response = given()
                .header("Content-type", "multipart/form-data")
                .param("petid", petId)
                .multiPart("additionalMetadata", metadata)
                .multiPart("file", new File(filePath))
                .when()
                .post("/pet/" + petId + "/uploadImage")
                .then()
                .extract().response();
        Assert.assertEquals(response.statusCode(), expectedResponseCode);
        Assert.assertEquals(Integer.parseInt(response.jsonPath().getString("code")), expectedResponseCode);
    }

    // 2. Add new pet to the store
    // 2.1 New pet with all the details - passed
    @DataProvider(name = "createPet")
    public Object[][] createPetData() {
        return new Object[][]{
                {12, 0, "domestic", "dog", "C:\\Pictures\\dog.png", 0, "pet", "available", 200},
                {13, 0, "wild", "lion", "C:\\Pictures\\lion.png", 0, "zoo", "available", 200},
                {14, 0, "", "", "", 0, "", "", 200}
        };
    }

    @Test(dataProvider = "createPet", testName = "Adding a new pet with Post request")
    public void addNewPet(int petId, int categoryId, String categoryName, String name, String urls, int tagId, String tagName, String status, int expectedResponseCode) {
        JSONObject newPetPayload = petCreationRequestBody(petId, categoryId, categoryName, name, urls, tagId, tagName, status);
        ;
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(newPetPayload.toString())
                .when()
                .post("/pet")
                .then()
                .extract().response();
        Assert.assertEquals(response.statusCode(), expectedResponseCode);
        Assert.assertEquals(Integer.parseInt(response.jsonPath().getString("id")), petId);
    }

    // 2.2 New pet with min data - passed
    @DataProvider(name = "createPetMin")
    public Object[][] createPetMinData() {
        return new Object[][]{
                {12345, null, null},
                {null, "puppy", null},
                {null, null, null}
        };
    }

    @Test(dataProvider = "createPetMin", testName = "Adding a new pet with few fields")
    public void addNewPetMinimumData(Integer petId, String name, List<JSONObject> tags) {
        JSONObject newPetSemiPayload = new JSONObject();
        if (petId != null) {
            newPetSemiPayload.put("id", petId);
        } else if (name != null) {
            newPetSemiPayload.put("name", name);
        } else if (tags != null) {
            newPetSemiPayload.put("tags", tags);
        }
        Response response = given()
                .header("Content-type", "application/json")
                .body(newPetSemiPayload.toString())
                .when()
                .post("/pet")
                .then()
                .extract().response().prettyPeek();

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.getBody().jsonPath().get("id"));
    }

    // 3. Update an existing pet - passed  ( It is updating a pet if it exists, if not creating a new one
    @DataProvider(name = "updateExistingPet")
    public Object[][] updateExistingPetData() {
        return new Object[][]{
                {12, 0, "domestic", "dog", "C:\\Pictures\\dog.png", 0, "pet", "available", 200},
                {13, 0, "wild", "lion", "C:\\Pictures\\lion.png", 0, "zoo", "available", 200},
                {15, 0, "", "donkey", "", 0, "", "pending", 200}
                //{21, null, "", "donkey", "", 0, "", "pending", 200} //few values case throwing error????

        };
    }

    @Test(dataProvider = "updateExistingPet", testName = " Updating the existing pet")
    public void updateExistingPet(int petId, int categoryId, String categoryName, String name, String urls, int tagId, String tagName, String status, int expectedResponseCode) {
        JSONObject updatePetPayload = petCreationRequestBody(petId, categoryId, categoryName, name, urls, tagId, tagName, status);
        Response response = given()
                .header("Content-type", "application/json")
                .body(updatePetPayload.toString())
                .when()
                .put("/pet")
                .then()
                .extract().response();
        Assert.assertEquals(response.statusCode(), expectedResponseCode);
        Assert.assertEquals(Integer.parseInt(response.jsonPath().getString("id")), petId);
    }

    // 4. Finds pets by status
    @DataProvider(name = "petByStatus")
    public Object[][] petByStatusData() {
        return new Object[][]{
                {"available"},
                {"available", "pending"},
        };
    }

    @Test(dataProvider = "petByStatus", testName = "Find pet by status")
    public void findPetByStatus(String[] status) {
        RequestSpecification requestSpecification = given()
                .header("Content-type", "application/json")
                .when();
                for (String s : status) {
                    requestSpecification.queryParam("status", s);
                }
                Response response = requestSpecification
                .get("/pet/findByStatus")
                .then()
                .extract().response();
                Assert.assertEquals(response.statusCode(), 200);
    }


    // 5. get pet by ID
    // 5.1 Finding a pet by giving the valid ID - passed
    @DataProvider(name = "petByIdSuccess")
    public Object[][] petByIdSuccessData() {
        return new Object[][]{
                {15, 200}
        };
    }

    @Test(dataProvider = "petByIdSuccess", testName = "Get the pet by ID success case")
    public void getPetByIdSuccess(int petID, int expectedResponseCode) {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/pet/" + petID)
                .then()
                .extract().response();
        Assert.assertEquals(response.statusCode(), expectedResponseCode);
        Assert.assertEquals(Integer.parseInt(response.jsonPath().getString("id")), petID);
    }

    // 5.2 Finding a pet by invalid ID gives error - passed
    @DataProvider(name = "petByIdFailure")
    public Object[][] petByIdFailureData() {
        return new Object[][]{
                {11, 404, "Pet not found"}
        };
    }

    @Test(dataProvider = "petByIdFailure", testName = "Get the pet by ID failure case")
    public void getPetByIdFailure(int petID, int expectedResponseCode, String message) {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/pet/" + petID)
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), expectedResponseCode);
        Assert.assertEquals(response.jsonPath().getString("message"), message);
    }

    // 6. Updates a pet in the store with formdata - passed( It covers both cases updating an existing pet & if pet doesn't exist throws 404)
    @DataProvider(name = "updatePet")
    public Object[][] updatePetData() {
        return new Object[][]{
                {13, "dog", "ready", 200, "13"},
                {11, "bunny", "available", 404, "not found"},
        };
    }

    @Test(dataProvider = "updatePet", testName = "Update the status and name of the pet for a given pet id")
    public void petUpdate(int petID, String name, String status, int expectedResponseCode, String message) {
        Response response = given()
                .param("petid", petID)
                .formParam("name", name)
                .formParam("status", status)
                .when()
                .post("/pet/" + petID)
                .then()
                .extract().response().peek();
        Assert.assertEquals(response.statusCode(), expectedResponseCode);
        Assert.assertEquals(response.jsonPath().getString("message"), message);
    }

    // 7.Delete a pet - passed
    //7.1 Delete unexisting pet
    @DataProvider(name = "deleteUnExistingPet")
    public Object[][] deleteUnExistingPetData() {
        return new Object[][]{
                {11, 404}
        };
    }

    @Test(dataProvider = "deleteUnExistingPet", testName = "Deleting an non existing pet should through exception")
    public void deletePetNotInExistence(int petId, int expectedResponseCode) {
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .delete("/pet/" + petId)
                .then()
                .extract().response().peek();
        Assert.assertEquals(response.statusCode(), expectedResponseCode);
    }

    // 7.2 Delete existing pet
    @DataProvider(name = "deleteExistingPet")
    public Object[][] deleteExistingPetData() {
        return new Object[][]{
                {12, 200},
        };
    }

    @Test(dataProvider = "deleteExistingPet", testName = "Deleting an existing pet")
    void deleteExistingPet(int petId, int expectedResponseCode) {
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .delete("/pet/" + petId)
                .then()
                .extract().response().peek();
        Assert.assertEquals(response.statusCode(), expectedResponseCode);
        Assert.assertEquals(Integer.parseInt(response.jsonPath().getString("message")), petId);
    }
}
