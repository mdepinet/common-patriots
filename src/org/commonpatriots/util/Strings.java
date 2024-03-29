package org.commonpatriots.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;


public final class Strings {

	public static String nullToEmpty(String str) {
		if (str == null) {
			return "";
		} else {
			return str;
		}
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || "".equals(str);
	}

	public static String formatForUser(String str) {
		if (isNullOrEmpty(str)) {
			return "";
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1).replaceAll("_", " ").toLowerCase();
	}

	public static String formatPhoneNumber(String phone) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try {
			PhoneNumber number = phoneUtil.parse(phone, "US");
			return phoneUtil.format(number, PhoneNumberFormat.NATIONAL);
		} catch (NumberParseException e) {
			e.printStackTrace();
			return phone;
		}
	}

	public static String formatZip(String zip) {
		if (!Validation.isZip(zip)) {
			System.err.println("Tried to format illegal zip code: " + zip);
			return zip;
		} else if (zip.matches("^\\d\\d\\d\\d\\d$")) {
			return zip;
		} else if (zip.matches("^\\d\\d\\d\\d\\d-\\d\\d\\d\\d$")) {
			return zip;
		} else {
			return zip.substring(0,5) + "-" + zip.substring(5);
		}
	}
}
