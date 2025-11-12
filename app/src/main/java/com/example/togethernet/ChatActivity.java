package com.example.togethernet;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;
import com.example.togethernet.network.MessageManager;
import com.example.togethernet.network.DiscoveredDevice;
import com.example.togethernet.network.DiscoveryManagerService;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Intent;
import android.os.IBinder;
import com.example.togethernet.network.DiscoveryManagerService.LocalBinder;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private String nodeMac;
    private String nodeId;
    private BluetoothDevice device;
    private MessageManager messageManager;
    private DiscoveryManagerService mService;
    private boolean mBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder)service;
            mService = binder.getService();
            mBound = true;
            messageManager = MessageManager.getInstance(); // singleton hahaha
            if (messageManager == null){
                MessageManager.initialize(ChatActivity.this, mService.getGattClientManager(), mService.getGattServerManager());
                messageManager = MessageManager.getInstance();
            }
            nodeMac = getIntent().getStringExtra("node_mac");
            nodeId = getIntent().getStringExtra("node_id");

            TextView chattingWithView = findViewById(R.id.texting_with);
            if (chattingWithView != null) chattingWithView.setText(nodeId != null ? nodeId : "");
            device = null;
            for (DiscoveredDevice d : DiscoveryManagerService.discoveredDevices) {
                if (d.getMac().equals(nodeMac)) { device = d.getGattConnectionObj(); break; }
            }
            if (device == null) {
                Log.e(TAG, "Device with MAC not found. Cannot initiate chat.");
                finish(); return;
            }
            messageManager.connectToDevice(device);
            messageManager.setChatListener(new MessageManager.ChatListener(){
                // logs while within chat activity
                @Override
                public void onMessageReceived(String fromMac, String message) {
                    Log.i(TAG, "[CHAT] Message from " + fromMac + ": " + message);
                }
                @Override
                public void onMessageSent(String toMac, String message) {
                    Log.i(TAG, "[CHAT] Sent to " + toMac + ": " + message);
                }
            });
            EditText input = findViewById(R.id.message_input);
            ImageButton sendBtn = findViewById(R.id.send_button);
            sendBtn.setOnClickListener((v) -> {
                String msg = input.getText().toString();
                if(msg.length() > 0){
                    messageManager.sendMessage(msg);
                    input.setText("");
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, DiscoveryManagerService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mBound){
            unbindService(connection);
            mBound = false;
        }
    }
}