package org.cloudsky.cordovaPlugins;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;


import java.util.List;

public class ScannerActivity extends AppCompatActivity implements BarcodeReaderFragment.BarcodeReaderListener {

    private BarcodeReaderFragment readerFragment;
    private boolean showAlertCheck = true;

    public static final String EXTRA_QRVALUE = "qrValue";
    public static final String EXTRA_PARAMS = "params";
    public static final int RESULT_ERROR =5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.scanner_view);

        addBarcodeReaderFragment();
    }

    private void addBarcodeReaderFragment() {
        readerFragment = BarcodeReaderFragment.newInstance(true, false, View.VISIBLE);
        readerFragment.setListener(this);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fm_container, readerFragment);
        fragmentTransaction.commitAllowingStateLoss();

    }


    @Override
    public void onScanned(Barcode barcode) {

        Intent result = new Intent ();
        result.putExtra(EXTRA_QRVALUE, barcode.rawValue);
        setResult(Activity.RESULT_OK, result);
        finish();

    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
        Log.e("barcodes ", ": " + barcodes.size());
        if (barcodes.size() > 1) {
        //    readerFragment.pauseScanning();
            if (showAlertCheck) {
           //     showAlertDialog();
            }
        }
    }

    private void showAlertDialog() {
        showAlertCheck = false;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("Please keep your camera more closure to barcode.");
        adb.setTitle("Title of alert dialog");
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ScannerActivity.this, "Scanning started again.",
                        Toast.LENGTH_SHORT).show();
                showAlertCheck = true;
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

        setResult(RESULT_ERROR);
        finish();
    }

    @Override
    public void onCameraPermissionDenied() {
        Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_LONG).show();
    }
}
