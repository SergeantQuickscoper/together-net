package com.example.togethernet.database.sqllitehelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TogetherNet.db";
    public static final int DATABASE_VERSION = 1;

    // Schema for NODES table
    public static final String TABLE_NODES = "node_info";
    public static final String COLUMN_NODES_INT_ID = "id";
    public static final String COLUMN_NODES_ID = "node_id";
    private static final String CREATE_TABLE_NODES = "CREATE TABLE " + TABLE_NODES +
            " (" + COLUMN_NODES_INT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NODES_ID + " TEXT NOT NULL);";

    // TODO: Add messages schema and mesh schema here later

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_NODES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        onCreate(db);
    }
}