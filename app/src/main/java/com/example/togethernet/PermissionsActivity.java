package com.example.togethernet;

import android.os.Bundle;
import android.widget.Button
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.content.Intent;

public class PermissionsActivity extends AppCompatActivity {
    Button btPermsBtn;
    Button wifiPermsBtn;
    Button fgPermsBtn;
    Button locationPermsBtn;
    Button createNickNameBtn;

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
        locationPermsBtn = findViewById(R.id.LocationPerms);
        createNickNameBtn = findViewById(R.id.CreateNickNameButton);
        btPermsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle Bluetooth permissions


            }
        });

        wifiPermsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle WiFi permissions
            }
        });

        fgPermsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle Foreground permissions
            }
        });

        locationPermsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle Location permissions
            }
        });

        createNickNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle continue button click (e.g., go to next screen)
                System.out.println("Hello");
            }
        });
    }
}