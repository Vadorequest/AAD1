package com.iha.wcc.data;

/**
 * A Network item represents a Network object used in the list.
 * TODO description.
 */
public class Network {
	public String name;
	public String ip;
	public String gateway;
	public String subnetMask;

	public Network(String ip) {
		this.name = ip;
		this.ip = "";
	}
	
	public Network(String ip, String port, String gateway, String subnetMask) {
		this.name = ip;
		this.ip = port;
		this.gateway = gateway;
		this.subnetMask = subnetMask;
	}

	@Override
	public String toString() {
		return 
				this.name + System.getProperty ("line.separator") + 
				" Host IP: " + this.ip + System.getProperty ("line.separator") +
				" Gateway: " + this.gateway + System.getProperty ("line.separator") +
				" Subnet mask: " + this.subnetMask;
	}
}
