package com.example.wifiscanner3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Context context =this;
    Button scBtn;
    ListView scanlist;
    Map<String,ScanResult> scantable = new HashMap<>();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
              //  startBeermay() // <-- Start Beemray here
            } else {
                // Permission was denied or request was cancelled
            }
        }
    }
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    WifiManager wifiManager ;
    BroadcastReceiver wifiScanReceiver ;
    private void scanSuccess() {
        scantable.clear();
        arrayList.clear();
        List<ScanResult> results = wifiManager.getScanResults();
        for (ScanResult scanResult : results) {
            arrayList.add(scanResult.SSID);
            scantable.put(scanResult.SSID,scanResult);
            adapter.notifyDataSetChanged();
        }
    }
    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        arrayList.add("FAIL");
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // checkLocationPermission();
        if (ActivityCompat.checkSelfPermission((Activity)this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity)this,new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        wifiManager=(WifiManager)
                this.getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };
        setContentView(R.layout.activity_main);
        scBtn=findViewById(R.id.scBtn);
        scBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.startScan();
            }
        });
        scanlist= findViewById(R.id.ScanList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        scanlist.setAdapter(adapter);
        scanlist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String o = (String)scanlist.getItemAtPosition(position);
                ScanResult temp=scantable.get(o);
                String lvl;
                if(temp.level<=-70) lvl="Weak";
                else if(temp.level<=-60) lvl="Fair";
                else if(temp.level<=-50) lvl="Good";
                else lvl="Excellent";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Information")
                        .setMessage("SSID - " +temp.SSID+"\n"+ "BSSID - "+temp.BSSID+"\n"+"Capabilities - "+temp.capabilities+"\n"+"Frequency - "+temp.frequency+" hHz\n"+"Level - " + lvl +"\n" + "TimeStamp - "+ temp.timestamp)
                        .setCancelable(false)
                        .setNegativeButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
              //  Toast.makeText(getBaseContext(),"SSID - " +temp.SSID+"\n"+ "BSSID - "+temp.BSSID+"\n"+"Capabilities - "+temp.capabilities+"\n"+"Frequency - "+temp.frequency+" hHz\n"+"Level - " + lvl +"\n" + "TimeStamp - "+ temp.timestamp,Toast.LENGTH_LONG).show();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
    }

}
