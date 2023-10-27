package com.sam.odometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

public class OdometerService extends Service {
    private static double distanceInMeters;
    private static Location lastLocation = null;
    private final IBinder binder = new OdometerBinder();
    private LocationListener listener;
    private LocationManager locManager;
    public static final String PERMISSION_STRING
            = android.Manifest.permission.ACCESS_FINE_LOCATION;

    public class OdometerBinder extends Binder {

        OdometerService getBinder() {
            return OdometerService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public double getDistance() {
        return this.distanceInMeters / 1609.344;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listener = location -> {
            if (lastLocation == null) {
                lastLocation = location;
            }
            distanceInMeters += location.distanceTo(lastLocation);
            lastLocation = location;
        };

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                == PackageManager.PERMISSION_GRANTED) {
            String provider = locManager.getBestProvider(new Criteria(), true);
            if (provider != null) {
                locManager.requestLocationUpdates(provider, 1000, 1, listener);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locManager != null && listener != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                    == PackageManager.PERMISSION_GRANTED) {
                locManager.removeUpdates(listener);
            }
            locManager = null;
            listener = null;
        }
    }
}