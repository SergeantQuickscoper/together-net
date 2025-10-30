package com.example.togethernet;


import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.example.togethernet.database.dao.NodeDAO;
import com.example.togethernet.database.model.Nodes;
public class UserCreationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // TEMPORARY quick and dirty check
        NodeDAO nodeDB = new NodeDAO(this);
        Nodes node = nodeDB.getNode();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // refs (maybe change to better ones in xml but cant bother now)
        TextInputEditText nicknameInput = findViewById(R.id.nickNameInput);
        TextView nodeIdView = findViewById(R.id.textView10);
        Button continueButton = findViewById(R.id.button7);

        // TODO: look into this this linter error
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        nicknameInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                String nickname = s.toString().trim();
                if(!nickname.isEmpty()){
                    String nodeId = generateNodeId(androidId, nickname);
                    nodeIdView.setText(nodeId);
                }
                else{
                    nodeIdView.setText("N/A");
                }
            }

            // useless ignore
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // On continue click
        continueButton.setOnClickListener(v -> {
            String nickname = nicknameInput.getText() != null ? nicknameInput.getText().toString().trim() : "";
            String nodeId = nodeIdView.getText().toString();

            if(nickname.isEmpty()){
                nicknameInput.setError("Nickname required");
                return;
            }

            System.out.println("Node ID: " + nodeId);

            // TODO: transfer to network discovery activity here asw
        });
    }

    //unique Node ID using SHA-256 hash of Android ID + nickname
    private String generateNodeId(String androidId, String nickname){
        try {
            String input = androidId + "_" + nickname;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // first few bytes to hex for compact readable ID
            StringBuilder hex = new StringBuilder();
            for(int i = 0; i < 6; i++){ // shorter 12-char ID
                hex.append(String.format("%02x", hash[i]));
            }
            return nickname + "@" + hex.toString().toUpperCase();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return "ERROR";
        }
    }
}
