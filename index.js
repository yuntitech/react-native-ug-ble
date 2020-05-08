"use strict";
const {
  NativeModules,
  NativeEventEmitter,
  DeviceEventEmitter,
  Platform,
} = require("react-native");
const { UGBleModule } = NativeModules;

export const isBluetoothOpen = async () => {
  return UGBleModule.isBluetoothOpen();
};

export const isConnDevice = async (address) => {
  return UGBleModule.isConnDevice(address);
};

export const disConnectDevice = (address) => {
  UGBleModule.disConnectDevice();
};

export const startScanAndTime = (scanTime) => {
  UGBleModule.startScanAndTime(scanTime);
};

export const connectDevice = (address) => {
  UGBleModule.connectDevice(address);
};
