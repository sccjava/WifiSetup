package com.test.wifisetup;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private String mSSID = null;
    private String mPwd = null;
    private String mSpeaker = null;
    private EditText mEditTextSSID = null;
    private EditText mEditTextPwd = null;
    private EditText mEditTextSpeaker = null;


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
            connectingSSID = mSpeaker;
            WifiConnectionManager wifi0 = new WifiConnectionManager(this, connectingSSID, "");
            wifi0.connectToWifi();
        }else{
            connectingSSID = mSSID;
            WifiConnectionManager wifi1 = new WifiConnectionManager(this, connectingSSID, mPwd);
            wifi1.connectToWifi();
        }
        Log.d(TAG, "connecting..............................................." + connectingSSID);

        handler.sendEmptyMessageDelayed(what == 0 ? 1 : 0, 30 * 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditTextSSID = (EditText) findViewById(R.id.ssid);
        mEditTextPwd = (EditText) findViewById(R.id.password);
        mEditTextSpeaker = (EditText) findViewById(R.id.speaker_ssid);

        wifiReceiver = new WifiNetworkStateReceiver(new OnNetworkStateChangeListener() {
            @Override
            public void onNetworkChange(@NonNull NetworkInfo network, @Nullable WifiInfo wifi) {
                Log.d(TAG, String.format("Notify wifi changed ssid=%s, state=%s", wifi== null ? "": wifi.getSSID(), network.getState()));
                if(wifi != null && connectingSSID != null)
                {
                    String ip = getCurrentIP();
                    if(wifi.getSSID().contains(connectingSSID) && network.getState() == NetworkInfo.State.CONNECTED && ip!= null && (ip.contains("192.") || ip.contains("10."))){
                        okCnt.set(okCnt.get()+1);
                        Toast.makeText(getApplicationContext(), "ok.." + connectingSSID+"," + ip, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "ok..............................................." + connectingSSID +", " + ip);
                    }else{
                        failCnt.set(failCnt.get()+1);
                        Log.d(TAG, "fail..............................................." + connectingSSID+", " + ip);
                        Toast.makeText(getApplicationContext(), "fail.." + connectingSSID +"," +ip, Toast.LENGTH_LONG).show();
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
        mSSID = mEditTextSSID.getText().toString();
        mPwd = mEditTextPwd.getText().toString();
        mSpeaker = mEditTextSpeaker.getText().toString();
        switch(view.getId()){
            case R.id.speaker:
                Log.d(TAG, "click speaker.....");
                WifiConnectionManager wifi0 = new WifiConnectionManager(this, mSpeaker, "");
                wifi0.connectToWifi();
                break;
            case R.id.dlink:
                Log.d(TAG, "click router.....");
                WifiConnectionManager wifi1 = new WifiConnectionManager(this, mSSID, mPwd);
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

    private String getCurrentIP(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo == null){
            return null;
        }
        int ipAddress = wifiInfo.getIpAddress();
        String ip = IPv4Util.intToIp(ipAddress);
        return ip;
    }
}
