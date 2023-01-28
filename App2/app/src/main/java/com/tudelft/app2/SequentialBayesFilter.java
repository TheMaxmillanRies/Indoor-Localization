package com.tudelft.app2;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SequentialBayesFilter extends BayesFilter{

    @Override
    String getLocation(Map<String, AccessPoint> accessPoints, List<ScanResult> scans, int totalScans) {
        for (ScanResult scan: scans) {
            if (!accessPoints.containsKey(scan.BSSID))
                continue;

            AccessPoint accessPoint = accessPoints.get(scan.BSSID);
            if((float)accessPoint.getCount()/totalScans < 0.8)
                continue;

            List<String> cells = new ArrayList<>(accessPoint.getKeys());

            float sum = 0.0f;
            for (String cell: cells) {
                float[] cellData = accessPoint.getCellData(String.valueOf(cell));
                float temp = belief.get(String.valueOf(cell)) * cellData[scan.level];
                sum += temp;
                belief.put(String.valueOf(cell), temp);
            }
            float finalSum = sum;
            belief.replaceAll((k, v) -> v / finalSum);

            for (String key: belief.keySet()) {
                if (belief.get(key) >= 0.9)
                    return key;
            }
        }

        return "";
    }
}
