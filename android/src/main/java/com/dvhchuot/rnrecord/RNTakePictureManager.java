package com.dvhchuot.rnrecord;

import com.dvhchuot.rnrecord.data.ReactProps;
import com.dvhchuot.rnrecord.ui.PicktureView;
import com.dvhchuot.rnrecord.ui.RecordView;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;

public class RNTakePictureManager extends SimpleViewManager<PicktureView> {
    public static final String REACT_CLASS = "RNTakePicture";

    @Override
    public String getName() {
        // Tell React the name of the module
        // https://facebook.github.io/react-native/docs/native-components-android.html#1-create-the-viewmanager-subclass
        return REACT_CLASS;
    }

    @Override
    public PicktureView createViewInstance(ThemedReactContext context){
        // Create a view here
        // https://facebook.github.io/react-native/docs/native-components-android.html#2-implement-method-createviewinstance
        return new PicktureView(context);
    }

    @ReactProp(name = "isFocused")
    public void setIsFocused(PicktureView view, Boolean isFocused) {
        view.setIsFocused(isFocused);
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put(ReactProps.onTakeDone, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onTakeDone))
                .put(ReactProps.onTakeExit, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onTakeExit))
                .build();
    }
}
