package org.cloudsky.cordovaPlugins;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import com.google.android.gms.vision.barcode.Barcode;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ScannerActivity extends Activity implements BarcodeReaderFragment.BarcodeReaderListener {
        private String barcodeValue;
    private BarcodeReaderFragment readerFragment;
    private boolean showAlertCheck = true;
    public static final String EXTRA_QRVALUE = "qrValue";
    public static final String EXTRA_ISTAG= "is_tag";
    public static final String EXTRA_PARAMS = "params";
    public static final int RESULT_ERROR =5;
    private String package_name;
    private Resources resources;
    private Boolean button_one_visibility;
    private Boolean check_double_barcode;
    private Handler handler;
    private String errorMessage;
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
         errorMessage= params.optString("double_barcode_detection_message");
        check_double_barcode= params.optBoolean("check_double_barcode",false);

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
        Button cancelBtn= findViewById(getResourceId("id/cancel"));
        if(button_one_visibility)
        {
            cancelBtn.setVisibility(View.VISIBLE);
        }
        Button btnUnTag= findViewById(getResourceId("id/untaggable"));
        if(button_two_visibility)
        {
            btnUnTag.setVisibility(View.VISIBLE);
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent ();
                result.putExtra(EXTRA_QRVALUE, "null");
                result.putExtra(EXTRA_ISTAG, ":cancel");
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        btnUnTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent ();
                result.putExtra(EXTRA_QRVALUE, "null");
                result.putExtra(EXTRA_ISTAG, ":untag");
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        addBarcodeReaderFragment();
        handler = new Handler();

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

        barcodeValue=barcode.rawValue;
        if(check_double_barcode)
        {
        if(showAlertCheck)
        {
         handler.postDelayed(sendResult, 4000);
        }


        }else {
            Intent result = new Intent ();
            result.putExtra(EXTRA_QRVALUE, barcode.rawValue);
            setResult(Activity.RESULT_OK, result);
            finish();
        }


    }
    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
        Log.e("barcodes ", ": " + barcodes.size());
        if(check_double_barcode)
        {
            if (barcodes.size() > 1) {
                readerFragment.pauseScanning();
                if (showAlertCheck) {
                    showAlertDialog();
                }
            }
        }


    }
    private void showAlertDialog() {
        showAlertCheck=false;
        handler.removeCallbacks(sendResult);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage(errorMessage);
        adb.setTitle("Barcode Detection");
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showAlertCheck=true;
             addBarcodeReaderFragment();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_CANCELED);
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

    Runnable sendResult=new Runnable() {
        @Override
        public void run() {
            Intent result = new Intent ();
            result.putExtra(EXTRA_QRVALUE, barcodeValue);
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    };
}

