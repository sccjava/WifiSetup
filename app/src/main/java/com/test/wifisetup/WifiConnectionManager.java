package com.test.wifisetup;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Connects to a Wifi and notifies of success/failure.
 */
public class WifiConnectionManager {
    private final static String TAG = "WifiConnectionManager";

    private static final int CONNECTION_TIMEOUT = 45;
    private static final TimeUnit CONNECTION_TIMEOUT_UNITS = TimeUnit.SECONDS;

    private final WifiManager mManager;
    private final Context mContext;
    private final String mPassword;
    private final String mSsid;
    private int networkId;


    public WifiConnectionManager(Context ctx , @NonNull String ssid, @Nullable String password) {
        mContext = ctx;
        mManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mSsid = ssid;
        mPassword = password;
    }

    public void connectToWifi() {
        // Set first so that networks can be configured.
        mManager.setWifiEnabled(true);

        // Find network ID
        networkId = getNetworkForSsid(mSsid);
        if (networkId != -1) {
            Log.d(TAG, String.format("Enabling network %s %s", mSsid, networkId));
            // Enable and connect to device.
            // Note: do not use .reconnect() after as on Marshmallow this disconnects the WiFi.
            if (!mManager.enableNetwork(networkId, true)) {
                Log.d(TAG, " Could not enable network " + networkId);
                return;
            }
        }
        else {
            Log.d(TAG, "Could not get networkId for " + mSsid);
            return;
        }
    }

    /**
     * @see <a href="http://www.codota.com/android/scenarios/5189167eda0a1626d18f747e/android.net.wifi.WifiManager?tag=out_2013_05_05_07_19_34">
     *      Need to remove network first</a>
     * @see <a href="http://stackoverflow.com/q/2140133">Add a new network configuration</a>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getNetworkForSsid(String ssid) {
        if (!ssid.startsWith("\"")) {
            ssid = "\"" + ssid + "\"";
        }
        WifiConfiguration config = createWifiConfig(ssid);
        // Try adding first, because on HTC device once you remove the network you cannot add it again.
        int existing = findConfiguredNetwork(ssid);
        int networkId;
        if (existing == -1) {
            networkId = mManager.addNetwork(config);
        }
        else {
            /*if(true){
                return existing;
            }*/
            config.networkId = existing;
            networkId = mManager.updateNetwork(config);
            if (networkId == -1) {
                // LG manufacturer requires configurations be removed before they are added.
                Log.d(TAG, String.format("Disabling %s before we try adding again.", config.networkId));
                mManager.disableNetwork(existing);
                mManager.removeNetwork(existing);
                networkId = mManager.addNetwork(config);
            }
        }
        Log.d(TAG, String.format("Added %s for %s", networkId, config));
        if (networkId == -1) {
            // Marshmallow does not allow changing WiFi configurations of devices that app has not created.
            // removeNetwork fails with "WifiStateMachine: Not authorized to remove network" and "Not authorized to
            // update network" when calling addNetwork or updateNetwork.
            // In this case we cannot change the wifi password on the home network for the phone.
            return findConfiguredNetwork(ssid);
        }
        return networkId;
    }

    private WifiConfiguration createWifiConfig(String ssid) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        if (mPassword != null && mPassword.length() > 0) {
            config.preSharedKey = "\"" + mPassword + "\"";
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        }
        else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }

    private int findConfiguredNetwork(String ssid) {
        if(mManager == null){
            return -1;

        }
        if (mManager.getConfiguredNetworks() != null){
            for (WifiConfiguration config : mManager.getConfiguredNetworks()) {
                if (ssid.equals(config.SSID)) {
                    return config.networkId;
                }
            }
        }
        return -1;
    }
}
