package com.fitpay.android.utils;


import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.security.ECCKeyPair;

import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * KeysManager is designed to create and manage @ECCKeyPair object.
 */
final public class KeysManager {
    private static final String TAG = KeysManager.class.getName();

    public static final int KEY_API = 0;
    public static final int KEY_WV = KEY_API + 1;
    public static final int KEY_FPCTRL = KEY_WV + 1;

    private static final String ALGORITHM = "ECDH";
    private static final String EC_CURVE = "secp256r1";
    private static final String KEY_TYPE = "AES";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            KeysManager.KEY_API,
            KeysManager.KEY_WV,
            KeysManager.KEY_FPCTRL
    })
    public @interface KeyType {
    }

    private static KeysManager sInstance;

    public static KeysManager getInstance() {
        if (sInstance == null) {
            sInstance = new KeysManager();
            SecurityProvider.getInstance().initProvider();
        }

        return sInstance;
    }

    public static void clear(){
        sInstance = null;
    }

    private Map<Integer, ECCKeyPair> mKeysMap;

    private KeysManager() {
        mKeysMap = new HashMap<>();
    }

    // Create the public and private keys
    private ECCKeyPair createECCKeyPair() throws Exception {
        String keyId = "e6b62e07-c4cc-4844-88ef-7b7d1ecb4709";
        String strPrivateKey = "308193020100301306072a8648ce3d020106082a8648ce3d030107047930770201010420cb4f12b23191eae4c4fc0f23856d8a89684b020c68cc11000a0c8381d867e68ea00a06082a8648ce3d030107a1440342000406c1ff432f49b14f9318503226e42f905333d154fe884829b36ad67ab77714b4cbb3cdf278165c5c86781862bf4254f7081cef0f3730622e183b54ce31371247";
        String strPublicKey = "3059301306072a8648ce3d020106082a8648ce3d0301070342000406c1ff432f49b14f9318503226e42f905333d154fe884829b36ad67ab77714b4cbb3cdf278165c5c86781862bf4254f7081cef0f3730622e183b54ce31371247";

        ECCKeyPair eccKeyPair = new ECCKeyPair();
        eccKeyPair.setKeyId(keyId);

        eccKeyPair.setPrivateKey(strPrivateKey);
        eccKeyPair.setPublicKey(strPublicKey);

        return eccKeyPair;
    }

    // methods for ASN.1 encoded keys


    public PrivateKey getPrivateKey(byte[] privateKey) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM,
                SecurityProvider.getInstance().getProvider());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        return kf.generatePrivate(keySpec);
    }

    public PublicKey getPublicKey(byte[] publicKey) throws Exception {
        return getPublicKey(ALGORITHM, publicKey);
    }

    public PublicKey getPublicKey(String algorithm, byte[] publicKey) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(algorithm,
                SecurityProvider.getInstance().getProvider());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        return kf.generatePublic(keySpec);
    }

    public SecretKey getSecretKey(@KeyType int type) {

        ECCKeyPair keyPair = getPairForType(type);
        SecretKey secretKey = keyPair.getSecretKey();

        if (secretKey == null) {
            secretKey = createSecretKey(keyPair.getPrivateKey(), keyPair.getServerPublicKey());
            keyPair.setSecretKey(secretKey);
        }

        return secretKey;
    }

    private SecretKey createSecretKey(String privateKeyStr, String publicKeyStr) {

        try {
            PrivateKey privateKey = getPrivateKey(Hex.hexStringToBytes(privateKeyStr));
            PublicKey publicKey = getPublicKey(Hex.hexStringToBytes(publicKeyStr));

            KeyAgreement keyAgreement = null;
            try {
                keyAgreement = KeyAgreement.getInstance(ALGORITHM,
                        SecurityProvider.getInstance().getProvider());
            } catch (Exception e) {
                //hack for unit tests
                keyAgreement = KeyAgreement.getInstance(ALGORITHM);
            }

            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            return keyAgreement.generateSecret(KEY_TYPE);
        } catch (Exception e) {
            FPLog.e(TAG, e);
            return null;
        }
    }

    public ECCKeyPair getPairForType(@KeyType int type) {
        return mKeysMap.get(type);
    }

    public ECCKeyPair createPairForType(@KeyType int type) throws Exception {
        removePairForType(type);

        ECCKeyPair keyPair = createECCKeyPair();
        mKeysMap.put(type, keyPair);
        return keyPair;
    }

    public void removePairForType(@KeyType int type) {
        if (mKeysMap.containsKey(type)) {
            mKeysMap.remove(type);
        }
    }

    public void updateECCKey(final @KeyType int type, @NonNull final Runnable successRunnable, final ApiCallback callback) {
        Observable.defer(() -> {
            try {
                ECCKeyPair keyPair = createPairForType(type);
                Call<ECCKeyPair> getKeyCall = ApiManager.getInstance().getClient().createEncryptionKey(keyPair);
                Response<ECCKeyPair> response = getKeyCall.execute();
                if (response.isSuccessful() && response.errorBody() == null) {
                    return Observable.just(response.body());
                } else if (response.errorBody() != null) {
                    try {
                        return Observable.error(new Throwable(response.errorBody().toString()));
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                } else {
                    return Observable.error(new Throwable(response.message()));
                }

            } catch (Exception e) {
                return Observable.error(e);
            }
        }).subscribeOn(Schedulers.io()).toBlocking().subscribe(eccKeyPair -> {
            eccKeyPair.setPrivateKey(mKeysMap.get(type).getPrivateKey());
            mKeysMap.put(type, eccKeyPair);

            if (successRunnable != null) {
                successRunnable.run();
            }
        }, throwable -> {
            FPLog.e(TAG, throwable);
            callback.onFailure(ResultCode.REQUEST_FAILED, throwable.toString());
        });
    }

    public String getKeyId(@KeyType int type) {
        ECCKeyPair keyPair = getPairForType(type);
        return keyPair != null ? keyPair.getKeyId() : null;
    }

    public boolean keyRequireUpdate(@KeyType int type) {
        ECCKeyPair keyPair = getPairForType(type);
        return keyPair == null || keyPair.isExpired();
    }
}
