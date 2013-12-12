package com.iha.wcc.data;

/**
 * A device item representing an item of the list.
 * @deprecated Not used anymore but could be useful if the application was improved with the selection of the network as main page.
 */
public class Device {
	public String ip;
	public int port;

	public Device(String ip) {
		this.ip = ip;
		this.port = 0;
	}
	
	public Device(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	@Override
	public String toString() {
		return this.ip+":"+this.port;
	}
}
