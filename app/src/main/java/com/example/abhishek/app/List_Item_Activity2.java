package com.example.abhishek.app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.abhishek.app.event.BothEyesClosedEvent;
import com.example.abhishek.app.event.LeftEyeClosedEvent;
import com.example.abhishek.app.event.RightEyeClosedEvent;
import com.example.abhishek.app.tracker.FaceTracker;
import com.example.abhishek.app.util.PlayServicesUtil;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


public class List_Item_Activity2 extends AppCompatActivity {
    EditText editText;
    ListView listView;
    int x=0;
    String[] values = new String[]{"Enter*", "Clear", "Space", "Next>>"};
    final String[] char1 = new String[]{"w","h","t","i","y"};
    final String[] char2 = new String[]{"e","a","o","n","s"};
    final String[] char3 = new String[]{"r","d","l","c","u"};
    final String[] char4 = new String[]{"m","f","g","p","b"};
    final String[] char5 = new String[]{"v","k","j","x","q","z"};
    ArrayList<String> listItems,listItems1,listItems2,listItems3,listItems4,listItems5;
    ArrayAdapter<String> adapter;
    private static final int REQUEST_CAMERA_PERM = 69;
    private static final String TAG = "FaceTracker";
    private FaceDetector mFaceDetector;
    private CameraSource mCameraSource;
    private final AtomicBoolean updating = new AtomicBoolean(false);
    private int itemid=0;
    int l,r,c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list__item_2);
        editText = (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.list);

        PlayServicesUtil.isPlayServicesAvailable(this, 69);

        // permission granted...?
        if (isCameraPermissionGranted()) {
            // ...create the camera resource
            createCameraResources();
        } else {
            // ...else request the camera permission
            requestCameraPermission();
        }


        listItems= new ArrayList<String>(Arrays.asList(values));
        listItems1= new ArrayList<String>(Arrays.asList(char1));
        listItems2= new ArrayList<String>(Arrays.asList(char2));
        listItems3= new ArrayList<String>(Arrays.asList(char3));
        listItems4= new ArrayList<String>(Arrays.asList(char4));
        listItems5= new ArrayList<String>(Arrays.asList(char5));


        listItems.addAll(listItems1);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listItems);

        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                {
                    String str=editText.getText().toString();

                    if(position==0) {

                        Intent i= new Intent();
                        i.putExtra("keyName",str.substring(0,str.length()-1));
                        List_Item_Activity1.getStr=1;
                        setResult(RESULT_OK, i);
                        finish();


                    }
                    else if(position==1) {
                        editText.setText(str.substring(0,str.length()-1));
                    }

                    else if(position==2) {
                        editText.setText(str.concat(" "));
                    }

                    //----------------------------------
                    else if(position==3){

                        int ind=listItems.size()-4;
                        while(ind!=0){
                            listItems.remove(listItems.size()-1);
                            ind--;
                        }

                        x++;
                        if(x==5)
                            x=0;
                        ArrayList<String> sh=listItems;

                        switch(x){
                            case 0: sh=listItems1;
                                break;
                            case 1: sh=listItems2;
                                break;
                            case 2: sh=listItems3;
                                break;
                            case 3: sh=listItems4;
                                break;
                            case 4: sh=listItems5;
                                break;
                        }

                        listItems.addAll(sh);
                        adapter.notifyDataSetChanged();



                    }

                    else{
                        editText.setText(str.concat(listItems.get(position)).substring(0,listItems.get(position).length()-1));
                    }

                }


            }
        });
    }

    private boolean isCameraPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCameraResources();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("EyeControl")
                .setMessage("No camera permission")
                .setPositiveButton("Ok", listener)
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // register the event bus
        EventBus.getDefault().register(this);

        // start the camera feed
        if (mCameraSource != null && isCameraPermissionGranted()) {
            try {
                //noinspection MissingPermission
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "onResume: Camera.start() error");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister from the event bus
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        // stop the camera source
        if (mCameraSource != null) {
            mCameraSource.stop();
        } else {
            Log.e(TAG, "onPause: Camera.stop() error");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // release them all...
        if (mFaceDetector != null) {
            mFaceDetector.release();
        } else {
            Log.e(TAG, "onDestroy: FaceDetector.release() error");
        }
        if (mCameraSource != null) {
            mCameraSource.release();
        } else {
            Log.e(TAG, "onDestroy: Camera.release() error");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLeftEyeClosed(LeftEyeClosedEvent e) {

        if(l==3)
        {   listItems.set(itemid,listItems.get(itemid).substring(0,listItems.get(itemid).length()-1));

            if(itemid==0)
                itemid=listItems.size()-1;
            else
                itemid--;

            listItems.set(itemid,listItems.get(itemid)+"*");
            adapter.notifyDataSetChanged();
            l=0; }
        l++;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRightEyeClosed(RightEyeClosedEvent e) {

        if(r==3)
        {
            listItems.set(itemid,listItems.get(itemid).substring(0,listItems.get(itemid).length()-1));

            if(itemid==listItems.size()-1)
                itemid=0;
            else
                itemid++;

            listItems.set(itemid,listItems.get(itemid)+"*");
            adapter.notifyDataSetChanged();
            r=0;}
        r++;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBothEyesClosed(BothEyesClosedEvent e) {
        if(c==3){
            listView.performItemClick(listView.getAdapter().getView(itemid,null,null),itemid,listView.getItemIdAtPosition(itemid));
            c=0;}
        c++;
    }



    private boolean catchUpdatingLock() {
        // set updating and return previous value
        return !updating.getAndSet(true);
    }
    private void releaseUpdatingLock() {
        updating.set(false);
    }

    private void createCameraResources() {
        Context context = getApplicationContext();

        // create and setup the face detector
        mFaceDetector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();

        // now that we've got a detector, create a processor pipeline to receive the detection
        // results
        mFaceDetector.setProcessor(new LargestFaceFocusingProcessor(mFaceDetector, new FaceTracker()));

        // operational...?
        if (!mFaceDetector.isOperational()) {
            Log.w(TAG, "createCameraResources: detector NOT operational");
        } else {
            Log.d(TAG, "createCameraResources: detector operational");
        }

        // Create camera source that will capture video frames
        // Use the front camera
        mCameraSource = new CameraSource.Builder(this, mFaceDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30f)
                .build();
    }


}


