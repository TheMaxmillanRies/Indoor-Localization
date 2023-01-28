package com.tudelft.app2;

import android.net.wifi.ScanResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainManager {
    private Map<String, AccessPoint> accessPoints;

    public TrainManager()
    {
        accessPoints = new HashMap<>();
    }

    public void updateAccessPoints(List<ScanResult> scanResults, String label)
    {
        // Write results to a label
        for (ScanResult scanResult : scanResults) {
            //if(fileManager.macAddressExists(scanResult.BSSID))
            //   continue;
            // If new BSSID then create a new entry in the map
            accessPoints.putIfAbsent(scanResult.BSSID, new AccessPoint(scanResult.BSSID, Utils.CELL_NUMBER));

            AccessPoint point = accessPoints.get(scanResult.BSSID);
            point.addToCellHistogram(label.toLowerCase(), scanResult.level);
            point.increaseCounter();

            // Update the entry in the map
            accessPoints.put(scanResult.BSSID, point);
        }
    }

    public Map<String, AccessPoint> getAccessPoints() {
        return accessPoints;
    }

    public void clearAccessPoints()
    {
        accessPoints.clear();
    }
}
