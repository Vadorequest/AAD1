package com.iha.wcc.data;

/**
 * A device item representing an item of the list.
 */
public class DeviceItem {
	public String id;
	public String name;
	public String details;

	public DeviceItem(String id, String content) {
		this.id = id;
		this.name = content;
		this.details = "";
	}
	
	public DeviceItem(String id, String content, String details) {
		this.id = id;
		this.name = content;
		this.details = details;
	}

	@Override
	public String toString() {
		return name;
	}
}
