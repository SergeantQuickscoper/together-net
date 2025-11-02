package com.example.togethernet.database.model;

import java.util.UUID;
public class Messages{
    private String id;
    private String text;
    private String sourceNodeId;
    private String destinationNodeId;
    private long timestamp;

    public Messages(String text, String sourceNodeId, String destinationNodeId, long timestamp){
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.timestamp = timestamp;
    }

    public Messages(String id, String text, String sourceNodeId, String destinationNodeId, long timestamp){
        this.id = id;
        this.text = text;
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.timestamp = timestamp;
    }
    public String getId(){
         return id; 
    }
    public String getText(){ 
        return text; 
    }
    public String getSourceNodeId(){ 
        return sourceNodeId; 
    }

    public String getDestinationNodeId(){ 
        return destinationNodeId; 
    }
    public long getTimestamp(){ 
        return timestamp; 
    }

    public void setText(String text){ 
        this.text = text; 
    }
    
    public void setSourceNodeId(String sourceNodeId){ 
        this.sourceNodeId = sourceNodeId; 
    }

    public void setDestinationNodeId(String destinationNodeId){ 
        this.destinationNodeId = destinationNodeId; 
    }

    public void setTimestamp(long timestamp){ 
        this.timestamp = timestamp; 
    }
}

