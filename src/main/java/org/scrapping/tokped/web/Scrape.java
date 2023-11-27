package org.scrapping.tokped.web;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class Scrape {
    private static WebDriver driver;

    public static boolean IsTokopedia(java.lang.String url) {
        if (url.startsWith("https://www.tokopedia.com")) {
            return true;
        }
        return false;
    }
    public static void RunScraping() {
        System.out.println("Setting up...");

        driver = new ChromeDriver();
        System.out.println("Scraping...");
        java.lang.String path = "/home/acla.hamzah/work-shopee/tokped-web-scrapping/appium/scraping-100k-data/categories_v2.csv";

        try {
            FileReader filereader = new FileReader(path);

            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withCSVParser(parser)
                    .build();

            List<java.lang.String[]> allData = csvReader.readAll();

            for (java.lang.String[] row : allData) {
                try {
                    for (java.lang.String cell : row) {
                        java.lang.String[] values = cell.split(",");

                        if (values.length >= 5 & !Objects.equals(values[0], "id")) {
                            java.lang.String categoryIdFromDB = values[0];
                            java.lang.String nameFromDB = values[1];
                            java.lang.String categoryURLFromScraping = values[4];
                            System.out.print(categoryIdFromDB + " " + nameFromDB + " " + categoryURLFromScraping + "\t");

                            driver.get(categoryURLFromScraping);
                            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(10000));

                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            for (int i = 0; i < 17; i++) {
                                js.executeScript("window.scrollBy(0,250)", "");
                                Thread.sleep(100);
                            }
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"zeus-root\"]/div/div[2]/div/div[2]/div/div[2]/div[3]/div[2]/nav/ul/li[11]/button")));

                            Document doc = Jsoup.parse(driver.getPageSource());
                            Elements items = doc.getElementsByClass("css-bk6tzz e1nlzfl2");
                            Thread.sleep(2000);
                            System.out.println(items.toArray().length + "Item");
                            for (Element item : items) {
                                Element anchor = item.child(0);
                                java.lang.String url = anchor.attr("href");

                                if (!IsTokopedia(url)) {
                                    System.out.println("url not from tokopedia or a top ads");
                                    continue;
                                }
                                driver.get(url);

                                // get data
                                WebDriverWait waitProductName = new WebDriverWait(driver, Duration.ofMillis(10000));
                                WebDriverWait waitProductDesc = new WebDriverWait(driver, Duration.ofMillis(10000));
                                WebDriverWait waitProductTitle = new WebDriverWait(driver, Duration.ofMillis(10000));
                                WebDriverWait waitProductImage = new WebDriverWait(driver, Duration.ofMillis(10000));
                                StringBuilder str1 = new StringBuilder();

                                try {
                                    waitProductName.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"pdp_comp-product_content\"]/div/h1")));
                                    WebElement title = driver.findElement(By.xpath("//*[@id=\"pdp_comp-product_content\"]/div/h1"));

                                    waitProductDesc.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"pdp_comp-product_detail\"]/div[2]/div[2]/div/span/span/div")));
                                    WebElement description = driver.findElement(By.xpath("//*[@id=\"pdp_comp-product_detail\"]/div[2]/div[2]/div/span/span/div"));

                                    waitProductTitle.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"pdpFloatingActions\"]/div[4]/div/p[2]")));
                                    WebElement price = driver.findElement(By.xpath("//*[@id=\"pdpFloatingActions\"]/div[4]/div/p[2]"));

                                    waitProductImage.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"pdp_comp-product_media\"]/div/div[2]/div/div/div[1]/div/div/img")));
                                    List<WebElement> image_url_div = driver.findElements(By.cssSelector("[data-testid=\"PDPImageThumbnail\"]"));

                                    str1.append("\ntitle: ").append(title.getText());
                                    str1.append("\ndetail: ").append(description.getText());
                                    for (WebElement e : image_url_div) {
                                        WebElement eDiv = e.findElement(By.tagName("div"));
                                        WebElement eDiv2 = eDiv.findElement(By.className("css-bqlp8e"));
                                        WebElement eDiv3 = eDiv2.findElement(By.className("css-1c345mg"));
                                        java.lang.String image_url = eDiv3.getAttribute("src");

                                        str1.append("\nimages: ").append(image_url);
                                    }
                                    str1.append("\ncategoryId: ").append(categoryIdFromDB);
                                    str1.append("\nprice: ").append(price.getText()).append("\n");
                                } catch (Exception e) {
                                    System.out.println("An Error Occured. skip to next product...");
                                    continue;
                                }

                                java.lang.String txtPath = "/home/acla.hamzah/work-shopee/tokped-web-scrapping/appium/scraping-100k-data/result.txt";
                                byte[] arr = str1.toString().getBytes();
                                try {
                                    Files.write(Paths.get(txtPath), arr, StandardOpenOption.APPEND);
                                } catch (IOException io) {
                                    System.out.println("error invalid path: " + io.getMessage());
                                }

                                Thread.sleep(1000);
                            }

                            Thread.sleep(1000);
                        }
                    }
                    System.out.println();

                } catch (Exception e) {
                    continue;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
