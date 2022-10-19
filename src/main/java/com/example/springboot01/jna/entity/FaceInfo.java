package com.example.springboot01.jna.entity;

import java.io.Serializable;

public class FaceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工编号 */
    private String userNo;

    /** 员工姓名 */
    private String userName;

    /** 刷脸时间 */
    private String faceSwipingTime;

    /** 刷脸地点 */
    private String faceSwipingAddress;

    private String orgNo;

    public String getOrgNo() {
        return orgNo;
    }

    public void setOrgNo(String orgNo) {
        this.orgNo = orgNo;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFaceSwipingTime() {
        return faceSwipingTime;
    }

    public void setFaceSwipingTime(String faceSwipingTime) {
        this.faceSwipingTime = faceSwipingTime;
    }

    public String getFaceSwipingAddress() {
        return faceSwipingAddress;
    }

    public void setFaceSwipingAddress(String faceSwipingAddress) {
        this.faceSwipingAddress = faceSwipingAddress;
    }

    @Override
    public String toString() {
        return "FaceInfo{" +
                "userNo='" + userNo + '\'' +
                ", userName='" + userName + '\'' +
                ", faceSwipingTime='" + faceSwipingTime + '\'' +
                ", faceSwipingAddress='" + faceSwipingAddress + '\'' +
                '}';
    }
}
