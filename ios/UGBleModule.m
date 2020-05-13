//
//  UGBleModule.m
//  UgBle
//
//  Created by jaybo on 2020/5/13.
//  Copyright © 2020 yunti. All rights reserved.
//

@import CoreBluetooth;
#import "UGBleModule.h"
#import "BluetoothShareManager.h"

/// 扫描设备的通知
/// { address: string, name: string }
NSString *const UGScanBluetoothDeviceNotification = @"scanBluetoothDeviceNotification";

/// 连接设备状态的通知
/// { type: number, address: string, name: string }
NSString *const UGConnectDeviceTypeNotification = @"connectDeviceTypeNotification";

typedef NS_ENUM(NSInteger, UGConnectionStatus) {
    /// 连接成功
    UGConnectionStatusSuccess = 0,
    
    /// 连接失败
    UGConnectionStatusFailure = 1,
    
    /// 断开连接
    UGConnectionStatusDisconnected = 3,
    
    /// 设备地址为空
    UGConnectionStatusAddressNull = 4,
    
    /// 蓝牙未打开
    UGConnectionStatusBluetoothClosed = 5
};

@interface UGBleModule () <CBCentralManagerDelegate, BluetoothShareManagerDelegate>
@property (nonatomic) CBCentralManager *cbCentralManager;
@property (nonatomic) BOOL isBluetoothOn;
@property (nonatomic) NSMutableDictionary<NSString *, CBPeripheral *> *connectedDevices;
@property (nonatomic) NSMutableDictionary<NSString *, CBPeripheral *> *scannedDevices;
@end

@implementation UGBleModule

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    if (self) {
        _cbCentralManager = [[CBCentralManager alloc] init];
        _cbCentralManager.delegate = self;
        _connectedDevices = [NSMutableDictionary dictionary];
        [[BluetoothShareManager shareManager] setManageDelegate:self];
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[UGConnectDeviceTypeNotification, UGScanBluetoothDeviceNotification];
}


RCT_EXPORT_METHOD(isBluetoothOpen:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    resolve(@(self.isBluetoothOn));
}

//
///**
// * 设备是否连接
// */
//export const isConnDevice = async (address): Promise<boolean> => {
RCT_EXPORT_METHOD(isConnDevice:(NSString *)address
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    CBPeripheral *device = [self.connectedDevices valueForKey:address];
    if (device) {
        resolve(@(YES));
    } else {
        resolve(@(NO));
    }
}

///**
// * 断开设备连接
// */
//export const disConnectDevice = () => {
RCT_EXPORT_METHOD(disConnectDevice) {
    for (NSString *identifier in self.connectedDevices.copy) {
        CBPeripheral *device = [self.connectedDevices valueForKey:identifier];
        [[BluetoothShareManager shareManager] CancelDisconnectBlueToothDevice:device];
    }
}

///**
// * 扫描设备
// */
//export const startScanAndTime = (scanTime) => {
RCT_EXPORT_METHOD(startScanAndTime:(NSNumber *)scanTime
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    // iOS并没有用到这个时间，Android才会用到
    [[BluetoothShareManager shareManager] ScanBlueToothDevice];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(stopScan:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [[BluetoothShareManager shareManager] StopScanBleDevice];
    resolve([NSNull null]);
}

///**
// * 连接设备并接收数据
// */
//export const connectDevice = async (address) => {
RCT_EXPORT_METHOD(connectDevice:(NSString *)address
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    CBPeripheral *peripheral = [self.scannedDevices valueForKey:address];
    if (peripheral) {
        [[BluetoothShareManager shareManager] ConnectBlueToothDevicePeripheral:peripheral];
        resolve([NSNull null]);
    } else {
        reject(@"1", @"无法连接", nil);
    }
}

- (NSDictionary *)connectNotificationBodyWithPeripheral:(CBPeripheral *)peripheral
                                       connectionStatus:(UGConnectionStatus)status {
    NSDictionary *body = @{
        @"type": @(status),
        @"address": peripheral.identifier.UUIDString,
        @"name": peripheral.name
    };
    return body;
}


#pragma mark - BluetoothShareManagerDelegate

/**
 设备连接成功

 @param peripheral 连接成功对象
 */
- (void)BlueToothDidConnectPeripheral:(CBPeripheral *)peripheral {
    [self.connectedDevices setValue:peripheral
                             forKey:peripheral.identifier.UUIDString];

    UGConnectionStatus status = UGConnectionStatusSuccess;
    NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
    [self sendEventWithName:UGConnectDeviceTypeNotification body:body];
}

/**
 断开设备连接
 */
- (void)BlueToothDidDisconnectPeripheral:(CBPeripheral*) peripheral {
    [self.connectedDevices setValue:nil
                             forKey:peripheral.identifier.UUIDString];

    UGConnectionStatus status = UGConnectionStatusDisconnected;
    NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
    [self sendEventWithName:UGConnectDeviceTypeNotification body:body];
}

/**
 连接失败
 */
- (void)BlueToothDidFailToConnectPeripheral:(CBPeripheral *)peripheral
                                      error:(NSError *)error {
    [self.connectedDevices setValue:nil
                             forKey:peripheral.identifier.UUIDString];

    UGConnectionStatus status = UGConnectionStatusFailure;
    NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
    [self sendEventWithName:UGConnectDeviceTypeNotification body:body];
}

/**
 超时连接
 */
- (void)BlueToothDidTimeoutConnectPeripheral:(CBPeripheral *)peripheral
                                       error:(NSError *)error {
    [self.connectedDevices setValue:nil
                             forKey:peripheral.identifier.UUIDString];

    UGConnectionStatus status = UGConnectionStatusFailure;
    NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
    [self sendEventWithName:UGConnectDeviceTypeNotification body:body];
}

/**
 扫描外设返回

 @param peripheral 扫描到的外设对象
 */
- (void)ScanBlueToothDevicePeripheral:(CBPeripheral*)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData {
    NSDictionary *body = @{
        @"address": peripheral.identifier.UUIDString,
        @"name": peripheral.name
    };
    [self sendEventWithName:UGScanBluetoothDeviceNotification body:body];
}

/**
 //模拟鼠标接收包

 @param manager manager
 @param dataPacket 模拟鼠标数据接收包
 */
- (void)manager:(BluetoothShareManager *)manager didReceviceDataPacket:(BLE_DATAPACKET *)dataPacket {
    // TODO:
}


/**
 模拟按键接收包

 @param hotkeyIndex 按钮的标志
 @param keyStatus 按钮状态
 */
- (void)manager:(BluetoothShareManager *)manager didReceviceHotKeyDataPacket:(UInt32) hotkeyIndex status:(enum BLE_KeyStatus) keyStatus {
    // TODO:
}


/**
 获取修改蓝牙的名称

 @param bleName BLE Update Name
 */
- (void)manager:(BluetoothShareManager *)manager didReceviceBleDeviceUpdateName:(NSString*) bleName {

}


#pragma mark - CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    switch (central.state) {
        case CBManagerStatePoweredOn: {
            self.isBluetoothOn = YES;
            break;
        }
        default: {
            self.isBluetoothOn = NO;
            break;
        }
    }
}


@end
