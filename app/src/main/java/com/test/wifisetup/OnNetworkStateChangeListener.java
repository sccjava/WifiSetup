package com.harman.wifisetup;

import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Listener for WifiNetworkStateReceiver
 */
public interface OnNetworkStateChangeListener {

    /**
     * @param wifi WiFi that is connected or null if not connected.
     */
    void onNetworkChange(@NonNull NetworkInfo network, @Nullable WifiInfo wifi);

}
