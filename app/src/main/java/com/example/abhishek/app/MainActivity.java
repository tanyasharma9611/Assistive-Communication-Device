package com.example.abhishek.app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import com.example.abhishek.app.util.PlayServicesUtil;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import com.example.abhishek.app.event.BothEyesClosedEvent;
import com.example.abhishek.app.event.LeftEyeClosedEvent;
import com.example.abhishek.app.event.NeutralFaceEvent;
import com.example.abhishek.app.event.RightEyeClosedEvent;
import com.example.abhishek.app.tracker.FaceTracker;


public class MainActivity  extends AppCompatActivity implements OnInitListener {

    ListView listView;
    public int index;
    private static final int REQUEST_CAMERA_PERM = 69;
    private static final String TAG = "FaceTracker";
    private FaceDetector mFaceDetector;
    private CameraSource mCameraSource;
    private final AtomicBoolean updating = new AtomicBoolean(false);
    private int itemid=0;
    ArrayAdapter<String> adapter;
    String[] values;
    int l,r,c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_item_click_event_example);

        // check that the play services are installed
        PlayServicesUtil.isPlayServicesAvailable(this, 69);

        // permission granted...?
        if (isCameraPermissionGranted()) {
            // ...create the camera resource
            createCameraResources();
        } else {
            // ...else request the camera permission
            requestCameraPermission();
        }



        listView = (ListView) findViewById(R.id.list);

        final TextToSpeech tts = new TextToSpeech(this, (OnInitListener) this);
        tts.setLanguage(Locale.ENGLISH);
        tts.setPitch(0.8f);
        tts.setSpeechRate(1.1f);

        values = new String[]{"Food*", "Water", "Washroom", "Emergency", "Start a conversation!"};


        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (position == 4) {

                    Intent myIntent = new Intent(MainActivity.this, List_Item_Activity1.class);
                    startActivity(myIntent);

                }


                index = position;
                if (position !=4 ){
                        //The item selected from the words array is toasted
                    tts.speak(values[index], TextToSpeech.QUEUE_FLUSH, null);
                    //the item selected from the  pronouciation array is synthesized using the speak method.
                    tts.speak(" ", TextToSpeech.QUEUE_ADD, null);

                }
            }

        });
    }



    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(MainActivity.this,
                    "", Toast.LENGTH_SHORT).show();
        }

        //If text to speech is not supported than we output an error message
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(MainActivity.this,
                    "Error occurred while initializing Text-To-Speech engine probably you dont have it installed", Toast.LENGTH_LONG).show();
        }

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
        {   values[itemid]=values[itemid].substring(0,values[itemid].length()-1);
            if(itemid==0)
                itemid=values.length-1;
            else
                itemid--;
            values[itemid]=values[itemid]+"*";
            adapter.notifyDataSetChanged();
            l=0; }
        l++;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRightEyeClosed(RightEyeClosedEvent e) {

        if(r==3)
        {   values[itemid]=values[itemid].substring(0,values[itemid].length()-1);
            if(itemid==values.length-1)
                itemid=0;
            else
                itemid++;
            values[itemid]=values[itemid]+"*";
            adapter.notifyDataSetChanged();
            r=0;}
        r++;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBothEyesClosed(BothEyesClosedEvent e) {
        if(c==3){
            listView.performItemClick(listView.getAdapter().getView(itemid,null,null),itemid,listView.getItemIdAtPosition(itemid));
            Toast.makeText(this,values[itemid].substring(0,values[itemid].length()-1)+" is clicked.",Toast.LENGTH_SHORT).show();
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
