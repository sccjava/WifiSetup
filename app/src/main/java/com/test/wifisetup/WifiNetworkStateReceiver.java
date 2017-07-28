package com.test.wifisetup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.test.wifisetup.OnNetworkStateChangeListener;

/**
 * Broadcast listener that listens for broadcast changes.
 */
public class WifiNetworkStateReceiver extends BroadcastReceiver {

    private final OnNetworkStateChangeListener listener;

    public WifiNetworkStateReceiver(@NonNull OnNetworkStateChangeListener listener) {
        this.listener = listener;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo network = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        WifiInfo wifi = null;
        if (network.isConnected()) {
            wifi = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
        }
        listener.onNetworkChange(network, wifi);
    }

}
