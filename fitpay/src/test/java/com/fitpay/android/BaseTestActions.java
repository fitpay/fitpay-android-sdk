package com.fitpay.android;

import android.content.Context;

import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.api.models.security.ECCKeyPair;
import com.fitpay.android.paymentdevice.DeviceSyncManager;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.SecurityProvider;

import org.conscrypt.Conscrypt;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.security.Provider;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import mockit.Mock;
import mockit.MockUp;

public abstract class BaseTestActions {

    protected static Context mContext;

    @BeforeClass
    public static void init() {
        cleanAll();

        if (!Conscrypt.isAvailable()) {
            //tests fix for UnsatisfiedLinkError: org.conscrypt.NativeCrypto.EVP_has_aes_hardware()I
            new MockUp<Conscrypt>() {
                @mockit.Mock
                Provider newProvider() {
                    return null;
                }
            };
        } else {
            SecurityProvider.getInstance().setProvider(Conscrypt.newProvider());
        }

        TestConstants.configureFitpay(mContext = Mockito.mock(Context.class));

        RxAndroidPlugins.setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
            @Override
            public Scheduler apply(Callable<Scheduler> schedulerCallable) throws Exception {
                return Schedulers.trampoline();
            }
        });

        RxAndroidPlugins.setMainThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return Schedulers.trampoline();
            }
        });

        new MockUp<Schedulers>() {
            @Mock
            Scheduler from(Executor executor) {
                return Schedulers.trampoline();
            }
        };

        if (!TestConstants.testConfig.useRealTests) {
            new MockUp<ECCKeyPair>() {
                @Mock
                public boolean isExpired() {
                    return false;
                }
            };
        }
    }

    @AfterClass
    public static void clean() {
        mContext = null;
        cleanAll();
    }

    private static void cleanAll() {
        DeviceSyncManager.clean();
        NotificationManager.clean();
        FPLog.clean();
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() {
        NotificationManager.clean();
    }

    public A2AVerificationRequest getA2AVerificationRequest() {
        String a2aVerificationRequest = "{\"cardType\":\"VISA\",\"creditCardId\":\"b3f70e43-c066-4e9d-b5ec-b7237302f9cc\",\"verificationId\":\"3b42e65f-1608-4afd-bd72-646142b00a6e\",\"returnLocation\":\"\\/idv\\/b3f70e43-c066-4e9d-b5ec-b7237302f9cc\\/select\\/3b42e65f-1608-4afd-bd72-646142b00a6e\",\"context\":{\"applicationId\":\"com.fitpay.issuerdemo\",\"action\":\"generate_auth_code\",\"payload\":\"eyJ1c2VySWQiOiIxNTdmMTUxOC1kYzRjLTRhYWMtYWRmNS03NjRkMDE2MTJjNGEiLCJ0b2tlbml6YXRpb25JZCI6ImIzZjcwZTQzLWMwNjYtNGU5ZC1iNWVjLWI3MjM3MzAyZjljYyIsInZlcmlmaWNhdGlvbklkIjoiM2I0MmU2NWYtMTYwOC00YWZkLWJkNzItNjQ2MTQyYjAwYTZlIn0=\"}}";
        return Constants.getGson().fromJson(a2aVerificationRequest, A2AVerificationRequest.class);
    }
}

