package org.commonpatriots.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonpatriots.CPException;
import org.commonpatriots.proto.CPData.State;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;


public final class Validation {
	private static final Pattern emailPattern =
			Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
	private static final Pattern addressPattern = Pattern.compile("\\d+ [\\w ]+\\.?");
	private static final Pattern zipPattern = Pattern.compile("\\d\\d\\d\\d\\d(-?\\d\\d\\d\\d)?");
	private static final Pattern cityPattern = Pattern.compile("[\\w ]+");
	private static final Pattern namePattern = Pattern.compile("[\\w ]+");
	private static final Pattern colorPattern = Pattern.compile("#[\\dA-Fa-f]{6}");
	// This urlPattern is definitely not perfect, but it will rule out a lot of crap at least
	private static final Pattern urlPattern = Pattern.compile(
			"^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?" // URL before parameters
			+ "(([?&][\\w \\.-]+=[\\w \\.-]+)+)?$" // Possible parameters
			, Pattern.CASE_INSENSITIVE);

	public static boolean isPhoneNumber(String phone) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try {
			return phoneUtil.isValidNumber(phoneUtil.parse(phone, "US"));
		} catch (NumberParseException e) {
			return false;
		}
	}

	public static boolean isName(String name) {
		return namePattern.matcher(name).matches();
	}

	public static boolean isEmailAddress(String email) {
		return emailPattern.matcher(email).matches();
	}

	public static boolean isAddress(String address) {
		return addressPattern.matcher(address).matches();
	}

	public static boolean isZip(String zip) {
		return zipPattern.matcher(zip).matches();
	}

	public static int parseZip(String zip) throws CPException {
		if (!isZip(zip)) {
			throw new CPException("Cannot parse " + zip + " as zip code!");
		}
		return Integer.parseInt(zip.replaceAll("-", ""));
	}

	public static boolean isCity(String city) {
		return cityPattern.matcher(city).matches();
	}

	public static boolean isAddressExtended(String address) {
		Matcher addrMatcher = addressPattern.matcher(address);
		String remaining = address;
		if (addrMatcher.find()) {
			remaining = address.substring(addrMatcher.end()).trim();
			if (remaining.startsWith(",")) {
				remaining = remaining.substring(1).trim();
			}
			if (Strings.isNullOrEmpty(remaining)) {
				return true;
			}
			Matcher cityMatcher = cityPattern.matcher(remaining);
			if (cityMatcher.find()) {
				remaining = remaining.substring(cityMatcher.end()).trim();
				if (remaining.startsWith(",")) {
					remaining = remaining.substring(1).trim();
				}
				if (Strings.isNullOrEmpty(remaining)) {
					return true;
				}
				String[] stateZip = remaining.split("[,\\s]");
				String state = stateZip[0].trim();
				try {
					State.valueOf(state);
				} catch (IllegalArgumentException ex) {
					return false; // Invalid state
				}
				if (stateZip.length == 1) {
					return true;
				}
				String zip = stateZip[1].trim();
				return isZip(zip);
			}
		}
		return false;
	}

	public static boolean isColor(String color) {
		return colorPattern.matcher(color).matches();
	}

	public static boolean isURL(String url) {
		return urlPattern.matcher(url).matches();
	}
}
