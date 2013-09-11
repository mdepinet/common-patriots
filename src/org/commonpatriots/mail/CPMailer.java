package org.commonpatriots.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.commonpatriots.util.CPUtil;
import org.commonpatriots.util.Pair;
import org.commonpatriots.util.Strings;

public class CPMailer {
	/*VisibleForTesting*/ static final String ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT =
			"Add %s as a coordinator for unit %s";
	/*VisibleForTesting*/ static final String ADD_ADMINISTRATOR_SUBJECT_FORMAT =
			"Add %s as an administrator for Common Patriots";
	private static final String emailRegexEscaped = "\\\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,4}\\\\b";
	private static final Pattern ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_PATTERN =
			Pattern.compile(ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT.replaceFirst("%s", emailRegexEscaped)
					.replaceAll("%s", "[\\\\w\\\\d ]+"));
	private static final Pattern ADD_ADMINISTRATOR_SUBJECT_PATTERN =
			Pattern.compile(ADD_ADMINISTRATOR_SUBJECT_FORMAT.replaceFirst("%s", emailRegexEscaped));
	private static final String ADD_SERVICE_UNIT_COORDINATOR_MESSAGE_FORMAT = "%s (%s) would like to be a" +
			"coordinator for your unit (%s).  To approve, reply \"Confirm\" to this email. To reject, "
			+ "reply \"Reject\" to this email.";
	private static final String ADD_ADMINISTRATOR_MESSAGE_FORMAT = "%s (%s) would like to be an administrator for "
			+ "Common Patriots.  To approve, reply \"Confirm\" to this email. To reject, "
			+ "reply \"Reject\" to this email.";
	private static final String CONFIRM_ADMINISTRATOR_SUBJECT_FORMAT = "%s is now an administrator";
	private static final String CONFIRM_ADMINISTRATOR_MESSAGE_FORMAT = "%s (%s) has been approved as an "
			+ "administrator for Common Patriots by %s (%s)";
	private static final String CONFIRM_SU_COORDINATOR_SUBJECT_FORMAT = "%s is now a coordinator";
	private static final String CONFIRM_SU_COORDINATOR_MESSAGE_FORMAT = "%s (%s) has been approved as a "
			+ "unit coordinator for %s by %s (%s)";
	private static final String REJECT_ADMINISTRATOR_SUBJECT_FORMAT = "%s's administrator request was rejected";
	private static final String REJECT_ADMINISTRATOR_MESSAGE_FORMAT = "%s (%s) rejected %s's (%s) request to become "
			+ "an administrator for Common Patriots";
	private static final String REJECT_SU_COORDINATOR_SUBJECT_FORMAT = "%s's coordinator request was rejected";
	private static final String REJECT_SU_COORDINATOR_MESSAGE_FORMAT = "%s (%s) rejected %s's (%s) request to become"
			+ " a unit coordinator for %s";

	private static final String SERVICE_REQUEST_SUBJECT_FORMAT = "%s's Service Request";
	private static final String SERVICE_REQUEST_MESSAGE_FORMAT = "%s (%s) has requested service at %s (%f, %f).  " +
			"The original request was for %s.  Please find a suitable service unit (adding a polygon if necessary) and "
			+ "respond at your earliest convenience.";

	private static final Properties props = new Properties();
	private static final Session session = Session.getDefaultInstance(props, null);
	private static final InternetAddress systemAddress;
	public static final InternetAddress webmasterAddress;
	static {
		InternetAddress sysAddr;
		InternetAddress webmaster;
		try {
			sysAddr = new InternetAddress("system@common-patriots.appspotmail.com", "Common Patriots");
			webmaster = new InternetAddress("webmaster.commonpatriots@gmail.com", "Common Patriots Webmaster");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			sysAddr = null;
			webmaster = null;
		}
		systemAddress = sysAddr;
		webmasterAddress = webmaster;
	}

