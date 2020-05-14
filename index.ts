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
  /**
   * 连接成功
   */
  CONNECT_DEVICE_TYPE_SUCCESS = 0,

  /**
   * 连接失败
   */
  CONNECT_DEVICE_TYPE_ONFAIL = 1,

  /**
   * 断开连接
   */
  CONNECT_DEVICE_TYPE_DISCONNECT = 3,

  /**
   * 设备地址为空
   */
  CONNECT_DEVICE_TYPE_ADDRESS_NULL = 4,

  /**
   * 蓝牙未打开
   */
  CONNECT_DEVICE_TYPE_BLUETOOTH_CLOSE = 5,
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
