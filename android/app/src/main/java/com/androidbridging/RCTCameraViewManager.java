package com.androidbridging;

import android.content.res.Resources;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;

import net.ossrs.rtmp.ConnectCheckerRtmp;

public class RCTCameraViewManager extends SimpleViewManager<RCTCameraView> implements SurfaceHolder.Callback {

    private static final String CAMERA_VIEW = "RCTCameraView";

    private SurfaceHolder surfaceHolder;
    private RCTCameraView cameraView;
    private CustomRTMPCamera2 rtmpCamera2;

    @NonNull
    @Override
    public String getName() {
        return CAMERA_VIEW;
    }

    @NonNull
    @Override
    protected RCTCameraView createViewInstance(@NonNull ThemedReactContext reactContext) {
        cameraView = new RCTCameraView(reactContext);
        surfaceHolder = cameraView.getHolder();
        surfaceHolder.addCallback(this);
        return cameraView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("RNCameraView", "surfaceCreated");
        rtmpCamera2 = new CustomRTMPCamera2(cameraView, new ConnectCheckerRtmp() {
            @Override
            public void onConnectionSuccessRtmp() {

            }

            @Override
            public void onConnectionFailedRtmp(@NonNull String reason) {

            }

            @Override
            public void onNewBitrateRtmp(long bitrate) {

            }

            @Override
            public void onDisconnectRtmp() {

            }

            @Override
            public void onAuthErrorRtmp() {

            }

            @Override
            public void onAuthSuccessRtmp() {

            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("RNCameraView", "surfaceChanged");
        rtmpCamera2.startPreview(CameraHelper.Facing.BACK);
        if(rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo(1280, 720, 30, 120*1024, 0, 90)){
            rtmpCamera2.startStream("rtmp://global-live.mux.com:5222/app/105c61cd-26c4-5fd1-4186-37490351b2eb");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("RNCameraView", "surfaceDestroyed");
        if(rtmpCamera2.isStreaming()) {
            rtmpCamera2.stopStream();
        }
    }
}
