//
//  BluetoothShareManager.h
//  BluetoothMacDemo
//
//  Created by UGEE_MAC on 2018/2/3.
//  Copyright © 2018年 UGEE_MAC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import <UIKit/UIKit.h>
#include "libSignBLE.h"
@class BluetoothShareManager;



@protocol BluetoothShareManagerDelegate <NSObject>

/**
 设备连接成功

 @param peripheral 连接成功对象
 */
-(void)BlueToothDidConnectPeripheral:(CBPeripheral*) peripheral;


/**
 断开设备连接

 @param peripheral <#peripheral description#>
 */
-(void)BlueToothDidDisconnectPeripheral:(CBPeripheral*) peripheral;


/**
 连接失败

 @param peripheral <#peripheral description#>
 @param error <#error description#>
 */
-(void)BlueToothDidFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error;

/**
 超时连接

 @param peripheral <#peripheral description#>
 @param error <#error description#>
 */
-(void)BlueToothDidTimeoutConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error;

/**
 扫描外设返回

 @param peripheral 扫描到的外设对象
 */
-(void)ScanBlueToothDevicePeripheral:(CBPeripheral*) peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData;




/**
 //模拟鼠标接收包

 @param manager manager
 @param dataPacket 模拟鼠标数据接收包
 */
- (void)manager:(BluetoothShareManager *)manager didReceviceDataPacket:(BLE_DATAPACKET *)dataPacket ;


/**
 //模拟按键接收包

 @param manager <#manager description#>
 @param hotkeyIndex 按钮的标志
 @param keyStatus 按钮状态
 */
- (void)manager:(BluetoothShareManager *)manager didReceviceHotKeyDataPacket:(UInt32) hotkeyIndex status:(enum  BLE_KeyStatus) keyStatus;


/**
 获取修改蓝牙的名称

 @param manager <#manager description#>
 @param bleName BLE Update Name
 */
- (void)manager:(BluetoothShareManager *)manager didReceviceBleDeviceUpdateName:(NSString*) bleName;



@end


@interface BluetoothShareManager : NSObject
{
    id<BluetoothShareManagerDelegate> mDelegate;
    NSMutableData *nameData;
    BLE_TABLET_DEVICEINFO mDeviceInfo;
    BOOL updateDeviceNameFlag;
}




+(instancetype) shareManager;

-(void)setManageDelegate:(id) tempDelegate;

-(void)ScanBlueToothDevice;
-(void)StopScanBleDevice;
-(void)CancelDisconnectBlueToothDevice:(CBPeripheral*)deviceModel;
-(void)ConnectBlueToothDevicePeripheral:(CBPeripheral*)deviceMdel;
    

//
//获取设备信息
-(void)bleGetDeviceInfo:(BLE_TABLET_DEVICEINFO*) lpDeviceInfo;

//获取设备名称
-(void)sendGetDeviceNameCommand:(unsigned char*) bytes length:(int)dataLen;
//
-(void)sendUpdateDeviceNameCommand:(NSString*) updateName length:(int)dataLen;

//获取设备压感值,x,y值
-(void)sendGetPressureCommand:(unsigned char*) bytes length:(int)dataLen;

//获取设备串号
-(void)sendGetBleDeviceSerialNumber:(unsigned char*) bytes length:(int)dataLen;

-(void)sendVerificationRequest;


@end
