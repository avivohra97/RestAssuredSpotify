package org.qe.Utils;

import java.util.Arrays;
import java.util.Base64;

public class Encode {

    public static String encode(String val){
        return Base64.getEncoder().encodeToString(val.getBytes());
    }
    public static String decode(String val){
        return Arrays.toString(Base64.getDecoder().decode(val));
    }
}
