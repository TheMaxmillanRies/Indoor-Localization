package com.tudelft.app2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class AccessPoint {
    /**
     * Name
     */
    private String name;

    private int count;
    /**
     * dBm range for RSSI strength
     */
    private final int range = 100;
    /**
     * Dictionary of cells for 1 access point
     */
    private Map<String, float[]> table;
    /**
     * Guassian Filter
     */
    //private final float[] gaussianFilter = {0.006f, 0.061f, 0.242f, 0.383f, 0.242f, 0.061f, 0.006f};
    //private final float[] gaussianFilter = {0.00017844030101984716f, 0.015194648031729924f, 0.21868009956799153f, 0.5319230405352436f, 0.21868009956799153f, 0.015194648031729924f, 0.00017844030101984716f};

private final float[] gaussianFilter = {
            2.0539125955565396e-15f,
            1.0769163338864018e-12f,
            2.853835501248625e-10f,
            3.875541665365745e-8f,
            0.0000027044647128438015f,
            0.00009728896148653021f,
            0.0018106030017099872f,
            0.017498076791294337f,
            0.08814020177283344f,
            0.23217269725228706f,
            0.32055677742759325f,
            0.23217269725228706f,
            0.08814020177283344f,
            0.017498076791294337f,
            0.0018106030017099872f,
            0.00009728896148653021f,
            0.0000027044647128438015f,
            3.875541665365745e-8f,
            2.853835501248625e-10f,
            1.0769163338864018e-12f,
            2.0539125955565396e-15f,
};
    public AccessPoint(String name, int cellNumber) {
        table = new HashMap<>();
        this.name = name;
        this.count = 0;

        for (int i = 1; i <= cellNumber; i++)
            addCell("c" + i);
    }

    public void addCell(String cellName) {
        table.put(cellName, new float[range]);
    }

    public void addCell(String cellName, float[] data) {
        table.put(cellName, data);
    }

    public void addToCellHistogram(String cellName, int rssiValue) {
        table.put(cellName, addOneToHistogram(table.get(cellName), rssiValue));
    }

    private float[] addOneToHistogram(float[] histogram, int index) {
        histogram[index]++;
        return histogram;
    }

    public void normalizeHistograms() {
        table.replaceAll((k, v) -> normalize(table.get(k)));
    }

    private float[] normalize(float[] histogram) {
        int sum = 0;
        for (int i = 0; i < range; i++)
            sum += histogram[i];

        if (sum == 0) return histogram;

        for (int i = 0; i < range; i++)
            histogram[i] = histogram[i] / sum;
        return histogram;
    }

    public float[] getCellData(String cellName) {
        return table.get(cellName);
    }

    public void increaseCounter() {
        count++;
    }

    public void gaussianSmoothing() {
        table.replaceAll((k, v) -> convolution(table.get(k)));
    }

    private float[] convolution(float[] histogram) {
        float[] result = new float[range];
        for (int i = (gaussianFilter.length / 2); i < range - (gaussianFilter.length / 2); i++) {

            for (int j = -gaussianFilter.length / 2; j < gaussianFilter.length / 2; j++) {
                result[i] += histogram[i + j] * gaussianFilter[j + gaussianFilter.length / 2];
            }
        }
        return result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Map<String, float[]> getTable() {
        return table;
    }

    public Set<String> getKeys() {
        return table.keySet();
    }

}
