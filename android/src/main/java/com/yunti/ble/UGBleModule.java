package com.yunti.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.util.Log;

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

    public IBleUsbDataReturnInterface iBleUsbDataReturnInterface;

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
     *
     * @param promise
     */
    @ReactMethod
    public void isBluetoothOpen(Promise promise) {
        promise.resolve(UgBleFactory.getInstance().isBluetoothOpen());
    }

    /**
     * 设备是否链接
     *
     * @param promise
     */
    @ReactMethod
    public void isConnDevice(Promise promise, String address) {
        if (TextUtils.isEmpty(address)) {
            promise.resolve(false);
        } else {
            promise.resolve(UgBleFactory.getInstance().isConnDevice(reactContext, address));
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
     *
     * @param time
     */
    @ReactMethod
    public void startScanAndTime(int time) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
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
     *
     * @param address
     */
    @ReactMethod
    public void connectDevice(String address,Promise promise) {
        Activity activity = getCurrentActivity();
        if (deviceList == null || activity == null) {
            return;
        }
        for (int i = 0; i < deviceList.size(); i++) {
            BluetoothDevice bluetoothDevice = deviceList.get(i);
            if (bluetoothDevice.getAddress().equals(address)) {
                connectDevice(bluetoothDevice, activity,promise);
            }
        }

    }

    private void connectDevice(BluetoothDevice bluetoothDevice, final Activity activity, final Promise promise) {
        UgBleFactory.getInstance().connect(activity, bluetoothDevice, new IBleUsbDataReturnInterface() {
            @Override
            public void onGetBleUsbDataReturn(final byte bleButton, final int bleX, final int bleY, final short blePressure) {
                //包含悬浮(-96)和按下(-95)，以及笔板分离（-64）
                Log.v("villa", "bleButtons : " + bleButton + ",bleXs : " + bleX + ",bleYs : " + bleY + ",blePressures : " + blePressure);
                if (iBleUsbDataReturnInterface != null) {
                    iBleUsbDataReturnInterface.onGetBleUsbDataReturn(bleButton, bleX, bleY, blePressure);
                }

            }

            @Override
            public void onGetBleUsbSolfKeyBroad(byte bleHardButton, int bleHardX, int bleHardY) {
                Log.v("villa", "bleHardButton : " + bleHardButton + ",bleHardX : " + bleHardX + ",bleHardY : " + bleHardY);
                if (iBleUsbDataReturnInterface != null) {
                    iBleUsbDataReturnInterface.onGetBleUsbSolfKeyBroad(bleHardButton, bleHardX, bleHardY);
                }
            }

            @Override
            public void onGetBleUsbHardKeyBroad(final byte bleSolfButton, final int bleSolfX, final int bleSolfY) {
                Log.v("villa", "bleSolfButton : " + bleSolfButton + ",bleSolfX : " + bleSolfX + ",bleSolfY : " + bleSolfY);
                if (iBleUsbDataReturnInterface != null) {
                    iBleUsbDataReturnInterface.onGetBleUsbHardKeyBroad(bleSolfButton, bleSolfX, bleSolfY);
                }
            }

            @Override
            public void onGetBleUsbScreenMax(int rc, int maxX, int maxY, int maxButton, int maxPressure) {
                if (iBleUsbDataReturnInterface != null) {
                    iBleUsbDataReturnInterface.onGetBleUsbScreenMax(rc, maxX, maxY, maxButton, maxPressure);
                }
            }

            @Override
            public void onGetBleUsbConnectType(final int type) {
                if (iBleUsbDataReturnInterface != null) {
                    iBleUsbDataReturnInterface.onGetBleUsbConnectType(type);
                }
                promise.resolve(type);
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String bleType = null;
//                        if (CONNECT_DEVICE_TYPE_ONFAIL == type) {
//                            bleType = "连接失败";
////                    UgBleFactory.getInstance().disConnect();
//                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_ONFAIL");
//                        } else if (CONNECT_DEVICE_TYPE_SUCCESS == type) {
//                            bleType = "连接成功";
//                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_SUCCESS");
//                        } else if (CONNECT_DEVICE_TYPE_DISCONNECT == type) {
//                            bleType = "断开连接，重连中....";
//                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_DISCONNECT");
////                    mHandler.postDelayed(runs,5000);
//                        } else if (CONNECT_DEVICE_TYPE_ADDRESS_NULL == type) {
//                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_ADDRESS_NULL");//
//                        } else if (CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE == type) {
//                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
//                        } else if (6 == type) {
//                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
//                        } else if (-3 == type || type == -4) {
//                            bleType = "连接失败";
//                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
//                        } else {
//                            bleType = "异常，重连中....";
//                        }
//                        Toast.makeText(activity, bleType, Toast.LENGTH_SHORT).show();
//                    }
//                });
            }

            @Override
            public void onGetBleUsbBatteryLevel(String s) {
                if (iBleUsbDataReturnInterface != null) {
                    iBleUsbDataReturnInterface.onGetBleUsbBatteryLevel(s);
                }
            }
        });
    }

    /**
     * 设置 连接的数据回调
     * @param l
     */
    public void addIBleUsbDataReturnInterface(IBleUsbDataReturnInterface l) {
        this.iBleUsbDataReturnInterface = l;

    }
}