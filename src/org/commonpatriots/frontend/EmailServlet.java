package org.commonpatriots.frontend;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.commonpatriots.data.ServiceUnitBo;
import org.commonpatriots.data.UserBo;
import org.commonpatriots.data.UserCollectionBo;
import org.commonpatriots.mail.CPMailer;
import org.commonpatriots.proto.CPData.User.UserType;
import org.commonpatriots.util.Pair;
import org.commonpatriots.util.Strings;

import com.google.appengine.api.users.User;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class EmailServlet extends HttpServlet {
	private Provider<UserBo> userBoProvider;
	private Provider<ServiceUnitBo> serviceUnitBoProvider;
	private Provider<UserCollectionBo> userCollectionBoProvider;
	
	@Inject
	public EmailServlet(Provider<UserBo> userBoProvider, Provider<ServiceUnitBo> serviceUnitBoProvider,
			Provider<UserCollectionBo> userCollectionBoProvider) {
		this.userBoProvider = userBoProvider;
		this.serviceUnitBoProvider = serviceUnitBoProvider;
		this.userCollectionBoProvider = userCollectionBoProvider;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String incomingAddress = req.getRequestURI();
		int index = incomingAddress.lastIndexOf('/') ;
		if (index < 0 || index > incomingAddress.length()) {
			incomingAddress = "";
		} else {
			incomingAddress = incomingAddress.substring(incomingAddress.lastIndexOf("/") + 1);
		}
		switch (incomingAddress) {
			case "system@common-patriots.appspotmail.com":
				handleSystemMessage(req, resp);
				break;
			default:
				System.err.println("Received message for " + incomingAddress + ". Ignoring...");
		}
	}

	private void handleSystemMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Pair<String, Pair<String, String>> messageInfo = CPMailer.getMessageInfo(req.getInputStream());
		if (messageInfo == null) {
			System.err.println("Couldn't parse message to system.");
			return;
		}
		String from = messageInfo.first;
		String subject = messageInfo.second.first;
		String content = messageInfo.second.second.trim();
		boolean confirm = content.startsWith("Confirm")
				|| content.startsWith("CONFIRM") || content.startsWith("confirm");
		boolean reject = content.startsWith("Reject") || content.startsWith("REJECT") || content.startsWith("reject");
		if (confirm ^ reject) {
			if (CPMailer.isAddAdminSubject(subject)) {
				handleAddAdminMessage(from, CPMailer.getUserFromAddAdminSubject(subject), confirm);
			} else if (CPMailer.isAddServiceUnitCoordinatorSubject(subject)) {
				Pair<String, String> userEmailAndSuName = CPMailer.getUserFromAddServiceUnitCoordinatorSubject(subject);
				handleAddServiceUnitCoordinatorMessage(
					from, userEmailAndSuName.first, userEmailAndSuName.second, confirm);
			} else {
				System.err.println("Received unknown system message with subject " + subject + ". Ignoring...");
			}
		} else {
			System.err.println("Received unknown system message with content " + content + ". Ignoring...");
		}
	}

	// TODO: Make sure we don't allow double confirms or rejects that override confirms, etc.
	private void handleAddAdminMessage(String from, String userEmail, boolean confirm) {
		UserBo user = userBoProvider.get();
		if ((user.openByEmail(from) && user.getType() == UserType.ADMINISTRATOR && user.isConfirmed())
				|| CPMailer.webmasterAddress.getAddress().equals(from)) {
			String approverName = CPMailer.webmasterAddress.getAddress().equals(from) ? "webmaster"
					: user.getGoogleUser().getNickname();
			if (user.openByEmail(userEmail)) {
				String newAdminName = user.getGoogleUser().getNickname();
				String newAdminEmail = user.getEmail();
				if (confirm) {
					user.setType(UserType.ADMINISTRATOR);
					user.setConfirmed(true);
					user.setServiceUnitId(null);
				} else { // Reject
					user.setConfirmed(true);
					user.setType(UserType.CUSTOMER); // If was service unit coordinator, will need to re-confirm that
					// Perhaps this is too strict (since we should be able to use hasServiceUnitId to determine whether
					// they were a coordinator before), but it avoids an easy mistake introducing a security risk later
				}
				user.save();
				UserCollectionBo ucBo = userCollectionBoProvider.get();
				ucBo.openAdministrators();
				CPMailer.sendAddAdministratorActionTakenMessage(ucBo.getEmailsAndNames(), newAdminName, newAdminEmail,
						approverName, from, confirm);
			}
		} else {
			System.err.println(from + " tried to respond to admin request, but is not allowed to.");
		}
	}

	private void handleAddServiceUnitCoordinatorMessage(
			String from, String userEmail, String suName, boolean confirm) {
		UserBo user = userBoProvider.get();
		ServiceUnitBo suBo = serviceUnitBoProvider.get();
		if (suBo.openByName(suName) && ((user.openByEmail(from) && user.isConfirmed()
				&& (user.getType() == UserType.SERVICE_UNIT_COORDINATOR || user.getType() == UserType.ADMINISTRATOR))
				|| (!Strings.isNullOrEmpty(from) && from.equals(suBo.getEmail())))
				|| CPMailer.webmasterAddress.getAddress().equals(from)) {
			User googleUser = user.getGoogleUser();
			String approverName = CPMailer.webmasterAddress.getAddress().equals(from) ? "webmaster"
					: googleUser == null ? suName : googleUser.getNickname();
			if (user.openByEmail(userEmail)) {
				String newCoordName = user.getGoogleUser().getNickname();
				String newCoordEmail = user.getEmail();
				if (confirm) {
					user.setType(UserType.SERVICE_UNIT_COORDINATOR);
					user.setServiceUnitId(suBo.toDataObject().getId());
					user.setConfirmed(true);
				} else { // Reject
					user.setConfirmed(true);
					user.setType(UserType.CUSTOMER);
					user.setServiceUnitId(null);
				}
				user.save();
				UserCollectionBo ucBo = userCollectionBoProvider.get();
				ucBo.open(suBo.toDataObject().getId());
				List<Pair<String, String>> emails = ucBo.getEmailsAndNames();
				emails.add(Pair.of(suBo.getEmail(), suBo.getName()));
				CPMailer.sendAddCoordinatorActionTakenMessage(emails, newCoordName, newCoordEmail,
						approverName, from, suName, confirm);
			}
		} else {
			System.err.println(from + " tried to respond to service unit coordinator request, but is not allowed to.");
		}
	}
}
