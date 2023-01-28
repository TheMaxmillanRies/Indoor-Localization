package com.tudelft.app2;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiController {

    /**
     * dBm range for RSSI strength
     */
    private final int RANGE = 100;

    WifiManager wifiManager;
    BroadcastReceiver wifiScanReceiver;

    private List<ScanResult> scanResults;

    WifiListener listener;

    public WifiController(Context applicationContext, WifiListener listener)
    {
        wifiManager = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);

        this.listener = listener;

        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    try {
                        scanSuccess();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    scanFailure();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        applicationContext.registerReceiver(wifiScanReceiver, intentFilter);
    }


    public void getScanResults(boolean ordered)
    {
        // Start a wifi scan.
        boolean success = wifiManager.startScan();

        if (!success) {
            scanFailure();
        }
    }

    private void scanSuccess() throws JSONException, IOException {
        scanResults = wifiManager.getScanResults();
        Log.d("test", "test");
        //Sort list in decreasing order
        scanResults.sort((scanResult, t1) -> Integer.compare(t1.level, scanResult.level));

        // Write results to a label
        for (ScanResult scanResult : scanResults)
            scanResult.level = scanResult.level + RANGE;

        listener.callback(scanResults);
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
    }
}
