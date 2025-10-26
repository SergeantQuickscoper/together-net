package com.example.togethernet.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.togethernet.database.sqllitehelper.DBHelper;
import com.example.togethernet.database.model.Nodes;


public class NodeDAO{
    private final DBHelper dbHelper;

    public NodeDAO(Context context){
        dbHelper = new DBHelper(context);
    }


    // Add new node to DB. ensure  it should just be called only once when user registers their node
    public long insertNode(Nodes node){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NODES_ID, node.getNodeId());
        long id = db.insert(DBHelper.TABLE_NODES, null, values);
        db.close();
        return id;
    }

    // TODO: maybe add a method to allow updating the node id

    // get a node or null if doesnt exist
    public Nodes getNode(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DBHelper.TABLE_NODES,
                new String[]{DBHelper.COLUMN_NODES_INT_ID, DBHelper.COLUMN_NODES_ID},
                null, null, null, null, null
        );
        Nodes node = null;
        if(cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NODES_INT_ID));
            String nodeId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NODES_ID));
            node = new Nodes(id, nodeId);
            cursor.close();
        }
        db.close();
        return node;
    }

    // for a reset
    public void clearNode(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.TABLE_NODES, null, null);
        db.close();
    }
}
