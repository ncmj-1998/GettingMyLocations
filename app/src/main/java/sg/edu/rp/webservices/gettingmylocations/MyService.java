package sg.edu.rp.webservices.gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileWriter;

public class MyService extends Service {

    boolean started;
    FusedLocationProviderClient client;
    LocationCallback mLocationCallBack;
    String folderLocation;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        client = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();
                }
            }
        };

        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LocationService";
        File targetFile = new File(folderLocation);

        if (!targetFile.exists()) {
            boolean result = targetFile.mkdir();
            Log.d("Folder creation","created");
            if (result) {
            }
        }

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!started) {
            started = true;

            if (checkLocationPermission()) {

                LocationRequest mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setSmallestDisplacement(100);

                client.requestLocationUpdates(mLocationRequest, mLocationCallBack, null);
                Task<Location> task = client.getLastLocation();
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            saveLocationRecord(location);
                        } else {
                            Toast.makeText(getApplicationContext(), "No Last Known Location Found", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Toast.makeText(getApplicationContext(), "Service is Running", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Service is Still Running", Toast.LENGTH_LONG).show();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        client.removeLocationUpdates(mLocationCallBack);
        Toast.makeText(getApplicationContext(), "Service is Stopped", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    private void saveLocationRecord(Location location) {
        File targetFile = new File(folderLocation, "locations.txt");
Log.v("file location",String.valueOf(targetFile));
        try {
            FileWriter writer = new FileWriter(targetFile, true);
            writer.write(location.getLatitude() + ", " + location.getLongitude());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to write", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private boolean checkLocationPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        return permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED ||
                permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED;

    }

    private boolean checkStoragePermission() {
        int permissionCheck_Write = ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck_Read = ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        return permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED ||
                permissionCheck_Read == PermissionChecker.PERMISSION_GRANTED;

    }


}
