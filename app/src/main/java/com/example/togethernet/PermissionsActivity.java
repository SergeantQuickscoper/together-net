package com.example.togethernet;

import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import android.os.Build;

public class PermissionsActivity extends AppCompatActivity {
    Button btPermsBtn;
    Button wifiPermsBtn;
    Button fgPermsBtn;
    Button createNickNameBtn;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static final int REQUEST_FOREGROUND_SERVICE_PERMISSIONS = 2;
    private static boolean btpermsGranted = false;
    private static boolean foregroundPermsGranted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permissions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btPermsBtn = findViewById(R.id.BTperms);
        wifiPermsBtn = findViewById(R.id.WIFIperms);
        fgPermsBtn = findViewById(R.id.ForegroundPerms);
        createNickNameBtn = findViewById(R.id.CreateNickNameButton);


        btPermsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String[] btPermissions;

                // target perms: aka API 31+
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    btPermissions = new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.FOREGROUND_SERVICE_LOCATION
                    };
                }
                else{
                    // support min Android 6+ (this seems like a mess to deal with rn so im not going to work on this for much time)
                    btPermissions = new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    };
                }

                boolean allGranted = true;
                for(String perm : btPermissions){
                    if (ContextCompat.checkSelfPermission(PermissionsActivity.this, perm) != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }

                if(!allGranted){
                    ActivityCompat.requestPermissions(
                            PermissionsActivity.this,
                            btPermissions,
                            REQUEST_BLUETOOTH_PERMISSIONS
                    );
                }
                else{
                    // maybe stylize this to theme?
                    Toast.makeText(PermissionsActivity.this,
                            "Bluetooth permissions already granted",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        wifiPermsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // TODO: Handle WiFi permissions
            }
        });

        fgPermsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String[] fgPermissions;

                // target perms
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    fgPermissions = new String[]{
                            Manifest.permission.FOREGROUND_SERVICE,
                            Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                            Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE,
                            Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                    };
                }
                else{
                    // No foreground-service-type permission before API 31
                    fgPermissions = new String[]{
                            Manifest.permission.FOREGROUND_SERVICE // technically min is still 28 worry about this later
                    };
                }

                boolean allGranted = true;
                for (String perm : fgPermissions){
                    if (ContextCompat.checkSelfPermission(PermissionsActivity.this, perm)
                            != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }

                if (!allGranted){
                    ActivityCompat.requestPermissions(
                            PermissionsActivity.this,
                            fgPermissions,
                            REQUEST_FOREGROUND_SERVICE_PERMISSIONS
                    );
                }
                else{
                    Toast.makeText(PermissionsActivity.this,"Foreground service permissions already granted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        createNickNameBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // TODO: Add check here later for permissions (loop thru all perms add after wifi perms ig)
                Intent switchToNickName = new Intent(PermissionsActivity.this, UserCreationActivity.class);
                startActivity(switchToNickName);
            }
        });
    }
}