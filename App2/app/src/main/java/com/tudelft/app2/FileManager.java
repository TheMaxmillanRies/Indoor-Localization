package com.tudelft.app2;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileManager {
    Context applicationContext;

    public FileManager(Context applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Takes a map of access points and saves/updates them on the internal storage
     *
     * @param accessPoints Map of access points
     */
    public void save(Map<String, AccessPoint> accessPoints, String cell) throws JSONException, IOException {
        for (Map.Entry<String, AccessPoint> point : accessPoints.entrySet()) {
            JSONObject obj = new JSONObject();

            // If file exists, load the already existing json content
            File file = new File(applicationContext.getDataDir() + "/bayes-data", point.getKey()+".json");

            // Iterate over the new cells and add respective cell data.
            float[] smoothedData = point.getValue().getCellData(cell).clone(); // Does it add to 1?
            int count = point.getValue().getCount();

            if(file.exists()) {
                obj = load(file.getName());
                count += obj.getInt("count");

                float[] oldData = Utils.arrayFromString(obj.getString(cell));
                for (int i = 0; i < smoothedData.length; i++)
                    smoothedData[i] += oldData[i];

                try {
                    obj.put(cell, Arrays.toString(smoothedData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                for (String newCell : point.getValue().getKeys()) {
                    obj.put(newCell, Arrays.toString(point.getValue().getCellData(newCell)));
                }
            }
            obj.put("count", count);

            // Save matrix on storage
            writeFileOnInternalStorage(point.getKey() + ".json", obj.toString());
        }
    }

    /**
     * Takes a list of wifi scans and loads the file on internal storage with the same name. If
     * file is not found, the loading skips that specific scan.
     *
     * @param scanResults  Map of access points
     * @return List of loaded access points
     */
    public Map<String, AccessPoint> load(List<ScanResult> scanResults) throws JSONException, IOException {
        Map<String, AccessPoint> accessPoints = new HashMap<>();

        List<Pair<String, String>> results = new ArrayList<>();
        for(ScanResult scan : scanResults) {
            String fileName = scan.BSSID + ".json";
            results.add(readFromFile("bayes-data", fileName));
        }

        for(Pair<String, String> file : results)
        {
            if(file.first.equals(""))
                continue;
            AccessPoint sample = new AccessPoint(file.first, Utils.CELL_NUMBER);

            JSONObject obj = new JSONObject(file.second);
            for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                String key = it.next();
                if(key.equals("count"))
                    continue;
                sample.addCell(key, Utils.arrayFromString(obj.getString(key)));
            }

            // Pre-process data
            sample.normalizeHistograms();
            sample.gaussianSmoothing();

            sample.setCount(obj.getInt("count"));
            accessPoints.put(file.first, sample);
        }

        return accessPoints;
    }

    /**
     *  This function assumes that file already exists. It will load the file and create a new
     *  JSONObject from it.
     *
     * @param sFileName Name of the file to be loaded
     * @return JSONObject create from the loaded data
     */
    private JSONObject load(String sFileName) throws IOException, JSONException {
        Pair<String, String> file = readFromFile("bayes-data", sFileName);
        return new JSONObject(file.second);
    }

    private void writeFileOnInternalStorage(String sFileName, String sBody){
        File myDir = new File(applicationContext.getDataDir(), "bayes-data");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        try {
            File file = new File (myDir, sFileName);
            if (file.exists())
                file.delete();
            FileWriter writer = new FileWriter(file);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param dir Directory name where the file lies
     * @param fileName Loaded file name
     * @return Pair containing the name of the file and the json content
     */
    private Pair<String, String> readFromFile(String dir, String fileName) throws IOException {
        String result;
        InputStream inputStream;

        File file = new File(applicationContext.getDataDir() + "/" + dir, fileName);

        if(!file.exists())
            return new Pair<>("", "");

        inputStream = new FileInputStream(file);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String receiveString;
        StringBuilder stringBuilder = new StringBuilder();

        while ((receiveString = bufferedReader.readLine()) != null) {
            stringBuilder.append(receiveString);
        }

        result = stringBuilder.toString();

        return new Pair<>(file.getName().replace(".json", ""), result);
    }


}
