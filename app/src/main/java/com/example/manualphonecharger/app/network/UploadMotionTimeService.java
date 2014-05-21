package com.example.manualphonecharger.app.network;

import android.app.IntentService;
import android.content.Intent;

import com.example.manualphonecharger.app.customview.PowerGaugeView;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import java.util.HashMap;
import java.util.Map;

public class UploadMotionTimeService extends IntentService {
    private static final String NAME = "UploadMotionTimeService";

    public UploadMotionTimeService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NetworkingHelper networkingHelper = NetworkingHelper.getInstance();
        Pubnub pubnub = networkingHelper.getPubnub();
        Callback callback = networkingHelper.getCallback();
        HashMap<Integer, Integer> timeMotion =
                (HashMap) intent.getSerializableExtra(PowerGaugeView.TIME_MOTION_TAG);
        String messageString = createMessageString(timeMotion);
        //send data as message, pubnub has a soft cap of 1800 characters on free accounts
        pubnub.publish(NetworkingHelper.PUBNUB_CHANNEL, messageString, callback);
    }

    private String createMessageString(HashMap<Integer, Integer> timeMotion) {
        String messageString = "{TimeMotion:{";
        for (Map.Entry<Integer, Integer> entry : timeMotion.entrySet()) {
            messageString += entry.getKey() + ":" + entry.getValue() + ",";
        }
        messageString = messageString.substring(0, messageString.length() - 1) + "}}";
        return messageString;
    }
}
