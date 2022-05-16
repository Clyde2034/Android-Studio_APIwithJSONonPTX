package com.clyde.apiwithjson;


import android.os.Build;

import androidx.annotation.RequiresApi;

import java.security.SignatureException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC_SHA1 {
    public HMAC_SHA1() {
    }


    public static String Signature(String xData, String AppKey) throws SignatureException {
        try {
            Base64.Encoder encoder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encoder = Base64.getEncoder();
            }
            SecretKeySpec signingKey = new SecretKeySpec(AppKey.getBytes("UTF-8"), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(xData.getBytes("UTF-8"));
            String result = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                result = encoder.encodeToString(rawHmac);
            }
            return result;
        } catch (Exception var7) {
            throw new SignatureException("Failed to generate HMAC : " + var7.getMessage());
        }
    }
}



