package com.yunti.ble;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

/**
 * @version V1.0 <描述当前版本功能>
 * @FileName: UGBleModule.java
 * @author: villa_mou
 * @date: 05-14:43
 * @desc
 */
public class UGBleModule extends ReactContextBaseJavaModule {

    private ReactApplicationContext reactContext;


    public UGBleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "UGBleModule";
    }
}