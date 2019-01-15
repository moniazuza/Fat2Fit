package com.example.monik.fat2fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "GoogleFitActivity";

    private float height;
    private float weight;
    private boolean male; // to determine users gender

    private GoogleApiClient googleApiClient = null;

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        height = Float.valueOf(findViewById(R.id.heightEditText).toString());
        weight = Float.valueOf(findViewById(R.id.weightEditText).toString());
        male = findViewById(R.id.maleRadioButton).isSelected();

        final Button saveSettingsButton = (Button) findViewById(R.id.saveSettingsButton);
        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings(height, weight, male);
            }
        });
    }

    private void saveSettings(float height, float weight, boolean gender) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataSet weightDataSet = createDataForRequest(DataType.TYPE_WEIGHT,
                DataSource.TYPE_RAW, weight, startTime, endTime, TimeUnit.MINUTES);

        DataSet heightDataSet = createDataForRequest(DataType.TYPE_HEIGHT,
                DataSource.TYPE_RAW, height, startTime, endTime, TimeUnit.MINUTES);

        insertDataSet(weightDataSet);
        insertDataSet(heightDataSet);

    }

    private DataSet createDataForRequest(DataType dataType, int dataSourceType, Object values,
                                         long startTime, long endTime, TimeUnit timeUnit) {
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(dataType)
                .setType(dataSourceType)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(startTime, endTime, timeUnit);

        if (values instanceof Integer) {
            dataPoint = dataPoint.setIntValues((Integer) values);
        } else {
            dataPoint = dataPoint.setFloatValues((Float) values);
        }

        dataSet.add(dataPoint);

        return dataSet;
    }

    //wyświetlenie nazwy zmiennej w logu?
    private  void insertDataSet(DataSet dataSet){
        com.google.android.gms.common.api.Status weightInsertStatus =
                Fitness.HistoryApi.insertData(googleApiClient, dataSet)
                        .await(1, TimeUnit.MINUTES);
        if (!weightInsertStatus.isSuccess()) {
            Log.i(TAG, "There was a problem inserting the " + dataSet.getDataType().getName());
        }
        Log.i(TAG, "Data insert was successful!");
    }

    //jeśli nie będę korzystać z tych samych scopów, to zostanie tak, a
    // jak w każdej aktywności będą te same, to do nadklasy
    @Override
    protected void connectToGoogleApi() {
        Log.i(TAG, "CONNECTING TO GOOGLE API");
        googleApiClient = new GoogleApiClient.Builder(this)
                // selecting the APIs
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.GOALS_API)
                // specifying the scopes of access
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                // providing callbacks
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }
}
