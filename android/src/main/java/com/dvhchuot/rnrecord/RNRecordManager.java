package com.dvhchuot.rnrecord;

import com.dvhchuot.rnrecord.data.ReactProps;
import com.dvhchuot.rnrecord.ui.RecordView;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;

public class RNRecordManager extends SimpleViewManager<RecordView> {
    public static final String REACT_CLASS = "RNRecord";

    @Override
    public String getName() {
        // Tell React the name of the module
        // https://facebook.github.io/react-native/docs/native-components-android.html#1-create-the-viewmanager-subclass
        return REACT_CLASS;
    }

    @Override
    public RecordView createViewInstance(ThemedReactContext context){
        // Create a view here
        // https://facebook.github.io/react-native/docs/native-components-android.html#2-implement-method-createviewinstance
        return new RecordView(context);
    }

    //    @ReactProp(name = "exampleProp")
//    public void setExampleProp(View view, String prop) {
//        // Set properties from React onto your native component via a setter method
//        // https://facebook.github.io/react-native/docs/native-components-android.html#3-expose-view-property-setters-using-reactprop-or-reactpropgroup-annotation
//    }
    @ReactProp(name="filter")
    public void setFilter(RecordView view, String filter) {
        view.setFilter(filter);
    }

    @ReactProp(name = "isFocused")
    public void setIsFocused(RecordView view, Boolean isFocused) {
        view.setIsFocused(isFocused);
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put(ReactProps.onDone, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onDone))
                .put(ReactProps.onExit, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onExit))
                .put(ReactProps.onPressPhoto, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onPressPhoto))
                .put(ReactProps.onFilterTap, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onFilterTap))
                .put(ReactProps.onErrorRecord, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onErrorRecord))
                .put(ReactProps.onErrorDiskspace, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onErrorDiskspace))
                .put(ReactProps.onPressDraft, //Same as name registered with receiveEvent
                        MapBuilder.of("registrationName", ReactProps.onPressDraft))
                .build();
    }
}
