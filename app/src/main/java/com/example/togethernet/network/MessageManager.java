package com.example.togethernet.network;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import com.example.togethernet.database.dao.MessagesDAO;
import com.example.togethernet.database.dao.NodeDAO;
import com.example.togethernet.database.model.Messages;
import com.example.togethernet.database.model.Nodes;
import com.example.togethernet.network.DiscoveredDevice;

public class MessageManager {
    private static MessageManager instance;
    private final GattClientManager clientManager;
    private final GattServerManager serverManager;
    private static final String TAG = "MessageManager";
    private BluetoothDevice currentDevice; // last connected device
    private String currentMac;
    private String currentNodeId; // node ID of the device we're chatting with
    private final Context appContext;
    private final MessagesDAO messagesDAO;
    private final NodeDAO nodeDAO;
    private String myNodeId;
    
    // message encoding constants
    private static final char MSG_START = '\u0001'; // Start of message with node ID
    private static final char NODE_ID_END = '\u0002'; // Node ID delimiter

    public interface ChatListener{
        void onMessageReceived(String fromMac, String message);
        void onMessageSent(String toMac, String message); 
    }

    private ChatListener chatListener;

    private MessageManager(Context context, GattClientManager client, GattServerManager server){
        this.clientManager = client;
        this.serverManager = server;
        this.appContext = context.getApplicationContext();
        this.messagesDAO = new MessagesDAO(appContext);
        this.nodeDAO = new NodeDAO(appContext);
        Nodes n = nodeDAO.getNode();
        this.myNodeId = (n != null) ? n.getNodeId() : "unknown";
        setupCallbacks();
    }

    public static void initialize(Context context, GattClientManager client, GattServerManager server){
        if (instance == null) {
            instance = new MessageManager(context, client, server);
        }
    }

    public static MessageManager getInstance(){
        if (instance == null) throw new IllegalStateException("MessageManager not initialized");
        return instance;
    }

    public void setChatListener(ChatListener listener){
        this.chatListener = listener;
    }

    private void setupCallbacks(){
        if (clientManager != null) {
            clientManager.setMessageListener((mac, msg) -> {
                // Parse node ID from message: format is \u0001NODEID\u0002MESSAGE
                String[] parsed = parseMessageWithNodeId(msg);
                String sourceNodeId = parsed[0];
                String actualMessage = parsed[1];
                
                if (chatListener != null) chatListener.onMessageReceived(mac, actualMessage);
                Log.i(TAG, "MessageManager received via client from " + sourceNodeId + ": " + actualMessage);
                // Save incoming to DB: from nodeId to me
                saveMsgToDb(actualMessage, sourceNodeId, myNodeId, System.currentTimeMillis());
            });
        }
        if (serverManager != null) {
            serverManager.setMessageListener((mac, msg) -> {
                // Parse node ID from message: format is \u0001NODEID\u0002MESSAGE
                String[] parsed = parseMessageWithNodeId(msg);
                String sourceNodeId = parsed[0];
                String actualMessage = parsed[1];
                
                if (chatListener != null) chatListener.onMessageReceived(mac, actualMessage);
                Log.i(TAG, "MessageManager received via server from " + sourceNodeId + ": " + actualMessage);
                // Save incoming to DB: from nodeId to me
                saveMsgToDb(actualMessage, sourceNodeId, myNodeId, System.currentTimeMillis());
            });
        }
    }

    // Parse message format: \u0001NODEID\u0002MESSAGE
    // Uses control characters 0x01 and 0x02 as delimiters (won't appear in normal text)
    // Returns [nodeId, message]
    private String[] parseMessageWithNodeId(String msg) {
        if (msg == null || msg.length() == 0) {
            return new String[]{"unknown", ""};
        }
        
        char startDelim = '\u0001'; // SOH (Start of Heading)
        char endDelim = '\u0002';   // STX (Start of Text)
        
        if (msg.charAt(0) == startDelim) {
            int endIndex = msg.indexOf(endDelim);
            if (endIndex > 1 && endIndex < msg.length()) {
                String nodeId = msg.substring(1, endIndex); // Extract node ID
                String actualMessage = msg.substring(endIndex + 1); // Extract message
                Log.d(TAG, "Parsed nodeId=" + nodeId + ", message length=" + actualMessage.length());
                return new String[]{nodeId, actualMessage};
            }
        }
        // Fallback: try to get from MAC lookup, or use MAC as nodeId
        Log.w(TAG, "Message doesn't have node ID prefix, using fallback");
        return new String[]{"unknown", msg};
    }

