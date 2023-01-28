package com.tudelft.app2;

import static org.junit.Assert.*;

import android.net.wifi.ScanResult;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TrainManagerTest {

    List<ScanResult> scanResults;
    TrainManager trainManager;
    @Before
    public void setUp() {
        trainManager = new TrainManager();
        scanResults = new ArrayList<>();

        ScanResult firstResult = new ScanResult();
        firstResult.level = 20;
        firstResult.BSSID = "mac:11";
        ScanResult secondResult = new ScanResult();
        secondResult.level = 50;
        secondResult.BSSID = "mac:22";

        scanResults.add(firstResult);
        scanResults.add(secondResult);
    }

    @Test
    public void updateAccessPoints() {
        assertTrue(trainManager.getAccessPoints().isEmpty());
        trainManager.updateAccessPoints(scanResults, "c1");

        assertFalse(trainManager.getAccessPoints().isEmpty());
        assertEquals(2, trainManager.getAccessPoints().size());
    }
}