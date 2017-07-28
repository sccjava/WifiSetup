package com.test.wifisetup;

import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.test.wifisetup.R;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "MainActivity";

    private Button speaker, dlink, autoTest;

    private WifiNetworkStateReceiver wifiReceiver;

    private TextView ok;
    private TextView fail;

    private AtomicInteger okCnt = new AtomicInteger();
    private AtomicInteger failCnt = new AtomicInteger();

    private String connectingSSID = null;


    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                case 1:
                    autoConnect(msg.what);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void autoConnect(int what){
        if(what == 0){
            connectingSSID = "HK_Omni_10+_Setup_ffd";
            WifiConnectionManager wifi0 = new WifiConnectionManager(this, connectingSSID, "");
            wifi0.connectToWifi();
        }else{
            connectingSSID = "D-Link_DIR-816_5G";
            WifiConnectionManager wifi1 = new WifiConnectionManager(this, connectingSSID, "12345678");
            wifi1.connectToWifi();
        }
        Log.d(TAG, "connecting..............................................." + connectingSSID);

        handler.sendEmptyMessageDelayed(what == 0 ? 1 : 0, 30 * 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiReceiver = new WifiNetworkStateReceiver(new OnNetworkStateChangeListener() {
            @Override
            public void onNetworkChange(@NonNull NetworkInfo network, @Nullable WifiInfo wifi) {
                Log.d(TAG, String.format("Notify wifi changed ssid=%s, state=%s", wifi== null ? "": wifi.getSSID(), network.getState()));
                if(wifi != null && connectingSSID != null)
                {
                    if(wifi.getSSID().contains(connectingSSID) && network.getState() == NetworkInfo.State.CONNECTED){
                        okCnt.set(okCnt.get()+1);
                        Toast.makeText(getApplicationContext(), "ok.." + connectingSSID, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ok..............................................." + connectingSSID);
                    }else{
                        failCnt.set(failCnt.get()+1);
                        Log.d(TAG, "fail..............................................." + connectingSSID);
                        Toast.makeText(getApplicationContext(), "fail.." + connectingSSID, Toast.LENGTH_SHORT).show();
                    }
                    connectingSSID = null;
                    ok.setText(okCnt.get()+"");
                    fail.setText(failCnt.get()+"");
                }
            }
        });
        this.registerReceiver(wifiReceiver, wifiReceiver.getIntentFilter());

        speaker = (Button)findViewById(R.id.speaker);
        dlink = (Button)findViewById(R.id.dlink);
        autoTest = (Button)findViewById(R.id.autoTest);

        ok = (TextView)findViewById(R.id.ok);
        fail = (TextView)findViewById(R.id.fail);

        speaker.setOnClickListener(this);
        dlink.setOnClickListener(this);
        autoTest.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.speaker:
                Log.d(TAG, "click speaker.....");
                WifiConnectionManager wifi0 = new WifiConnectionManager(this, "HK_Omni_10+_Setup_ffd", "");
                wifi0.connectToWifi();
                break;
            case R.id.dlink:
                Log.d(TAG, "click dlink.....");
                WifiConnectionManager wifi1 = new WifiConnectionManager(this, "D-Link_DIR-816_5G", "12345678");
                wifi1.connectToWifi();
                break;
            case R.id.autoTest:
                Log.d(TAG, "click autoTest.....");
                handler.sendEmptyMessage(0);
                Toast.makeText(this,"start to auto test", Toast.LENGTH_SHORT).show();
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
