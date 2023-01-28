package com.tudelft.app2;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class FileManagerTest {

    Context appContext;

    FileManager fileManager;

    @Before
    public void setUp() throws IOException {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fileManager = new FileManager(appContext);

        File file = new File(appContext.getDataDir() + "/bayes-data", "00:0c:f6:6c:cf:24.json");
        if (file.exists())
            file.delete();

        FileWriter writer = new FileWriter(file);
        writer.append("test");
        writer.flush();
        writer.close();
    }
}