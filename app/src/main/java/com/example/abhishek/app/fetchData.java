package com.example.abhishek.app;

import android.os.AsyncTask;

import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Abhishek on 8/29/2017.
 */

public class fetchData extends AsyncTask<Void,Void,Void> {

    String data="";
    String singleParsed="";
    String[] strings;
    String urlstr="";
    String str="";
    String words="";

    fetchData(String str)
    {
        this.str=str;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        urlstr = "http://13.126.121.148/"+str;

        try {
            URL url = new URL(urlstr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();



            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

            while(line != null) {
                line = reader.readLine();
                data+=line;
            }

            data=data.substring(0,data.length()-4);

            try{
//                JSONObject JO= new JSONObject(data);
//                singleParsed = ""+ JO.get("message");
                JSONParser par = new JSONParser();
                org.json.simple.JSONObject obj = (org.json.simple.JSONObject)par.parse(data);
                singleParsed= obj.get("message").toString();
                words= obj.get("words").toString();

            }catch (Exception e){
                e.printStackTrace();
            }


            httpURLConnection.disconnect();

        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }


        strings=singleParsed.split(",");



        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        for (int i=0;i<strings.length;i++)
            List_Item_Activity1.listItems.add(strings[i]);

        List_Item_Activity1.adapter.notifyDataSetChanged();

    }
}
