package com.example.psd.musicupdate;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.psd.musicupdate.com.example.psd.musicupdate.dao.MUDbHelper;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dto.MusicListResponse;
import dto.MusicListResponseList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static Long lastChecked;
    public static final String LAST_UPDATED = "lastUpdated";
    public static SharedPreferences sh;
    private TextView tv1;
    private EditText ed1;
    private Button b1;
    private TableLayout localSongTable;
    public static String IP = "piboard.hopto.org";
    public static String SERVER_URL_PREFIX = "http://";
    public static String SERVER_URL_SUFFIX = ":8090/messager/webapi/myClass";
    public static String SERVER_URL = "http://"+IP+":8090/messager/webapi/myClass";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        populateTables();
       // tv1.setText(new StringBuilder("isExternalStorageReadable ").append(isExternalStorageReadable()).append("isExternalStorageWritable ").append(isExternalStorageWritable()).toString());
    }

    private void populateTables() {

    }

    private void init() {

        sh = this.getSharedPreferences("com.example.UpdateMusic", Context.MODE_PRIVATE);
        b1 = (Button) findViewById(R.id.b1);
        ed1 = (EditText) findViewById(R.id.ed1);
        tv1 = (TextView) findViewById(R.id.tv1);
        localSongTable = (TableLayout) findViewById(R.id.MAlocalSongTable);
        b1.setOnClickListener(this);
        verifyStoragePermissions(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.b1){

            fetchLastUpdated();
            IP = ed1.getText().toString();
            SERVER_URL = new StringBuilder().append(SERVER_URL_PREFIX).append(IP).append(SERVER_URL_SUFFIX).toString();
            CheckForUpdate check = new CheckForUpdate();
            check.execute();
        }
    }


    public void fetchLastUpdated() {
        lastChecked = sh.getLong(LAST_UPDATED, 19800000);
    }

    public static  void setLastUpdated() {
        SharedPreferences.Editor ed = sh.edit();
        ed.putLong(LAST_UPDATED , System.currentTimeMillis()+19800000);
        ed.commit();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    private class CheckForUpdate extends AsyncTask<String, Integer, String> {


        String message = new String();
        @Override
        protected String doInBackground(String... urlParams) {
            MusicListResponseList newSongs = checkMusicUpdate();
            MUDbHelper dbHelper = new MUDbHelper(getApplicationContext(),null, null, 0);
            if(newSongs==null){
                message = "getting null new songs from api";
                return "error";
            }
            ArrayList<MusicListResponse> songList = newSongs.getData();
            if(songList==null){
                message = "getting null list in new songs from api";
                return "error";
            }

            ArrayList<Map<String,Object>> localValues = dbHelper.getAllLocalEntries();
            ArrayList<Integer> localValueIds = new ArrayList<>();
            ArrayList<Integer> remoteValueIds = new ArrayList<>();

            for(Map<String,Object> map: localValues){
                localValueIds.add((Integer)map.get("PI_ID"));
            }
            for(MusicListResponse song: songList){
                remoteValueIds.add(song.getId());
            }

            Map<Integer,String> idVsNames = new HashMap<>();

            ArrayList<Integer> newValueIds = new ArrayList<>(remoteValueIds);
            newValueIds.removeAll(localValueIds);
            for(Integer id: newValueIds){
                String fileName = getFileNameById(id);
                idVsNames.put(id,fileName);
                saveFile(fileName,id);
            }
            ArrayList<Map<String,Object>> newValuesToAdd = new ArrayList<>();
            Set<Integer> ids = idVsNames.keySet();
            StringBuilder sbr = new StringBuilder("new files added: ");
            for(Integer id: ids){
                Map<String, Object> map = new HashMap<>();
                map.put("PI_ID",id);
                map.put("NAME",idVsNames.get(id));
                newValuesToAdd.add(map);
                sbr.append(idVsNames.get(id)).append("; ");
            }
            dbHelper.addEntries(newValuesToAdd);

            return sbr.toString();
        }

        private void saveFile(String fileName, Integer id) {
            try {
                URL url = new URL(new StringBuilder(SERVER_URL).append("/getFileById").append("?id=").append(String.valueOf(id)).toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED&&conn.getResponseCode()!= HttpURLConnection.HTTP_OK){
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }
                InputStream connIs = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                String output;
                System.out.println("Output from Server .... \n");
                StringBuilder data = new StringBuilder();
                File targetFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC), fileName);
                targetFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(targetFile);

                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = connIs.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
                connIs.close();
                fos.close();
                conn.disconnect();
                ContentValues values = new ContentValues(7);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME,targetFile.getName());
                values.put(MediaStore.Audio.Media.ARTIST,"artist");
                values.put(MediaStore.Audio.Media.ALBUM,"album");
                values.put(MediaStore.Audio.Media.MIME_TYPE,"audio/mp3");
                values.put(MediaStore.Audio.Media.IS_MUSIC, true);
                values.put(MediaStore.Audio.Media.DATA, targetFile.getPath());
                Context context = getApplicationContext();
                context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+targetFile.getPath())));


            }
            catch(Exception e){
                message = e.toString();
            }
        }

        private String getFileNameById(Integer id) {
            String name = null;
            try {
                URL url = new URL(new StringBuilder(SERVER_URL).append("/getNameById").append("?id=").append(String.valueOf(id)).toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED&&conn.getResponseCode()!= HttpURLConnection.HTTP_OK){
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                String output;
                System.out.println("Output from Server .... \n");
                StringBuilder data = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                    data.append(output);
                }

                conn.disconnect();
                name = (data.toString());
                if(name == null){
                    throw new NullPointerException();
                }

            }
            catch(Exception e){
                message = e.toString();
            }
            return name;
        }

        @Override
        protected void onPostExecute(String s) {
            tv1.setText(s);
            setLastUpdated();
            super.onPostExecute(s);
        }

        private MusicListResponseList checkMusicUpdate() {
            MusicListResponseList newSongs= null;
            try {
                URL url = new URL(new StringBuilder(SERVER_URL).append("/checkForUpdate").toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                //if(conn.)
                String input = "{\"user\":\"palash\",\"lastChecked\":"+ MainActivity.lastChecked+"}";
                OutputStream os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();

                if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED&&conn.getResponseCode()!= HttpURLConnection.HTTP_OK){
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                String output;
                System.out.println("Output from Server .... \n");
                StringBuilder data = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                    data.append(output);
                }

                conn.disconnect();
                message = (data.toString());



                Gson gson = new Gson();
                newSongs = gson.fromJson(message, MusicListResponseList.class);
            }
            catch(Exception e){
                message = e.toString();
            }
            return newSongs;
        }

    }



}