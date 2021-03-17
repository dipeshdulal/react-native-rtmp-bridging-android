package com.androidbridging;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.pedro.rtplibrary.view.OpenGlView;

public class RCTCameraView extends CustomOpenGLView {

    public RCTCameraView(Context context) {
        super(context);
        // 1 means fill
        this.setKeepAspectRatio(true);
        this.setAspectRatioMode(1);
    }

    public RCTCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
}
