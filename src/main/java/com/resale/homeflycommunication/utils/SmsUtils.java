package com.resale.homeflycommunication.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SmsUtils {

    private static final char[] HEX_TABLE = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Generates HMAC-SHA256 hash for Vodafone SMS API.
     */
    public static String generateSecureHash(Map<String, String> fields, String secretKey) {
        try {
            StringBuilder builder = new StringBuilder();

            // Keep insertion order (LinkedHashMap preserves order)
            int count = 0;
            int size = fields.size();
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                if (val != null && !val.isEmpty()) {
                    builder.append(key).append("=").append(val);
                    if (++count < size) builder.append("&");
                }
            }

            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] raw = mac.doFinal(builder.toString().getBytes(StandardCharsets.UTF_8));
            return toHex(raw);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secure hash", e);
        }
    }


    /**
     * Builds XML request body for Vodafone Web2SMS API.
     */
    public static String buildSmsXml(String accountId, String password, String senderName, String mobile, String message, String secureHash) {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<SubmitSMSRequest xmlns:=\"http://www.edafa.com/web2sms/sms/model/\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://www.edafa.com/web2sms/sms/model/ SMSAPI.xsd\""
                + " xsi:type=\"SubmitSMSRequest\">"

                + "<AccountId>" + accountId + "</AccountId>"
                + "<Password>" + password + "</Password>"
                + "<SecureHash>" + secureHash + "</SecureHash>"

                + "<SMSList>"
                + "<SenderName>" + senderName + "</SenderName>"
                + "<ReceiverMSISDN>" + mobile + "</ReceiverMSISDN>"
                + "<SMSText>" + message + "</SMSText>"
                + "</SMSList>"

                + "</SubmitSMSRequest>";
    }


    /**
     * Convert byte array to hex.
     */
    private static String toHex(byte[] input) {
        StringBuilder sb = new StringBuilder(input.length * 2);
        for (byte b : input) {
            sb.append(HEX_TABLE[(b >> 4) & 0xF]);
            sb.append(HEX_TABLE[b & 0xF]);
        }
        return sb.toString();
    }
}


