package com.example.springboot01.jna.NetSDKDemo;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.springboot01.jna.Acs.UserManage;
import com.example.springboot01.jna.entity.FaceInfo;
import com.example.springboot01.jna.utils.JnaProperRead;
import com.example.springboot01.jna.utils.StringUtils;
import com.sun.jna.Pointer;
import org.json.JSONException;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//布防回调函数
public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {

    private String ip;
    private HCNetSDK hCNetSDK;
    private int lUserID;


    public FMSGCallBack_V31() {
    }

    public FMSGCallBack_V31(String ip,HCNetSDK hCNetSDK,int lUserID) {
        this.ip = ip;
        this.hCNetSDK = hCNetSDK;
        this.lUserID = lUserID;
    }


    //报警信息回调函数
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        return true;
    }
    public void AlarmDataHandle(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        System.out.println("报警类型 lCommand:" + Integer.toHexString(lCommand));
        switch (lCommand) {
            case HCNetSDK.COMM_ALARM_ACS: //门禁主机报警信息
                HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
                strACSInfo.write();
                Pointer pACSInfo = strACSInfo.getPointer();
                pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
                strACSInfo.read();
                /**门禁事件的详细信息解析，通过主次类型的可以判断当前的具体门禁类型，例如（主类型：0X5 次类型：0x4b 表示人脸认证通过，
                        主类型：0X5 次类型：0x4c 表示人脸认证失败）*/
                //只收集刷脸成功数据
                if (!("5".equals(Integer.toHexString(strACSInfo.dwMajor)) && "4b".equals(Integer.toHexString(strACSInfo.dwMinor)))){
                    return;
                }

                //刷脸时间
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String faceTime = sf.format(new Date());
                FaceInfo faceInfo = new FaceInfo();
                String userNo = String.valueOf(strACSInfo.struAcsEventInfo.dwEmployeeNo);
                faceInfo.setUserNo(userNo);
                faceInfo.setFaceSwipingTime(faceTime);

                String ip = this.ip;
                String location = (String) JnaProperRead.dataMap.get(ip);
                faceInfo.setFaceSwipingAddress(location);

                try {
                    Map<String,Object> map = UserManage.SearchUserInfo(lUserID, userNo,hCNetSDK);
                    if (map.size() != 0){
                        JSONObject userInfoSearch = (JSONObject) map.get("UserInfoSearch");
                        JSONArray userInfo = (JSONArray) userInfoSearch.get("UserInfo");
                        if (userInfo != null){
                            JSONObject o = (JSONObject) userInfo.get(0);
                            String name = (String) o.get("name");
                            faceInfo.setUserName(StringUtils.toUTF8(name));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(faceInfo);

                //调用接口
                sendHttpInfo(faceInfo);

                System.out.println("【门禁主机报警信息】卡号：" + new String(strACSInfo.struAcsEventInfo.byCardNo).trim() + "，卡类型：" +
                        strACSInfo.struAcsEventInfo.byCardType + "，报警主类型：" + Integer.toHexString(strACSInfo.dwMajor) + "，报警次类型：" + Integer.toHexString(strACSInfo.dwMinor));
                System.out.println("工号1："+strACSInfo.struAcsEventInfo.dwEmployeeNo);
                //温度信息（如果设备支持测温功能，人脸温度信息从NET_DVR_ACS_EVENT_INFO_EXTEND_V20结构体获取）
//                if (strACSInfo.byAcsEventInfoExtendV20 == 1) {
//                    HCNetSDK.NET_DVR_ACS_EVENT_INFO_EXTEND_V20 strAcsInfoExV20 = new HCNetSDK.NET_DVR_ACS_EVENT_INFO_EXTEND_V20();
//                    strAcsInfoExV20.write();
//                    Pointer pAcsInfoExV20 = strAcsInfoExV20.getPointer();
//                    pAcsInfoExV20.write(0, strACSInfo.pAcsEventInfoExtendV20.getByteArray(0, strAcsInfoExV20.size()), 0, strAcsInfoExV20.size());
//                    strAcsInfoExV20.read();
//                    System.out.println("实时温度值：" + strAcsInfoExV20.fCurrTemperature);
//                }
//                //考勤状态
//                if (strACSInfo.byAcsEventInfoExtend == 1) {
//                    HCNetSDK.NET_DVR_ACS_EVENT_INFO_EXTEND strAcsInfoEx = new HCNetSDK.NET_DVR_ACS_EVENT_INFO_EXTEND();
//                    strAcsInfoEx.write();
//                    Pointer pAcsInfoEx = strAcsInfoEx.getPointer();
//                    pAcsInfoEx.write(0, strACSInfo.pAcsEventInfoExtend.getByteArray(0, strAcsInfoEx.size()), 0, strAcsInfoEx.size());
//                    strAcsInfoEx.read();
//                    System.out.println("考勤状态：" + strAcsInfoEx.byAttendanceStatus);
//                    System.out.println("工号2："+new String(strAcsInfoEx.byEmployeeNo).trim());
//                }
//
//                /**
//                 * 报警时间
//                 */
//                String year=Integer.toString(strACSInfo.struTime.dwYear);
//                String SwipeTime=year+strACSInfo.struTime.dwMonth+strACSInfo.struTime.dwDay+strACSInfo.struTime.dwHour+strACSInfo.struTime.dwMinute+
//                        strACSInfo.struTime.dwSecond;
//                if (strACSInfo.dwPicDataLen > 0) {
////                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
////                    String newName = sf.format(new Date());
////                    System.out.println("打卡时间："+newName);
//                    FileOutputStream fout;
//                    try {
//                        /**
//                         * 人脸保存路径
//                         */
//                        String filename = "../pic/" + SwipeTime + "_ACS_Event_" + new String(strACSInfo.struAcsEventInfo.byCardNo).trim() + ".jpg";
//                        fout = new FileOutputStream(filename);
//                        //将字节写入文件
//                        long offset = 0;
//                        ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(offset, strACSInfo.dwPicDataLen);
//                        byte[] bytes = new byte[strACSInfo.dwPicDataLen];
//                        buffers.rewind();
//                        buffers.get(bytes);
//                        fout.write(bytes);
//                        fout.close();
//                    } catch (FileNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
                break;
            case HCNetSDK.COMM_ID_INFO_ALARM: //身份证信息
                HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
                strIDCardInfo.write();
                Pointer pIDCardInfo = strIDCardInfo.getPointer();
                pIDCardInfo.write(0, pAlarmInfo.getByteArray(0, strIDCardInfo.size()), 0, strIDCardInfo.size());
                strIDCardInfo.read();
                System.out.println("报警主类型：" + Integer.toHexString(strIDCardInfo.dwMajor) + "，报警次类型：" + Integer.toHexString(strIDCardInfo.dwMinor));
                /**
                 * 身份证信息
                 */
                String IDnum=new String(strIDCardInfo.struIDCardCfg.byIDNum).trim();
                System.out.println("【身份证信息】：身份证号码：" + IDnum+ "，姓名：" +
                        new String(strIDCardInfo.struIDCardCfg.byName).trim() + "，住址："+new String(strIDCardInfo.struIDCardCfg.byAddr));

                /**
                 * 报警时间
                 */
                String year1=Integer.toString(strIDCardInfo.struSwipeTime.wYear);
                String SwipeTime1=year1+strIDCardInfo.struSwipeTime.byMonth+strIDCardInfo.struSwipeTime.byDay+strIDCardInfo.struSwipeTime.byHour+strIDCardInfo.struSwipeTime.byMinute+
                        strIDCardInfo.struSwipeTime.bySecond;
                /**
                 * 保存图片
                 */
                //身份证图片
                if (strIDCardInfo.dwPicDataLen>0||strIDCardInfo.pPicData!=null)
                {
//                    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
//                    String nowtime = sf.format(new Date());
                    FileOutputStream fout;
                    try {
                        String filename = "../pic/" + SwipeTime1 + "_"+IDnum  + ".jpg";
                        fout = new FileOutputStream(filename);
                        //将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = strIDCardInfo.pPicData.getByteBuffer(offset, strIDCardInfo.dwPicDataLen);
                        byte[] bytes = new byte[strIDCardInfo.dwPicDataLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (strIDCardInfo.dwCapturePicDataLen>0||strIDCardInfo.pCapturePicData!=null)
                {
//                    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
//                    String nowtime = sf.format(new Date());
                    FileOutputStream fout;
                    try {
                        /**
                         * 人脸图片保存路径
                         */
                        String filename = "../pic/" + SwipeTime1 + "_CapturePic_" +".jpg";
                        fout = new FileOutputStream(filename);
                        //将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = strIDCardInfo.pCapturePicData.getByteBuffer(offset, strIDCardInfo.dwCapturePicDataLen);
                        byte[] bytes = new byte[strIDCardInfo.dwCapturePicDataLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
                //门禁仅测温事件解析
            case HCNetSDK.COMM_VCA_ALARM:
                ByteBuffer dataBuffer = pAlarmInfo.getByteBuffer(0, dwBufLen);
                byte[] dataByte = new byte[dwBufLen];
                dataBuffer.rewind();
                dataBuffer.get(dataByte);
                //报警事件json报文
                System.out.println("仅测温报警事件："+new String(dataByte));
                break;
            default:
                System.out.println("报警类型" + Integer.toHexString(lCommand));
                break;
        }
    }

    private void sendHttpInfo(FaceInfo faceInfo) {
//        HttpURLConnection con = null;
//        BufferedReader buffer = null;
//        StringBuffer resultBuffer = null;
//
//        try {
//            System.out.println("开始调用http接口");
//            URL url = new URL("http://10.186.96.5:23308/cqcs/warning/platform/api/attendance/attendancePush");
////            URL url = new URL("http://93.37.0.190:18089/cqcs/warning/platform/api/getJna3");
//            //得到连接对象
//            con = (HttpURLConnection) url.openConnection();
//            //设置请求类型
//            con.setRequestMethod("POST");
//            //设置Content-Type，此处根据实际情况确定
//            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            //允许写出
//            con.setDoOutput(true);
//            //允许读入
//            con.setDoInput(true);
//            //不使用缓存
//            con.setUseCaches(false);
//            OutputStream os = con.getOutputStream();
//
//
////            Map paraMap = new HashMap();
////            paraMap.put("type", "wx");
////            paraMap.put("mchid", "10101");
//            //组装入参
//            os.write(("userNo="+faceInfo.getUserNo()+"&userName="+faceInfo.getUserName()
//                    +"&faceSwipingTime="+faceInfo.getFaceSwipingTime()+"&faceSwipingAddress="+faceInfo.getFaceSwipingAddress()+"").getBytes());
//
////            os.write("".getBytes());
//
//            //得到响应码
//            int responseCode = con.getResponseCode();
//            System.out.println("响应码："+ responseCode);
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                //得到响应流
//                InputStream inputStream = con.getInputStream();
//                //将响应流转换成字符串
//                resultBuffer = new StringBuffer();
//                String line;
//                buffer = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
////                while ((line = buffer.readLine()) != null) {
////                    resultBuffer.append(line);
////                }
//
//                while (true){
//                    if((line = buffer.readLine()) != null)
//                        resultBuffer.append(line);
//                    else
//                        break;
//                }
//
//                System.out.println("result:" + resultBuffer.toString());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



        String url = "http://10.186.96.5:18089/cqcs/warning/platform/api/warning/attendance/attendancePush";//指定URL
//        String url = "http://93.37.1.190:18089/cqcs/warning/platform/api/getJna3";//指定URL
        Map<String, Object> map = new HashMap<>();//存放参数
        map.put("userNo", faceInfo.getUserNo());
        map.put("userName", faceInfo.getUserName());
        map.put("faceSwipingTime", faceInfo.getFaceSwipingTime());
        map.put("faceSwipingAddress", faceInfo.getFaceSwipingAddress());
        map.put("orgNo", JnaProperRead.orgNo);
//        HashMap<String, String> headers = new HashMap<>();//存放请求头，可以存放多个请求头
//        headers.put("xxx", xxx);
        //发送get请求并接收响应数据
//        String result= HttpUtil.createGet(url).addHeaders(headers).form(map).execute().body();
        //发送post请求并接收响应数据
//        String result= HttpUtil.createPost(url).addHeaders(headers).form(map).execute().body();
        String result= HttpUtil.createPost(url).form(map).execute().body();
        System.out.println("调用预警平台返回数据："+result);

    }



}
