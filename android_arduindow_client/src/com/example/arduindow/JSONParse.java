package com.example.arduindow;

public class JSONParse {
	String [] vals;
	int loaded = 0;
	
	public String getValue (String key) {
		if (loaded == 0) {
			return "";
		}
		for (int i = 0; i < vals.length; i++) {
			String []t = vals[i].split (": ");
			if (t[0].substring(t[0].indexOf("\"")+1,t[0].lastIndexOf("\"")).equals (key)) {
				return t[1].substring(t[1].indexOf("\"")+1,t[1].lastIndexOf("\""));
			}
		}
		return "";
	}
	
	public JSONParse (String s) {
		if (s.indexOf("\"") != -1) {
			vals = s.substring (s.indexOf("\""),s.lastIndexOf("\"")+1).replace("\t","").split(",\n");
			loaded = 1;
		}
	}
}
