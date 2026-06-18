package org.example;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContactListTest {

    private static final String EMAIL    = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "@testmail.com";
    private static final String PASSWORD = "Test@12345";
    private static final String FIRST_NAME = "Test";
    private static final String LAST_NAME  = "User";

    private static String token;
    private static String randomizedFirstName;
    private static String randomizedLastName;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "https://thinking-tester-contact-list.herokuapp.com";
    }

    @Test
    @Order(1)
    public void addUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", FIRST_NAME);
        user.put("lastName",  LAST_NAME);
        user.put("email",     EMAIL);
        user.put("password",  PASSWORD);

        token = given()
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .post("/users")
                .then()
                .log().all()
                .statusCode(201)
                .body("user._id",         notNullValue())
                .body("user.firstName",   equalTo(FIRST_NAME))
                .body("user.lastName",    equalTo(LAST_NAME))
                .body("user.email",       equalTo(EMAIL))
                .extract().jsonPath().getString("token");
    }

    @Test
    @Order(2)
    public void getUser() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/users/me")
                .then()
                .log().all()
                .statusCode(200)
                .body("firstName", equalTo(FIRST_NAME))
                .body("lastName",  equalTo(LAST_NAME))
                .body("email",     equalTo(EMAIL));
    }

    @Test
    @Order(3)
    public void updateUser() {
        randomizedFirstName = "Can_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        randomizedLastName = "Koca_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("firstName", randomizedFirstName);
        updateBody.put("lastName", randomizedLastName);

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateBody)
                .when()
                .patch("/users/me")
                .then()
                .log().all()
                .statusCode(200)
                .body("firstName", equalTo(randomizedFirstName))
                .body("lastName",  equalTo(randomizedLastName));
    }

    @Test
    @Order(4)
    public void getUpdatedUser() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/users/me")
                .then()
                .log().all()
                .statusCode(200)
                .body("firstName", equalTo(randomizedFirstName))
                .body("lastName",  equalTo(randomizedLastName));
    }

    @Test
    @Order(5)
    public void deleteUser() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/users/me")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    @Order(6)
    public void verifyUserDeletion() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/users/me")
                .then()
                .log().all()
                .statusCode(401);
    }
}
