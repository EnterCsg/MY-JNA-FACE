package com.example.springboot01.jna.Acs;


import com.example.springboot01.jna.Commom.osSelect;
import com.example.springboot01.jna.NetSDKDemo.FMSGCallBack_V31;
import com.example.springboot01.jna.NetSDKDemo.HCNetSDK;
import com.sun.jna.Native;
import com.sun.jna.Pointer;


public class AcsMain extends Thread{

	HCNetSDK hCNetSDK = null;
	int lUserID = -1;//用户句柄
	int lAlarmHandle = -1;
	int lListenHandle = -1;
	FMSGCallBack_V31 fMSFCallBack_V31 = null;
	int iCharEncodeType = 0;//设备字符集

	public String ip;

	public AcsMain(){}

	public AcsMain(String ip){
		this.ip = ip;
	}

	/**
	 * @param
	 */
	public void run() {

		System.out.println("ip为:"+ip+"，开始运行！");

		if(hCNetSDK == null)
		{
			if(!CreateSDKInstance())
			{
				System.out.println("Load SDK fail");
				return;
			}
		}
		//linux系统建议调用以下接口加载组件库
		if (osSelect.isLinux())
		{
			HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
			HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
			//这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
			String strPath1 = System.getProperty("user.dir") + "/lib/libcrypto.so.1.1";
			String strPath2 = System.getProperty("user.dir") + "/lib/libssl.so.1.1";

			System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
			ptrByteArray1.write();
			hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

			System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
			ptrByteArray2.write();
			hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());

			String strPathCom = System.getProperty("user.dir") + "/lib";
			HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
			System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
			struComPath.write();
			hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
		}

		hCNetSDK.NET_DVR_Init();
		boolean i= hCNetSDK.NET_DVR_SetLogToFile(3, "..//sdklog", false);

		Boolean flag = Login(ip,"admin","gwkq1234",(short) 8000);	//登陆
		if (flag){
			System.out.println("ip地址为："+ip+"的设备登录成功");
			//设置报警回调函数
			if (fMSFCallBack_V31 == null) {
				fMSFCallBack_V31 = new FMSGCallBack_V31(ip,hCNetSDK,lUserID);
				Pointer pUser = null;
				if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
					System.out.println("ip为:"+ip+"设置回调函数失败!");
				} else {
					System.out.println("ip为:"+ip+"设置回调函数成功!");
				}
			}
			Alarm.SetAlarm(lUserID,ip,hCNetSDK,lAlarmHandle);//报警布防
//			Alarm.StartListen(ip,lListenHandle,hCNetSDK,fMSFCallBack_V31);//报警监听
			while(true){
				try {
					Thread.sleep(3000000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
/*
		MutilCard.setGroupCfg(lUserID);
		MutilCard.setMultiCardCfg(lUserID);
*/

		/**
		 * 人员管理模块
		 */
//		UserManage.SearchUserInfo(lUserID);		//查询所有人员
//		UserManage.SetCardTemplate(lUserID,2);
//		UserManage.deleteUserInfo(lUserID);
//		UserManage.AddUserInfo(lUserID,"ceshi1");    //添加人员

		/**
		 * 人脸管理模块
		 */
/*		FaceManage.SearchFaceInfo(lUserID,"tset123");
		FaceManage.AddFaceByBinary(lUserID,"ceshi1");
		FaceManage.AddFaceByUrl(lUserID,"test111");
		FaceManage.DeleteFaceInfo(lUserID,"test111");*/
//		FaceManage.CaptureFaceInfo(lUserID);

		/**
		 * 卡号管理模块
		 */
//		CardManage.searchCardInfo(lUserID,"ceshi1");
//		CardManage.addCardInfo(lUserID,"12345");
//		CardManage.searchCardInfo(lUserID,"test111");
//		CardManage.searchAllCardInfo(lUserID);
//		CardManage.deleteCardInfo(lUserID,"111");
//		CardManage.deleteAllCardInfo(lUserID);
//		CardManage.getAllCardNumber(lUserID);
		/**
		 * 指纹管理模块
		 */
//		FingerManage.fingerCapture(lUserID);
//		FingerManage.fingerCpaureByisapi(lUserID);
/*		FingerManage.fingerCpaureByisapi(lUserID);
		eventSearch.SearchAllEvent(lUserID);
		FingerManage.SearchFingerInfo(lUserID,"2222");
		*指纹数据的BASE64编码
		String fingerdata="";
		FingerManage.setOneFinger(lUserID,"zhangsan",fingerdata);
		FingerManage.deleteFinger(lUserID,"zhangsan");*/

		/**
		 * 报警布防模块
		 */
//		Alarm.SetAlarm(lUserID,ip,hCNetSDK);//报警布防
//		Alarm.StartListen();//报警监听

		//根据工号获取姓名
//		UserManage.SearchUserInfo(lUserID,"123");


		/**
		 * 增加sleep时间，保证程序一直运行，+

		 */
//		Thread.sleep(3000000);

		/**
		 * 撤防，端口监听，注销设备，释放SDK
		 */
//		AcsMain.Logout();
		
	}


	/**
	 * 设备登录
	 * @param ipadress IP地址
	 * @param user  用户名
	 * @param psw  密码
	 * @param port 端口，默认8000
	 */
	public Boolean Login(String ipadress, String user, String psw, short port) {
		//注册
		HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
		String m_sDeviceIP = ipadress;//设备ip地址
		m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
		System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());
		String m_sUsername = user;//设备用户名
		m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
		System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());
		String m_sPassword = psw;//设备密码
		m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
		System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());
		m_strLoginInfo.wPort = port; //sdk端口
		m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是


//		byte byUseTransport;
		m_strLoginInfo.byUseTransport=0;
		m_strLoginInfo.byProxyType=0;
		m_strLoginInfo.byLoginMode=2;
		m_strLoginInfo.byHttps=2;
		m_strLoginInfo.iProxyID=0;
		m_strLoginInfo.byVerifyMode=0;
//		m_strLoginInfo.byRes2=0;




