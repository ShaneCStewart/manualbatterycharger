package com.example.manualphonecharger.app.network;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

public class NetworkingHelper {
    private static final String TAG = "NetworkingHelper";
    public static final String PUBNUB_CHANNEL = "shaneTestingDemoStringFoo";
    private static final String PUBLISH_KEY = "pub-c-d8eae383-2e9e-400e-a2f2-a517a6824b12";
    private static final String SUBSCRIBE_KEY = "sub-c-cb6192b6-dede-11e3-a226-02ee2ddab7fe";

    public Pubnub pubnub;
    private Callback callback;

    private NetworkingHelper() {
        init();
    }

    private void init() {
        initPubnub();
        initPubnubSubscibe();
        initCallback();
    }

    private static class NetworkingHelperHolder {
        private static final NetworkingHelper INSTANCE = new NetworkingHelper();
    }

    public static NetworkingHelper getInstance() {
        return NetworkingHelperHolder.INSTANCE;
    }


    public Pubnub getPubnub() {
        return pubnub;
    }

    public Callback getCallback() {
        return callback;
    }


    private void initPubnub() {
        pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY);
    }

    private void initPubnubSubscibe() {
        try {
            pubnub.subscribe(PUBNUB_CHANNEL, new Callback() {

                        @Override
                        public void connectCallback(String channel, Object message) {
                            Log.d(TAG, "SUBSCRIBE : CONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            Log.d(TAG, "SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            Log.d(TAG, "SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            Log.d(TAG, "SUBSCRIBE : " + channel + " : "
                                    + message.getClass() + " : " + message.toString());
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            Log.d(TAG, "SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        } catch (PubnubException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void initCallback() {
        callback = new Callback() {
            public void successCallback(String channel, Object response) {
                Log.d(TAG, response.toString());
            }

            public void errorCallback(String channel, PubnubError error) {
                Log.d(TAG, error.toString());
            }
        };
    }
}
