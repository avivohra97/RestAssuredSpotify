package org.qe;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.MatcherAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.*;
import static io.restassured.specification.ProxySpecification.auth;

public class PlaylistTest {

    RequestSpecification requestSpecification;
    ResponseSpecification responseSpecification;
    String access_token;
    String userId;
    String playListId;
    Response userPlaylist;
    Properties prop;

    @BeforeClass
    public void beforeClass() throws IOException {
        prop = new Properties();
        prop.load(new FileInputStream(new File(System.getProperty("user.dir")+"/src/test/resources/config.properties")));
        access_token = prop.getProperty("access_token");
        if(getUserDetailsCheck() == 401){
            access_token = refreshToken().path("access_token");
            prop.setProperty("access_token",access_token);
            try (FileOutputStream out = new FileOutputStream(System.getProperty("user.dir")+"/src/test/resources/config.properties")){
                prop.setProperty("access_token", access_token);
                prop.store(out, null);
            }
        }

        RequestSpecBuilder requestSpecificationBuilder = new RequestSpecBuilder();
                requestSpecificationBuilder.
                setBaseUri("https://api.spotify.com/").
                setBasePath("v1").
                addHeader("Authorization","Bearer "+access_token).
                log(LogDetail.ALL);
        this.requestSpecification = requestSpecificationBuilder.build();
        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
        responseSpecBuilder.expectContentType(ContentType.JSON).
                log(LogDetail.ALL);
        this.responseSpecification = responseSpecBuilder.build();

    }


    public int getUserDetailsCheck(){
        Response response = given().
                baseUri("https://api.spotify.com/").
                basePath("v1").
                header("Authorization","Bearer "+this.access_token).
                log().all().
                get("/me").then().log().all().
                extract().response();

        return response.statusCode();
    }

    public Response refreshToken(){
        Response response = given().
                baseUri("https://accounts.spotify.com/api/token").
                header("Content-Type","application/x-www-form-urlencoded").
                queryParam("grant_type","refresh_token").
                queryParam("refresh_token",prop.getProperty("refresh_token")).
                queryParam("client_id",prop.getProperty("client_id")).
                header("Authorization","Basic "+encode(prop.getProperty("client_id")+":"+prop.getProperty("client_secret"))).
                when().log().all().
                post().then().log().all().
                assertThat().statusCode(200).
                extract().response();
        return response;
    }

    public String encode(String val){
        return Base64.getEncoder().encodeToString(val.getBytes());
    }


    @Test
    public void getUserDetails(){
        Response response = given().spec(requestSpecification).
                get("/me").then().spec(responseSpecification).
                assertThat().
                statusCode(200).and().extract().response();
        userId = response.path("id");
        System.out.println(userId+" : is the user id");
    }
    @Test(dependsOnMethods = "getUserDetails")
    public void getUserPlaylists(){
        userPlaylist = given().spec(requestSpecification).
                pathParam("userId",userId).
                get("/users/{userId}/playlists").

        then().spec(responseSpecification).
                assertThat().
                statusCode(200).and().extract().response();
        List<String> playListIds = userPlaylist.path("items.id");
        System.out.println(playListIds.toString()+" : is the playlists id id");
        playListId = userPlaylist.path("items[0].id");
        System.out.println(playListId+" : is the playlist id");

    }

    @Test(dependsOnMethods = "getUserDetails")
    public Response createPlaylist(){
        Response resp = given().
                spec(requestSpecification).
                pathParam("userId",userId).
                body("{\n" +
                        "    \"name\": \"New Playlist\",\n" +
                        "    \"description\": \"New playlist description\",\n" +
                        "    \"public\": false\n" +
                        "}").
                post("/users/{userId}/playlists").
                then().spec(responseSpecification).
                assertThat().
                statusCode(201).
                extract().response();
        return  resp;
    }

    @Test(dependsOnMethods = {"getUserPlaylists","getUserDetails"})
    public void updatePlaylist(){
        Response existingRecord = userPlaylistRecord();
        int existingCount = existingRecord.path("total");
        String existingDesc = existingRecord.path("items[0].description");
        String existingId = existingRecord.path("items[0].id");
        Response response = given().spec(requestSpecification).
                pathParam("playlistId",playListId).
                body("{\n" +
                        "    \"name\": \"Update Playlist\",\n" +
                        "    \"description\": \"Update playlist description\",\n" +
                        "    \"public\": false\n" +
                        "}").
                put("/playlists/{playlistId}").
                then().
                assertThat().
                statusCode(200).
                extract().response();

        Response newRecord = userPlaylistRecord();
        int newCount = newRecord.path("total");
        String newDesc = newRecord.path("items[0].description");
        String newId = newRecord.path("items[0].id");
        Assert.assertEquals(newCount,existingCount);
    }

    public Response userPlaylistRecord(){
       Response newResp = given().spec(requestSpecification).
                pathParam("userId",userId).
                get("/users/{userId}/playlists").

                then().spec(responseSpecification).
                assertThat().
                statusCode(200).and().extract().response();
       return newResp;
    }

    @Test
    public void updatePlaylistInvalid() {
        Response response = given().spec(requestSpecification).
                pathParam("playlistId", 12459).
                body("{\n" +
                        "    \"name\": \"\",\n" +
                        "    \"description\": \"Update playlist description\",\n" +
                        "    \"public\": false\n" +
                        "}").
                put("/playlists/{playlistId}").
                then().
                assertThat().
                statusCode(400).
                extract().response();
    }

}
