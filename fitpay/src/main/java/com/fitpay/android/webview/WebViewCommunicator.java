package com.fitpay.android.webview;

import android.app.Activity;
import android.webkit.JavascriptInterface;

import com.fitpay.android.cardscanner.IFitPayCardScanner;
import com.fitpay.android.paymentdevice.DeviceService;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;

import org.json.JSONException;


public interface WebViewCommunicator {

    /**
     * this method is called by the WV to initiate sync() or sendUserData() indirectly
     */
    @JavascriptInterface
    void dispatchMessage(String message) throws JSONException;

    /**
     * this method is called by the WV to initiate the sync sequence in the SDK
     */
    @JavascriptInterface
    void sync(String callBackId);

    /**
     * this method is called by the WV to provide 'session data' (deviceID, userID, OAuth token) to the SDK
     */
    @JavascriptInterface
    void sendUserData(String callbackId, String deviceId, String token, String userId);

    /**
     * this method is called by the WV onLoad() to retrieve JSON object with host device and wearable data
     */
    @JavascriptInterface
    String retrieveConfigJson();

    /**
     * @deprecated See {@link WebViewCommunicatorImpl (Activity, IPaymentDeviceConnector, int)}
     *
     * Provide a configured DeviceService to the communicator to support operations that require interaction with the payment device
     * One example is sync.
     *
     * @param deviceService
     */
    @Deprecated
    void setDeviceService(DeviceService deviceService);

    /**
     * Provide a {@link IFitPayCardScanner} implemention to handle card image scanning within the native OS.  In order to enable
     * the "useWebCardScanner" must be set to false in the {@link com.fitpay.android.webview.models.WvConfig} when launching the
     * webview.
     *
     * @param cardScanner
     */
    void setCardScanner(IFitPayCardScanner cardScanner);
    IFitPayCardScanner getCardScanner();

    /**
     * Called by the webview when the consumer requests a card scan operation and the "useWebCardScanner" is false in the
     * {@link com.fitpay.android.webview.models.WvConfig}
     *
     * * @param callbackId rtm callback id
     */
    void startScan(String callbackId);
}
