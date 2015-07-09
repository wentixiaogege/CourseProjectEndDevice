package org.CourseProjectEndDevice;

import javax.xml.bind.DatatypeConverter;

import edu.itu.course.PropertyReading;

public class TestReadingAddress {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyReading propertyReading = new PropertyReading();
		
		int msb= DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[0];
		int lsb= DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[1];
		
		System.out.println("DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())"+
				DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress()).toString()+"\n"+
				msb+"\n"+
				lsb);
		
	}

}
