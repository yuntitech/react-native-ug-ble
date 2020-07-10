package com.yunti.ble;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
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
    private BluetoothDevice currentDevice;
    /**
     * 蓝牙扫描的回调
     */
    UgeeScanCallback scanCallback;

    /**
     * 设备链接的回调
     */
    OnUiCallback callBack;

    public OnUiCallback iUiCallBack;
    private int mBleUsbMaxX = 1;
    private int mBleUsbMaxY = 1;

    public UGBleModule(ReactApplicationContext reactContext) {
        super(reactContext);
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

    @Override
    public void initialize() {
        super.initialize();
        initScanCallBack();
        initUiCallBack();
        initConnectClass();
    }

    private void initConnectClass() {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity != null && ugeePenClass == null) {
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ugeePenClass = new UgeePenClass(currentActivity, callBack) {
                        @Override
                        public void onCompleteService() {
                            mUgeePenBinder = ugeePenClass.getUgeePenBinder();
                        }
                    };
                    ugeePenClass.isBindService();
                }
            });
        }

    }


    private void initUiCallBack() {
        if (callBack == null) {
            callBack = new OnUiCallback() {
                @Override
                public void onStateChanged(int state, String s) {
                    Log.e("villa", "state==" + state);
                    if (iUiCallBack != null) {
                        iUiCallBack.onStateChanged(state, s);
                    }
                    Log.e("villa", "getReactApplicationContext==" + getReactApplicationContext());
                    if (getReactApplicationContext() != null && currentDevice != null) {
                        WritableMap event = Arguments.createMap();
                        event.putInt("type", state);
                        event.putString("address", currentDevice.getAddress());
                        event.putString("name", currentDevice.getName());
                        getReactApplicationContext()
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(connectDeviceTypeNotification, event);
                    }
                }

                @Override
                public void onPenServiceError(String s) {
                    if (iUiCallBack != null) {
                        iUiCallBack.onPenServiceError(s);
                    }
                    Log.e("villa", "onPenServiceError===" + s);
                }

                @Override
                public void onPenPositionChanged(int deviceType, int x, int y, int pressure, byte state) {
                    if (iUiCallBack != null) {
                        iUiCallBack.onPenPositionChanged(deviceType, x, y, pressure, state);
                    }
                    Log.e("villa", "onPenPositionChanged===deviceType" + deviceType + "===x==" + x + "===y===" + y + "===pressure==" + pressure + "==state==" + state);
                }

                @Override
                public void onUgeeKeyEvent(int i) {
                    if (iUiCallBack != null) {
                        iUiCallBack.onUgeeKeyEvent(i);
                    }
                }

                @Override
                public void onUgeeBattery(int i) {
                    if (iUiCallBack != null) {
                        iUiCallBack.onUgeeBattery(i);
                    }
                    Log.e("villa", "onUgeeBattery===" + i);
                }

                @Override
                public void onUgeePenWidthAndHeight(int width, int height, int pressure) {
                    UGBleModule.this.mBleUsbMaxX = width;
                    UGBleModule.this.mBleUsbMaxY = height;
                    if (iUiCallBack != null) {
                        iUiCallBack.onUgeePenWidthAndHeight(width, height, pressure);
                    }
                    Log.e("villa", "onUgeePenWidthAndHeight===width==" + width + "--height==" + height + "--pressure==" + pressure);
                }
            };
        }
    }

    private void initScanCallBack() {
        scanCallback = new UgeeScanCallback() {

            @Override
            public void onBlueToothScanResult(final BluetoothDevice bluetoothDevice, int i, boolean b) {
                Log.e("villa", "onBlueToothScanResult===" + bluetoothDevice.getName());
                if (deviceList != null) {
                    if (!deviceList.contains(bluetoothDevice)) {
                        deviceList.add(bluetoothDevice);
                        if (getReactApplicationContext() != null) {
                            Log.e("villa", "emit===" + bluetoothDevice.getName());
                            WritableMap event = Arguments.createMap();
                            event.putString("address", bluetoothDevice.getAddress());
                            event.putString("name", bluetoothDevice.getName());
                            getReactApplicationContext()
                                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit(scanBluetoothDeviceNotification, event);
                        }
                    }

                }

            }

            @Override
            public void onBlueToothScanFailed(int i) {
                Log.e("villa", "onBlueToothScanFailed===" + i);

            }
        };

    }


    /**
     * 蓝牙是否打开
     */
    @SuppressLint("MissingPermission")
    @ReactMethod
    public void isBluetoothOpen(Promise promise) {
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter != null && blueadapter.isEnabled()) {
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
        try {
            if (ugeePenClass != null) {
                UgeeDevice ugeeDevice = ugeePenClass.getUgeePenBinder().getConnectedDevice();
                boolean isOpen = ugeeDevice != null;
                promise.resolve(isOpen);
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
    public void startScanAndTime(final int time) {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceList.clear();
                mRobotUgeeScannerCompat = new UgeeScannerCompat(scanCallback, activity);
                mRobotUgeeScannerCompat.startScan(time);
            }
        });
    }

    /**
     * 链接
     *
     * @param address
     */
    @ReactMethod
    public void connectDevice(final String address) {
        Log.e("villa", "address==" + address);
        final Activity activity = getCurrentActivity();
        if (deviceList == null || activity == null) {
            return;
        }
        for (int i = 0; i < deviceList.size(); i++) {
            BluetoothDevice bluetoothDevice = deviceList.get(i);
            if (bluetoothDevice.getAddress().equals(address)) {
                this.currentDevice = bluetoothDevice;
                if (mUgeePenBinder != null) {
                    try {
                        mUgeePenBinder.connectDevice(address);
                    } catch (RemoteException e) {
                        Toast.makeText(activity, "连接设备失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }


    /**
     * 设置 连接的数据回调
     */
    public void addIBleUsbDataReturnInterface(OnUiCallback l) {
        this.iUiCallBack = l;

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
        initUiCallBack();
        initConnectClass();
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        mRobotUgeeScannerCompat = null;
        try {
            if (mUgeePenBinder != null) {
                mUgeePenBinder.disconnectDevice();
            }
            if (ugeePenClass != null) {
                ugeePenClass.onUgeePenDestroy();
            }
            ugeePenClass = null;
            callBack = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
