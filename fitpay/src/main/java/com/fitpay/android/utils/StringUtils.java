package com.fitpay.android.utils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * Created by Vlad on 26.02.2016.
 */
final class StringUtils {

    /**
     * Get encrypted string
     * @param type key type
     * @param decryptedString decrypted string
     * @return encrypted string
     */
    public static String getEncryptedString(@KeysManager.KeyType int type, String decryptedString) {

        JWEAlgorithm alg = JWEAlgorithm.A256GCMKW;
        EncryptionMethod enc = EncryptionMethod.A256GCM;

        ECCKeyPair keyPair = KeysManager.getInstance().getPairForType(type);

        JWEHeader.Builder jweHeaderBuilder = new JWEHeader.Builder(alg, enc)
                .contentType("application/json")
                .keyID(keyPair.getKeyId());

        JWEHeader header = jweHeaderBuilder.build();
        Payload payload = new Payload(decryptedString);
        JWEObject jweObject = new JWEObject(header, payload);
        try {
            JWEEncrypter encrypter = new AESEncrypter(KeysManager.getInstance().getSecretKey(type));
            jweObject.encrypt(encrypter);
        } catch (JOSEException e) {
            Constants.printError(e.toString());
        }

        return jweObject.serialize();
    }

    /**
     * Get decrypted string
     * @param type key type
     * @param encryptedString encrypted string
     * @return decrypted string
     */
    public static String getDecryptedString(@KeysManager.KeyType int type, String encryptedString) {

        JWEObject jweObject;
        try {
            jweObject = JWEObject.parse(encryptedString);
            JWEHeader jweHeader = jweObject.getHeader();
            if(jweHeader.getKeyID() == null || jweHeader.getKeyID().equals(KeysManager.getInstance().getKeyId(type))) {
                jweObject.decrypt(new AESDecrypter(KeysManager.getInstance().getSecretKey(type)));
                return jweObject.getPayload().toString();
            }
        } catch (ParseException | JOSEException e) {
            Constants.printError(e.toString());
        }

        return null;
    }

    /**
     * Convert String to SHA1
     * @param inputString original string
     * @return converted string
     */
    public static String toSHA1(String inputString){
        StringBuilder sb = new StringBuilder();

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] result = digest.digest(inputString.getBytes("UTF-8"));
            for (byte b : result) // This is your byte[] result..
            {
                sb.append(String.format("%02X", b));
            }
        } catch (Exception e) {
            Constants.printError(e.toString());
        }

        return sb.toString().toLowerCase();
    }
}