package org.cloudsky.cordovaPlugins;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ScannerActivity extends Activity implements BarcodeReaderFragment.BarcodeReaderListener {

    private BarcodeReaderFragment readerFragment;
    private boolean showAlertCheck = true;
    public static final String EXTRA_QRVALUE = "qrValue";
    public static final String EXTRA_ISTAG= "is_tag";
    public static final String EXTRA_PARAMS = "params";
    public static final int RESULT_ERROR =5;
    private String package_name;
    private Resources resources;
    private Boolean button_one_visibility;
    private String barCodeResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getResourceId("layout/scanner_view"));
        Intent startIntent = getIntent();
        String paramStr = startIntent.getStringExtra(EXTRA_PARAMS);
        JSONObject params;
        try {
            params = new JSONObject(paramStr);

        }
        catch (JSONException e) {
            params = new JSONObject();
        }
        String textTitle = params.optString("text_title");
        String textInstructions = params.optString("text_instructions");
        Boolean drawSight = params.optBoolean("drawSight", true);
        String  text_one= params.optString("text_one");
        String text_two = params.optString("text_two");
        String text_three= params.optString("text_three");
        Boolean text_one_visibility = params.optBoolean("text_one_visibility", false);
        Boolean text_two_visibility = params.optBoolean("text_two_visibility", false);
        Boolean text_three_visibility = params.optBoolean("text_three_visibility", false);
        button_one_visibility = params.optBoolean("button_one_visibility", false);
        Boolean button_two_visibility = params.optBoolean("button_two_visibility", false);
        TextView text1= findViewById(getResourceId("id/text1"));
        text1.setText(text_one);
        if(text_one_visibility)
        {
            text1.setVisibility(View.VISIBLE);
        }
        TextView text2= findViewById(getResourceId("id/text2"));
        text2.setText(text_two);
        if(text_two_visibility)
        {
            text2.setVisibility(View.VISIBLE);
        }
        TextView text3= findViewById(getResourceId("id/text3"));
        text3.setText(text_three);
        if(text_three_visibility)
        {
            text3.setVisibility(View.VISIBLE);
        }
        Button btnTag= findViewById(getResourceId("id/tag"));
        if(button_one_visibility)
        {
            btnTag.setVisibility(View.VISIBLE);
        }
        Button btnUnTag= findViewById(getResourceId("id/untag"));
        if(button_two_visibility)
        {
            btnUnTag.setVisibility(View.VISIBLE);
        }

        btnTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent ();
                result.putExtra(EXTRA_QRVALUE, barCodeResult);
                  result.putExtra(EXTRA_ISTAG, ":true");
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        btnUnTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent ();
                result.putExtra(EXTRA_QRVALUE, barCodeResult);
                result.putExtra(EXTRA_ISTAG, ":false");
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        //whichCamera = params.optString("camera");
        //flashMode = params.optString("flash");
        addBarcodeReaderFragment();
    }
    
    private void addBarcodeReaderFragment() {
        readerFragment = BarcodeReaderFragment.newInstance(true, false, View.VISIBLE);
        readerFragment.setListener(this);
        FragmentManager supportFragmentManager =getFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(getResourceId("id/fm_container"), readerFragment);
        fragmentTransaction.commitAllowingStateLoss();

    }




    @Override
    public void onScanned(Barcode barcode) {
        Log.e("onScanned : ", ": " + barcode.rawValue);
        barCodeResult=barcode.rawValue;
        if(!button_one_visibility)
        {
            Intent result = new Intent ();
            result.putExtra(EXTRA_QRVALUE, barcode.rawValue);
            setResult(Activity.RESULT_OK, result);
            finish();
        }else{
            Toast.makeText(this, "Scanning finished, please select TAG or UNTAG ?", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
        Log.e("barcodes ", ": " + barcodes.size());
        if (barcodes.size() > 1) {
            //readerFragment.pauseScanning();
            if (showAlertCheck) {
//                showAlertDialog();
            }
        }
    }
    private void showAlertDialog() {
        showAlertCheck=false;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("Please keep your camera more closure to barcode.");
        adb.setTitle("Title of alert dialog");
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                showAlertCheck=true;
                addBarcodeReaderFragment();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        adb.show();
    }
    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
        Log.e("sparseArray ", ": " + sparseArray.size());
    }
    @Override
    public void onScanError(String errorMessage) {
        //Toast.makeText(this, "errorMessage: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onCameraPermissionDenied() {
       // Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_LONG).show();
    }
    private int getResourceId (String typeAndName)
    {
        if(package_name == null) package_name = getApplication().getPackageName();
        if(resources == null) resources = getApplication().getResources();
        return resources.getIdentifier(typeAndName, null, package_name);
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
