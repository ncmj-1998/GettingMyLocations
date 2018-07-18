package sg.edu.rp.webservices.gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class RecordActivity extends AppCompatActivity {

    Button btnRefresh;
    TextView tvNumber;
    ListView lvRecords;
    ArrayAdapter aaRecords;
    ArrayList<String> alRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        btnRefresh = findViewById(R.id.btnRefresh);
        tvNumber = findViewById(R.id.tvNumber);
        lvRecords = findViewById(R.id.lvRecords);

        aaRecords = new ArrayAdapter(RecordActivity.this,
                android.R.layout.simple_list_item_1, alRecords);
        lvRecords.setAdapter(aaRecords);

        Intent intent = getIntent();
        alRecords = intent.getStringArrayListExtra("locations");
        aaRecords.notifyDataSetChanged();

        tvNumber.setText(String.valueOf(alRecords.size()));

        btnRefresh.setOnClickListener(new View.OnClickListener() {
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
                        Intent intent = new Intent(RecordActivity.this, MainActivity.class);
                        intent.putExtra("locations", locations);
                        startActivity(intent);
                        bufferedReader.close();
                        reader.close();

                    } catch (Exception e) {
                        Toast.makeText(RecordActivity.this, "Failed to read", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    String msg = "Permission not granted to Read";
                    Toast.makeText(RecordActivity.this, msg, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(RecordActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }
            }
        });
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