    private String getNodeIdFromMac(String mac) {
        // Look up node ID from discovered devices
        for (DiscoveredDevice d : DiscoveryManagerService.discoveredDevices) {
            if (d.getMac().equals(mac)) {
                Log.d(TAG, "getNodeIdFromMac: Found nodeId=" + d.getNodeID() + " for MAC=" + mac);
                return d.getNodeID();
            }
        }
        // mac fallback cuz im losing it
        Log.w(TAG, "Node ID not found for MAC: " + mac + ", discoveredDevices size=" + DiscoveryManagerService.discoveredDevices.size());
        return mac;
    }

    private void saveMsgToDb(String text, String source, String dest, long timestamp) {
        Messages m = new Messages(text, source, dest, timestamp);
        messagesDAO.insertMessage(m);
    }

    public void connectToDevice(BluetoothDevice device){
        if (clientManager == null) {
            Log.e(TAG, "Cannot connect: GattClientManager is null");
            return;
        }
        this.currentDevice = device;
        this.currentMac = device.getAddress();
        this.currentNodeId = getNodeIdFromMac(device.getAddress());
        Log.d(TAG, "connectToDevice: MAC=" + device.getAddress() + ", nodeId=" + currentNodeId);
        clientManager.connect(device);
    }

    public void sendMessage(String msg) {
        if (clientManager == null) {
            Log.e(TAG, "Cannot send: GattClientManager is null");
            return;
        }
        
        String destNodeId = (currentNodeId != null ? currentNodeId : "unknown");
        
        // Encode sender's node ID in message: \u0001NODEID\u0002MESSAGE
        // With WRITE_TYPE_DEFAULT, we can send up to 512 bytes, so no chunking needed for most messages
        String encodedMessage = MSG_START + myNodeId + NODE_ID_END + msg;
        
        // Safety check: if message is still too long (>500 bytes to leave room), fall back to chunking
        if (encodedMessage.length() > 500) {
            Log.w(TAG, "Message very long (" + encodedMessage.length() + " bytes), using chunking fallback");
            sendChunkedMessageFallback(msg);
        } else {
            clientManager.sendMessage(encodedMessage);
            Log.d(TAG, "Sent message (" + encodedMessage.length() + " bytes, nodeId=" + myNodeId.length() + " bytes)");
        }
        
        Log.d(TAG, "sendMessage: Saving message from " + myNodeId + " to " + destNodeId);
        // Save to DB first (save original message, not encoded)
        saveMsgToDb(msg, myNodeId, destNodeId, System.currentTimeMillis());
        if(chatListener != null && currentMac != null){
            chatListener.onMessageSent(currentMac, msg);
        }
    }
    
    // Fallback for extremely long messages (>500 bytes)
    // TODO: Implement real chunking fallback using current MTU for full BTLE compatibility; for now, truncates the message.
    private void sendChunkedMessageFallback(String msg) {
        // Truncate to fit within 512 byte limit (leaving room for node ID encoding)
        int maxLength = 500 - myNodeId.length() - 2; // Leave room for delimiters
        if (msg.length() > maxLength) {
            Log.w(TAG, "Message truncated from " + msg.length() + " to " + maxLength + " bytes");
            msg = msg.substring(0, maxLength);
        }
        String encodedMessage = MSG_START + myNodeId + NODE_ID_END + msg;
        clientManager.sendMessage(encodedMessage);
    }

    public BluetoothDevice getCurrentDevice(){
        return currentDevice;
    }
}
