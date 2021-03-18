package com.androidbridging;

import android.content.Context;
import android.util.AttributeSet;

import com.androidbridging.rtmp.CustomOpenGLView;

public class RCTCameraView extends CustomOpenGLView {

    public RCTCameraView(Context context) {
        super(context);
        // 1 means fill
        this.setKeepAspectRatio(true);
        this.setAspectRatioMode(1);
        this.setForceRender(true);
    }

    public RCTCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


}
