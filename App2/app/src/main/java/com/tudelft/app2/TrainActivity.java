package com.tudelft.app2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class TrainActivity extends AppCompatActivity implements WifiListener{
    /**
     * The wifi manager.
     */
    private WifiController wifiController;

    private FileManager fileManager;

    private TrainManager trainManager;

    private int TOTAL_NUMBER_SAMPLES = 30;

    int samples = 0;

    // UI
    ProgressBar progressBar;
    TextView samplesProgressText;
    EditText totalSamples;
    EditText editText;
    Button trainButton;
    TextView infoPanel;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        wifiController = new WifiController(getApplicationContext(), this);

        fileManager = new FileManager(getApplicationContext());
        trainManager = new TrainManager();

        progressBar = findViewById(R.id.progressBar2);
        samplesProgressText = findViewById(R.id.sampleProgress);
        totalSamples = findViewById(R.id.samplesInput);
        editText = findViewById(R.id.labelField);
        infoPanel = findViewById(R.id.infoPanel);
        trainButton = findViewById(R.id.trainButton);

        totalSamples.setText("30");

        updateProgressUI();
    }

    @SuppressLint("SetTextI18n")
    public void updateProgressUI()
    {
        progressBar.setMax(TOTAL_NUMBER_SAMPLES);
        progressBar.setProgress(samples);
        samplesProgressText.setText(samples + "/" + TOTAL_NUMBER_SAMPLES);
    }

    public void reset()
    {
        trainManager.clearAccessPoints();
        samples = 0;
        TOTAL_NUMBER_SAMPLES = Integer.parseInt(totalSamples.getText().toString());
        updateProgressUI();
    }

    public void reset(View view)
    {
        reset();
    }

    @SuppressLint("SetTextI18n")
    public void train(View v) {
        if (samples == TOTAL_NUMBER_SAMPLES)
            return;

        trainButton.setEnabled(false);

        wifiController.getScanResults(true);

        // Update the total number of samples
        if (Integer.parseInt(totalSamples.getText().toString()) != TOTAL_NUMBER_SAMPLES)
            reset();

        samples++;
    }

    @SuppressLint("SetTextI18n")
    public void save(View v) throws JSONException, IOException {

        if(samples < TOTAL_NUMBER_SAMPLES - 1)
        {
            infoPanel.setText("Number of samples not reached!");
            return;
        }

        fileManager.save(trainManager.getAccessPoints(), editText.getText().toString());
        reset();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void callback(List<ScanResult> scanResults) {
        trainManager.updateAccessPoints(scanResults, editText.getText().toString());

        infoPanel.setText("Number of samples not reached!");
        updateProgressUI();

        trainButton.setEnabled(true);

        String s = "Previous Scan:\n";
        int count = 0;
        for(ScanResult scanResult : scanResults) {
            if (count > 4)
                break;
            s += "BSSID: " + scanResult.BSSID + ", RSSI: " + scanResult.level + "\n";
            count++;
        }

        infoPanel.setText(s);
    }
}