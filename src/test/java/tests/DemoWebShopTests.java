package tests;

import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.restassured.http.Cookies;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.filters;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.baseURI;
import static listeners.CustomAllureListener.customAllureTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

public class DemoWebShopTests {

    @BeforeAll
    static void beforeAll() {
        baseURI = "http://demowebshop.tricentis.com";
        filters(customAllureTemplate());
    }

    @Test
    @Owner("KELONMYOSA")
    @DisplayName("Проверка подписки на новости")
    @Link(value = "Testing URL", url = "http://demowebshop.tricentis.com")
    void subscribeNewsLetterTest() {
        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("email=123%40123.ru")
                .when()
                .post("/subscribenewsletter")
                .then()
                .statusCode(200)
                .body("Success", is(true))
                .body("Result", is("Thank you for signing up! A verification email has been sent." +
                        " We appreciate your interest."));
    }

    @Test
    @Owner("KELONMYOSA")
    @DisplayName("Проверка корректного добавления двух товаров в корзину")
    @Link(value = "Testing URL", url = "http://demowebshop.tricentis.com/")
    void addTwoItemsToCartTest() {
        Cookies cookies =
                given()
                        .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                        .body("addtocart_31.EnteredQuantity=1")
                        .when()
                        .post("/addproducttocart/details/31/1")
                        .then()
                        .statusCode(200)
                        .body("success", is(true))
                        .body("message", is("The product has been added to your " +
                                "<a href=\"/cart\">shopping cart</a>"))
                        .body("updatetopcartsectionhtml", is("(1)"))
                        .extract().response().getDetailedCookies();

        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .cookie(String.valueOf(cookies))
                .body("addtocart_43.EnteredQuantity=1")
                .when()
                .post("http://demowebshop.tricentis.com/addproducttocart/details/43/1")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("message", is("The product has been added to your " +
                        "<a href=\"/cart\">shopping cart</a>"))
                .body("updatetopcartsectionhtml", is("(2)"));

        String checkCartHTML =
                given()
                        .cookie(String.valueOf(cookies))
                        .when()
                        .get("/cart")
                        .then()
                        .statusCode(200)
                        .extract().response().asString();

        Document doc = Jsoup.parse(checkCartHTML);
        String fistItem = doc.select("div.name").get(0).text();
        String secondItem = doc.select("div.name").get(1).text();

        assertThat(fistItem).isEqualTo("Smartphone");
        assertThat(secondItem).isEqualTo("14.1-inch Laptop");
    }
}
