package com.tudelft.app2;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParallelBayesFilter extends BayesFilter{

    @Override
    String getLocation(Map<String, AccessPoint> accessPoints, List<ScanResult> scans, int totalScans) {
        ArrayList<Map<String, Float>> parallelBeliefs = new ArrayList<>();

        for (ScanResult scan: scans) {
            if(!accessPoints.containsKey(scan.BSSID))
                continue;

            AccessPoint accessPoint = accessPoints.get(scan.BSSID);
            if((float)accessPoint.getCount()/totalScans < 0.8)
                continue;

            List<String> cells = new ArrayList<>(accessPoint.getKeys());
            Map<String, Float> newBelief = new HashMap<>();

            float sum = 0.0f;
            for (Object cell: cells) {
                float[] cellData = accessPoint.getCellData(String.valueOf(cell));
                float temp = belief.get(String.valueOf(cell)) * cellData[scan.level];
                sum += temp;
                newBelief.put(String.valueOf(cell), temp);
            }
            float finalSum = sum;
            newBelief.replaceAll((k, v) -> finalSum != 0 ? v / finalSum : v);
            // Add belief to list of beliefs
            parallelBeliefs.add(newBelief);
        }

        return GetHighestCellFrequency(parallelBeliefs);
    }

    public String GetHighestCellFrequency(ArrayList<Map<String, Float>> accessPointsBelief) {
        Map<String, Integer> histogram = new HashMap<>();
        // Initialize histogram
        Map<String, Float> temp = accessPointsBelief.get(0);
        for (String key : temp.keySet())
            histogram.put(key, 0);

        // For each accessPoint get highest and update histogram
        for (Map<String, Float> accessPointBelief: accessPointsBelief) {
            String maxKey = "";
            float max = -Float.MAX_VALUE;
            for (String key : accessPointBelief.keySet()) {
                if (accessPointBelief.get(key) > max) {
                    max = accessPointBelief.get(key);
                    maxKey = key;
                }
            }
            histogram.put(maxKey, histogram.get(maxKey) + 1);
        }

        Set<Integer> values = new HashSet<>(histogram.values());
        boolean isSame = values.size() <= 1;

        if (isSame)
            return "";

        // Get max of histogram
        int max = -Integer.MAX_VALUE;
        String cell = "";
        for (String key : histogram.keySet()) {
            if (histogram.get(key) > max) {
                max = histogram.get(key);
                cell = key;
            }
        }
        return cell;
    }
}
