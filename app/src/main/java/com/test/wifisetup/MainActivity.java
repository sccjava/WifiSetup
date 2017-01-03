package com.harman.wifisetup;

import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "MainActivity";

    private Button speaker, dlink;

    private WifiNetworkStateReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiReceiver = new WifiNetworkStateReceiver(new OnNetworkStateChangeListener() {
            @Override
            public void onNetworkChange(@NonNull NetworkInfo network, @Nullable WifiInfo wifi) {
                Log.d(TAG, String.format("Notify wifi changed ssid=%s, state=%s", wifi== null ? "": wifi.getSSID(), network.getState()));
            }
        });
        this.registerReceiver(wifiReceiver, wifiReceiver.getIntentFilter());

        speaker = (Button)findViewById(R.id.speaker);
        dlink = (Button)findViewById(R.id.dlink);

        speaker.setOnClickListener(this);
        dlink.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.speaker:
                Log.d(TAG, "click speaker.....");
                WifiConnectionManager wifi0 = new WifiConnectionManager(this, "Omni10_Setup_8B1", "");
                wifi0.connectToWifi();
                break;
            case R.id.dlink:
                Log.d(TAG, "click dlink.....");
                WifiConnectionManager wifi1 = new WifiConnectionManager(this, "D-Link_DIR-816(2.4G)", "12345678");
                wifi1.connectToWifi();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.unregisterReceiver(wifiReceiver);
    }
}
