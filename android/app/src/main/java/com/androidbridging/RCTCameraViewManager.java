package com.androidbridging;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidbridging.rtmp.CustomRTMPCamera2;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.pedro.encoder.input.gl.render.filters.object.ImageObjectFilterRender;
import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.rtplibrary.util.BitrateAdapter;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.util.Map;

public class RCTCameraViewManager extends SimpleViewManager<RCTCameraView> implements SurfaceHolder.Callback {

    private static final String CAMERA_VIEW = "RCTCameraView";
    private static final String START_PUBLISH_COMMAND = "startPublish";
    private static final String STOP_PUBLISH_COMMAND = "stopPublish";

    private int streamWidth = 1280;
    private int streamHeight = 720;
    private String streamUrl = "";
    private boolean audioMuted = false;
    private boolean videoMuted = false;
    private String camera = "front";

    private SurfaceHolder surfaceHolder;
    private RCTCameraView cameraView;
    private CustomRTMPCamera2 rtmpCamera2;
    private BitrateAdapter bitrateAdapter;
    private ThemedReactContext reactContext;
    private ImageObjectFilterRender imageObjectFilterRender;

    @ReactProp(name="streamWidth")
    public void setStreamWidth(RCTCameraView view, int streamWidth) {
        this.streamWidth = streamWidth;
    }

    @ReactProp(name="streamHeight")
    public void setStreamheight(RCTCameraView view, int streamHeight) {
        this.streamHeight = streamHeight;
    }

    @ReactProp(name="streamUrl")
    public void setStreamUrl(RCTCameraView view, String streamUrl) {
        this.streamUrl = streamUrl;
    }

    @ReactProp(name="audioMuted")
    public void setAudioMuted(RCTCameraView view, boolean muted) {
        this.audioMuted = muted;
        if (rtmpCamera2 == null) { return; }

        if(this.audioMuted){
            rtmpCamera2.disableAudio();
        } else {
            rtmpCamera2.enableAudio();
        }

    }

    @ReactProp(name="videoMuted")
    public void setVideoMuted(RCTCameraView view, boolean muted) {
        this.videoMuted = muted;
        if (rtmpCamera2 == null){return;}

        if(this.videoMuted) {
            imageObjectFilterRender.setAlpha(1);
        } else {
            imageObjectFilterRender.setAlpha(0);
        }

    }

    @ReactProp(name="camera")
    public void setCamera(RCTCameraView view, String camera) {
        this.camera = camera;
        if(rtmpCamera2 != null){
            if (rtmpCamera2.isFrontCamera() && camera.equals("back")) {
                rtmpCamera2.switchCamera();
            }
            if (!rtmpCamera2.isFrontCamera() && camera.equals("front")) {
                rtmpCamera2.switchCamera();
            }
        }
    }

    @Override
    public void receiveCommand(@NonNull RCTCameraView root, String commandId, @Nullable ReadableArray args) {
        Log.d("receiving commands: ", "receiveCommand: "+ commandId + "  " + args.toString());
        switch (commandId) {
            case START_PUBLISH_COMMAND:
                this.startPublish(args.getString(0));
                break;
            case STOP_PUBLISH_COMMAND:
                this.stopPublish();
                break;
            default:
        }
    }

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
        this.reactContext = reactContext;
        return cameraView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("RNCameraView", "surfaceCreated");
        rtmpCamera2 = new CustomRTMPCamera2(cameraView, new ConnectCheckerRtmp() {
            @Override
            public void onConnectionSuccessRtmp() {
                bitrateAdapter = new BitrateAdapter(new BitrateAdapter.Listener() {
                    @Override
                    public void onBitrateAdapted(int bitrate) {
                        rtmpCamera2.setVideoBitrateOnFly(bitrate);
                    }
                });
                bitrateAdapter.setMaxBitrate(rtmpCamera2.getBitrate());
            }

            @Override
            public void onConnectionFailedRtmp(@NonNull String reason) {

            }

            @Override
            public void onNewBitrateRtmp(long bitrate) {
                if(bitrateAdapter != null ){
                    bitrateAdapter.adaptBitrate(bitrate);
                }
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
        rtmpCamera2.startPreview(CameraHelper.Facing.FRONT, streamHeight, streamWidth);
        imageObjectFilterRender = new ImageObjectFilterRender();
        rtmpCamera2.getGlInterface().setFilter(imageObjectFilterRender);
        Log.d("width/height", "width: "+width+" height: "+height);

        imageObjectFilterRender.setImage(
                BitmapFactory.decodeResource(this.reactContext.getResources(), R.mipmap.pause)
        );

        imageObjectFilterRender.setAlpha(0f);

    }

    public void startPublish(String streamKey) {
        if(rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo(streamHeight, streamWidth, 30, 700*1024, 5,90)){
            rtmpCamera2.startStream(streamUrl+streamKey);

            if(this.audioMuted){
                rtmpCamera2.disableAudio();
            } else {
                rtmpCamera2.enableAudio();
            }

            if(this.videoMuted) {
                rtmpCamera2.pauseRecord();
            } else {
                rtmpCamera2.resumeRecord();
            }

        }

    }

    public void stopPublish(){
        rtmpCamera2.stopStream();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("RNCameraView", "surfaceDestroyed");
        if(rtmpCamera2.isStreaming()) {
            rtmpCamera2.stopStream();
        }
    }
}
