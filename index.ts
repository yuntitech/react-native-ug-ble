"use strict";
const {
  NativeModules,
  NativeEventEmitter,
  DeviceEventEmitter,
  Platform,
} = require("react-native");
const { UGBleModule } = NativeModules;
/**
 * 扫描设备的通知
 */
export const scanBluetoothDeviceNotification =
  "scanBluetoothDeviceNotification";
/**
 * 连接设备状态的通知
 */
export const connectDeviceTypeNotification = "connectDeviceTypeNotification";
export const isBluetoothOpen = async (): Promise<boolean> => {
  return UGBleModule.isBluetoothOpen();
};

export const isConnDevice = async (address): Promise<boolean> => {
  return UGBleModule.isConnDevice(address);
};

export const disConnectDevice = (address) => {
  UGBleModule.disConnectDevice();
};

export const startScanAndTime = (scanTime) => {
  UGBleModule.startScanAndTime(scanTime);
};

export const connectDevice = async (address) => {
  UGBleModule.connectDevice(address);
};