	// TODO: Reminder emails
	public static void sendAddServiceUnitCoordinatorMessage(List<Pair<String, String>> recipients,
			String newUserNickname, String newUserEmail, String suName) {
		String messageBody = String.format(ADD_SERVICE_UNIT_COORDINATOR_MESSAGE_FORMAT, newUserNickname, newUserEmail,
				suName);
		String subject = String.format(ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT, newUserEmail, suName);
		try {
            List<InternetAddress> to = makeInternetAddresses(recipients);
            sendSystemMessage(subject, messageBody, to);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
	}

	public static void sendAddAdministratorMessage(List<Pair<String, String>> recipients,
			String newUserNickname, String newUserEmail) {
		String messageBody = String.format(ADD_ADMINISTRATOR_MESSAGE_FORMAT, newUserNickname, newUserEmail);
		String subject = String.format(ADD_ADMINISTRATOR_SUBJECT_FORMAT, newUserEmail);
		try {
            List<InternetAddress> to = makeInternetAddresses(recipients);
            sendSystemMessage(subject, messageBody, to);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
	}

	public static void sendAddAdministratorActionTakenMessage(List<Pair<String, String>> recipients,
			String newAdminName, String newAdminEmail, String approverName, String approverEmail, boolean confirm) {
		String messageBody = null;
		String subject = null;
		if (confirm) {
			messageBody = String.format(CONFIRM_ADMINISTRATOR_MESSAGE_FORMAT,
					newAdminName, newAdminEmail, approverName, approverEmail);
			subject = String.format(CONFIRM_ADMINISTRATOR_SUBJECT_FORMAT, newAdminEmail);
		} else {
			messageBody = String.format(REJECT_ADMINISTRATOR_MESSAGE_FORMAT,
					approverName, approverEmail, newAdminName, newAdminEmail);
			subject = String.format(REJECT_ADMINISTRATOR_SUBJECT_FORMAT, newAdminEmail);
		}
		try {
            List<InternetAddress> to = makeInternetAddresses(recipients);
            sendSystemMessage(subject, messageBody, to);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
	}

	public static void sendAddCoordinatorActionTakenMessage(List<Pair<String, String>> recipients,
			String newCoordName, String newCoordEmail, String approverName, String approverEmail, String suName,
			boolean confirm) {
		String messageBody = null;
		String subject = null;
		if (confirm) {
			messageBody = String.format(CONFIRM_SU_COORDINATOR_MESSAGE_FORMAT,
					newCoordName, newCoordEmail, suName, approverName, approverEmail);
			subject = String.format(CONFIRM_SU_COORDINATOR_SUBJECT_FORMAT, newCoordEmail);
		} else {
			messageBody = String.format(REJECT_SU_COORDINATOR_MESSAGE_FORMAT,
					approverName, approverEmail, newCoordName, newCoordEmail, suName);
			subject = String.format(REJECT_SU_COORDINATOR_SUBJECT_FORMAT, newCoordEmail);
		}
		try {
            List<InternetAddress> to = makeInternetAddresses(recipients);
            sendSystemMessage(subject, messageBody, to);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
	}

	public static Pair<String, Pair<String, String>> getMessageInfo(InputStream input) throws IOException {
		try {
			MimeMessage message = new MimeMessage(session, input);
			Object content = message.getContent();
			if (message.getContentType().startsWith("text/plain")) {
				return Pair.of(((InternetAddress) message.getFrom()[0]).getAddress(),
						Pair.of(message.getSubject(), (String) content));
			} else if (message.getContentType().startsWith("multipart")) {
				Multipart multiContent = (Multipart) content;
				for (int i = 0; i < multiContent.getCount(); i++) {
					BodyPart part = multiContent.getBodyPart(i);
					if (part.getContentType().startsWith("text/plain")) {
						return Pair.of(((InternetAddress) message.getFrom()[0]).getAddress(),
								Pair.of(message.getSubject(), (String) part.getContent()));
					}
				}
				return null;
			} else {
				return null;
			}
		} catch (MessagingException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static boolean isAddAdminSubject(String subject) {
		if (Strings.isNullOrEmpty(subject)) {
			return false;
		}
		subject = trimReplyPrefix(subject);
		return ADD_ADMINISTRATOR_SUBJECT_PATTERN.matcher(subject).matches();
	}

	public static boolean isAddServiceUnitCoordinatorSubject(String subject) {
		if (Strings.isNullOrEmpty(subject)) {
			return false;
		}
		subject = trimReplyPrefix(subject);
		return ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_PATTERN.matcher(subject).matches();
	}

	public static String getUserFromAddAdminSubject(String subject) {
		// There's not really a fantastic way to do this, but we'll try!
		if (!isAddAdminSubject(subject)) {
			return null;
		}
		subject = trimReplyPrefix(subject);
		int userEmailStartIndex = ADD_ADMINISTRATOR_SUBJECT_FORMAT.indexOf("%s");
		int userEmailEndIndex =
				subject.indexOf(ADD_ADMINISTRATOR_SUBJECT_FORMAT.substring(userEmailStartIndex + 2));
		if (userEmailEndIndex < userEmailStartIndex || userEmailEndIndex > subject.length()) {
			return null;
		} else {
			return subject.substring(userEmailStartIndex, userEmailEndIndex).trim().toLowerCase();
		}
	}

	public static Pair<String, String> getUserFromAddServiceUnitCoordinatorSubject(String subject) {
		// There's not really a fantastic way to do this, but we'll try!
		if (!isAddServiceUnitCoordinatorSubject(subject)) {
			return null;
		}
		subject = trimReplyPrefix(subject);
		int userEmailStartIndex = ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT.indexOf("%s");
		int serviceUnitNameFormatStartIndex =
				ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT.indexOf("%s", userEmailStartIndex + 1);
		String formatSecondPart = ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT
				.substring(userEmailStartIndex + 2, serviceUnitNameFormatStartIndex);
		int userEmailEndIndex = subject.indexOf(formatSecondPart);
		if (userEmailEndIndex < userEmailStartIndex || userEmailEndIndex > subject.length()) {
			return null;
		} else {
			return Pair.of(
					subject.substring(userEmailStartIndex, userEmailEndIndex).trim().toLowerCase(),
					subject.substring(userEmailEndIndex + formatSecondPart.length()).trim());
		}
	}

	public static void sendServiceRequestMessage(List<Pair<String, String>> recipients,
			String senderEmail, String senderName, String address, String originalQuery, double lat, double lng) {
		String messageBody = String.format(SERVICE_REQUEST_MESSAGE_FORMAT, senderName, senderEmail, address, lat, lng,
				originalQuery);
		String subject = String.format(SERVICE_REQUEST_SUBJECT_FORMAT, senderName);
		try {
            List<InternetAddress> to = makeInternetAddresses(recipients);
            sendPersonalMessage(subject, messageBody, to, new InternetAddress(senderEmail, senderName));
        } catch (MessagingException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
	}

	private static String trimReplyPrefix(String subject) {
		subject = subject.trim();
		if (subject.startsWith("Re:") || subject.startsWith("RE:")) {
			subject = subject.substring(3).trim();
		}
		return subject;
	}

	private static List<InternetAddress> makeInternetAddresses(List<Pair<String, String>> recipients)
			throws UnsupportedEncodingException {
		List<InternetAddress> to = CPUtil.newLinkedList();
        for (Pair<String, String> recipient : recipients) {
            to.add(new InternetAddress(recipient.first, recipient.second));
        }
        return to;
	}

	private static void sendPersonalMessage(String subject, String messageBody, List<InternetAddress> to,
			InternetAddress from) throws MessagingException {
		sendMessage(subject, messageBody, to, from);
	}

	private static void sendSystemMessage(String subject, String messageBody, List<InternetAddress> to)
			throws MessagingException {
		sendMessage(subject, messageBody, to, systemAddress);
	}

	private static void sendMessage(String subject, String messageBody, List<InternetAddress> to,
			InternetAddress from) throws MessagingException {
        Message msg = new MimeMessage(session);
        msg.setFrom(from);
        msg.addRecipient(Message.RecipientType.BCC, webmasterAddress);
        for (InternetAddress recipient : to) {
            msg.addRecipient(Message.RecipientType.TO, recipient);
        }
        msg.setSubject(subject);
        msg.setText(messageBody);
        Transport.send(msg);
	}
}
