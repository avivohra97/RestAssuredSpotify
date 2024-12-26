package org.qe.spec;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.qe.Utils.PropertyBuilder;
import org.testng.annotations.BeforeClass;

import java.io.FileOutputStream;
import java.io.IOException;

public class SpecBuilder {

    public static RequestSpecification reqSpec(String uri, String path, String accessToken) throws IOException {

        return new RequestSpecBuilder().
                setBaseUri(uri).
                setBasePath(path).
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
