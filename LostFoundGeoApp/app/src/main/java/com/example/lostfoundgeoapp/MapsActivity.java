package com.example.lostfoundgeoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private com.example.lostfoundgeoapp.DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;

    private EditText editRadius;
    private Button btnApplyRadius;

    private Location currentUserLocation;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadCurrentLocationAndItems();
                } else {
                    Toast.makeText(this, "Location permission is required for radius search", Toast.LENGTH_SHORT).show();
                    showAllItems();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        databaseHelper = new com.example.lostfoundgeoapp.DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editRadius = findViewById(R.id.editRadius);
        btnApplyRadius = findViewById(R.id.btnApplyRadius);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnApplyRadius.setOnClickListener(v -> applyRadiusSearch());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            loadCurrentLocationAndItems();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadCurrentLocationAndItems() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            currentUserLocation = location;

            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
            }

            showAllItems();
        });
    }

    private void showAllItems() {
        googleMap.clear();

        List<LostFoundItem> items = databaseHelper.getAllItems();

        for (LostFoundItem item : items) {
            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(item.getPostType() + ": " + item.getName())
                    .snippet(item.getDescription() + " | " + item.getLocationName()));
        }

        if (!items.isEmpty()) {
            LostFoundItem first = items.get(0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(first.getLatitude(), first.getLongitude()),
                    10
            ));
        }
    }

    private void applyRadiusSearch() {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String radiusText = editRadius.getText().toString().trim();

        if (radiusText.isEmpty()) {
            Toast.makeText(this, "Enter radius in km", Toast.LENGTH_SHORT).show();
            return;
        }

        double radiusKm = Double.parseDouble(radiusText);
        showItemsWithinRadius(radiusKm);
    }

    private void showItemsWithinRadius(double radiusKm) {
        googleMap.clear();

        LatLng userLatLng = new LatLng(
                currentUserLocation.getLatitude(),
                currentUserLocation.getLongitude()
        );

        googleMap.addMarker(new MarkerOptions()
                .position(userLatLng)
                .title("Your Current Location"));

        googleMap.addCircle(new CircleOptions()
                .center(userLatLng)
                .radius(radiusKm * 1000));

        List<LostFoundItem> items = databaseHelper.getAllItems();
        int count = 0;

        for (LostFoundItem item : items) {
            float[] distanceResult = new float[1];

            Location.distanceBetween(
                    currentUserLocation.getLatitude(),
                    currentUserLocation.getLongitude(),
                    item.getLatitude(),
                    item.getLongitude(),
                    distanceResult
            );

            double distanceKm = distanceResult[0] / 1000.0;

            if (distanceKm <= radiusKm) {
                LatLng itemLatLng = new LatLng(item.getLatitude(), item.getLongitude());

                googleMap.addMarker(new MarkerOptions()
                        .position(itemLatLng)
                        .title(item.getPostType() + ": " + item.getName())
                        .snippet(item.getDescription() + " | " + String.format("%.2f km away", distanceKm)));

                count++;
            }
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));

        Toast.makeText(this, count + " item(s) found within radius", Toast.LENGTH_SHORT).show();
    }
}