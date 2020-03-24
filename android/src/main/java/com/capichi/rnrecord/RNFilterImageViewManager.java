package com.capichi.rnrecord;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nullable;

public class RNFilterImageViewManager extends SimpleViewManager<FilterImageView> {
    public static final String REACT_CLASS = "RNFilter";

    @Override
    public String getName() {
        // Tell React the name of the module
        // https://facebook.github.io/react-native/docs/native-components-android.html#1-create-the-viewmanager-subclass
        return REACT_CLASS;
    }

    @Override
    public FilterImageView createViewInstance(ThemedReactContext context){
        // Create a view here
        // https://facebook.github.io/react-native/docs/native-components-android.html#2-implement-method-createviewinstance
        return new FilterImageView(context);
    }

    @ReactProp(name = "source")
    public void setSrc(final FilterImageView view, @Nullable ReadableMap src) {
        String uri = src.getString("uri");
        view.setSource(uri);
    }

    @ReactProp(name="filter")
    public void setFilter(FilterImageView view, String filter) {
        view.setFilter(filter);
    }

    @ReactProp(name = "isFocused")
    public void setIsFocused(RecordView view, @Nullable Boolean isFocused) {
        view.setIsFocused(isFocused);
    }
}
