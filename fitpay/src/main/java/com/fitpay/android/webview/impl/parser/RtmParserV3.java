package com.fitpay.android.webview.impl.parser;

import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;

/**
 * RtmMessage parser v3
 */
public class RtmParserV3 extends RtmParserV2 {

    public RtmParserV3(WebViewCommunicatorImpl impl) {
        super(impl);
    }

    @Override
    public void parseMessage(RtmMessage msg) {
        switch (msg.getType()) {
            case RtmType.CARD_SCANNED:
                impl.startScan();
                break;

            default:
                super.parseMessage(msg);
        }
    }
}
