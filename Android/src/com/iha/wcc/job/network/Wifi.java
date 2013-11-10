package com.iha.wcc.job.network;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class Wifi {
	private WifiManager wifiManager;
	private DhcpInfo dhcpInfo;
	private WifiInfo wifiInfo;
	
	private String networkName;
	private String macAddress;
	private String dns1;
	private String dns2;
	private String leaseDuration;

	private int gateway;
	private int subnetMask;
	private int ipAddress;
	private int serverIpAddress;
	
	/**
	 * Retrieve all information about the current network from a WifiManager instance.
	 * @param wifiManager The current network.
	 * @return Wifi Contains all information about the wifi.
	 */
	public Wifi getInfo(WifiManager wifiManager){
		this.wifiManager = wifiManager;
		this.dhcpInfo = wifiManager.getDhcpInfo();
		this.wifiInfo = wifiManager.getConnectionInfo();
		
		this.networkName = wifiInfo.getSSID();
		this.macAddress = wifiInfo.getMacAddress();
		this.dns1 = String.valueOf(dhcpInfo.dns1);
		this.dns2 = String.valueOf(dhcpInfo.dns2); 
		this.leaseDuration = String.valueOf(dhcpInfo.leaseDuration);
		   
		this.gateway = dhcpInfo.gateway;         
		this.subnetMask = dhcpInfo.netmask;
		this.ipAddress = dhcpInfo.ipAddress; 
		this.serverIpAddress = dhcpInfo.serverAddress;
        
		return this;
	}
	
	// Getters.

	public WifiManager getWifiManager() {
		return wifiManager;
	}

	public DhcpInfo getDhcpInfo() {
		return dhcpInfo;
	}

	public WifiInfo getWifiInfo() {
		return wifiInfo;
	}

	public String getDns1() {
		return dns1;
	}

	public String getDns2() {
		return dns2;
	}

	public String getLeaseDuration() {
		return leaseDuration;
	}

	public String getGateway() {
		return Formatter.formatIpAddress(gateway);
	}

	public String getSubnetMask() {
		return Formatter.formatIpAddress(subnetMask);
	}

	public String getIpAddress() {
		return Formatter.formatIpAddress(ipAddress);
	}

	public String getServerIpAddress() {
		return Formatter.formatIpAddress(serverIpAddress);
	}

	public String getNetworkName() {
		return networkName;
	}

	public String getMacAddress() {
		return macAddress;
	}
}
