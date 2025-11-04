package com.example.togethernet.network;

public class DiscoveredDevice{
    private String mac;
    private String nodeID;

    DiscoveredDevice(String mac_, String nodeID_){
        mac = mac_;
        nodeID = nodeID_;
    }
    public String getMac() {
        return mac;
    }

    public String getNodeID() {
        return nodeID;
    }
}
