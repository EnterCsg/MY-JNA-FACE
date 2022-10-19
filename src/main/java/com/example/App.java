package com.example;

import com.example.springboot01.jna.Acs.AcsMain;
import com.example.springboot01.jna.utils.JnaProperRead;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args){

		//获取jna.properties文件中的数据。
		Map<String, Object> jnaData = JnaProperRead.dataMap;
		for(String key:jnaData.keySet()){
			new AcsMain(key).start();
		}
    }
}
