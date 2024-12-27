package org.qe.spec;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.qe.Utils.PropertyBuilder;
import org.testng.annotations.BeforeClass;

import java.io.FileOutputStream;
import java.io.IOException;

import static io.restassured.RestAssured.config;
import static io.restassured.config.LogConfig.logConfig;

public class SpecBuilder {

    public static RequestSpecification reqSpec(String uri, String path, String accessToken) throws IOException {

        LogConfig logConfig = LogConfig.logConfig().blacklistHeader("Authorization");
        RestAssured.config = RestAssured.config().logConfig(logConfig);
        return new RequestSpecBuilder().
                setBaseUri(uri).
                setBasePath(path).
                addFilter(new AllureRestAssured()).
                addHeader("Authorization","Bearer "+accessToken).
                log(LogDetail.ALL).build();
    }

    public static ResponseSpecification resSpec() throws IOException {

        return new ResponseSpecBuilder().
                expectContentType(ContentType.JSON).
                expectStatusCode(200).
                log(LogDetail.ALL).build();
    }
}
