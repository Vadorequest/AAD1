package com.iha.wcc.session;

public class CarSession {
	// Temporally data, I don't know yet what I'll need in the future, maybe nothing. 
	private static String ipAddress;
	private static String port;
	
	public static String getIpAddress() {
		return ipAddress;
	}
	public static void setIpAddress(String ipAddress) {
		CarSession.ipAddress = ipAddress;
	}
	public static String getPort() {
		return port;
	}
	public static void setPort(String port) {
		CarSession.port = port;
	}
	
}
