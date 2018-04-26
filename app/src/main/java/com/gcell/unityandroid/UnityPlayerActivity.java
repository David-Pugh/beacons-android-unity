package com.gcell.unityandroid;

import com.unity3d.player.*;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;




import java.util.ArrayList;
import java.util.List;
import static android.content.ContentValues.TAG;

import com.gcell.ibeacon.gcellbeaconscanlibrary.GCellBeaconScanManagerService;

public class UnityPlayerActivity extends Activity
{

    private String TAG = "GCScanManager";
    private String permissionExplanationMessage = "In order to see nearby beacons and get the most out of this app you need to allow access to your Location.";
    private boolean showLocationPermissionExplanationDialog = true;
    private int coarseLocationRequestcode = 1;
    private boolean deBug = true;


	protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

		mUnityPlayer = new UnityPlayer(this);
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();

        //Check location permissions and launch beacn scanning service
        Boolean granted = checkPermissions();
        // use this to start and trigger a service
        if (granted) {
            startBeaconScanning();
        }



    }
/** Start the beacon scanning service **/
    private void startBeaconScanning(){
        Intent i = new Intent(this, GCellBeaconScanManagerService.class);
        // potentially add data to the intent
        i.putExtra("use_regions", true);
        //If true the service will automatically load a JSON file and create the appropriate Regions
        Log.d(TAG, "Starting Service");
        this.startService(i);

    }


    /**
     * Checks Coarse location permissions for API > 23
     * Also checks to see if an explanation dialog is required and shows this if appropriate
     *
     * @return boolean value of whether permission is already granted (true) or has been requested (false)
     */
    private boolean checkPermissions() {
        Log.i(TAG, "Checking Location Permissions.....");
        //Get permissions for API > 23
        //First check to see if permission is granted
        if (Build.VERSION.SDK_INT >= 23) {
            Log.i(TAG, "API >= 23.....");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (deBug) {
                    Log.i(TAG, "Current Status - Location Permission Not Granted.");
                }
                if (deBug) {
                    Log.i(TAG, "Requesting Location Permissions.....");
                }
                final Activity  m = this;
                // Check to see if we should show an explanation of why we need to ask for Location permissions
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || showLocationPermissionExplanationDialog) {
                    //If so, show message and ask for permissions
                    showMessageOK(permissionExplanationMessage,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Request Permissions
                                    ActivityCompat.requestPermissions(m , new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, coarseLocationRequestcode);
                                }
                            });
                    return false;
                }
                // else just ask for permissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, coarseLocationRequestcode);
                return false;

            } else {
                if (deBug) {
                    Log.i(TAG, "Location Granted");
                }
                return true;
            }

        }
        return true;
    }

    /**
     * Display the permission explanation dialog
     *
     * @param message
     * @param okListener
     */
    private void showMessageOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                startBeaconScanning();
            } else {
                // permission denied!
            }
        }
    }




    // Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();
		super.onDestroy();
	}

	// Pause Unity
	@Override protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
	}

	// This ensures the layout will be correct.
	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}

	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
	/*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }



}
