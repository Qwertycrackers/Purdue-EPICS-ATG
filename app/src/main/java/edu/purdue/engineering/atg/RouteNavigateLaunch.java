package edu.purdue.engineering.atg;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * All code herein is owned by Purdue-EPICS-DRC, and was created by the Fall 2017 team.
 */

public class RouteNavigateLaunch extends AppCompatActivity implements OnMapReadyCallback{
    private StatsManager stats;
    private RoutePtr route;
    private FusedLocationProviderClient locator;
    private LocationRequest locationRequest;
    private Intent intent;
    private Intent routeIntent;
    private MapView map;

    private boolean requestingLocationUpdates = false;
    private boolean locationPermissions = false;
    /** Create the app */
    @TargetApi(26)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_navigate);
        setSupportActionBar((Toolbar) findViewById(R.id.routeNavigatScreenToolBar));
        intent = getIntent();
        stats = intent.getParcelableExtra("stats");
        intent.setExtrasClassLoader(RoutePtr.class.getClassLoader());
        route = intent.getParcelableExtra("route");
        map = (MapView) findViewById(R.id.mapView);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);
        stats.setLayout((LinearLayout) findViewById(R.id.route_navigate_layout));

        locator = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest()
                .setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        routeIntent = new Intent(this, RouteNavigate.class)
                .putExtra("route", route)
                .putExtra("locationRequest", locationRequest)
        ;
        locationPermissions = (Build.VERSION.SDK_INT < 26)||checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        startService(routeIntent);
    }
/** Start the app */
    public void onStart() {
        super.onStart();
        map.onStart();
    }
/** Resume the app */
    public void onResume() {
        super.onResume();
        startGPS();
        map.onResume();
    }
/** Pause the app */
    public void onPause() {
        super.onPause();
        stopGPS();
        map.onPause();
    }
/** Stop the app */
    public void onStop() {
        super.onStop();
        map.onStop();
    }
/** Destroy the app */
    public void onDestroy() {
        super.onDestroy();
        stopService(routeIntent);
        map.onDestroy();
    }
/** Start the GPS location updates */
    private void startGPS() {
        if (locationPermissions && !requestingLocationUpdates) {
            try {
                locator.requestLocationUpdates(locationRequest, stats.getLocationCallback(), null);
            } catch (SecurityException e) {
                stats.updateGPSState(StatsManager.GPS_NEED_PERMISSION);
            }

        }

    }
/** Stop the GPS location updates */
    private void stopGPS() {
        if (locationPermissions && requestingLocationUpdates) {
            try {
                locator.removeLocationUpdates(stats.getLocationCallback());
            } catch (SecurityException e) {
                stats.updateGPSState(StatsManager.GPS_NEED_PERMISSION);
            }

        }
    }
/** Save the app instance if it is flipped */
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        map.onSaveInstanceState(outState);
    }
/** Handle low memory state */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    /** Prepare the Google Map once it is ready
     *
     * @param googleMap The map to prepare
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        RouteNode[] nodes = route.getRouteNodes();
        for(RouteNode node : nodes)
            googleMap.addMarker(new MarkerOptions().position(new LatLng(node.getLocation().getLatitude(),node.getLocation().getLongitude())));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nodes[0].getLocation().getLatitude(),nodes[0].getLocation().getLongitude()),15));
        if(Build.VERSION.SDK_INT >= 23)
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }

    }
}