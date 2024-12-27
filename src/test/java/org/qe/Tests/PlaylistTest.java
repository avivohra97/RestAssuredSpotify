package org.qe.Tests;

import io.qameta.allure.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.qe.POJO.Item;
import org.qe.POJO.PlaylistPOJO;
import org.qe.Utils.Encode;
import org.qe.Utils.PropertyBuilder;
import org.qe.spec.SpecBuilder;
import org.qe.spec.TokenManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import static io.qameta.allure.SeverityLevel.CRITICAL;
import static io.restassured.RestAssured.*;


@Epic("Spotify api usecases")
@Feature("api features")
public class PlaylistTest {

    RequestSpecification requestSpecification;
    ResponseSpecification responseSpecification;
    String access_token;
    Response userPlaylist;
    Properties prop;
    String baseUri = "https://api.spotify.com/";
    String filePath = System.getProperty("user.dir")+"/src/test/resources/config.properties";
    @BeforeClass
    public void beforeClass() throws Exception {
        prop = PropertyBuilder.setUpProperties(filePath);
        access_token = TokenManager.getToken(prop);
        this.requestSpecification = SpecBuilder.reqSpec(baseUri,"v1",access_token);
        this.responseSpecification = SpecBuilder.resSpec();
    }
     @Test(description = "testng desc to get user details")
     @Story("club cases")
    public void getUserDetails() throws IOException {
        Response response = given().spec(requestSpecification).
                get("/me").then().spec(responseSpecification).
                and().extract().response();
        String userId = response.path("id");
        PropertyBuilder.setProperty("userId",userId);
        PropertyBuilder.updateProperty(filePath);
        System.out.println(userId+" : is the user id");
    }
    @Description("allure desc for get user playlists")
    @Test(dependsOnMethods = "getUserDetails")
    public void getUserPlaylists() throws IOException {
        userPlaylist = given().spec(requestSpecification).
                pathParam("userId",prop.getProperty("userId")).
                get("/users/{userId}/playlists").
                then().spec(responseSpecification).
                and().extract().response();
        List<String> playListIds = userPlaylist.path("items.id");
        System.out.println(playListIds.toString()+" : is the playlists id id");
        String playListId = userPlaylist.path("items[0].id");
        PropertyBuilder.setProperty("playListId",playListId);
        PropertyBuilder.updateProperty(filePath);
        System.out.println(playListId+" : is the playlist id");

    }

    @Test(dependsOnMethods = "getUserDetails")
    public void createPlaylist(){
        double val = Math.random()*10;
        Item reqPlaylist = new Item();
        reqPlaylist.setName("create pojo Playlist "+val);
        reqPlaylist.setDescription("pojo create desc");
        reqPlaylist.setPublic(false);
        Item respPlaylist = given().
                spec(requestSpecification).
                pathParam("userId",prop.getProperty("userId")).
                body(reqPlaylist).
        post("/users/{userId}/playlists").
        then().
                assertThat().
                statusCode(201).
                extract().response().as(Item.class);
        System.out.println("playlist created");
        Assert.assertEquals(reqPlaylist.getName(),respPlaylist.getName());
    }

    @Test(dependsOnMethods = {"getUserPlaylists","getUserDetails"})
    @Description("This test attempts to log into the website using a login and a password. Fails if any error happens.\n\nNote that this test does not test 2-Factor Authentication.")
    @Severity(CRITICAL)
    @Owner("John Doe")
    @Link(name = "Website", url = "https://dev.example.com/")
    @Issue("AUTH-123")
    @TmsLink("TMS-456")
    @Story("club cases")
    @Step
    public void updatePlaylist(){

        Item existingRecord = userPlaylistRecordSingle(prop.getProperty("playListId"));
        String existingName = existingRecord.getName();
        String existingId = existingRecord.getId();
        System.out.println("case "+existingRecord.toString());

        double val = Math.random()*10;
        Item reqPlaylist = new Item();
        reqPlaylist.setName("Updated pojo Playlist "+val);
        reqPlaylist.setDescription("pojo updated desc "+ val);
        reqPlaylist.setPublic(false);
        given().spec(requestSpecification).
                pathParam("playlistId",prop.getProperty("playListId")).
                body(reqPlaylist).
                contentType(ContentType.JSON).
                put("/playlists/{playlistId}").
                then().
                assertThat().
                statusCode(200);

        Item newRecord = userPlaylistRecordSingle(prop.getProperty("playListId"));
        System.out.println("case "+newRecord.toString());

        String newName = newRecord.getName();
        String newId = newRecord.getId();
        Assert.assertEquals(newId,existingId);
        Assert.assertNotSame(existingName,newName);
    }

    public PlaylistPOJO userPlaylistRecord(){
       PlaylistPOJO newResp = given().spec(requestSpecification).
                pathParam("userId",prop.getProperty("userId")).
                get("/users/{userId}/playlists").
                then().spec(responseSpecification).
                assertThat().
                statusCode(200).and().extract().response().as(PlaylistPOJO.class);
       return newResp;
    }

    @Step
    public Item userPlaylistRecordSingle(String id){
        Item newResp;
        newResp = given().spec(requestSpecification).
                pathParam("playlistId",id).
                get("/playlists/{playlistId}").

                then().spec(responseSpecification).
                assertThat().
                statusCode(200).and().extract().response().as(Item.class);
        return newResp;
    }

    @Test
    public void updatePlaylistInvalid() {
        Item invalidItem = new Item();
        invalidItem.setName("");
        invalidItem.setDescription("invalid desc");
        invalidItem.setPublic(false);
        Response response = given().spec(requestSpecification).
                pathParam("playlistId", 12459).
                body(invalidItem).
                put("/playlists/{playlistId}").
                then().
                assertThat().
                statusCode(400).
                extract().response();
    }

}
