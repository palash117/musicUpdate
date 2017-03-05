package com.example.psd.musicupdate;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

import dto.MusicListResponse;
import dto.MusicListResponseList;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener{

    EditText edSharedText;
    Button addSong;
    TextView tv;
    public static String IP = "piboard.hopto.org";
    public static String SERVER_URL_PREFIX = "http://";
    public static String SERVER_URL_SUFFIX = ":8090/messager/webapi/myClass";
    public static String SERVER_URL = "http://"+IP+":8090/messager/webapi/myClass";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        init();


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = handleSendText(intent); // Handle text being sent
                edSharedText.setText(sharedText);
                // } else if (type.startsWith("image/")){

            }
        }

    }

    private void init() {
        edSharedText = (EditText) findViewById(R.id.edSharedValue);
        addSong = (Button) findViewById(R.id.addSong);

        tv = (TextView) findViewById(R.id.tvMessage);
        addSong.setOnClickListener(this);
    }

    String handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null) {
            // Update UI to reflect text being shared
            return new String("NO_DATA_FOUND");
        }
        ExpandUrl expand = new ExpandUrl();
        expand.execute(sharedText);
        return sharedText;

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.addSong){
            AddSong adSong = new AddSong();
            adSong.execute(edSharedText.getText().toString(), new String("palash"));
        }
    }


    private class ExpandUrl extends AsyncTask<String, Integer, String> {


        String message = new String();
        @Override
        protected String doInBackground(String... urlParams) {
            String expandedUrl = expandUrl(urlParams[0]);
            return expandedUrl;
        }

        public  String expandUrl(String shortenedUrl)  {
            String response = "ERROR";
            try {
                URL url = new URL(shortenedUrl);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);

                // stop following browser redirect
                httpURLConnection.setInstanceFollowRedirects(false);

                // extract location header containing the actual destination URL
                String expandedURL = httpURLConnection.getHeaderField("Location");
                httpURLConnection.disconnect();

                return expandedURL;
            }
            catch (Exception e){
                return response;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);
            edSharedText.setText(s);
        }


    }

    private class AddSong extends AsyncTask<String, Integer, String> {


        String message = new String();
        @Override
        protected String doInBackground(String... urlParams) {
            String message = addSong(urlParams[0], urlParams[1]);
            return message;
        }


        private String addSong(String link, String user) {
            String name = null;
            try {
                URL url = new URL(new StringBuilder(SERVER_URL).append("/insertRecord").toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");


                conn.setRequestProperty("Content-Type", "application/json");
                InsertRecordDto inDto = new InsertRecordDto();
                inDto.setLink(link);
                inDto.setUser(user);
                String json = new Gson().toJson(inDto, InsertRecordDto.class);

                PrintWriter pr = new PrintWriter(conn.getOutputStream());
                pr.write(json);
                pr.flush();
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
                if(message == null){
                    throw new NullPointerException();
                }

            }
            catch(Exception e){
                message = e.toString();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            tv.setText(s);
        }


    }

    class InsertRecordDto {
        public String getLink() {
            return link;
        }
        public void setLink(String link) {
            this.link = link;
        }
        public String getUser() {
            return user;
        }
        public void setUser(String user) {
            this.user = user;
        }
        private String link;
        private String user;

    }


}

