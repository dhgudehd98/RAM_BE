package com.sh.Ram.selenium;

import com.sh.Ram.entity.Member;
import com.sh.Ram.entity.Product;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KreamDataCrawling  {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;


//    @Bean
    @Transactional
    public void run() throws Exception {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            driver.get("https://kream.co.kr/search?shop_category_id=23&title=%ED%9B%84%EB%93%9C&exclude_filter=category_id");

            Thread.sleep(5000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".search-screen")
            ));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (int i = 0; i < 5; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
                Thread.sleep(2000);
            }

            List<WebElement> items = driver.findElements(
                    By.cssSelector("a.product_card")
            );
            System.out.println("총 상품 수 : " + items.size());

            Member member = memberRepository.getReferenceById(1L);

            for (WebElement item : items) {
                try {
                    String imageUrl = "";
                    try {
                        imageUrl = item.findElement(
                                By.cssSelector(".image_container .base-image-responsive__image")
                        ).getAttribute("src");
                    } catch (Exception e) {
                        imageUrl = "이미지 없음";
                    }

                    String brand = "";
                    try {
                        brand = item.findElement(
                                By.cssSelector("[data-sdui-id^='product_brand_name'] p")
                        ).getText();
                    } catch (Exception e) {
                        brand = "브랜드 없음";
                    }

                    String name = "";
                    try {
                        List<WebElement> pTags = item.findElements(
                                By.cssSelector(".text_body p")
                        );
                        name = pTags.size() > 1 ? pTags.get(1).getText() : pTags.get(0).getText();
                    } catch (Exception e) {
                        name = "상품명 없음";
                    }

                    String price = "";
                    try {
                        price = item.findElement(
                                By.cssSelector(".price-info-container p.semibold")
                        ).getText();
                    } catch (Exception e) {
                        price = "0";
                    }

                    if (brand.equals("브랜드 없음") && name.equals("상품명 없음")) {
                        continue;
                    }

                    // 가격 변환 "157,000원" → 157000
                    int priceInt = 0;
                    try {
                        priceInt = Integer.parseInt(
                                price.replaceAll("[^0-9]", "")  // 숫자만 추출
                        );
                    } catch (Exception e) {
                        priceInt = 0;
                    }

                    String description = "후드티입니다 기모입니다.";
                    Product product = new Product(member, name, description, priceInt, imageUrl);
                    productRepository.save(product);

                    System.out.println("저장완료 - 브랜드 : " + brand + " 상품명 : " + name + " 가격 : " + priceInt);

                } catch (Exception e) {
                    System.out.println("스킵 : " + e.getMessage());
                }
            }

        } finally {
            driver.quit();
        }
    }
}