package com.tudelft.app2;

import static org.junit.Assert.*;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class AccessPointTest {

    @Before
    public void setUp() {
    }

    @Test
    public void cells() {
        AccessPoint accessPoint = new AccessPoint("test", 3);

        assertEquals(3, accessPoint.getKeys().size());

        Set<String> set = new HashSet<>();

        set.add("c1");
        set.add("c2");
        set.add("c3");

        assertEquals(set, accessPoint.getKeys());
    }

    @Test
    public void addCell() {
        AccessPoint accessPoint = new AccessPoint("test", 2);
        accessPoint.addCell("c3");

        float[] c1 = accessPoint.getCellData("c1");
        float[] c2 = accessPoint.getCellData("c2");
        float[] c3 = accessPoint.getCellData("c3");

        assert c1 != null;
        assert c2 != null;
        assert c3 != null;

        assertEquals(100, c1.length);
        assertEquals(c1.length, c2.length);
        assertEquals(c1.length, c3.length);

        for(int i = 0; i < c1.length; i++)
        {
            assertEquals(0.0, c1[i], 0.01);
            assertEquals(0.0, c2[i], 0.01);
            assertEquals(0.0, c3[i], 0.01);
        }
    }

    @Test
    public void addToCellHistogram() {
        AccessPoint accessPoint = new AccessPoint("test", 2);

        float[] c1 = accessPoint.getCellData("c1");
        float[] c2 = accessPoint.getCellData("c2");

        assertEquals(0.0, c1[50], 0.01);
        assertEquals(0.0, c2[5], 0.01);

        for (int i = 0; i < 3; i++) {
            accessPoint.addToCellHistogram("c1", 50);
            accessPoint.addToCellHistogram("c2", 5);
        }

        assertEquals(3.0, c1[50], 0.01);
        assertEquals(3.0, c2[5], 0.01);
    }

    @Test
    public void normalizeHistograms() {
        AccessPoint accessPoint = new AccessPoint("test", 0);

        float[] data = new float[100];
        data[0] = 0.0f;
        data[1] = 5.0f;
        data[2] = 3.0f;
        data[3] = 80.0f;

        accessPoint.addCell("c1", data);

        accessPoint.normalizeHistograms();

        float[] c1 = accessPoint.getCellData("c1");

        assertEquals(0.0f, c1[0], 0.01);
        assertEquals(0.056f, c1[1], 0.01);
        assertEquals(0.034f, c1[2], 0.01);
        assertEquals(0.9f, c1[3], 0.01);
    }
}