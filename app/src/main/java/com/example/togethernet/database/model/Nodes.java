package com.example.togethernet.database.model;

public class Nodes{
    private int id;
    private String nodeId;

    public Nodes(int id, String nodeId){
        this.id = id;
        this.nodeId = nodeId;
    }

    public Nodes(String nodeId){
        this.nodeId = nodeId;
    }

    public int getId(){
        return id;
    }

    public String getNodeId(){
        return nodeId;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setNodeId(String nodeId){
        this.nodeId = nodeId;
    }

}
