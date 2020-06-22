//
//  UGBleModule.m
//  UgBle
//
//  Created by jaybo on 2020/5/13.
//  Copyright © 2020 yunti. All rights reserved.
//

@import CoreBluetooth;
#import "UGBleModule.h"

// 需要真机调试的时候把这里改为1
#define BKL_UG_BLE_DEBUG 0

#define BKL_UG_BLE_ENABLED BKL_UG_BLE_DEBUG || !DEBUG

#if BKL_UG_BLE_ENABLED
#import "BluetoothShareManager.h"
#endif


/// 扫描设备的通知
/// { address: string, name: string }
NSString *const UGScanBluetoothDeviceNotification = @"scanBluetoothDeviceNotification";

/// 连接设备状态的通知
/// { type: number, address: string, name: string }
NSString *const UGConnectDeviceTypeNotification = @"connectDeviceTypeNotification";

/// 连接设备状态的通知，给原生代码用的，NSNotificationCenter发送
/// { type: number, address: string, name: string }
NSString *const UGBleConnectionDidChangeNotification = @"cn.bookln.UGBleConnectionDidChangeNotification";

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

@interface UGBleModule ()
<
CBCentralManagerDelegate
#if BKL_UG_BLE_ENABLED
, BluetoothShareManagerDelegate
#endif
>
@property (nonatomic) CBCentralManager *cbCentralManager;
@property (nonatomic) BOOL isBluetoothOn;
@property (nonatomic) NSMutableDictionary<NSString *, CBPeripheral *> *connectedDevices;
@property (nonatomic) NSMutableDictionary<NSString *, CBPeripheral *> *scannedDevices;
@property (nonatomic, nullable) NSDictionary *dataToSend;
@property (nonatomic) NSTimeInterval fireInterval;
@property (nonatomic) NSTimeInterval idleInterval;
@property (nonatomic, nullable, weak) NSTimer *sendDataTimer;

@property (nonatomic) NSTimeInterval lastCheckIdleTime;
@property (nonatomic) NSTimeInterval firstSkipTime;

@property (nonatomic, nullable, copy) NSDictionary *deviceInfo;
@property (nonatomic) BOOL didSetDelegate;

@end

@implementation UGBleModule

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    if (self) {
        _fireInterval = 0.03;
        _idleInterval = 5;
        _cbCentralManager = [[CBCentralManager alloc] init];
        _cbCentralManager.delegate = self;
        _connectedDevices = [NSMutableDictionary dictionary];
        _scannedDevices = [NSMutableDictionary dictionary];
    }
    return self;
}

- (void)dealloc {
    [self stopTimer];
}

- (NSArray<NSString *> *)supportedEvents {
    return @[UGConnectDeviceTypeNotification, UGScanBluetoothDeviceNotification];
}

- (void)startTimerIfNeeded {
    if (_sendDataTimer == nil) {
        _sendDataTimer = [NSTimer scheduledTimerWithTimeInterval:_fireInterval
                                                          target:self
                                                        selector:@selector(sendReceivedData)
                                                        userInfo:nil
                                                         repeats:YES];
    }
}

- (void)stopTimer {
    [_sendDataTimer invalidate];
    _sendDataTimer = nil;
}

- (void)sendReceivedData {
    if (self.dataToSend) {
        NSString *notificationName = @"UGBleDidReceiveDataPacketNotification";
        [[NSNotificationCenter defaultCenter] postNotificationName:notificationName
                                                            object:self.dataToSend];
        self.dataToSend = nil;
        self.firstSkipTime = 0;
    } else {
        NSTimeInterval now = [[NSDate date] timeIntervalSince1970];
        if (self.firstSkipTime == 0) {
            self.firstSkipTime = now;
        }
        
        BOOL idleForAWhile = now - self.firstSkipTime > self.idleInterval;
        if (idleForAWhile) {
            [self stopTimer];
        }
    }
}

- (void)setDelegateIfNeeded {
    if (self.didSetDelegate) {
        return;
    }
    
    self.didSetDelegate = YES;
    #if BKL_UG_BLE_ENABLED
    [[BluetoothShareManager shareManager] setManageDelegate:self];
    #endif
}


RCT_EXPORT_METHOD(isBluetoothOpen:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [self setDelegateIfNeeded];
    resolve(@(self.isBluetoothOn));
}

