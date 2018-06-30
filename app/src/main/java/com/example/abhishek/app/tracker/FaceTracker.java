

package com.example.abhishek.app.tracker;

import com.example.abhishek.app.event.BothEyesClosedEvent;
import com.example.abhishek.app.event.LeftEyeClosedEvent;
import com.example.abhishek.app.event.NeutralFaceEvent;
import com.example.abhishek.app.event.RightEyeClosedEvent;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import org.greenrobot.eventbus.EventBus;

public class FaceTracker extends Tracker<Face> {

    private static final float PROB_THRESHOLD = 0.7f;
    private static final String TAG = FaceTracker.class.getSimpleName();
    private boolean leftClosed;
    private boolean rightClosed;

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        if (leftClosed && face.getIsLeftEyeOpenProbability() > PROB_THRESHOLD) {
            leftClosed = false;
        } else if (!leftClosed &&  face.getIsLeftEyeOpenProbability() < PROB_THRESHOLD){
            leftClosed = true;
        }
        if (rightClosed && face.getIsRightEyeOpenProbability() > PROB_THRESHOLD) {
            rightClosed = false;
        } else if (!rightClosed && face.getIsRightEyeOpenProbability() < PROB_THRESHOLD) {
            rightClosed = true;
        }

        if (leftClosed && !rightClosed) {
            EventBus.getDefault().post(new LeftEyeClosedEvent());
        } else if (rightClosed && !leftClosed) {
            EventBus.getDefault().post(new RightEyeClosedEvent());
        } else if (!leftClosed && !rightClosed) {
            EventBus.getDefault().post(new NeutralFaceEvent());
        } else if (rightClosed && leftClosed) {
            EventBus.getDefault().post(new BothEyesClosedEvent());
        }
    }
}
