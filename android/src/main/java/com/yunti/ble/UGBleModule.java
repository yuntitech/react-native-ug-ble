package com.yunti.ble;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;

import cn.ugee.pen.IRemoteUgeeService;
import cn.ugee.pen.callback.OnUiCallback;
import cn.ugee.pen.callback.UgeePenClass;
import cn.ugee.pen.model.UgeeDevice;
import cn.ugee.pen.scan.UgeeScanCallback;
import cn.ugee.pen.scan.UgeeScannerCompat;

/**
 * @version V1.0 <描述当前版本功能>
 * @FileName: UGBleModule.java
 * @author: villa_mou
 * @date: 05-14:43
 * @desc
 */
@ReactModule(name = UGBleModule.NAME)
public class UGBleModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    static final String NAME = "UGBleModule";
    private ReactApplicationContext reactContext;
    private UgeeScannerCompat mRobotUgeeScannerCompat;
    private UgeePenClass ugeePenClass;
    private IRemoteUgeeService mUgeePenBinder;
    /**
     * 扫描设备的通知
     */
    private String scanBluetoothDeviceNotification = "scanBluetoothDeviceNotification";
    /**
     * 连接设备的通知
     */
    private String connectDeviceTypeNotification = "connectDeviceTypeNotification";
    private List<BluetoothDevice> deviceList = new ArrayList();
    private static final int CONNECT_DEVICE_TYPE_SUCCESS = 0;//连接成功
    private static final int CONNECT_DEVICE_TYPE_ONFAIL = 1;//连接失败
    private static final int CONNECT_DEVICE_TYPE_DISCONNECT = 3;//断开连接
    private static final int CONNECT_DEVICE_TYPE_ADDRESS_NULL = 4;//设备地址为空
    private static final int CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE = 5;//蓝牙未打开
    private BluetoothDevice currentDevice;

    public OnUiCallback iBleUsbDataReturnInterface;
    private int mBleUsbMaxX = 1;
    private int mBleUsbMaxY = 1;

    public UGBleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            mRobotUgeeScannerCompat = new UgeeScannerCompat(scanCallback, currentActivity);
            ugeePenClass = new UgeePenClass(currentActivity, callBack) {
                @Override
                public void onCompleteService() {
                    mUgeePenBinder = ugeePenClass.getUgeePenBinder();
                }
            };
            ugeePenClass.isBindService();
        }

        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public int getBleUsbMaxX() {
        return mBleUsbMaxX;
    }

    public int getBleUsbMaxY() {
        return mBleUsbMaxY;
    }

    /**
     * 蓝牙扫描的回调
     */
    UgeeScanCallback scanCallback = new UgeeScanCallback() {

        @Override
        public void onBlueToothScanResult(final BluetoothDevice bluetoothDevice, int i, boolean b) {
            Log.v("villa", "onBlueToothScanResult===" + bluetoothDevice.getName());
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (deviceList != null) {
                            deviceList.add(bluetoothDevice);
                            if (reactContext != null) {
                                WritableMap event = Arguments.createMap();
                                event.putString("address", bluetoothDevice.getAddress());
                                event.putString("name", bluetoothDevice.getName());
                                reactContext
                                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                        .emit(scanBluetoothDeviceNotification, event);
                            }
                        }
                    }
                });
            }

        }

        @Override
        public void onBlueToothScanFailed(int i) {
            Log.e("villa", "onBlueToothScanFailed===" + i);

        }
    };

    /**
     * 设备链接的回调
     */
    OnUiCallback callBack = new OnUiCallback() {
        @Override
        public void onStateChanged(int state, String s) {
            if (iBleUsbDataReturnInterface != null) {
                iBleUsbDataReturnInterface.onStateChanged(state, s);
            }
            if (reactContext != null) {
                WritableMap event = Arguments.createMap();
                event.putInt("type", state);
                event.putString("address", currentDevice.getAddress());
                event.putString("name", currentDevice.getName());
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(connectDeviceTypeNotification, event);
            }
//            Log.e("villa", "onStateChanged===" + state);
//            switch (state) {
//                case RemoteState.STATE_ERROR:// 链接失败
//
//                    try {
//                        ugeePenClass.getUgeePenBinder().disconnectDevice();
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                case RemoteState.STATE_DEVICE_INFO: // 链接成功
//                    try {
////                            UgeeDevice mDevice = ugeePenClass.getUgeePenBinder().getConnectedDevice();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        try {
//                            ugeePenClass.getUgeePenBinder().disconnectDevice();
//                        } catch (Exception e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                    break;
//                case RemoteState.STATE_DISCONNECTED:// 链接断开
//
//                    break;
//            }
        }

        @Override
        public void onPenServiceError(String s) {
            if (iBleUsbDataReturnInterface != null) {
                iBleUsbDataReturnInterface.onPenServiceError(s);
            }
            Log.e("villa", "onPenServiceError===" + s);
        }

        @Override
        public void onPenPositionChanged(int deviceType, int x, int y, int pressure, byte state) {
            if (iBleUsbDataReturnInterface != null) {
                iBleUsbDataReturnInterface.onPenPositionChanged(deviceType, x, y, pressure, state);
            }
            Log.e("villa", "onPenPositionChanged===deviceType" + deviceType + "===x==" + x + "===y===" + y + "===pressure==" + pressure + "==state==" + state);
        }

        @Override
        public void onUgeeKeyEvent(int i) {
            if (iBleUsbDataReturnInterface != null) {
                iBleUsbDataReturnInterface.onUgeeKeyEvent(i);
            }
        }

        @Override
        public void onUgeeBattery(int i) {
            if (iBleUsbDataReturnInterface != null) {
                iBleUsbDataReturnInterface.onUgeeBattery(i);
            }
            Log.e("villa", "onUgeeBattery===" + i);
        }

        @Override
        public void onUgeePenWidthAndHeight(int width, int height, int pressure) {
            UGBleModule.this.mBleUsbMaxX = width;
            UGBleModule.this.mBleUsbMaxY = height;
            if (iBleUsbDataReturnInterface != null) {
                iBleUsbDataReturnInterface.onUgeePenWidthAndHeight(width, height, pressure);
            }
            Log.e("villa", "onUgeePenWidthAndHeight===width==" + width + "--height==" + height + "--pressure==" + pressure);
        }
    };


    /**
     * 蓝牙是否打开
     *
     * @param promise
     */
    @SuppressLint("MissingPermission")
    @ReactMethod
    public void isBluetoothOpen(Promise promise) {
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter != null && blueadapter.enable()) {
            promise.resolve(true);
        } else {
            promise.resolve(false);
        }

    }

    /**
     * 设备是否链接
     *
     * @param promise
     */
    @ReactMethod
    public void isConnDevice(String address, Promise promise) {
        Activity activity = getCurrentActivity();
//        if (TextUtils.isEmpty(address) || activity == null) {
//            promise.resolve(false);
//        } else {
//            boolean isConnected = UgBleFactory.getInstance().isConnDevice(activity, address);
//            promise.resolve(isConnected);
//        }
        try {
            if (ugeePenClass != null) {
                UgeeDevice ugeeDevice = ugeePenClass.getUgeePenBinder().getConnectedDevice();
                boolean isOpen = ugeeDevice != null;
                promise.resolve(isOpen);
                Log.e("villa", "isOpen==" + isOpen);
            } else {
                promise.resolve(false);
            }
        } catch (RemoteException e) {
            promise.resolve(false);
        }
    }

    /**
     * 断开链接
     */
    @ReactMethod
    public void disConnectDevice() {
        try {
            if (ugeePenClass != null) {
                ugeePenClass.getUgeePenBinder().disconnectDevice();
            }
        } catch (RemoteException e) {
            Toast.makeText(getCurrentActivity(), "断开蓝牙失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始扫描
     *
     * @param time
     */
    @ReactMethod
    public void startScanAndTime(int time) {
        Activity activity = getCurrentActivity();
        if (activity == null || mRobotUgeeScannerCompat == null) {
            return;
        }
        deviceList.clear();
        mRobotUgeeScannerCompat.startScan(time);
//        UgBleFactory.getInstance().startScanAndTime(activity, new IUgeeBleInterface() {
//            @Override
//            public void onGetBleDevice(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
//                deviceList.add(bluetoothDevice);
//                if (reactContext != null) {
//                    WritableMap event = Arguments.createMap();
//                    event.putString("address", bluetoothDevice.getAddress());
//                    event.putString("name", bluetoothDevice.getName());
//                    reactContext
//                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                            .emit(scanBluetoothDeviceNotification, event);
//                }
//
//            }
//
//            @Override
//            public void onGetBleStorage(String s) {
//
//            }
//
//            @Override
//            public void onGetBleFlashFlag(boolean b) {
//
//            }
//
//        }, time);
    }

    /**
     * 链接
     *
     * @param address
     */
    @ReactMethod
    public void connectDevice(String address) {
        Activity activity = getCurrentActivity();
        if (deviceList == null || activity == null) {
            return;
        }
        for (int i = 0; i < deviceList.size(); i++) {
            BluetoothDevice bluetoothDevice = deviceList.get(i);
            if (bluetoothDevice.getAddress().equals(address)) {
                this.currentDevice = bluetoothDevice;
//                connectDevice(bluetoothDevice, activity);
                try {
                    ugeePenClass.getUgeePenBinder().connectDevice(address);
                } catch (RemoteException e) {
                    Toast.makeText(activity, "连接设备失败", Toast.LENGTH_SHORT).show();

                }
            }
        }

    }

//    private void connectDevice(final BluetoothDevice bluetoothDevice, final Activity activity) {
//        UgBleFactory.getInstance().connect(activity, bluetoothDevice, new IBleUsbDataReturnInterface() {
//            @Override
//            public void onGetBleUsbDataReturn(final byte bleButton, final int bleX, final int bleY, final short blePressure) {
//                //包含悬浮(-96)和按下(-95)，以及笔板分离（-64）
//                if (iBleUsbDataReturnInterface != null) {
//                    iBleUsbDataReturnInterface.onGetBleUsbDataReturn(bleButton, bleX, bleY, blePressure);
//                }
//
//            }
//
//            @Override
//            public void onGetBleUsbSolfKeyBroad(byte bleHardButton, int bleHardX, int bleHardY) {
//                if (iBleUsbDataReturnInterface != null) {
//                    iBleUsbDataReturnInterface.onGetBleUsbSolfKeyBroad(bleHardButton, bleHardX, bleHardY);
//                }
//            }
//
//            @Override
//            public void onGetBleUsbHardKeyBroad(final byte bleSolfButton, final int bleSolfX, final int bleSolfY) {
//                if (iBleUsbDataReturnInterface != null) {
//                    iBleUsbDataReturnInterface.onGetBleUsbHardKeyBroad(bleSolfButton, bleSolfX, bleSolfY);
//                }
//            }
//
//            @Override
//            public void onGetBleUsbScreenMax(int rc, int maxX, int maxY, int maxButton, int maxPressure) {
//                UGBleModule.this.mBleUsbMaxX = maxX;
//                UGBleModule.this.mBleUsbMaxY = maxY;
//                if (iBleUsbDataReturnInterface != null) {
//                    iBleUsbDataReturnInterface.onGetBleUsbScreenMax(rc, maxX, maxY, maxButton, maxPressure);
//                }
//            }
//
//            @Override
//            public void onGetBleUsbConnectType(final int type) {
//                if (iBleUsbDataReturnInterface != null) {
//                    iBleUsbDataReturnInterface.onGetBleUsbConnectType(type);
//                }
//
//                if (reactContext != null) {
//                    WritableMap event = Arguments.createMap();
//                    event.putInt("type", type);
//                    event.putString("address", bluetoothDevice.getAddress());
//                    event.putString("name", bluetoothDevice.getName());
//                    reactContext
//                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                            .emit(connectDeviceTypeNotification, event);
//                }
//
////                activity.runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        String bleType = null;
////                        if (CONNECT_DEVICE_TYPE_ONFAIL == type) {
////                            bleType = "连接失败";
//////                    UgBleFactory.getInstance().disConnect();
////                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_ONFAIL");
////                        } else if (CONNECT_DEVICE_TYPE_SUCCESS == type) {
////                            bleType = "连接成功";
////                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_SUCCESS");
////                        } else if (CONNECT_DEVICE_TYPE_DISCONNECT == type) {
////                            bleType = "断开连接，重连中....";
////                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_DISCONNECT");
//////                    mHandler.postDelayed(runs,5000);
////                        } else if (CONNECT_DEVICE_TYPE_ADDRESS_NULL == type) {
////                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_ADDRESS_NULL");//
////                        } else if (CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE == type) {
////                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
////                        } else if (6 == type) {
////                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
////                        } else if (-3 == type || type == -4) {
////                            bleType = "连接失败";
////                            Log.i("tagData", "onGetBleUsbConnectType CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE");//
////                        } else {
////                            bleType = "异常，重连中....";
////                        }
////                        Toast.makeText(activity, bleType, Toast.LENGTH_SHORT).show();
////                    }
////                });
//            }
//
//            @Override
//            public void onGetBleUsbBatteryLevel(String s) {
//                if (iBleUsbDataReturnInterface != null) {
//                    iBleUsbDataReturnInterface.onGetBleUsbBatteryLevel(s);
//                }
//            }
//        });
//    }

    /**
     * 设置 连接的数据回调
     *
     * @param l
     */
    public void addIBleUsbDataReturnInterface(OnUiCallback l) {
        this.iBleUsbDataReturnInterface = l;

    }

    @ReactMethod
    public void openBle() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.putExtra(Intent.EXTRA_PACKAGE_NAME, "cn.bookln.saas.hhtk");
            activity.startActivity(enableBtIntent);

        }
    }

    /**
     * 停止扫描Ble设备
     */
    @ReactMethod
    public void stopScan() {
        Activity currentActivity = getCurrentActivity();
        if (mRobotUgeeScannerCompat != null && currentActivity != null) {
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRobotUgeeScannerCompat.stopScan();
                }
            });

        }

    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        stopScan();
        mRobotUgeeScannerCompat = null;
        callBack = null;
        try {
            mUgeePenBinder.disconnectDevice();
            ugeePenClass.onUgeePenDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
