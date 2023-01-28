package com.tudelft.app2;

import android.net.wifi.ScanResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BayesFilter {
    protected Map<String, Float> belief;

    public BayesFilter()
    {
        belief = new HashMap<>();
    }
    abstract String getLocation(Map<String, AccessPoint> accessPoints, List<ScanResult> scans, int totalScans);

    public void resetBelief() {
        belief.clear();

        // Need to add the cells for initial belief
        for(int i = 1; i <= Utils.CELL_NUMBER; i++)
            belief.put("c"+i, 1.0f);

        belief.replaceAll((k, v) -> v / (belief.size()));
    }
}
