package com.example.volumio;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final String urlPlus1 = "http://192.168.1.94/api/v1/commands/?cmd=volume&volume=plus";
    final String urlMinus1 = "http://192.168.1.94/api/v1/commands/?cmd=volume&volume=minus";
    final String urlPlus2 = "http://192.168.1.96/api/v1/commands/?cmd=volume&volume=plus";
    final String urlMinus2 = "http://192.168.1.96/api/v1/commands/?cmd=volume&volume=minus";
    final String urlPlay = "http://192.168.1.94/api/v1/commands/?cmd=play";
    final String urlPause = "http://192.168.1.94/api/v1/commands/?cmd=pause";
    String url = "";
    private WebView myWebView;

    String getResponse = "";
    String status = "";


    Button ActivateRssi;
    TextView showRssi;
    TextView showRssi2;
    Switch algo1;
    Switch algo2;
    Button play;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "MainActivity";
    double resultCode=0;
    double resultCode2=0;
    double totalDist=0;
    double playDist=0;
    double rangePercentile = 0.20;
    double middleDist=0;
    double middleDistLow=0;
    double middleDistHigh=0;
    double middlePercintile = 0.10;
     RequestQueue queue;

   //private ObservableInteger obsInt = new ObservableInteger();


    // your URL






    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                resultCode = bundle.getDouble("rssiValue");
                resultCode2 = bundle.getDouble("rssiValue2");
                showRssi.setText("Distance to speaker 1: " + resultCode);
                showRssi2.setText("Distance to speaker 2: " + resultCode2);
               // obsInt.set(resultCode);
                totalDist = resultCode + resultCode2;
                playDist = totalDist*rangePercentile;
                middleDist=totalDist/bundle.size();
                middleDistLow=middleDist+(middleDist*middlePercintile);
                middleDistHigh=middleDist-(middleDist*middlePercintile);
                Log.d(TAG, "diffDistance : " + (resultCode - resultCode2));
                if(algo1.isChecked()){
                    if (playDist > resultCode ){
                        sendRequest(urlPlus2);
                        sendRequest(urlMinus1);

                    }
                   else if(playDist > resultCode2) {
                        sendRequest(urlMinus2);
                        sendRequest(urlPlus1);
                    }
                }

                   else if(resultCode - resultCode2 < 1 && resultCode - resultCode2 > -1){
                        sendRequest(urlPlus1);
                        sendRequest(urlPlus2);
                        Log.d(TAG, "onReceive: " + (resultCode - resultCode2));

                }

            }
        }
    };


    public void onClick(View view) {

        Intent intent = new Intent(this, RssiService.class);
        // add infos for the service which file to download and where to store
        startService(intent);
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        algo1 = (Switch) findViewById(R.id.algoritm1);
        algo2 = (Switch) findViewById(R.id.algoritm2);
        ActivateRssi = (Button) findViewById(R.id.ActivateRssi);
        showRssi = (TextView) findViewById(R.id.showRssi);
        showRssi2 = (TextView) findViewById(R.id.showRssi2);
        play = (Button) findViewById(R.id.play);
        queue = Volley.newRequestQueue(this);
        queue.start();

        mediaClick(play, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }





       /* obsInt.SetOnIntegerChangeListener(new OnIntegerChangeListener()
        {
            @Override
            public void onIntegerChanged(float newValue)
            {

                //Log.d(TAG, "onIntegerChanged: " + newValue + resultCode2);
                if (newValue < resultCode2 ){
                    sendRequest(urlPlus2);
                    sendRequest(urlMinus1);
                }
                else{
                    sendRequest(urlMinus2);
                    sendRequest(urlPlus1);
                }

            }
        });
        */

        myWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("http://192.168.1.96/playback");
        myWebView.setWebViewClient(new WebViewClient());

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                RssiService.NOTIFICATION));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }



    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
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
    private String mediaClick(Button button, final String url){

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.length() == 0 ||  status.indexOf("pause") != -1){
                    status = sendRequest(urlPlay);
                    Log.d(TAG, "onClick: " + status);
                }
                else {
                    status = sendRequest(urlPause);
                    Log.d(TAG, "onClick: lol  " + status);
                }

            }
        });
        return status;
    }

    public String sendRequest(String url){

        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        try {
                            getResponse = response.getString("response");
                            Log.d(TAG, "onResponse: " + getResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }

        );

// add it to the RequestQueue
        queue.add(getRequest);
        return getResponse;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}