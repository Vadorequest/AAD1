package com.iha.wcc.data;

/**
 * A Network item represents a Network object used in the list.
 * TODO description.
 */
public class Network {
	public String name;
	public String ip;

	public Network(String ip) {
		this.name = ip;
		this.ip = "";
	}
	
	public Network(String ip, String port) {
		this.name = ip;
		this.ip = port;
	}

	@Override
	public String toString() {
		return this.name+" ("+this.ip+")";
	}
}
