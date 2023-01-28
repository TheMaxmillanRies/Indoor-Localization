package com.tudelft.app2;

import android.net.wifi.ScanResult;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public interface WifiListener {
    void callback(List<ScanResult> scanResults) throws JSONException, IOException;
}
