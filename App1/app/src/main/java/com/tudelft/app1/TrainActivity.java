package com.tudelft.app1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TrainActivity extends AppCompatActivity implements SensorEventListener {
    private final int MAX_FEATURE_COUNT = 200;
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

    private JSONArray featuresArray;
    private JSONArray wifiArray;

    private int sampleCount;
    private int featureCount;
    private boolean training;

    float max_x = -Float.MAX_VALUE;
    float max_y = -Float.MAX_VALUE;
    float max_z = -Float.MAX_VALUE;
    float min_x = Float.MAX_VALUE;
    float min_y = Float.MAX_VALUE;
    float min_z = Float.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        featuresArray = new JSONArray();
        wifiArray = new JSONArray();

        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

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
    }

    public void startTraining(View view)
    {
        training = true;
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(0);
    }

    public void startWifiTraining(View view) throws JSONException {
        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Start a wifi scan.
        wifiManager.startScan();
        // Store results in a list.
        List<ScanResult> scanResults = wifiManager.getScanResults();
        // Write results to a label
        scanResults.removeIf(scanResult -> !scanResult.BSSID.equals("60:f1:8a:76:a6:f0") && !scanResult.BSSID.equals("b4:b0:24:ef:64:09") && !scanResult.BSSID.equals("b4:b0:24:ef:64:08") && !scanResult.BSSID.equals("30:d1:7e:bd:ff:80"));

        EditText editText = (EditText) findViewById(R.id.labelView);
        String label = editText.getText().toString();

        JSONObject obj = new JSONObject();

        obj.put("label", label);
        // Create items.
        TextView textRssi = (TextView) findViewById(R.id.textRSSI);
        textRssi.setText("");


        for (ScanResult scanResult : scanResults) {
            obj.put(scanResult.BSSID, scanResult.level);
            textRssi.setText(textRssi.getText() + "\n\tBSSID = "
                    + scanResult.BSSID + "    RSSI = "
                    + scanResult.level + "dBm");
        }

        if (scanResults.size() != 4)
        {
            textRssi.setText(textRssi.getText() + "Not added");
            return;
        }

        wifiArray.put(obj);
    }

    public void saveWifiData(View view) throws IOException {
        writeFileOnInternalStorage("data.json", wifiArray.toString(), "wifi");
        wifiArray = new JSONArray();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // get the the x,y,z values of the accelerometer
        if (!training)
            return;

        EditText editText = (EditText) findViewById(R.id.labelView);
        String label = editText.getText().toString();

        if (featureCount >= MAX_FEATURE_COUNT) {
            training = false;
            featureCount = 0;
            // Convert JsonObject to String Format
            String dataString = featuresArray.toString();

            try {
                writeFileOnInternalStorage(label + ".json", dataString, "datasets");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (sampleCount == 20) {
            sampleCount = 0;
            featureCount++;
            float feature_x, feature_y, feature_z;
            feature_x = max_x - min_x;
            feature_y = max_y - min_y;
            feature_z = max_z - min_z;

            resetMax();

            JSONObject jsonObject = new JSONObject();
            try {
                editText = (EditText) findViewById(R.id.labelView);
                label = editText.getText().toString();
                jsonObject.put("label", label);
                jsonObject.put("x", feature_x);
                jsonObject.put("y", feature_y);
                jsonObject.put("z", feature_z);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            featuresArray.put(jsonObject);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setProgress((int)((float)featureCount/MAX_FEATURE_COUNT*100));

            Log.d("progress", Integer.toString((int)((float)featureCount/MAX_FEATURE_COUNT*100)));
            return;
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

    public void writeFileOnInternalStorage(String sFileName, String sBody, String dir) throws IOException {
        File myDir = new File(getApplicationContext().getDataDir(), dir);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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