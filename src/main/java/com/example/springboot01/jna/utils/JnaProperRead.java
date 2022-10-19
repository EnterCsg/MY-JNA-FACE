package com.example.springboot01.jna.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JnaProperRead {

    public static Map<String,Object> dataMap;

    public static String orgNo;

    static {
        Map<String,Object> map = new HashMap<>();
        InputStream inputStream = JnaProperRead.class.getClassLoader().getResourceAsStream("jna.properties");
        Properties properties = new Properties();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            properties.load(bf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String ip_locations = properties.getProperty("ip-location");
        String orgno = properties.getProperty("orgNo");
        if (ip_locations !=null && !"".equals(ip_locations)){
            if (ip_locations.contains(";")){
                String[] ip_location = ip_locations.split(";");
                for (String ip : ip_location) {
                    String[] ip_loArray = ip.split(",");
                    map.put(ip_loArray[0],ip_loArray[1]);
                }
            } else {
                String[] ip_loArray = ip_locations.split(",");
                map.put(ip_loArray[0],ip_loArray[1]);
            }
        }
        dataMap = map;
        orgNo = orgno;
    }

//    public static Map<String,Object> getJnaData(){
//        Map<String,Object> map = new HashMap<>();
//        InputStream inputStream = JnaProperRead.class.getClassLoader().getResourceAsStream("jna.properties");
//        Properties properties = new Properties();
//        try {
//            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//            properties.load(bf);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String ip_locations = properties.getProperty("ip-location");
//        if (ip_locations !=null && !"".equals(ip_locations)){
//            if (ip_locations.contains(";")){
//                String[] ip_location = ip_locations.split(";");
//                for (String ip : ip_location) {
//                    String[] ip_loArray = ip.split(",");
//                    map.put(ip_loArray[0],ip_loArray[1]);
//                }
//            } else {
//                String[] ip_loArray = ip_locations.split(",");
//                map.put(ip_loArray[0],ip_loArray[1]);
//            }
//        }
//        return map;
//    }
//
//    public static String getOrgNo(){
//        InputStream inputStream = JnaProperRead.class.getClassLoader().getResourceAsStream("jna.properties");
//        Properties properties = new Properties();
//        try {
//            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//            properties.load(bf);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String orgNo = properties.getProperty("orgNo");
//        return orgNo;
//    }


}
