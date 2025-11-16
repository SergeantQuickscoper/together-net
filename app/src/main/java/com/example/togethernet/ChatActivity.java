package com.example.togethernet;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.togethernet.database.dao.MessagesDAO;
import com.example.togethernet.database.model.Messages;
import com.example.togethernet.database.dao.NodeDAO;
import com.example.togethernet.database.model.Nodes;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.text.SimpleDateFormat;
import android.view.ViewGroup;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private String nodeMac;
    private String nodeId;
    private BluetoothDevice device;
    private MessageManager messageManager;
    private DiscoveryManagerService mService;
    private boolean mBound = false;
    private MessagesDAO messagesDAO;
    private NodeDAO nodeDAO;
    private String myNodeId;
    private RecyclerView messagesListView;
    private MessagesAdapter messagesAdapter;
    private List<Messages> chatHistory = new ArrayList<>();

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
            loadHistory();
            device = null;
            for (DiscoveredDevice d : DiscoveryManagerService.discoveredDevices) {
                if (d.getMac().equals(nodeMac)) { device = d.getGattConnectionObj(); break; }
            }
            if (device == null) {
                Log.e(TAG, "Device with MAC not found. Cannot initiate chat.");
                finish(); return;
            }
            // Set listener BEFORE connecting so we don't miss any messages
            messageManager.setChatListener(new MessageManager.ChatListener() {
                // logs while within chat activity
                @Override
                public void onMessageReceived(String fromMac, String message) {
                    Log.i(TAG, "[CHAT] Message from " + fromMac + ": " + message);
                    runOnUiThread(() -> {
                        // Small delay to ensure DB write completes
                        messagesListView.postDelayed(() -> {
                            loadHistory();
                            Log.d(TAG, "UI updated after receiving message");
                        }, 100);
                    });
                }
                @Override
                public void onMessageSent(String toMac, String message) {
                    Log.i(TAG, "[CHAT] Sent to " + toMac + ": " + message);
                    runOnUiThread(() -> {
                        // Small delay to ensure DB write completes
                        messagesListView.postDelayed(() -> {
                            loadHistory();
                            Log.d(TAG, "UI updated after sending message");
                        }, 100);
                    });
                }
            });
            messageManager.connectToDevice(device);
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
        // init DAO
        messagesDAO = new MessagesDAO(this);
        nodeDAO = new NodeDAO(this);
        Nodes mine = nodeDAO.getNode();
        myNodeId = (mine != null) ? mine.getNodeId() : "unknown";
        messagesListView = findViewById(R.id.message_list); // matches your layout
        messagesAdapter = new MessagesAdapter(chatHistory, myNodeId, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false); // Start from top
        messagesListView.setLayoutManager(layoutManager);
        messagesListView.setAdapter(messagesAdapter);
        Log.d(TAG, "RecyclerView initialized with adapter, initial itemCount: " + messagesAdapter.getItemCount());
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, DiscoveryManagerService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        // Refresh messages when activity resumes (in case messages arrived while in background)
        if (mBound && messageManager != null) {
            loadHistory();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mBound){
            unbindService(connection);
            mBound = false;
        }
    }

    private void loadHistory(){
        if (nodeId == null) {
            Log.w(TAG, "loadHistory: nodeId is null, cannot load history");
            return;
        }
        List<Messages> sent = messagesDAO.getMessagesBySender(myNodeId);
        List<Messages> received = messagesDAO.getMessagesByReceiver(myNodeId);
        Log.d(TAG, "loadHistory: Found " + sent.size() + " sent messages, " + received.size() + " received messages");
        Log.d(TAG, "loadHistory: Looking for messages with nodeId=" + nodeId + ", myNodeId=" + myNodeId);
        chatHistory.clear();
        // Filter messages for this specific node pair
        for(Messages m : sent) {
            Log.d(TAG, "loadHistory: Checking sent message: dest=" + m.getDestinationNodeId() + ", matches=" + m.getDestinationNodeId().equals(nodeId));
            if (m.getDestinationNodeId().equals(nodeId)) {
                chatHistory.add(m);
            }
        }
        for(Messages m : received) {
            Log.d(TAG, "loadHistory: Checking received message: source=" + m.getSourceNodeId() + ", matches=" + m.getSourceNodeId().equals(nodeId));
            if (m.getSourceNodeId().equals(nodeId)) {
                chatHistory.add(m);
            }
        }
        Collections.sort(chatHistory, (a,b)->Long.compare(a.getTimestamp(), b.getTimestamp()));
        Log.d(TAG, "loadHistory: Loaded " + chatHistory.size() + " messages for node " + nodeId);
        Log.d(TAG, "loadHistory: Adapter itemCount before notify: " + messagesAdapter.getItemCount());
        messagesAdapter.notifyDataSetChanged();
        Log.d(TAG, "loadHistory: Adapter itemCount after notify: " + messagesAdapter.getItemCount());
        if (chatHistory.size() > 0) {
            messagesListView.post(() -> {
                int lastPos = chatHistory.size() - 1;
                Log.d(TAG, "Scrolling to position: " + lastPos);
                messagesListView.scrollToPosition(lastPos);
            });
        }
    }
}

class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private final List<Messages> messages;
    private final String myId;
    private Typeface pet;
    public MessagesAdapter(List<Messages> items, String myId, Context context) {
        this.messages = items;
        this.myId = myId;
        pet = ResourcesCompat.getFont(context, R.font.pet);
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setPadding(20, 16, 20, 16);
        tv.setTextSize(16f);
        tv.setTextColor(0xFFFFFFFF); // White text
        tv.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new MessageViewHolder(tv);
    }
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int pos) {
        if (pos < 0 || pos >= messages.size()) {
            Log.e("MessagesAdapter", "Invalid position: " + pos + ", list size: " + messages.size());
            return;
        }
        Messages m = messages.get(pos);
        TextView tv = (TextView) holder.itemView;
        boolean isMe = m.getSourceNodeId().equals(myId);
        tv.setTypeface(pet);
        tv.setText((isMe ? "Me: " : "Them: ") + m.getText());
        tv.setTextAlignment(isMe ? TextView.TEXT_ALIGNMENT_TEXT_END : TextView.TEXT_ALIGNMENT_TEXT_START);
        tv.setTextColor(isMe ? 0xFF49FF40 : 0xFF291AFB); // Green for me, blue for them
        Log.d("MessagesAdapter", "Binding message " + pos + ": " + m.getText());
    }
    @Override 
    public int getItemCount() { 
        int count = messages.size();
        Log.d("MessagesAdapter", "getItemCount: " + count);
        return count;
    }
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        public MessageViewHolder(TextView tv) { super(tv); }
    }
}