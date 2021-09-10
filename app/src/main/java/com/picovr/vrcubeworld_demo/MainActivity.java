package com.picovr.vrcubeworld_demo;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.picovr.cvclient.CVController;
import com.picovr.cvclient.CVControllerListener;
import com.picovr.cvclient.CVControllerManager;
import com.picovr.vractivity.HmdState;
import com.picovr.vrlibs.VRActivity;

/**
 * @author welch
 */
public class MainActivity extends VRActivity {

    private final String TAG = "==VRCubeWorld==";

    private MainRender mainRender;
    private CVControllerManager cvManager;
    private CVController rightController, leftController;
    private final CVControllerListener cvListener = new CVControllerListener() {
        @Override
        public void onBindSuccess() {

        }

        @Override
        public void onBindFail() {
            Log.d(TAG, "bind fail");
        }

        @Override
        public void onThreadStart() {
            rightController = cvManager.getRightController();
            leftController = cvManager.getLeftController();
            mainRender.setController(rightController, leftController);
        }

        @Override
        public void onConnectStateChanged(int i, int i1) {
            Log.d(TAG, "cvController " + i + " state is " + i1);
        }

        @Override
        public void onMainControllerChanged(int i) {
            rightController = cvManager.getRightController();
            leftController = cvManager.getLeftController();
            mainRender.setController(rightController, leftController);
        }

        @Override
        public void onChannelChanged(int i, int i1) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                (WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
        super.onCreate(savedInstanceState);

        cvManager = new CVControllerManager(this);
        cvManager.setListener(cvListener);
        rightController = cvManager.getRightController();
        leftController = cvManager.getLeftController();

        mainRender = new MainRender(this);
        mainRender.setController(rightController, leftController);
        setRenderer(mainRender);
    }

    @Override
    public void onResume() {
        super.onResume();
        cvManager.bindService();
    }

    @Override
    public void onPause() {
        super.onPause();
        cvManager.unbindService();
    }

    @Override
    public void onFrameBegin(HmdState hmdState) {
        float[] hmdOrientation = hmdState.getOrientation();
        float[] hmdPosition = hmdState.getPos();
        float[] hmdData = new float[7];
        hmdData[0] = hmdOrientation[0];
        hmdData[1] = hmdOrientation[1];
        hmdData[2] = hmdOrientation[2];
        hmdData[3] = hmdOrientation[3];
        hmdData[4] = hmdPosition[0];
        hmdData[5] = hmdPosition[1];
        hmdData[6] = hmdPosition[2];

        cvManager.updateControllerData(hmdData);
        super.onFrameBegin(hmdState);
    }
}