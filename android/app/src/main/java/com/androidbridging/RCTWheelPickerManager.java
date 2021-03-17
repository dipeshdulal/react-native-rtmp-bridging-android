package com.androidbridging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.blackbox_vision.wheelview.LoopScrollListener;
import io.blackbox_vision.wheelview.view.WheelView;

public class RCTWheelPickerManager extends SimpleViewManager<WheelView> {

    public static final String REACT_CLASS = "RCTWheelPicker";

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactProp(name="data")
    public void setData(WheelView wheelView, ReadableArray data) {
        List<String> dataList = new LinkedList<>();
        for (int i = 0; i < data.size(); i++) {
            dataList.add(data.getString(i));
        }
        wheelView.setItems(dataList);
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomBubblingEventTypeConstants() {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> topChange = new HashMap<>();
        topChange.put("bubbled", "onChange");
        map.put("topChange", topChange);
        return map;
    }

    @NonNull
    @Override
    protected WheelView createViewInstance(@NonNull ThemedReactContext reactContext) {
        final WheelView wheelView = new WheelView(reactContext);
        wheelView.setLoopListener(new LoopScrollListener() {
            @Override
            public void onItemSelect(int item) {
                WritableMap event = Arguments.createMap();
                event.putInt("data", item);

                ((ReactContext) wheelView.getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(
                        wheelView.getId(),
                        "topChange",
                        event
                );
            }
        });
        return wheelView;
    }
}
