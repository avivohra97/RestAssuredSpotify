package org.qe.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyBuilder {
    public static  Properties property;
    public static Properties setUpProperties(String filePath) throws IOException {
        property = new Properties();
        property.load(new FileInputStream(new File(filePath)));
        return property;
    }

    public static Properties updateProperty(String filePath) throws IOException {
        property.store(new FileOutputStream(new File(filePath)),null);
        return property;
    }

    public String getProperty(String prop) {
        return property.getProperty(prop);
    }

    public static void setProperty(String key,String value) {
        property.setProperty(key,value);
    }
}
