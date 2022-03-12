package com.clyde.apiwithjson;


import android.os.Build;

import java.security.SignatureException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


//此Class是透過當下日期以及APP_KEY進行加密運算之後再傳回伺服器與APP_ID做匹配
//由PTX直接提供的，就無需再修改了。
//by asd66998854
public class HMAC_SHA1 {

    public static String Signature(String xData, String AppKey) throws SignatureException {

        try {
            final Base64.Encoder encoder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                encoder = Base64.getEncoder();
            } else {
                throw new SignatureException("Failed to generate HMAC : SDK version doesn't satisfy require");
            }
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(AppKey.getBytes("UTF-8"), "HmacSHA1");

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(xData.getBytes("UTF-8"));
            String result = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = encoder.encodeToString(rawHmac);
            } else {
                throw new SignatureException("Failed to generate HMAC : SDK version doesn't satisfy require");
            }
            return result;

        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
    }
}


