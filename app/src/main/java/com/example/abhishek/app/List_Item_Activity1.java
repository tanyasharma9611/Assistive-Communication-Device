package com.example.abhishek.app;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
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
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;


public class List_Item_Activity1 extends AppCompatActivity implements OnInitListener {
    public static TextView txtSpeechInput;
    public static ListView listView;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public static ArrayList<String> listItems;
    public static String[] values;
    public static ArrayAdapter<String> adapter;
    String chat="";
    public int index;
    public static int getStr=0;
    private static final int List_Item_Activity2_RESULT_CODE_=0;
    TextToSpeech tts;
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
        setContentView(R.layout.activity_list__item_1);
        listView = (ListView) findViewById(R.id.list);
        txtSpeechInput=(TextView)findViewById(R.id.txtSpeechInput);
        values = new String[]{"Tap to open keyboard*", "Tap to open mic"};

        PlayServicesUtil.isPlayServicesAvailable(this, 69);

        // permission granted...?
        if (isCameraPermissionGranted()) {
            // ...create the camera resource
            createCameraResources();
        } else {
            // ...else request the camera permission
            requestCameraPermission();
        }

         tts = new TextToSpeech(this, (OnInitListener) this);
        tts.setLanguage(Locale.ENGLISH);
        tts.setPitch(0.8f);
        tts.setSpeechRate(1.1f);

        listItems= new ArrayList<String>(Arrays.asList(values));
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listItems);

        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(List_Item_Activity1.this, List_Item_Activity2.class);
                    startActivityForResult(myIntent,List_Item_Activity2_RESULT_CODE_);

                }
                if (position == 1) {
                    promptSpeechInput();
                }

                //text to speech conversion of the responses.
                index = position;
                    if (position!=0&&position!=1 ) {
                        tts.speak(listItems.get(index), TextToSpeech.QUEUE_FLUSH, null);
                        tts.speak(" ", TextToSpeech.QUEUE_FLUSH, null);
                        chat+="Me : "+ listItems.get(index)+"\n";
                        txtSpeechInput.setText(chat);

                        int x=listItems.size()-2;
                        while (x != 0) {
                            listItems.remove(2);
                            x--;
                        }
                        adapter.notifyDataSetChanged();
                    }

            }
        });
    }

    private void promptSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    chat+="X : "+result.get(0)+"\n";
                    txtSpeechInput.setText(chat);
                    gen_responses(result.get(0));

                }
                break;
            }

            case List_Item_Activity2_RESULT_CODE_: {
                if(resultCode == RESULT_OK){
                    String returnString = data.getStringExtra("keyName");
                    chat+="Me : "+returnString+"\n";
                    txtSpeechInput.setText(chat);

                    tts.speak(returnString, TextToSpeech.QUEUE_FLUSH, null);
                    tts.speak(" ", TextToSpeech.QUEUE_FLUSH, null);

                }
                break;
            }
        }



    }


    public void gen_responses(String str){
        String[] words= str.split(" ");
        StringBuilder sen= new StringBuilder(words[0]);
        for(int i=1;i<words.length;i++)
        {
            sen.append("%20");
            sen.append(words[i]);
        }

        fetchData process= new fetchData(sen.toString());
        process.execute();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

        }

        //If text to speech is not supported than we output an error message
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(List_Item_Activity1.this,
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