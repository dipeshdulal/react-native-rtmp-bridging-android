package com.androidbridging.rtmp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import androidx.annotation.RequiresApi;
import com.pedro.encoder.input.gl.SurfaceManager;
import com.pedro.encoder.input.gl.render.ManagerRender;
import com.pedro.encoder.input.gl.render.filters.BaseFilterRender;
import com.pedro.encoder.utils.gl.GlUtil;
import com.pedro.rtplibrary.R;
import com.pedro.rtplibrary.view.Filter;
import com.pedro.rtplibrary.view.OpenGlViewBase;

/**
 * Created by pedro on 9/09/17.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CustomOpenGLView extends OpenGlViewBase {

    private ManagerRender managerRender = null;
    private boolean loadAA = false;

    private boolean AAEnabled = false;
    private boolean keepAspectRatio = false;
    private int aspectRatioMode = 0;
    private boolean isFlipHorizontal = false, isFlipVertical = false;

    public CustomOpenGLView(Context context) {
        super(context);
    }

    public CustomOpenGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OpenGlView);
        try {
            keepAspectRatio = typedArray.getBoolean(R.styleable.OpenGlView_keepAspectRatio, false);
            aspectRatioMode = typedArray.getInt(R.styleable.OpenGlView_aspectRatioMode, 0);
            AAEnabled = typedArray.getBoolean(R.styleable.OpenGlView_AAEnabled, false);
            ManagerRender.numFilters = typedArray.getInt(R.styleable.OpenGlView_numFilters, 1);
            isFlipHorizontal = typedArray.getBoolean(R.styleable.OpenGlView_isFlipHorizontal, false);
            isFlipVertical = typedArray.getBoolean(R.styleable.OpenGlView_isFlipVertical, false);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void init() {
        if (!initialized) managerRender = new ManagerRender();
        managerRender.setCameraFlip(isFlipHorizontal, isFlipVertical);
        initialized = true;
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return managerRender.getSurfaceTexture();
    }

    @Override
    public Surface getSurface() {
        return managerRender.getSurface();
    }

    @Override
    public void setFilter(int filterPosition, BaseFilterRender baseFilterRender) {
        filterQueue.add(new Filter(filterPosition, baseFilterRender));
    }

    @Override
    public void setFilter(BaseFilterRender baseFilterRender) {
        setFilter(0, baseFilterRender);
    }

    @Override
    public void enableAA(boolean AAEnabled) {
        this.AAEnabled = AAEnabled;
        loadAA = true;
    }

    @Override
    public void setRotation(int rotation) {
        managerRender.setCameraRotation(rotation);
    }

    public boolean isKeepAspectRatio() {
        return keepAspectRatio;
    }

    public void setKeepAspectRatio(boolean keepAspectRatio) {
        this.keepAspectRatio = keepAspectRatio;
    }

    public void setAspectRatioMode(int aspectRatioMode) {
        this.aspectRatioMode = aspectRatioMode;
    }

    public void setCameraFlip(boolean isFlipHorizontal, boolean isFlipVertical) {
        managerRender.setCameraFlip(isFlipHorizontal, isFlipVertical);
    }

    @Override
    public boolean isAAEnabled() {
        return managerRender != null && managerRender.isAAEnabled();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "size: " + width + "x" + height);
        this.previewWidth = width;
        this.previewHeight = height;
        if (managerRender != null) managerRender.setPreviewSize(previewWidth, previewHeight);
    }

    @Override
    public void run() {
        releaseSurfaceManager();
        surfaceManager = new SurfaceManager(getHolder().getSurface());
        surfaceManager.makeCurrent();
        managerRender.initGl(getContext(), encoderWidth, encoderHeight, previewWidth, previewHeight);
        managerRender.getSurfaceTexture().setOnFrameAvailableListener(this);
        if (surfaceManagerEncoder == null && surfaceManagerPhoto == null) {
            surfaceManagerPhoto = new SurfaceManager(encoderWidth, encoderHeight, surfaceManager);
        }
        semaphore.release();
        try {
            while (running) {
                if (frameAvailable || forceRender) {
                    frameAvailable = false;
                    surfaceManager.makeCurrent();
                    managerRender.updateFrame();
                    managerRender.drawOffScreen();
                    managerRender.drawScreen(previewWidth, previewHeight, keepAspectRatio, aspectRatioMode, 0,
                            true, false, false);
                    surfaceManager.swapBuffer();

                    synchronized (sync) {
                        if (surfaceManagerEncoder != null && !fpsLimiter.limitFPS()) {
                            surfaceManagerEncoder.makeCurrent();
                            if (muteVideo) {
                                managerRender.drawScreen(0, 0, false, aspectRatioMode, streamRotation, false,
                                        isStreamVerticalFlip, isStreamHorizontalFlip);
                            } else {
                                managerRender.drawScreen(encoderWidth, encoderHeight, false, aspectRatioMode,
                                        streamRotation, false, isStreamVerticalFlip, isStreamHorizontalFlip);
                            }
                        } else if (takePhotoCallback != null && surfaceManagerPhoto != null) {
                            surfaceManagerPhoto.makeCurrent();
                            managerRender.drawScreen(encoderWidth, encoderHeight, false, aspectRatioMode,
                                    streamRotation, false, isStreamVerticalFlip, isStreamHorizontalFlip);
                        }
                        if (takePhotoCallback != null) {
                            takePhotoCallback.onTakePhoto(GlUtil.getBitmap(encoderWidth, encoderHeight));
                            takePhotoCallback = null;
                        }
                        if (surfaceManagerEncoder != null) surfaceManagerEncoder.swapBuffer();
                        else if (surfaceManagerPhoto != null) surfaceManagerPhoto.swapBuffer();
                    }
                    if (!filterQueue.isEmpty()) {
                        Filter filter = filterQueue.take();
                        managerRender.setFilter(filter.getPosition(), filter.getBaseFilterRender());
                    } else if (loadAA) {
                        managerRender.enableAA(AAEnabled);
                        loadAA = false;
                    }
                }
            }
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        } finally {
            managerRender.release();
            releaseSurfaceManager();
        }
    }
}