package com.example.togethernet.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.togethernet.database.sqllitehelper.DBHelper;
import com.example.togethernet.database.model.Messages;
import java.util.ArrayList;
import java.util.List;

public class MessagesDAO{
    private final DBHelper dbHelper;

    public MessagesDAO(Context context){
        dbHelper = new DBHelper(context);
    }

    public long insertMessage(Messages message){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MESSAGE_ID, message.getId());
        values.put(DBHelper.COLUMN_MESSAGE_TEXT, message.getText());
        values.put(DBHelper.COLUMN_MESSAGE_SOURCE_NODE_ID, message.getSourceNodeId());
        values.put(DBHelper.COLUMN_MESSAGE_DEST_NODE_ID, message.getDestinationNodeId());
        values.put(DBHelper.COLUMN_MESSAGE_TIMESTAMP, message.getTimestamp());
        long id = db.insert(DBHelper.TABLE_MESSAGES, null, values);
        db.close();
        return id;
    }

    public List<Messages> getMessagesBySender(String senderNodeId){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Messages> messagesList = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_MESSAGES,
                null,
                DBHelper.COLUMN_MESSAGE_SOURCE_NODE_ID + "=?",
                new String[]{senderNodeId},
                null, null, DBHelper.COLUMN_MESSAGE_TIMESTAMP + " DESC");
        if(cursor.moveToFirst()) {
            do{
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_ID));
                String text = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_TEXT));
                String destinationNodeId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_DEST_NODE_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_TIMESTAMP));
                messagesList.add(new Messages(id, text, senderNodeId, destinationNodeId, timestamp));
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messagesList;
    }

    public List<Messages> getMessagesByReceiver(String receiverNodeId){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Messages> messagesList = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_MESSAGES,
                null,
                DBHelper.COLUMN_MESSAGE_DEST_NODE_ID + "=?",
                new String[]{receiverNodeId},
                null, null, DBHelper.COLUMN_MESSAGE_TIMESTAMP + " DESC");
        if(cursor.moveToFirst()){
            do{
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_ID));
                String text = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_TEXT));
                String sourceNodeId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_SOURCE_NODE_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE_TIMESTAMP));
                messagesList.add(new Messages(id, text, sourceNodeId, receiverNodeId, timestamp));
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messagesList;
    }
}

