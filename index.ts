import { NativeModules, Platform } from 'react-native';

const { UGBleModule } = NativeModules;
/**
 * 扫描设备的通知
 */
export const scanBluetoothDeviceNotification =
  'scanBluetoothDeviceNotification';
/**
 * 连接设备状态的通知
 */
export const connectDeviceTypeNotification = 'connectDeviceTypeNotification';

export enum ConnectStatusType {
  // public static final int STATE_DISCONNECTED = 0;
  // public static final int STATE_CONNECTING = 1;
  // public static final int STATE_CONNECTED = 2;
  // public static final int STATE_ERROR = 3;
  // public static final int STATE_ENTER_SYNC_MODE_SUCCESS = 4;
  // public static final int STATE_ENTER_SYNC_MODE_FAIL = 5;
  // public static final int STATE_DEVICE_INFO = 6;
  // public static final int NO_DEVICE = 7;
  /**
   * 连接成功
   */
  CONNECT_DEVICE_TYPE_SUCCESS = 6,

  /**
   * 连接失败
   */
  CONNECT_DEVICE_TYPE_ONFAIL = 3,

  /**
   * 断开连接
   */
  CONNECT_DEVICE_TYPE_DISCONNECT = 0,

  /**
   * 设备地址为空
   */
  CONNECT_DEVICE_TYPE_ADDRESS_NULL = 7,

  /**
   * 蓝牙未打开
   */
  CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE = 5,
}

export interface UGBleDeviceInfo {
  maxX: number;
  maxY: number;
  maxPressure: number;
}

/**
 * 蓝牙是否打开
 */
export const isBluetoothOpen = async (): Promise<boolean> => {
  return UGBleModule.isBluetoothOpen();
};

/**
 * 设备是否连接
 */
export const isConnDevice = async (address: string): Promise<boolean> => {
  return UGBleModule.isConnDevice(address);
};

/**
 * 断开设备连接
 */
export const disConnectDevice = () => {
  UGBleModule.disConnectDevice();
};

/**
 * 扫描设备
 */
export const startScanAndTime = (scanTime: number) => {
  UGBleModule.startScanAndTime(scanTime);
};

export const stopScan = async () => {
  if (Platform.OS === 'ios') {
    return UGBleModule.stopScan();
  }
};

/**
 * 连接设备并接收数据
 */
export const connectDevice = async (address: string) => {
  UGBleModule.connectDevice(address);
};

/**
 * 打开蓝牙
 */
export const openBle = () => {
  UGBleModule.openBle();
};

export const getDeviceInfo = async (): Promise<UGBleDeviceInfo> => {
  return UGBleModule.getDeviceInfo();
};