		m_strLoginInfo.write();
		HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息
		lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
		if (lUserID == -1) {
			System.out.println("ip为:"+ipadress+"登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
			return false;
		} else {
			System.out.println("ip为:"+ipadress+"登录成功！");
			m_strDeviceInfo.read();
			iCharEncodeType = m_strDeviceInfo.byCharEncodeType;
			return true;

		}
	}


	/**
	 * 登出操作
	 *
	 */
	public void Logout(String ip){

		/**退出之前判断布防监听状态，并做撤防和停止监听操作*/
	if (lAlarmHandle >= 0){
			if (!hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle)){
				System.out.println("ip为:"+ip+"撤防失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
			}else{
				System.out.println("ip为:"+ip+"撤防成功！！！");
			}
		}
		if (lListenHandle >= 0){
			if (!hCNetSDK.NET_DVR_StopListen_V30(lListenHandle)){
				System.out.println("取消监听失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
			}else{
				System.out.println("停止监听成功！！！");
			}
		}

		/**登出和清理，释放SDK资源*/
		if (lUserID>=0)
		{
			hCNetSDK.NET_DVR_Logout(lUserID);
		}
		hCNetSDK.NET_DVR_Cleanup();
	}



	/**
	 * 根据不同操作系统选择不同的库文件和库路径
	 * @return
	 */
	private boolean CreateSDKInstance()
	{
		if(hCNetSDK == null)
		{
			synchronized (HCNetSDK.class)
			{
				String strDllPath = "";
				try
				{
					//System.setProperty("jna.debug_load", "true");
					if(osSelect.isWindows())
						//win系统加载库路径
						strDllPath = System.getProperty("user.dir") + "\\lib\\HCNetSDK.dll";
					else if(osSelect.isLinux())
						//Linux系统加载库路径
						strDllPath = System.getProperty("user.dir") + "/lib/libhcnetsdk.so";
					hCNetSDK = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);
				}catch (Exception ex) {
					System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
					return false;
				}
			}
		}
		return true;
	}



}//AcsMain  Class结束
