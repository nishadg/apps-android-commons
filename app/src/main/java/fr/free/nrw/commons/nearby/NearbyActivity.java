package fr.free.nrw.commons.nearby;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import fr.free.nrw.commons.R;

public class NearbyActivity extends AppCompatActivity implements LocationService.Callbacks {

    private MyLocationListener myLocationListener;
    private LocationManager locationManager;
    private String provider;
    private Criteria criteria;
    private LatLng mLatestLocation;
    private LocationService myService;

    private double currentLatitude, currentLongitude;
    //private String gpsCoords;
    boolean init;

    private static final String TAG = NearbyActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
//        registerLocationManager();

        Intent intent = new Intent();
        intent.setAction("fr.free.nrw.commons.LOC");
        intent.setPackage(getPackageName());
        Log.d("PackageName", getPackageName());
        startService(intent);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected LatLng getmLatestLocation() {
        return mLatestLocation;
    }

    /**
     * Registers a LocationManager to listen for current location
     */
    protected void registerLocationManager() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);
        myLocationListener = new MyLocationListener();

        try {
            locationManager.requestLocationUpdates(provider, 400, 1, myLocationListener);
            Location location = locationManager.getLastKnownLocation(provider);
            //Location works, just need to 'send' GPS coords via emulator extended controls if testing on emulator
            Log.d(TAG, "Checking for location...");
            if (location != null) {
                myLocationListener.onLocationChanged(location);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal argument exception", e);
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
        }
    }

    protected void unregisterLocationManager() {
        try {
            locationManager.removeUpdates(myLocationListener);
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
        }
    }

    @Override
    public void updateClient(double latitude, double longitude) {
        currentLatitude = latitude;
        currentLongitude = longitude;
        mLatestLocation = new LatLng(currentLatitude, currentLongitude);
        Log.d("Location", "Latitude: " + String.valueOf(currentLatitude) + " Longitude: " + String.valueOf(currentLongitude));
        if(!init){
            init = true;
            // Begin the transaction
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            NearbyListFragment fragment = new NearbyListFragment();
            ft.add(R.id.container, fragment);
            ft.commit();
        }
    }

    /**
     * Listen for user's location when it changes
     */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            Log.d(TAG, "Latitude: " + String.valueOf(currentLatitude) + " Longitude: " + String.valueOf(currentLongitude));

            mLatestLocation = new LatLng(currentLatitude, currentLongitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, provider + "'s status changed to " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "Provider " + provider + " enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "Provider " + provider + " disabled");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
//        unregisterLocationManager();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            // We've binded to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(NearbyActivity.this); //Activity register in the service as client for callbacks!
            Toast.makeText(myService, "Fetching Location. Please wait.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
