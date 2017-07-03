package com.example.ss678673.myapplication;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pubnub.api.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.builder.SubscribeBuilder;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.json.*;
import com.pubnub.api.*;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "CT";
    static PubNub pnub;
    static Button conbutton;
    static Button disbutton;
    static Button sendbutton_ON;
    static Button sendbutton_OFF;
    static TextView temptext;
    static TextView humidtext;
    static HashMap<String,Object>result = null;
    String Msgtext = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.connect_button);
        button.setEnabled(true);
        conbutton = button;

        button = (Button) findViewById(R.id.send_button_ON);
        button.setEnabled(false);
        sendbutton_ON = button;

        button = (Button) findViewById(R.id.send_button_OFF);
        button.setEnabled(false);
        sendbutton_OFF = button;

        button = (Button) findViewById(R.id.disconnect_button);
        button.setEnabled(false);
        disbutton = button;

        TextView tview = (TextView) findViewById(R.id.temp);
        temptext = tview;

        tview = (TextView) findViewById(R.id.humid);
        humidtext = tview;
    }

    /*
     FUNCTION : Connect to the cloud on click of the button.
    */

    public void selfConstruct(View v) {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-5d4c7d26-aa3e-11e6-85a3-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-590cfa72-ae12-4726-98ad-644f9eb98e43");
        pnConfiguration.setSecure(false);
        PubNub pubnub = new PubNub(pnConfiguration);
        pnub = pubnub;
        conbutton.setEnabled(false);
        sendbutton_ON.setEnabled(true);
        sendbutton_OFF.setEnabled(true);
        disbutton.setEnabled(true);
        Context context = getApplicationContext();
        CharSequence text = "Connecting to the Cloud!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        Log.v (TAG, "Connecting to the Cloud !!");

        pnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pnub, PNMessageResult message) {
                Msgtext = message.getMessage().toString();
            }

            @Override
            public void presence(PubNub pnub, PNPresenceEventResult presence) {

            }
        });

        pnub.subscribe().channels(Arrays.asList("awesomeChannel")).execute();

    }
    
    /*
     FUNCTION : Connect to the cloud on click of the button.
    */

    /* Switch on the light */
    public void selfMessage_ON(View v) {

        try{
            JSONObject obj1 = new JSONObject();
            obj1.put("light", "1");

            JSONObject obj2 = new JSONObject();
            obj2.put("bedroom", obj1);

            JSONObject obj = new JSONObject();
            obj.put("sks", obj2);

            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(obj.toString(), new TypeReference<HashMap<String, Object>>(){}) ;
            System.out.println(result.toString());
        }catch(JSONException | IOException e){
            Log.e (TAG, "JSON Exception !!");
        }

        if(pnub == null){
            Log.v (TAG, "No connection to Cloud !!");
        } else {
            pnub.publish()
                  // .message(Arrays.asList("hello", "there"))
                  .message(result)
                  .channel("testChannel")
                  .async(new PNCallback<PNPublishResult>() {
                  @Override
                  public void onResponse(PNPublishResult result, PNStatus status) {
                      // handle publish result, status always present, result if successful
                      // status.isError to see if error happened
                      Context context = getApplicationContext();
                      CharSequence text = "Push message - publish";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(context, text, duration);
                      toast.show();
                  }
            });
        }
    }

/* Switch off the light */

    public void selfMessage_OFF(View v) {
        try{
            JSONObject obj1 = new JSONObject();
            obj1.put("light", "0");

            JSONObject obj2 = new JSONObject();
            obj2.put("bedroom", obj1);

            JSONObject obj = new JSONObject();
            obj.put("sks", obj2);

            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(obj.toString(), new TypeReference<HashMap<String, Object>>(){}) ;
            System.out.println(result.toString());
        }catch(JSONException | IOException e){
            Log.e (TAG, "JSON Exception !!");
        }

        if(pnub == null){
            Log.v (TAG, "No connection to Cloud !!");
        } else {
            pnub.publish()
                    // .message(Arrays.asList("hello", "there"))
                    .message(result)
                    .channel("testChannel")
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError to see if error happened
                        }
                    });
        }
    }


    /* Subscribe to get Temp/Humidity values */
    public void get_button_Temp(View v) {
        // Subscribe

        try {
            JSONObject obj1 = new JSONObject(Msgtext);

            temptext.setText("Temp : " + obj1.getString("temp"));
            humidtext.setText("Humidity : " + obj1.getString("humid"));
            //obj1.getString("humid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void selfDestruct(View v) {
        conbutton.setEnabled(true);
        sendbutton_ON.setEnabled(false);
        sendbutton_OFF.setEnabled(false);
        disbutton.setEnabled(false);
        pnub = null;
        Context context = getApplicationContext();
        CharSequence text = "Disconnected from the Cloud!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
