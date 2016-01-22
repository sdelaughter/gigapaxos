package edu.umass.cs.gigapaxos.examples.etherpad;

import java.util.Calendar;
import java.util.TimeZone;

public class PrintTime {
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		long t = calendar.getTimeInMillis();
		System.out.println(t);
	}
}