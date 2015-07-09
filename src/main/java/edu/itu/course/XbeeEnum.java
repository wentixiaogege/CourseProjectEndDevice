package edu.itu.course;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public enum XbeeEnum {
	/**
	 * 
	 */
	RELAY_ON ("relayOn"),
	RELAY_ON_DONE ("relayOnDone"),
	/**
	 * 
	 */
	RELAY_OFF("relayOff"),
	RELAY_OFF_DONE("relayOffDone"),
	/**
	 * 
	 */
	READING ("reading"),
	READING_DONE ("readingDone"),
	
	UNKNOWN ("unknown"),
	/**
	 * This is returned if an error occurs during packet parsing and does not correspond to a XBee API ID.
	 */
	ERROR_RESPONSE ("error");
	
	private static final Map<String,XbeeEnum> lookup = new HashMap<String,XbeeEnum>();
	
	static {
		for(XbeeEnum s : EnumSet.allOf(XbeeEnum.class)) {
			lookup.put(s.getValue(), s);
		}
	}
	
	public static XbeeEnum get(String value) { 
		return lookup.get(value); 
	}
	
    private final String value;
    
    private XbeeEnum(String value) {
        this.value = value;
    }

	public String getValue() {
		return value;
	}
}
