package com.example.togethernet;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button onboardingButton;
        onboardingButton = findViewById(R.id.onboardingButton);
        onboardingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // TODO: check for
                Intent switchToPermsActivity = new Intent(MainActivity.this, PermissionsActivity.class);
                startActivity(switchToPermsActivity);
            }
        });
    }
}