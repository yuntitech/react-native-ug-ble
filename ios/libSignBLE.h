#ifndef _libSignBLE_H
#define _libSignBLE_H

#ifndef DLL_EXPORT
#define DLL_EXPORT 
#endif // !DLL_EXPORT

//枚举类型定义
//事件状态枚举
enum BLE_EventType
{
	BLE_EventType_Pen = 1,
	BLE_EventType_Key = 2,
	BLE_EventType_Eraser = 3,
	BLE_EventType_ALL = 0xfe
};




//笔状态枚举
enum  BLE_PenStatus
{
	BLE_PenStatus_Hover,				//悬浮
	BLE_PenStatus_Down,					//按下
	BLE_PenStatus_Move,					//移动
	BLE_PenStatus_Up,					//抬起
	BLE_PenStatus_Leave					//离开
};

//按键状态枚举
enum BLE_KeyStatus
{
	BLE_KeyStatus_Up,					//按键抬起
	BLE_KeyStatus_Down					//按键按下
};

//设备连接状态
enum  BLE_DeviceStatus
{
	BLE_DeviceStatus_Disconnected,
	BLE_DeviceStatus_Connected,
	BLE_DeviceStatus_Sleep,//睡眠
	BLE_DeviceStatus_Awake//喚醒
};

//特征类型枚举
enum BLE_FeatureType
{
	BLE_FeatureType_Pen					//
};

//数位板X/Y轴范围
typedef		struct  tagBLE_AXIS
{
	unsigned long	min;
	unsigned long	max;
}BLE_AXIS,*PBLE_AXIS;

//数位板设备信息
typedef struct tagBLE_TABLET_DEVICEINFO
{
	BLE_AXIS			axisX;					//X轴范围
	BLE_AXIS			axisY;					//Y轴范围
	unsigned long		pressure;				//压感级别
	char				vendor[32];				//厂商名称
	char				product[32];			//产品名称
	unsigned long		version;				//驱动接口版本
	char				serialnum[32];			//设备序列号
}BLE_TABLET_DEVICEINFO,*PBLE_TABLET_DEVICEINFO;

//数位板数据包
typedef struct  tagBLE_DATAPACKET
{
	enum BLE_EventType		eventtype;				//事件类型	4
	unsigned short		physical_key;			//物理按键	2
	unsigned short		virtual_key;			//虚拟按键	2
	enum BLE_KeyStatus		keystatus;				//按键状态	4
	enum BLE_PenStatus		penstatus;				//笔尖状态	4
	unsigned short		x;						//x坐标		2
	unsigned short		y;						//y坐标		2
	unsigned short		pressure;				//压感		2
}BLE_DATAPACKET,*PBLE_DATAPACKET;

//设备连接状态
typedef struct  tagBLE_STATUSPACKET
{
	int			penAlive;
	int			penBattery;
	int			status;						//0  DISCONNECTED  1 CONNECTED  2 SLEEP  3 AWAKE
}BLE_STATUSPACKET,*PBLE_STATUSPACKET;

//笔特征设置
typedef struct  tagBLE_FEATURE_PEN
{
	bool				bIsSendEnabled;
	bool				bIsHoverEnabled;
	unsigned char		interval_X;
	unsigned char		interval_Y;
}BLE_FEATURE_PEN,*PBLE_FEATURE_PEN;


//typedef		int (CALLBACK * BLE_PACKDATAPROC)(BLE_DATAPACKET pktObj);
//typedef		int (CALLBACK * BLE_DEVNOTIFYPROC)(BLE_STATUSPACKET pktObj);

//////////////////////////////////////////////////////////////////////////
//返回值定义

#define			BLE_ERR_OK						0			//操作成功
#define			BLE_ERR_DEVICE_NOTFOUND			-1			//未找到可用设备
#define			BLE_ERR_DEVICE_OPENFAIL			-2			//设备打开失败
#define			BLE_ERR_DEVICE_NOTCONNECTED		-3			//设备未连接
#define			BLE_ERR_DEVICE_NORESPONSE		-4			//设备无应答

#define			BLE_ERR_INVALIDPARAM			-101		//无效参数
#define			BLE_ERR_READFAILED				-102		//读取失败
#define			BLE_ERR_WRITEFAILED				-103		//写入失败
#define			BLE_ERR_INVALIDKEY				-104		//无效Key

#ifdef __cplusplus
extern "C" {
#endif

	DLL_EXPORT	int		 bleOpenDevice();
	DLL_EXPORT	void	 bleCloseDevice();
	DLL_EXPORT	int		 bleGetDeviceInfo(BLE_TABLET_DEVICEINFO* lpDeviceInfo);
//	//DLL_EXPORT	long	 bleRegisterDataCallBack(BLE_PACKDATAPROC lpPackDataProc);
//	DLL_EXPORT	void	 bleUnregisterDataCallBack(long handler);
////	DLL_EXPORT	long	 bleRegisterDevNotifyCallBack(BLE_DEVNOTIFYPROC lpDevNotifyProc);
//	DLL_EXPORT	void	 bleUnregisterDevNotifyCallBack(long handler);
////	DLL_EXPORT	int		 bleSetStorage(long addr,LPVOID lpInput,int nSize,LPVOID lpKey,int nKeyLen);
//	DLL_EXPORT	int		 bleGetStorage(long addr,LPVOID lpOutput,int nSize,LPVOID lpKey,int nKeyLen);
//	DLL_EXPORT	int		 bleSetBTName(char* pStrName);
//	DLL_EXPORT	int		 bleGetBTName(char* pStrName,int nLen);
//	DLL_EXPORT	int		 bleSetFeature(BLE_FeatureType type,void* lpInfo);

#ifdef __cplusplus
}
#endif

#endif // !_libSignBLE_h
