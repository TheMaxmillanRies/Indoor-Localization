package com.tudelft.app1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class WifiSample{
    String label;
    int rssi1, rssi2, rssi3, rssi4;

    public WifiSample(String label, int rssi1, int rssi2, int rssi3, int rssi4)
    {
        this.label = label;
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        this.rssi4 = rssi4;
    }

    public WifiSample()
    {

    }

    public WifiSample(int rssi1, int rssi2, int rssi3, int rssi4)
    {
        this.label = "";
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        this.rssi4 = rssi4;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public int getRssi1() {
        return rssi1;
    }

    public int getRssi2() {
        return rssi2;
    }

    public int getRssi3() {
        return rssi3;
    }

    public int getRssi4() {
        return rssi4;
    }

    public void setRssi1(int rssi1) {
        this.rssi1 = rssi1;
    }

    public void setRssi2(int rssi2) {
        this.rssi2 = rssi2;
    }

    public void setRssi3(int rssi3) {
        this.rssi3 = rssi3;
    }

    public void setRssi4(int rssi4) {
        this.rssi4 = rssi4;
    }
}

class ActivitySample{
    private float x,y,z;
    private String label;


    public ActivitySample()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.label = "";
    }

    public ActivitySample(float x, float y, float z, String label)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public String getLabel() {
        return label;
    }
}
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ArrayList<ActivitySample> data;
    private ArrayList<WifiSample> wifiData;

    /**
     * The sensor manager object.
     */
    private SensorManager sensorManager;
    /**
     * The accelerometer.
     */
    private Sensor accelerometer;
    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;
    /**
     * The wifi info.
     */
    private WifiInfo wifiInfo;

    float max_x = -Float.MAX_VALUE;
    float max_y = -Float.MAX_VALUE;
    float max_z = -Float.MAX_VALUE;
    float min_x = Float.MAX_VALUE;
    float min_y = Float.MAX_VALUE;
    float min_z = Float.MAX_VALUE;

    int sampleCount = 0;

    boolean recording = false;

    String room = "Unknown";

    /**
     * Accelerometer x value
     */
    private float aX = 0;
    /**
     * Accelerometer y value
     */
    private float aY = 0;
    /**
     * Accelerometer z value
     */
    private float aZ = 0;

    private ArrayList<ActivitySample> data;

    @SuppressLint("SdCardPath")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new ArrayList<>();
        wifiData = new ArrayList<>();

        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //progressBar.setMax(1);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // No accelerometer!
        }

        TextView textView = (TextView) findViewById(R.id.activityView);
        textView.setText("Activity: Unknown");

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this::start);

        try {
            readData();
            readWifiData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void readData() throws JSONException {
        String[] results = readFromFiles("datasets");
        for(String str : results)
        {
            JSONArray array = new JSONArray(str);

            for (int i = 0; i < array.length(); i++)
            {
                ActivitySample sample = new ActivitySample();
                sample.setLabel(array.getJSONObject(i).getString("label"));
                sample.setX(array.getJSONObject(i).getInt("x"));
                sample.setY(array.getJSONObject(i).getInt("y"));
                sample.setZ(array.getJSONObject(i).getInt("z"));
                data.add(sample);
            }

        }
    }

    public void readWifiData() throws JSONException {
        String result = readFromFile("wifi");
        JSONArray array = new JSONArray(result);

        for (int i = 0; i < array.length(); i++)
        {
            WifiSample sample = new WifiSample(
                    array.getJSONObject(i).getString("label"),
                    array.getJSONObject(i).getInt("60:f1:8a:76:a6:f0"),
                    array.getJSONObject(i).getInt("b4:b0:24:ef:64:08"),
                    array.getJSONObject(i).getInt("b4:b0:24:ef:64:09"),
                    array.getJSONObject(i).getInt("30:d1:7e:bd:ff:80")
            );
            wifiData.add(sample);
        }

    }
    /** https://stackoverflow.com/questions/40483601/read-content-of-json-file-from-internal-storage */
    private String readFromFile(String dir)
    {
        String ret = "";
        InputStream inputStream = null;

        File file = new File(getApplicationContext().getDataDir() + "/" + dir, "/data.json");

        try {
            inputStream = new FileInputStream(file);

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }
            ret = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    private String[] readFromFiles(String dir) {

        String ret = "";
        InputStream inputStream = null;

        File dataDir = new File(getApplicationContext().getDataDir(), dir);
        File[] files = dataDir.listFiles();

        Log.d("file", getApplicationContext().getDataDir().toString());

        assert files != null;
        String[] results = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            try {
                inputStream = new FileInputStream(files[i]);

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                ret = stringBuilder.toString();
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            } finally {
                try {
                    assert inputStream != null;
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            results[i] = ret;
            ret = "";
        }


        return results;
    }

    /**  */
    public void enterTraining(View view)
    {
        Intent intent = new Intent(this, TrainActivity.class);
        startActivity(intent);
    }

    protected String KNN(int k, ActivitySample activity) {
        ArrayList<Float> distances = GetEuclideanDistance(activity);
        ArrayList<ActivitySample> top = new ArrayList<>();
        ArrayList<ActivitySample> copy = new ArrayList<>(data);
        for (int i = 0; i < k; i++) {
            int minIndex = 0;

            for (int j = 0; j < copy.size(); j++)
                if (distances.get(j) < distances.get(minIndex))
                    minIndex = j;

            top.add(copy.remove(minIndex));
            distances.remove(minIndex);
        }

        return GetHighestCount(top);
    }

    public void start(View view)
    {
        recording = true;
        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Start a wifi scan.
        wifiManager.startScan();
        // Store results in a list.
        List<ScanResult> scanResults = wifiManager.getScanResults();
        // Write results to a label
        scanResults.removeIf(scanResult -> !scanResult.BSSID.equals("60:f1:8a:76:a6:f0") && !scanResult.BSSID.equals("b4:b0:24:ef:64:09") && !scanResult.BSSID.equals("b4:b0:24:ef:64:08") && !scanResult.BSSID.equals("30:d1:7e:bd:ff:80"));

        if(scanResults.size() != 4) {
            room = "Unknown";
            return;
        }

        WifiSample testSample = new WifiSample();
        for (ScanResult scanResult : scanResults) {
            switch (scanResult.BSSID)
            {
                case "60:f1:8a:76:a6:f0":
                    testSample.setRssi1(scanResult.level);
                    break;
                case "b4:b0:24:ef:64:08":
                    testSample.setRssi2(scanResult.level);
                    break;
                case "b4:b0:24:ef:64:09":
                    testSample.setRssi3(scanResult.level);
                    break;
                case "30:d1:7e:bd:ff:80":
                    testSample.setRssi4(scanResult.level);
                    break;
                default:
                    break;
            }
        }
        room = KNNWifi(5, testSample);

        switch (room)
        {
            case "A":
                TextView textViewA = (TextView) findViewById(R.id.cellA);
                textViewA.setBackgroundColor( Color.parseColor("#FFFFFFFF"));
                break;
            case "B":
                TextView textViewB = (TextView) findViewById(R.id.cellB);
                textViewB.setBackgroundColor( Color.parseColor("#FFFFFFFF"));
                break;
            case "C":
                TextView textViewC = (TextView) findViewById(R.id.cellC);
                textViewC.setBackgroundColor( Color.parseColor("#FFFFFFFF"));
                break;
            case "D":
                TextView textViewD = (TextView) findViewById(R.id.cellD);
                textViewD.setBackgroundColor( Color.parseColor("#FFFFFFFF"));
                break;
            default:
                break;
        }

        new Handler().postDelayed(() -> {
            TextView textViewA = (TextView) findViewById(R.id.cellA);
            textViewA.setBackgroundColor( Color.parseColor("#B6B6B6"));

            TextView textViewB = (TextView) findViewById(R.id.cellB);
            textViewB.setBackgroundColor( Color.parseColor("#B6B6B6"));

            TextView textViewC = (TextView) findViewById(R.id.cellC);
            textViewC.setBackgroundColor( Color.parseColor("#B6B6B6"));

            TextView textViewD = (TextView) findViewById(R.id.cellD);
            textViewD.setBackgroundColor( Color.parseColor("#B6B6B6"));

        }, 500);
    }

    protected String KNNWifi(int k, WifiSample wifiSample) {
        ArrayList<Float> distances = GetEuclideanDistanceWifi(wifiSample);
        ArrayList<WifiSample> top = new ArrayList<>();
        ArrayList<WifiSample> copy = new ArrayList<>(wifiData);
        for (int i = 0; i < k; i++) {
            int minIndex = 0;

            for (int j = 0; j < copy.size(); j++)
                if (distances.get(j) < distances.get(minIndex))
                    minIndex = j;

            top.add(copy.remove(minIndex));
            distances.remove(minIndex);
        }

        return GetHighestCountWifi(top);
    }

    protected ArrayList<Float> GetEuclideanDistanceWifi(WifiSample testSample) {
        ArrayList<Float> distances = new ArrayList<>();

        for (WifiSample wifiSample  : wifiData)
        {
            distances.add((float)Math.sqrt(Math.pow(wifiSample.getRssi1() - testSample.getRssi1(), 2) + Math.pow(wifiSample.getRssi2() - testSample.getRssi2(), 2) + Math.pow(wifiSample.getRssi3() - testSample.getRssi3(), 2) + Math.pow(wifiSample.getRssi4() - testSample.getRssi4(), 2)));
        }

        for(int i = 0; i < distances.size(); i++)
            Log.v("distance", Float.toString(distances.get(i)) + " " + wifiData.get(i).getLabel());

        return distances;
    }

    protected String GetHighestCount(ArrayList<ActivitySample> activities) {
        int stillCount = 0, walkingCount = 0, activity3Count = 0;

        for (ActivitySample activitySample : activities)
        {
            if (activitySample.getLabel().equals("still"))
                stillCount++;
            else if (activitySample.getLabel().equals("walking"))
                walkingCount++;
            else
                activity3Count++;
        }

        if (stillCount > walkingCount && stillCount > activity3Count)
            return "still";
        else if (walkingCount > activity3Count)
            return "walking";
        else
            return "activity3";
    }

    protected String GetHighestCountWifi(ArrayList<WifiSample> top) {
        int r1 = 0, r2 = 0, r3 = 0, r4 = 0;

        for (WifiSample wifiSample : top)
        {
            if (wifiSample.getLabel().equals("A"))
                r1++;
            else if (wifiSample.getLabel().equals("B"))
                r2++;
            else if (wifiSample.getLabel().equals("C"))
                r3++;
            else
                r4++;
        }

        if (r1 > r2 && r1 > r3 && r1 > r4)
            return "A";
        else if (r2 > r3 && r2 > r4)
            return "B";
        else if (r3 > r4)
            return "C";
        else
            return "D";
    }

    protected ArrayList<Float> GetEuclideanDistance(ActivitySample activity) {
        ArrayList<Float> distances = new ArrayList<>();

        for (ActivitySample activitySample : data)
            distances.add((float)Math.sqrt(Math.pow(activitySample.getX() - activity.getX(), 2) + Math.pow(activitySample.getY() - activity.getY(), 2) + Math.pow(activitySample.getZ() - activity.getZ(), 2)));


        return distances;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(data.isEmpty())
            return;
        if(!recording)
            return;
        if (sampleCount == 20) {
            sampleCount = 0;
            float feature_x, feature_y, feature_z;
            feature_x = max_x - min_x;
            feature_y = max_y - min_y;
            feature_z = max_z - min_z;

            String knnResult = KNN(7, new ActivitySample(feature_x, feature_y, feature_z, ""));

            TextView textView = (TextView) findViewById(R.id.activityView);
            textView.setText("Activity: " + knnResult + "\n" + "Room: " + room);

            resetMax();
            recording = false;
        }
        // Retrieve sensor data
        aX = sensorEvent.values[0];
        aY = sensorEvent.values[1];
        aZ = sensorEvent.values[2];

        // Update min/max
        if (aX > max_x) max_x = aX;
        if (aY > max_y) max_y = aY;
        if (aZ > max_z) max_z = aZ;
        if (aX < min_x) min_x = aX;
        if (aY < min_y) min_y = aY;
        if (aZ < min_z) min_z = aZ;

        sampleCount++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // onPause() unregisters the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    public void resetMax()
    {
        max_x = -Float.MAX_VALUE;
        max_y = -Float.MAX_VALUE;
        max_z = -Float.MAX_VALUE;
        min_x = Float.MAX_VALUE;
        min_y = Float.MAX_VALUE;
        min_z = Float.MAX_VALUE;
    }
}
