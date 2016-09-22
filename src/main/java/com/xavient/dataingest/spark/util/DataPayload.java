package com.xavient.dataingest.spark.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class DataPayload implements Serializable {

	public List<String> payload = new ArrayList<String>();

	public List<String> getPayload() {
		return payload;
	}

	public void setPayload(List<String> payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {

		String output = "";

		for (String s : payload) {
			output = output + s.trim() + "|";

		}
		output = StringUtils.removeEndIgnoreCase(output, "|")+ "\n";
		return output.trim();
	}
}