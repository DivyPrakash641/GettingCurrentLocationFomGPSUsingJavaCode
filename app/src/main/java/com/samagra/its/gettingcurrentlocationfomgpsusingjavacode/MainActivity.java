package com.samagra.its.gettingcurrentlocationfomgpsusingjavacode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int PERMISSION_REQUEST_CODE = 9001;
    public static final int GPS_REQUEST_CODE = 9003;
    public static final String TAG = "MapDebug";
    private final double LAT = 33.693590;
    private final double LNG = 73.068872;
    private int PLAY_SERVICES_ERROR_CODE = 9002;
    private boolean mLocationPermissionGranted;
    private GoogleMap mGoogleMap;
    private EditText mSearchAddress;
    private Button search;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LocationCallback mLocationCallback;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        mSearchAddress = findViewById(R.id.et_address);
        search = findViewById(R.id.btn_locate);
        search.setOnClickListener(this::geoLocate);

        initGoogleMap();
        mFusedLocationProviderClient = new FusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Toast.makeText(MainActivity.this, location.getLatitude() + "\n" + location.getLongitude(), Toast.LENGTH_SHORT).show();

                if (locationResult == null) {
                    return;
                }
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                showMarker(latLng);
                gotoLocation(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "OnLocationResult: " + location.getLatitude() + "\n" + location.getLongitude());
                Log.d(TAG, "OnLocationResult: Thread Name: " + Thread.currentThread().getName());

            }


        };

    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }

    // used to convert langitute and latitute into location name

    private void geoLocate(View view) {
        hideSoftKeyboard(view);

        String locationName = mSearchAddress.getText().toString();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // used to address from lagitute and latitute coordinates
            List<Address> addressList = geocoder.getFromLocation(LAT, LNG, 3);  //1

            // used to get address
            // List<Address> addressList = geocoder.getFromLocationName(locationName,3);  //1

            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                gotoLocation(address.getLatitude(), address.getLongitude());
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())));
                Toast.makeText(this, address.getLocality(), Toast.LENGTH_SHORT).show();
            }

            for (Address address : addressList) {
                Toast.makeText(this, address.getAddressLine(address.getMaxAddressLineIndex()), Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // this method is used to initiate marker on map
    private void showMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mGoogleMap.addMarker(markerOptions);
    }

    // this method is used for hidesoftkeyboard
    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // method to check permission  to acess a map
    private void initGoogleMap() {

        if (isServicesOk()) {

            if (isGPSEnabled()) {
                if (checkLocationPermission()) {

                    SupportMapFragment supportMapFragment = (SupportMapFragment)
                            getSupportFragmentManager().findFragmentById(R.id.fragment);

                    supportMapFragment.getMapAsync(this);
                    Toast.makeText(this, "Ready to Map!", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestLocationPermission();
            }
        }


    }

    // used to  ready a map
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        //  this is used to enable my current location button on map
        //  mGoogleMap.setMyLocationEnabled(true);
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        //getUpdateLocationInEveryTime();
        getCurrentLocationUpdate();

    }

    // this method is used to get current location every time
    @SuppressLint("MissingPermission")
    private void getUpdateLocationInEveryTime() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // mSearchAddress.setText(String.valueOf(location.getLatitude()));
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                showMarker(latLng);
                gotoLocation(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                isGPSEnabled();
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

    }

    // method to set default location
    private void gotoLocation(double Lat, double Lng) {
        LatLng latLng = new LatLng(Lat, Lng);
        // method used to set only langitute and latitute in map -----  CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);

        // method used to set langitute and latitute along with zoom in map
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mGoogleMap.moveCamera(cameraUpdate);
        // enable and desable gastures
        //  mGoogleMap.getUiSettings().setZoomGesturesEnabled(false);
        // used to set map type
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // for getting run time permission
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }

    }

    // used to enable GPS
    private boolean isGPSEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerenable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerenable) {
            return true;
        } else {

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Permission")
                    .setMessage("GPS is required by this app to work . Please enable GPS")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, GPS_REQUEST_CODE);

                        }
                    })
                    .setCancelable(false)
                    .show();
        }

        return false;
    }

    // used to check wheather user is enable gps are not after going for enable it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean providerenable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (providerenable) {
                Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show();
                // used to refresh an activity at run time
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            } else {
                Toast.makeText(this, "GPS is not enabled. Unable to show user location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // method to check that google play services are available are
    private boolean isServicesOk() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            // return true if success
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            // show dialog if error are corrected by user
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE, task ->
                    Toast.makeText(this, "Dialog is cancelled by user", Toast.LENGTH_SHORT).show());
            dialog.show();
        } else {
            // show toast when play services are not available to devices
            Toast.makeText(this, "Play services are required by this application", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    // this used to stop getting location when it is pause
    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationCallback != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    // this used to start getting location when it is resume
    @Override
    protected void onResume() {
        super.onResume();
        if (mLocationCallback != null) {
            getCurrentLocationUpdate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.current_location: {
                // getCurrentLocation();
            }
            break;
            case R.id.maptype_none: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
            break;
            case R.id.maptype_normal: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            break;
            case R.id.maptype_satellite: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
            break;
            case R.id.maptype_terrain: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
            break;
            case R.id.maptype_hybrid: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            isGPSEnabled();
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task != null) {

                    Location location = (Location) task.getResult();
                    gotoLocation(location.getLatitude(), location.getLongitude());
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    showMarker(latLng);
                }
            }
        });

    }


}

