package com.example.monik.fat2fit;

import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    //BŁĄD Z POŁĄCZENIEM CONNECTION ISSUE CODE 2!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!1
    //!!!!!!!!!!!!!!!!!!!111111111111

    //[START Auth_Variable_References]
    private static final int REQUEST_OAUTH = 1;
    // [END auth_variable_references]

    private static final String TAG = "GoogleFitActivity";

    private GoogleApiClient googleApiClient = null;
    TextView stepsTextView;
    int initialNumberOfSteps = 0;
    private boolean firstCount = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepsTextView = (TextView) findViewById(R.id.stepsTextView);
    }

    private void connectToGoogleApi() {
        Log.i(TAG, "CONNECTING TO GOOGLE API");
        googleApiClient = new GoogleApiClient.Builder(this)
                // selecting the Sensors API
                .addApi(Fitness.SENSORS_API)
                // specifying the scopes of access
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                // providing callbacks
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    //connecting to google backend, if user wasn't connected
    @Override
    protected void onStart() {
        super.onStart();
        firstCount = true;
        initialNumberOfSteps = 0;
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            connectToGoogleApi();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "CONNECTED!");
        subscribeToFitnessAPI();
    }

    private void subscribeToFitnessAPI() {
        // Creating a onDataPointListener object to be called when new data is available
        OnDataPointListener onDataPointListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {

                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    updateStepsTextView(val.asInt());
                }
            }
        };

        //Specifying what data sources to return
        DataSourcesRequest dataSourcesRequest = new DataSourcesRequest.Builder()
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                .build();

        //  Invoking the Sensors API with Google API client object and data sources request object
        PendingResult<DataSourcesResult> pendingResult =
                Fitness.SensorsApi.findDataSources(googleApiClient, dataSourcesRequest);

        //  Build a sensor registration request object
        SensorRequest sensorRequest = new SensorRequest.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build();

        //  Invoking the Sensors API with:
        // - The Google API client object
        // - The sensor registration request object
        // - The onDataPointListener object
        PendingResult<Status> result =
                Fitness.SensorsApi.add(googleApiClient,
                        new SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .build(), onDataPointListener);
        //checking the result
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "LISTENER REGISTERED");
                } else {
                    Log.d(TAG, "LISTENER NOT REGISTERED");
                }
            }
        });
    }

    private void updateStepsTextView(final int stepsNumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (firstCount && (stepsNumber != 0)) {
                    initialNumberOfSteps = stepsNumber;
                    firstCount = false;
                }
                if (stepsTextView != null) {
                    stepsTextView.setText("Step count: " + String.valueOf(stepsNumber - initialNumberOfSteps));
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        // To determine reason for loosing the connection
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "CONNECTION FAILED - NETWORK LOTS");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG, "CONNECTION FAILED - SERVICE DISCONNECTED");
        }
    }

    // Managing OAuth authentication
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // if user's not logged in or if I don't have permission to acces data
        if (connectionResult.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED ||
                connectionResult.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
            try {
                // Requesting authentication
                connectionResult.startResolutionForResult(this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "EXCEPTION WHILE CONNECTING TO THE GOOGLE FIT SERVICES", e);
            }
        } else {

            //TAKI BŁĄD JEST CAŁY CZAS!!!!
            Log.e(TAG, "CONNECTION ISSUE. CODE: = " + connectionResult.getErrorCode());
        }
    }

    //If user granted access to data, connecting to google fit api
    @Override
    protected void onActivityResult(int request, int result, Intent intent) {
        if (request == REQUEST_OAUTH) {
            if (result == RESULT_OK) {
                googleApiClient.connect();
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(googleApiClient.isConnected() || googleApiClient.isConnecting()){
            googleApiClient.disconnect();
        }
        initialNumberOfSteps = 0;
        firstCount = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                showSettings();
                return true;
            case R.id.help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHelp() {
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}