//
///**
// * 设备是否连接
// */
//export const isConnDevice = async (address): Promise<boolean> => {
RCT_EXPORT_METHOD(isConnDevice:(nonnull NSString *)address
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [self setDelegateIfNeeded];
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
    [self setDelegateIfNeeded];
    for (NSString *identifier in self.connectedDevices.copy) {
        CBPeripheral *device = [self.connectedDevices valueForKey:identifier];
        #if BKL_UG_BLE_ENABLED
        [[BluetoothShareManager shareManager] CancelDisconnectBlueToothDevice:device];
        #endif
    }
}

RCT_EXPORT_METHOD(openBle) {
    NSURL *url = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
    if ([[UIApplication sharedApplication] canOpenURL:url]) {
        NSDictionary *options = @{ UIApplicationOpenURLOptionUniversalLinksOnly: @(NO) };
        [[UIApplication sharedApplication] openURL:url
                                           options:options
                                 completionHandler:^(BOOL success) { }];
    }
}

///**
// * 扫描设备
// */
//export const startScanAndTime = (scanTime) => {
RCT_EXPORT_METHOD(startScanAndTime:(nonnull NSNumber *)scanTime
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [self setDelegateIfNeeded];
    
    #if BKL_UG_BLE_ENABLED
    // iOS并没有用到这个时间，Android才会用到
    [[BluetoothShareManager shareManager] ScanBlueToothDevice];
    #endif
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(stopScan:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [self setDelegateIfNeeded];
    
    #if BKL_UG_BLE_ENABLED
    [[BluetoothShareManager shareManager] StopScanBleDevice];
    #endif
    
    resolve([NSNull null]);
}

///**
// * 连接设备并接收数据
// */
//export const connectDevice = async (address) => {
RCT_EXPORT_METHOD(connectDevice:(nonnull NSString *)address
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [self setDelegateIfNeeded];
    
    CBPeripheral *peripheral = [self.scannedDevices valueForKey:address];
    if (peripheral) {
        #if BKL_UG_BLE_ENABLED
        [[BluetoothShareManager shareManager] ConnectBlueToothDevicePeripheral:peripheral];
        #endif
        
        resolve([NSNull null]);
    } else {
        reject(@"1", @"无法连接", nil);
    }
}

RCT_EXPORT_METHOD(getDeviceInfo:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    NSDictionary *info = [self nativeGetDeviceInfo];
    resolve(info);
}

- (nullable NSDictionary *)nativeGetDeviceInfo {
    [self setDelegateIfNeeded];
    
    #if BKL_UG_BLE_ENABLED
    BLE_TABLET_DEVICEINFO deviceInfo;
    [[BluetoothShareManager shareManager] bleGetDeviceInfo:&deviceInfo];
    if (deviceInfo.axisX.max != 0) {
        NSInteger maxX = deviceInfo.axisX.max;
        NSInteger maxY = deviceInfo.axisY.max;
        NSInteger maxPressure = deviceInfo.pressure;
        NSDictionary *info = @{
            @"maxX": @(maxX),
            @"maxY": @(maxY),
            @"maxPressure": @(maxPressure)
        };
        return info;
    }
    #endif
    
    return nil;
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

#if BKL_UG_BLE_ENABLED
- (NSDictionary *)createDataToSend:(BLE_DATAPACKET *)dataPacket {
    [self setDelegateIfNeeded];
    
    NSInteger penStatus = dataPacket->penstatus;
    
    CGFloat x = dataPacket->x;
    CGFloat y = dataPacket->y;
    NSMutableDictionary *data = [NSMutableDictionary dictionaryWithCapacity:10];
    [data setValue:@(x) forKey:@"x"];
    [data setValue:@(y) forKey:@"y"];
    [data setValue:@(dataPacket->pressure) forKey:@"pressure"];
    [data setValue:@(dataPacket->eventtype) forKey:@"eventType"];
    [data setValue:@(dataPacket->physical_key) forKey:@"physicalKey"];
    [data setValue:@(dataPacket->virtual_key) forKey:@"virtualKey"];
    [data setValue:@(dataPacket->keystatus) forKey:@"keyStatus"];
    [data setValue:@(penStatus) forKey:@"penStatus"];
    
    if (self.deviceInfo) {
        [data addEntriesFromDictionary:self.deviceInfo];
    }
    
    return data.copy;
}
#endif

- (void)sendConnectionDidChangeNotification:(NSDictionary *)body {
    [self sendEventWithName:UGConnectDeviceTypeNotification body:body];
    [[NSNotificationCenter defaultCenter] postNotificationName:UGBleConnectionDidChangeNotification object:body];
}

#pragma mark - BluetoothShareManagerDelegate

/**
 设备连接成功

 @param peripheral 连接成功对象
 */
- (void)BlueToothDidConnectPeripheral:(CBPeripheral *)peripheral {
    if (peripheral) {
        [self.connectedDevices setValue:peripheral
                                 forKey:peripheral.identifier.UUIDString];

        UGConnectionStatus status = UGConnectionStatusSuccess;
        NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
        [self sendConnectionDidChangeNotification:body];
    }
}

/**
 断开设备连接
 */
- (void)BlueToothDidDisconnectPeripheral:(CBPeripheral*) peripheral {
    if (peripheral) {
        [self.connectedDevices setValue:nil forKey:peripheral.identifier.UUIDString];

        UGConnectionStatus status = UGConnectionStatusDisconnected;
        NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
        [self sendConnectionDidChangeNotification:body];
    }
}

/**
 连接失败
 */
- (void)BlueToothDidFailToConnectPeripheral:(CBPeripheral *)peripheral
                                      error:(NSError *)error {
    if (peripheral) {
        [self.connectedDevices setValue:nil
                                 forKey:peripheral.identifier.UUIDString];

        UGConnectionStatus status = UGConnectionStatusFailure;
        NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
        [self sendConnectionDidChangeNotification:body];
    }
}

/**
 超时连接
 */
- (void)BlueToothDidTimeoutConnectPeripheral:(CBPeripheral *)peripheral
                                       error:(NSError *)error {
    if (peripheral) {
        [self.connectedDevices setValue:nil
                                 forKey:peripheral.identifier.UUIDString];

        UGConnectionStatus status = UGConnectionStatusFailure;
        NSDictionary *body = [self connectNotificationBodyWithPeripheral:peripheral connectionStatus:status];
        [self sendConnectionDidChangeNotification:body];
    }
}

/**
 扫描外设返回

 @param peripheral 扫描到的外设对象
 */
- (void)ScanBlueToothDevicePeripheral:(CBPeripheral*)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData {
    if (peripheral) {
        NSDictionary *body = @{
            @"address": peripheral.identifier.UUIDString,
            @"name": peripheral.name
        };
        [self.scannedDevices setValue:peripheral forKey:peripheral.identifier.UUIDString];
        [self sendEventWithName:UGScanBluetoothDeviceNotification body:body];
    }
}

#if BKL_UG_BLE_ENABLED

/**
 //模拟鼠标接收包

 @param manager manager
 @param dataPacket 模拟鼠标数据接收包
 */
- (void)manager:(BluetoothShareManager *)manager didReceviceDataPacket:(BLE_DATAPACKET *)dataPacket {
    if (self.deviceInfo == nil) {
        self.deviceInfo = [self nativeGetDeviceInfo];
    }
    
    if (self.deviceInfo == nil) {
        return;
    }
    
    NSInteger penStatus = dataPacket->penstatus;
    switch (penStatus) {
        case BLE_PenStatus_Down: {
            [self startTimerIfNeeded];
            
            self.dataToSend = [self createDataToSend:dataPacket];
            
            // 立即发送
            [self sendReceivedData];
            break;
        }
        case BLE_PenStatus_Up:
        case BLE_PenStatus_Hover: {
            self.dataToSend = [self createDataToSend:dataPacket];
            
            // 立即发送
            [self sendReceivedData];
            break;
        }
        case BLE_PenStatus_Move: {
            // 画的过程中省略一些事件
            self.dataToSend = [self createDataToSend:dataPacket];
            break;
        }
        case BLE_PenStatus_Leave: {
            self.dataToSend = [self createDataToSend:dataPacket];
            
            // 立即发送
            [self sendReceivedData];
            
            [self stopTimer];
            break;
        }
        default:
            break;
    }
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

#endif


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
