package org.atg.assigment.uitest;

import groovy.util.logging.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.*;
@Slf4j
public class ATGV4 {
    private static final Logger log = LoggerFactory.getLogger(ATGV4.class);
    static WebDriver driver;

    public static void main(String[] args) {

        driver = new ChromeDriver();
        getRaceDetails("V4",4);

        clearCoupon(); // To make sure all the races are empty

        selectCoupon();

        String[] myHorsesSelectionPerRace = new String[] {"4", "1", "2", "alla"};
        selectHorse(Arrays.asList(myHorsesSelectionPerRace));

        // click on the spel button
        driver.findElement(By.xpath("//button[@data-test-id='play-game-coupon']")).click();
    }

    private static void getRaceDetails(String raceName,int expectedRaceCount) {
        driver.get("https://www.atg.se/" + raceName);
        hardSleep(2000);
        driver.findElement(By.xpath("//button[@data-test-id='acceptAllCookiesBtn']")).click();
        hardSleep(2000);
        // open the V4 coupon via spel nu button
        driver.findElement(By.xpath("//a[@data-test-id='product-pages-bet-now-btn']")).click();

        // making sure that we have 4 races
        List<WebElement> races = driver.findElements(By.xpath("//button[@data-test-id='race-menu-leg']"));

        //check the count
       Assert.assertEquals(races.size(),expectedRaceCount);
    }

    public static void clearCoupon() {
        // Select the ... and then rensa button followed by bekräfta
        hardSleep(2000);
        driver.findElement(By.xpath("//button[contains(@class, 'toggleActionMenu')]")).click(); // Click the button with ...
        driver.findElement(By.xpath("//li[@value='clean-coupon']")).click(); // Click rensa
        driver.findElement(By.xpath("//button[contains(text(), 'Bekräfta')]")).click(); // Select bekrafta
    }

    private static void selectCoupon() {
        try
        {
            hardSleep(2000);
            driver.findElement(By.xpath("//button[@data-test-id='expand-coupon']")).click();
        }
        catch(NoSuchElementException e){
            log.info("Coupon is already expanded");
        }

    }

    public static void selectHorse(List<String> myHorsesSelectionPerRace) {

        for (String selection : myHorsesSelectionPerRace) {
            hardSleep(2000);
            int raceNumber = myHorsesSelectionPerRace.indexOf(selection) + 1;

            // Get the list of all the horses in the race and removes the scratched horses
            String horseXPath = "//button[contains(@data-test-id,'coupon-button-leg-" + raceNumber + "-start-')][@data-test-scratched='false']";

            if(selection.equalsIgnoreCase("Alla")) {
                driver.findElement(By.xpath("//div[@data-test-id='coupon-race-" + raceNumber + "']//button[text()='Alla']")).click();
            } else {
                // Gets the details of total no of horses in the race
                List<WebElement> horsesInTheRace = driver.findElements(By.xpath(horseXPath));
                // Get the horse number
                List<Integer> unScratchedHorses = new ArrayList<>();
                for(WebElement horse:horsesInTheRace)
                {
                    unScratchedHorses.add(Integer.parseInt(horse.getText()));
                }
                // After getting all the unscratched horses.Shuffle them and select the respective number of horses participating in the race
                Collections.shuffle(unScratchedHorses);

                for (int i = 0; i < Integer.parseInt(selection); i++) {
                    //selecting the horse in the race by clicking
                    driver.findElement(By.xpath("//button[contains(@data-test-id,'coupon-button-leg-" + raceNumber + "-start-" + unScratchedHorses.get(i) + "')]" )).click();
                }
            }
        }
    }

    private static void hardSleep(int milliSeconds) {
        // wait for the window to appear
        try {
            // Wait for 2 seconds (use explicit wait in real scenarios)
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}


