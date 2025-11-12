package com.example.togethernet;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.togethernet.network.DiscoveryManagerService;
import com.example.togethernet.network.DiscoveredDevice;
import java.util.ArrayList;
import java.util.List;
import androidx.core.content.res.ResourcesCompat;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;

public class SearchNodesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NodeIdAdapter adapter;
    private EditText searchBox;
    private List<DiscoveredDevice> allDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_nodes);
        recyclerView = findViewById(R.id.discoveredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allDevices = new ArrayList<>(DiscoveryManagerService.discoveredDevices);
        adapter = new NodeIdAdapter(allDevices);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new NodeIdAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(DiscoveredDevice device) {
                android.content.Intent intent = new android.content.Intent(SearchNodesActivity.this, ChatActivity.class);
                intent.putExtra("node_mac", device.getMac());
                intent.putExtra("node_id", device.getNodeID());
                startActivity(intent);
            }
        });

        searchBox = findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s){}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        allDevices = new ArrayList<>(DiscoveryManagerService.discoveredDevices);
        filter(searchBox.getText().toString());
    }

    private void filter(String text){
        List<DiscoveredDevice> filtered = new ArrayList<>();
        for(DiscoveredDevice device : allDevices){
            if(device.getNodeID().toLowerCase().contains(text.toLowerCase())){
                filtered.add(device);
            }
        }
        adapter.setDevices(filtered);
    }

    static class NodeIdAdapter extends RecyclerView.Adapter<NodeIdAdapter.NodeIdViewHolder>{
        private List<DiscoveredDevice> devices;
        private Typeface petFont;
        private static final int PET_BLUE = 0xFF2C1DFF; // my blue
        private OnItemClickListener listener;
        public interface OnItemClickListener { void onItemClick(DiscoveredDevice device); }
        public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

        NodeIdAdapter(List<DiscoveredDevice> devices){
            this.devices = devices;
        }

        public void setDevices(List<DiscoveredDevice> devices){
            this.devices = devices;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public NodeIdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            if (petFont == null){
                petFont = ResourcesCompat.getFont(parent.getContext(), R.font.pet);
            }
            return new NodeIdViewHolder(view, petFont);
        }
        @Override
        public void onBindViewHolder(@NonNull NodeIdViewHolder holder, int position){
            holder.bind(devices.get(position));
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(devices.get(position));
            });
        }
        @Override
        public int getItemCount(){
            return devices.size();
        }
        static class NodeIdViewHolder extends RecyclerView.ViewHolder{
            private final TextView textView;
            NodeIdViewHolder(@NonNull View itemView, Typeface font){
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
                textView.setTypeface(font);
                textView.setTextColor(PET_BLUE);
                textView.setTextSize(18f);
                textView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

            void bind(DiscoveredDevice device){
                textView.setText(device.getNodeID());
            }
        }
    }
}