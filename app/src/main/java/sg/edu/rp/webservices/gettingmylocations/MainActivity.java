package sg.edu.rp.webservices.gettingmylocations;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    TextView tvLatitude, tvLongitude;
    Button btnStart, btnStop, btnCheck;
    private LatLng lastLocation;
    Marker markerLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnCheck = findViewById(R.id.btnCheck);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()) {
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        tvLatitude.setText("Latitude: " + String.valueOf(lat));
                        tvLongitude.setText("Longitude: " + String.valueOf(lng));
                        lastLocation = new LatLng(lat, lng);

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15));

                        markerLast = map.addMarker(new MarkerOptions()
                                .position(lastLocation)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    } else {
                        String msg = "No Last Known Location Found";
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            String msg = "Permission not granted to retrieve location info";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment =
                (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkLocationPermission() && checkStoragePermission()) {
                    Intent i = new Intent(MainActivity.this, MyService.class);
                    startService(i);
                } else {
                    String msg = "Permission not granted to retrieve location info";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission() && checkStoragePermission()) {
                    Intent i = new Intent(MainActivity.this, MyService.class);
                    stopService(i);
                } else {
                    String msg = "Permission not granted to retrieve location info";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }

            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    String data = "";
                    ArrayList<String> locations = new ArrayList<>();
                    try {
                        String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LocationService";

                        File targetFile = new File(folderLocation, "locations.txt");
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader bufferedReader = new BufferedReader(reader);

                        String line = bufferedReader.readLine();
                        while (line != null) {
                            locations.add(line);
                            line = bufferedReader.readLine();
                        }
                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        intent.putExtra("locations", locations);
                        startActivity(intent);
                        bufferedReader.close();
                        reader.close();

                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to read", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    String msg = "Permission not granted to Read";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        UiSettings ui = map.getUiSettings();
        ui.setCompassEnabled(true);
        ui.setZoomControlsEnabled(true);
        ui.setMapToolbarEnabled(true);

//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15));

        int permissionCheck = ContextCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 1);
        }
    }

    private boolean checkLocationPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        return permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED ||
                permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED;

    }

    private boolean checkStoragePermission() {
        int permissionCheck_Write = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck_Read = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        return permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED ||
                permissionCheck_Read == PermissionChecker.PERMISSION_GRANTED;

    }

}