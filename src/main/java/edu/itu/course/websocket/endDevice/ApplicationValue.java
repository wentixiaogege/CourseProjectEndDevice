package edu.itu.course.websocket.endDevice;

import java.util.Hashtable;

/**
 * @author  作者 E-mail:
 * @date 创建时间：Sep 15, 2015 1:14:54 PM
 * @version 1.0
 * @parameter 
 * @since
 * @return
 */
public class ApplicationValue {
	
	private static Hashtable<String, Integer> licenceKeyHashTable;
	
	static {
		licenceKeyHashTable = new Hashtable<String, Integer>();
		licenceKeyHashTable.put("frequency", 1000); //default 1 seconds
	}
	
	public static long getLicenKeyValue() {
		long value = Long.parseLong(licenceKeyHashTable.get("frequency").toString());
		return value;
	}
	
	public static void setLicenKeyValue(int val) {
		licenceKeyHashTable.put("frequency", val);
	}
	
}

