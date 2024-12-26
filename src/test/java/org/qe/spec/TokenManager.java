package org.qe.spec;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.qe.Utils.Encode;

import java.time.Instant;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class TokenManager {
    private static String accessToken;
    private static Instant expiryTime;

    public static String getToken(Properties prop) throws Exception {
        Response resp;
        try {
            if(accessToken == null || Instant.now().isAfter(expiryTime)){
                System.out.println("refreshing token");
                resp = refreshToken(prop);
                accessToken = resp.path("access_token");
                int duration = resp.path("expires_in");
                expiryTime = Instant.now().plusSeconds(duration);
            }else {
                System.out.println("getting new token");
            }
        }catch (Exception e){
            throw new Exception("Aborting failed to get exception");
        }
        return accessToken;
    }
    public int getUserDetailsCheck(String baseUri, String accessToken){
        Response response = given().
                baseUri(baseUri).
                basePath("v1").
                header("Authorization","Bearer "+accessToken).
                log().all().
                get("/me").then().log().all().
                extract().response();

        return response.statusCode();
    }
    private static Response refreshToken(Properties  prop){
        Response response = given().
                baseUri("https://accounts.spotify.com/api/token").
                contentType(ContentType.URLENC).
                formParam("grant_type","refresh_token").
                formParam("refresh_token",prop.getProperty("refresh_token")).
                formParam("client_id",prop.getProperty("client_id")).
                header("Authorization","Basic "+ Encode.encode(prop.getProperty("client_id")+":"+prop.getProperty("client_secret"))).
                when().log().all().
                post().then().log().all().
                extract().response();

        return response;


    }

}
