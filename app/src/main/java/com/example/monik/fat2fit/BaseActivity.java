package com.example.monik.fat2fit;


import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

//Abstract, bo nie będę tworzyć takich obiektow, tylko to szablon dla reszty
public abstract class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    //[START Auth_Variable_References]
    protected static final int REQUEST_OAUTH = 1;
    // [END auth_variable_references]

    protected static final String TAG = "GoogleFitActivity";

    protected GoogleApiClient googleApiClient = null;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "CONNECTED!");
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

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            connectToGoogleApi();
        }
    }

    protected abstract void connectToGoogleApi();
}
