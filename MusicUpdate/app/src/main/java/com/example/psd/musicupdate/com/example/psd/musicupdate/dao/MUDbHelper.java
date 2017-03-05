package com.example.psd.musicupdate.com.example.psd.musicupdate.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by palash on 5/3/17.
 */

public class MUDbHelper extends SQLiteOpenHelper {
    protected static final Integer VERSION =1;
    protected static final String DB_NAME = "MU_DB";
    protected static final String LOCAL_ENTRIES_TABLE = "MU_LOCAL_ENTRIES";
    protected static final String LOCAL_ENTRIES_ID = "ID";
    protected static final String LOCAL_ENTRIES_PI_ID = "PI_ID";
    protected static final String LOCAL_ENTRIES_NAME = "NAME";

    public MUDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(QueryConstants.CREATE_LOCAL_ENTRIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(QueryConstants.DROP_TABLE);
        onCreate(db);
    }

    public ArrayList<Map<String,Object>> getAllLocalEntries(){

        ArrayList<Map<String, Object>> allEntries = new ArrayList<>();

        try {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery(QueryConstants.GET_ALL_ENTRIES, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Integer id = cursor.getInt(cursor.getColumnIndex(LOCAL_ENTRIES_ID));
                Integer piId = cursor.getInt(cursor.getColumnIndex(LOCAL_ENTRIES_PI_ID));
                String name = cursor.getString(cursor.getColumnIndex(LOCAL_ENTRIES_NAME));
                Map<String, Object> map = new HashMap<>();
                map.put("ID", id);
                map.put("PI_ID", piId);
                map.put("NAME", name);
                allEntries.add(map);
                cursor.moveToNext();
            }
        }
        catch (Exception e){
            System.out.print(e.toString());
        }
        return allEntries;
    }
    public void addEntries(ArrayList<Map<String,Object>>  entries){

        for(Map<String,Object> map: entries){
            Integer id = (Integer) map.get("ID");
            Integer piId = (Integer) map.get("PI_ID");
            String name = (String) map.get("NAME");
            ContentValues cValues = new ContentValues();
            cValues.put(LOCAL_ENTRIES_PI_ID, piId);
            cValues.put(LOCAL_ENTRIES_NAME, name);
            SQLiteDatabase db = getWritableDatabase() ;
            db.insert(LOCAL_ENTRIES_TABLE, null, cValues);
        }

    }
}
