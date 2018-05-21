package com.example.volumio;

/**
 * Created by Max on 17-05-2018.
 */
import android.Manifest;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.Collection;

public class RssiService extends IntentService implements BeaconConsumer {

    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    public static final String NOTIFICATION = "com.example.volumio";

    double rssi1 = 0;
    double rssi2 = 0;

    public RssiService() {
        super("RssiService");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.setForegroundScanPeriod(500l);
        beaconManager.setForegroundBetweenScanPeriod(0l);
        RangedBeacon.setSampleExpirationMilliseconds(2500l);

        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24  "));
        beaconManager.bind(this);

        Log.d(TAG, "onCreate: sucess");
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Beacon beacon1;
                Beacon beacon2;

                if (beacons.size() > 0) {

                    for (Beacon becon : beacons){

                        if (becon.getId1().toString().equals("e709028d-7393-49de-a76f-085de6adeaff")) {
                            beacon1 = becon;
                            rssi1 = beacon1.getDistance();
                            Log.d(TAG, "didRangeBeaconsInRegion: "  + rssi1 + " for id:  " + beacon1.getId1().toString());
                        }
                        if (becon.getId1().toString().equals("ce098d3d-dcca-43b5-a2d2-26da0655e67d")){
                            beacon2 = becon;
                            rssi2 = beacon2.getDistance();
                            Log.d(TAG, "didRangeBeaconsInRegion: "  + rssi2 + " for id:  " + beacon2.getId1().toString());
                        }

                    }
                    //Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.  "  + beacons.iterator().next().getId1().toString());

                    publishResults(rssi1, rssi2);
                }
            }
        });



        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        onBeaconServiceConnect();
    }

    private void publishResults(double result, double result2) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("rssiValue", result);
        intent.putExtra("rssiValue2", result2);

        sendBroadcast(intent);
        //Log.d(TAG, "publishResults: usdviuhsivudhsiuvh:    "+ result + "   nummer 2:   " + result2);
    }

}
