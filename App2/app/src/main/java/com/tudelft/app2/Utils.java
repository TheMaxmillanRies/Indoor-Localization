package com.tudelft.app2;

import android.content.Context;

import java.io.File;

public class Utils{
    public final static int CELL_NUMBER = 2;
    public final static int TOTAL_SCANS = 100;

    public static float[] arrayFromString(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        float[] result = new float[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Float.parseFloat(strings[i]);
        }
        return result;
    }
}
