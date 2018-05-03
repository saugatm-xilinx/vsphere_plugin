package com.solarflare.vcp.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FtpUrlProcessor {

	public static String getEncodedURL(String url){
		String userName = getUserName(url);
		String pswd = getPassword(url);
		String encodedUser = encodeSpecialCharacters(userName);
		String encodedPswd = encodeSpecialCharacters(pswd);
		url = url.replace(userName, encodedUser);
		url = url.replace(pswd, encodedPswd);
		return url;
	}
	
	private static String getUserName(String url){
		String userName = null;
		if(url != null && !url.isEmpty()){
			int start = url.indexOf("://");
			int end = url.indexOf(":", start+3);
			userName = url.substring(start+3, end);
		}
		return userName;
	}
	
	private static String  getPassword(String url){
		String password = null;
		if(url != null && !url.isEmpty()){
			int start = url.lastIndexOf(":");
			int end = url.lastIndexOf("@");
			password = url.substring(start+1, end);
		}
		return password;
	}
	
	private static String encodeSpecialCharacters(String str){
		String encodedStr = null;
		try {
			encodedStr = URLEncoder.encode(str,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encodedStr; 
	}
}
