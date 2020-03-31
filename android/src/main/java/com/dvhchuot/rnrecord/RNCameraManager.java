package com.dvhchuot.rnrecord;

import com.dvhchuot.rnrecord.data.ReactProps;
import com.dvhchuot.rnrecord.ui.CameraView;
import com.dvhchuot.rnrecord.ui.RecordView;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;

public class RNCameraManager extends SimpleViewManager<CameraView> {
    public static final String REACT_CLASS = "RNDVCamera";

    @Override
    public String getName() {
        // Tell React the name of the module
        // https://facebook.github.io/react-native/docs/native-components-android.html#1-create-the-viewmanager-subclass
        return REACT_CLASS;
    }

    @Override
    public CameraView createViewInstance(ThemedReactContext context){
        // Create a view here
        // https://facebook.github.io/react-native/docs/native-components-android.html#2-implement-method-createviewinstance
        return new CameraView(context);
    }

    //    @ReactProp(name = "exampleProp")
//    public void setExampleProp(View view, String prop) {
//        // Set properties from React onto your native component via a setter method
//        // https://facebook.github.io/react-native/docs/native-components-android.html#3-expose-view-property-setters-using-reactprop-or-reactpropgroup-annotation
//    }
    @ReactProp(name="filter")
    public void setFilter(CameraView view, String filter) {
        view.setFilter(filter);
    }

    @ReactProp(name = "isFocused")
    public void setIsFocused(CameraView view, Boolean isFocused) {
        view.setIsFocused(isFocused);
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put(ReactProps.onDoneStart, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onDoneStart))
                .put(ReactProps.onDoneSuccess, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onDoneSuccess))
                .put(ReactProps.onDVAfterUpdate, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onDVAfterUpdate))
                .put(ReactProps.onDVCameraReady, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onDVCameraReady))
                .put(ReactProps.onDVProgress, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onDVProgress))
                .build();
    }
}

