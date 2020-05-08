package com.yunti.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ble.support.UgBleFactory;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.ugee.pentabletinterfacelibrary.IBleUsbDataReturnInterface;
import com.ugee.pentabletinterfacelibrary.IUgeeBleInterface;

import java.util.ArrayList;
import java.util.List;


/**
 * @version V1.0 <描述当前版本功能>
 * @FileName: UGBleModule.java
 * @author: villa_mou
 * @date: 05-14:43
 * @desc
 */
public class UGBleModule extends ReactContextBaseJavaModule {

    private ReactApplicationContext reactContext;

    private String scanBluetoothDeviceNotification = "scanBluetoothDeviceNotification";
    private List<BluetoothDevice> deviceList = new ArrayList();
    private static final int CONNECT_DEVICE_TYPE_SUCCESS = 0;//连接成功
    private static final int CONNECT_DEVICE_TYPE_ONFAIL = 1;//连接失败
    private static final int CONNECT_DEVICE_TYPE_DISCONNECT = 3;//断开连接
    private static final int CONNECT_DEVICE_TYPE_ADDRESS_NULL = 4;//设备地址为空
    private static final int CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE = 5;//蓝牙未打开

    public UGBleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "UGBleModule";
    }

    /**
     * 蓝牙是否打开
     * @param promise
     */
    @ReactMethod
    public void isBluetoothOpen(Promise promise) {
        promise.resolve(UgBleFactory.getInstance().isBluetoothOpen());
    }

    /**
     * 设备是否链接
     * @param promise
     */
    @ReactMethod
    public void isConnDevice(Promise promise,String address) {
        if(TextUtils.isEmpty(address)){
            promise.resolve(false);
        }else{
            promise.resolve(UgBleFactory.getInstance().isConnDevice(reactContext,address));
        }
    }

    /**
     * 断开链接
     */
    @ReactMethod
    public void disConnectDevice() {
        UgBleFactory.getInstance().disConnect();
    }

    /**
     * 开始扫描
     * @param time
     */
    @ReactMethod
    public void startScanAndTime(int time) {
        Activity activity =getCurrentActivity();
        if(activity==null|| deviceList==null){
            return;
        }
        deviceList.clear();
        UgBleFactory.getInstance().startScanAndTime(activity, new IUgeeBleInterface() {
            @Override
            public void onGetBleDevice(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                deviceList.add(bluetoothDevice);
                WritableMap event = Arguments.createMap();
                event.putString("address", bluetoothDevice.getAddress());
                event.putString("name", bluetoothDevice.getName());
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(scanBluetoothDeviceNotification, event);
            }

            @Override
            public void onGetBleStorage(String s) {

            }

            @Override
            public void onGetBleFlashFlag(boolean b) {

            }

        }, time);
    }

    /**
     * 链接
     * @param address
     */
    @ReactMethod
    public void connectDevice(String address) {
        Activity activity =getCurrentActivity();
        if (deviceList == null || activity == null) {
            return;
        }
        for (int i = 0; i < deviceList.size(); i++) {
            BluetoothDevice bluetoothDevice = deviceList.get(i);
            if (bluetoothDevice.getAddress().equals(address)) {
                connectDevice(bluetoothDevice,activity);
            }
        }

    }

    private void connectDevice(BluetoothDevice bluetoothDevice, final Activity activity) {
        UgBleFactory.getInstance().connect(activity, bluetoothDevice, new IBleUsbDataReturnInterface() {
            @Override
            public void onGetBleUsbDataReturn(final byte bleButton, final int bleX, final int bleY, final short blePressure) {
                if (bleButton == -95) {
                    //包含悬浮(-96)和按下(-95)，以及笔板分离（-64）
                    Log.v("villa", "bleButtons : " + bleButton + ",bleXs : " + bleX + ",bleYs : " + bleY + ",blePressures : " + blePressure);
                }

            }

            @Override
            public void onGetBleUsbSolfKeyBroad(byte bleHardButton, int bleHardX, int bleHardY) {
                final int bleHardXs = bleHardX;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bleHardXs != 0) {
                            Toast.makeText(activity,"touchDown",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity,"touchUp",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onGetBleUsbHardKeyBroad(byte b, int i, int i1) {

            }

            @Override
            public void onGetBleUsbScreenMax(int i, int i1, int i2, int i3, int i4) {

            }

            @Override
            public void onGetBleUsbConnectType(final int type) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String bleType = null;
                        if (CONNECT_DEVICE_TYPE_ONFAIL == type) {
                            bleType ="连接失败";
//                    UgBleFactory.getInstance().disConnect();
                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_ONFAIL");
                        } else if (CONNECT_DEVICE_TYPE_SUCCESS == type) {
                            bleType = "连接成功";
                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_SUCCESS");
                        } else if (CONNECT_DEVICE_TYPE_DISCONNECT == type) {
                            bleType = "断开连接，重连中....";
                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_DISCONNECT");
//                    mHandler.postDelayed(runs,5000);
                        } else if (CONNECT_DEVICE_TYPE_ADDRESS_NULL == type) {
                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_ADDRESS_NULL");//
                        } else if (CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE == type) {
                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
                        } else if (6 == type) {
                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
                        } else if (-3 == type || type == -4) {
                            bleType = "连接失败";
                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
                        } else {
                            bleType ="异常，重连中....";
                        }
                        Toast.makeText(activity, bleType, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onGetBleUsbBatteryLevel(String s) {

            }
        });
    }
}