package com.example.lostfoundgeoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddAdvertActivity extends AppCompatActivity {

    EditText editName, editPhone, editDescription, editDate, editLocation;
    RadioButton radioLost, radioFound;
    Button btnCurrentLocation, btnSave;

    com.example.lostfoundgeoapp.DatabaseHelper databaseHelper;
    FusedLocationProviderClient fusedLocationClient;

    double selectedLat = 0.0;
    double selectedLng = 0.0;
    private final Calendar selectedDate = Calendar.getInstance();
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<android.content.Intent> autocompleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    String address = place.getAddress();
                    if (address == null) address = place.getName();
                    
                    editLocation.setText(address);

                    if (place.getLatLng() != null) {
                        selectedLat = place.getLatLng().latitude;
                        selectedLng = place.getLatLng().longitude;
                        Log.d("Places", "Selected: " + selectedLat + ", " + selectedLng);
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    assert result.getData() != null;
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Log.e("Places", "Autocomplete error: " + status.getStatusMessage());
                    Toast.makeText(this, "Place Search Error. Check API Key.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_advert);

        String apiKey = "AIzaSyB4WA6fbP9AmEqAgMy_J42yMJiySAZ-a-o";
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        databaseHelper = new com.example.lostfoundgeoapp.DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editDescription = findViewById(R.id.editDescription);
        editDate = findViewById(R.id.editDate);
        editDate.setFocusable(false);
        editDate.setClickable(true);

        editDate.setOnClickListener(v -> showDatePicker());
        editLocation = findViewById(R.id.editLocation);

        radioLost = findViewById(R.id.radioLost);
        radioFound = findViewById(R.id.radioFound);

        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnSave = findViewById(R.id.btnSave);

        // Fix: Ensure the EditText doesn't steal focus and triggers click reliably
        editLocation.setFocusable(false);
        editLocation.setFocusableInTouchMode(false);
        editLocation.setOnClickListener(v -> openAutocomplete());

        btnCurrentLocation.setOnClickListener(v -> checkLocationPermission());
        btnSave.setOnClickListener(v -> saveAdvert());
    }

    private void openAutocomplete() {
        Log.d("AddAdvert", "Opening Autocomplete");
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );

        android.content.Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                fields
        ).build(this);

        autocompleteLauncher.launch(intent);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateLocationUI(location.getLatitude(), location.getLongitude());
                    } else {
                        // Try last location as fallback
                        fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                            if (lastLoc != null) {
                                updateLocationUI(lastLoc.getLatitude(), lastLoc.getLongitude());
                            } else {
                                Toast.makeText(this, "Location unavailable. Check GPS settings.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Location", "Error", e);
                    Toast.makeText(this, "Location Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLocationUI(double lat, double lng) {
        selectedLat = lat;
        selectedLng = lng;
        String addressText = getAddressFromLatLng(lat, lng);
        editLocation.setText(addressText);
        Log.d("Location", "Updated UI: " + lat + ", " + lng);
        Toast.makeText(this, "Location found!", Toast.LENGTH_SHORT).show();
    }

    private String getAddressFromLatLng(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (Exception e) {
            Log.e("Geocoder", "Failed", e);
        }
        return String.format(Locale.getDefault(), "Lat: %.5f, Lng: %.5f", lat, lng);
    }

    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(Calendar.YEAR, selectedYear);
                    selectedDate.set(Calendar.MONTH, selectedMonth);
                    selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay);

                    SimpleDateFormat dateFormat =
                            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    editDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }
    private void saveAdvert() {
        String postType = radioLost.isChecked() ? "Lost" : "Found";
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String locationName = editLocation.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty()
                || date.isEmpty() || locationName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLat == 0.0 && selectedLng == 0.0) {
            Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = databaseHelper.insertItem(
                postType,
                name,
                phone,
                description,
                date,
                locationName,
                selectedLat,
                selectedLng
        );

        if (inserted) {
            Toast.makeText(this, "Advert saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }
}