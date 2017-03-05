package com.example.psd.musicupdate.com.example.psd.musicupdate.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by palash on 5/3/17.
 */

public  class QueryConstants extends MUDbHelper {
    public QueryConstants(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static String CREATE_LOCAL_ENTRIES_TABLE = "CREATE TABLE " + LOCAL_ENTRIES_TABLE + " (" + LOCAL_ENTRIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LOCAL_ENTRIES_PI_ID + " INTEGER ," + LOCAL_ENTRIES_NAME + " TEXT );";

    public static String DROP_TABLE = "DROP TABLE IF EXISTS "+ LOCAL_ENTRIES_NAME+ " ;";

    public static String GET_ALL_ENTRIES = "SELECT * FROM "+ LOCAL_ENTRIES_TABLE +";";
}