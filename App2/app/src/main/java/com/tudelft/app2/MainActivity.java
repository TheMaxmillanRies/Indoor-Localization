package com.tudelft.app2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements WifiListener{
    BayesFilter sequentialFilter;
    BayesFilter parallelFilter;

    FileManager fileManager;

    ArrayList<View> cells;
    TextView cellText;
    Button parallelButton;
    Button sequentialButton;

    /**
     * The wifi manager.
     */
    private WifiController wifiController;

    private boolean parallel = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sequentialFilter = new SequentialBayesFilter();
        parallelFilter = new ParallelBayesFilter();

        wifiController = new WifiController(getApplicationContext(), this);

        fileManager = new FileManager(getApplicationContext());

        cells = new ArrayList<>();
        cells.add((View) findViewById(R.id.c1));
        cells.add((View) findViewById(R.id.c2));
        cells.add((View) findViewById(R.id.c3));
        cells.add((View) findViewById(R.id.c4));
        cells.add((View) findViewById(R.id.c5));
        cells.add((View) findViewById(R.id.c6));
        cells.add((View) findViewById(R.id.c7));
        cells.add((View) findViewById(R.id.c8));
        cells.add((View) findViewById(R.id.c9));
        cells.add((View) findViewById(R.id.c10));
        cells.add((View) findViewById(R.id.c11));
        cells.add((View) findViewById(R.id.c12));
        cells.add((View) findViewById(R.id.c13));
        cells.add((View) findViewById(R.id.c13));
        cells.add((View) findViewById(R.id.c13));
        cellText = (TextView) findViewById(R.id.cellText);
        parallelButton = (Button) findViewById(R.id.parallel);
        sequentialButton = (Button) findViewById(R.id.sequential);
    }

    public void enterTraining(View view)
    {
        Intent intent = new Intent(this, TrainActivity.class);
        startActivity(intent);
    }

    public void locateSequential(View view) throws JSONException, IOException {
        locate(false);
    }

    public void locateParallel(View view) throws JSONException, IOException {
        locate(true);
    }

    @SuppressLint("SetTextI18n")
    private void locate(boolean parallel) throws JSONException, IOException {
        this.parallel = parallel;
        sequentialButton.setEnabled(false);
        parallelButton.setEnabled(false);
        wifiController.getScanResults(true);
    }

    public void resetBeliefs(View view)
    {
        sequentialFilter.resetBelief();
        parallelFilter.resetBelief();
    }

    @Override
    public void callback(List<ScanResult> scanResults) throws JSONException, IOException {
        Map<String, AccessPoint> accessPoints = fileManager.load(scanResults);

        String result;

        if(parallel)
            result = parallelFilter.getLocation(accessPoints, scanResults, Utils.TOTAL_SCANS); //change depending on total data size
        else
            result = sequentialFilter.getLocation(accessPoints, scanResults, Utils.TOTAL_SCANS); //change depending on total data size

        if (result.isEmpty()) return;

        int roomId = Integer.parseInt(result.replace("c",""));
        if (roomId >= 13)
            cellText.setText("c"+roomId);

        cells.get(roomId-1).setBackgroundColor(Color.parseColor("#FFEA7E7E"));

        // Revert back to default cell color after 500ms
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            cells.get(roomId-1).setBackgroundColor(Color.parseColor("#A5A5A5"));
            cellText.setText("c13\nc14\nc15");
        }, 600);

        sequentialButton.setEnabled(true);
        parallelButton.setEnabled(true);
    }
}