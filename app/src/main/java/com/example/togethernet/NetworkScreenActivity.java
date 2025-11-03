package com.example.togethernet;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.example.togethernet.network.DiscoveryManagerService;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
public class NetworkScreenActivity extends AppCompatActivity {
    Button btnSendMessage;
    TextView statusText;
    TextView count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_network_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnSendMessage = findViewById(R.id.btnSendMessage);
        statusText = findViewById(R.id.statusText);
        count = findViewById(R.id.totalDiscoveredNodesText);
        // start our ble service in the foreground
        Intent bleServiceIntent = new Intent(this, DiscoveryManagerService.class);
        ContextCompat.startForegroundService(this, bleServiceIntent);

        btnSendMessage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent switchToNodeSearch = new Intent(NetworkScreenActivity.this, SearchNodesActivity.class);
                startActivity(switchToNodeSearch);
            }
        });


    }
}