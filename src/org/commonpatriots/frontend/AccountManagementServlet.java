package org.commonpatriots.frontend;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.commonpatriots.CPException;
import org.commonpatriots.data.ServiceUnitBo;
import org.commonpatriots.data.UserBo;
import org.commonpatriots.data.UserCollectionBo;
import org.commonpatriots.mail.CPMailer;
import org.commonpatriots.proto.CPData.State;
import org.commonpatriots.proto.CPData.User.UserType;
import org.commonpatriots.util.CPUtil;
import org.commonpatriots.util.Pair;
import org.commonpatriots.util.Strings;
import org.commonpatriots.util.Validation;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class AccountManagementServlet extends HttpServlet {
	private Provider<UserBo> userBoProvider;
	private Provider<ServiceUnitBo> serviceUnitBoProvider;
	private Provider<UserCollectionBo> userCollectionBoProvider;
	private UserService userService;
	
	@Inject
	public AccountManagementServlet(Provider<UserBo> userBoProvider, Provider<ServiceUnitBo> serviceUnitBoProvider,
			Provider<UserCollectionBo> userCollectionBoProvider) {
		this.userBoProvider = userBoProvider;
		this.serviceUnitBoProvider = serviceUnitBoProvider;
		this.userCollectionBoProvider = userCollectionBoProvider;
		userService = UserServiceFactory.getUserService();
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = getUserOrRedirect(resp);
		if (user == null) {
			return;
		}
		UserBo userBo = userBoProvider.get();
		userBo.open(user);
		setUserInformation(userBo, req);
		try {
			getServletContext().getRequestDispatcher("/jsp/manageAccount.jsp").forward(req, resp);
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = getUserOrRedirect(resp);
		if (user == null) {
			return;
		}
		UserBo userBo = userBoProvider.get();
		userBo.open(user);

		// Update contact information
		String email = req.getParameter("email");
		if (!Strings.isNullOrEmpty(email) && !email.equals(userBo.getEmail()) && Validation.isEmailAddress(email)) {
			if (userBo.openByEmail(email)) {
				// Illegal email address!
				System.err.println(user.getNickname() + " tried to take over email address " + email +
						", owned by " + userBo.getGoogleUser().getNickname());
				userBo.open(user); // Switch user back
			} else {
				userBo.open(user); // Switch user back
				userBo.setEmail(email);
			}
		}
		String address = req.getParameter("address");
		if (!Strings.isNullOrEmpty(address) && Validation.isAddress(address)) {
			userBo.setAddress(address);
		}
		String phone = req.getParameter("phone");
		if (!Strings.isNullOrEmpty(phone) && Validation.isPhoneNumber(phone)) {
			userBo.setPhone(Strings.formatPhoneNumber(phone));
		}
		String city = req.getParameter("city");
		if (!Strings.isNullOrEmpty(city) && Validation.isCity(city)) {
			userBo.setCity(city);
		}
		try {
			userBo.setState(State.valueOf(req.getParameter("state")));
		} catch (IllegalArgumentException ex) {}
		String zip = req.getParameter("zip");
		if (!Strings.isNullOrEmpty(zip) && Validation.isZip(zip)) {
			try {
				userBo.setZip(Validation.parseZip(zip));
			} catch (CPException e) {
				// Do nothing.  This shouldn't happen.
			}
		}

		UserType oldType = userBo.getType();
		UserType newType;
		try {
			newType = UserType.valueOf(req.getParameter("userType"));
		} catch (IllegalArgumentException ex) {
			newType = oldType;
		}
		String oldSuId = userBo.getServiceUnitId();
		String newSuId = oldSuId;
		ServiceUnitBo suBo = serviceUnitBoProvider.get();
		if (!Strings.isNullOrEmpty(req.getParameter("serviceUnitName"))
				&& suBo.openByName(req.getParameter("serviceUnitName"))) {
			newSuId = suBo.toDataObject().getId();
		}
		if (newType == UserType.SERVICE_UNIT_COORDINATOR &&
				((oldSuId == null && newSuId != null) || !oldSuId.equals(newSuId))) {
			userBo.setType(newType);
			userBo.setServiceUnitId(newSuId);
			userBo.setConfirmed(false);
			UserCollectionBo ucBo = userCollectionBoProvider.get();
			ucBo.open(newSuId);
			List<Pair<String, String>> emailsAndNames = ucBo.getEmailsAndNames();
			String suEmail = suBo.getEmail();
			if (!Strings.isNullOrEmpty(suEmail) && Validation.isEmailAddress(suEmail)) {
				emailsAndNames.add(Pair.of(suEmail, suBo.getName()));
			}
			CPMailer.sendAddServiceUnitCoordinatorMessage(emailsAndNames,
					user.getNickname(), userBo.getEmail(), suBo.getName());
		} else if (newType == UserType.ADMINISTRATOR && oldType != UserType.ADMINISTRATOR) {
			userBo.setType(newType);
			userBo.setConfirmed(false);
			UserCollectionBo ucBo = userCollectionBoProvider.get();
			ucBo.openAdministrators();
			List<Pair<String, String>> emailsAndNames = ucBo.getEmailsAndNames();
			CPMailer.sendAddAdministratorMessage(emailsAndNames,
					user.getNickname(), userBo.getEmail());
		} else if (newType == UserType.CUSTOMER){
			userBo.setType(newType);
			userBo.setConfirmed(true);
		}
		suBo.close();
		userBo.save();
		resp.sendRedirect("/main.jsp");
	}

	private User getUserOrRedirect(HttpServletResponse resp) throws IOException {
		User user = userService.getCurrentUser();
		if (user == null) {
			resp.sendRedirect(userService.createLoginURL("/main.jsp"));
		} 
		return user;
	}

	private void setUserInformation(UserBo userBo, HttpServletRequest req) {
		// Type information
		List<Pair<Pair<String, String>, String>> types = CPUtil.newLinkedList();
		for (UserType type : UserType.values()) {
			types.add(Pair.of(Pair.of(type.name(), Strings.formatForUser(type.name())),
					type == userBo.getType() ? "checked" : ""));
		}
		req.setAttribute("types", types);

		// Service unit information
		String suId = userBo.getServiceUnitId();
		if (!Strings.isNullOrEmpty(suId)) {
			ServiceUnitBo suBo = serviceUnitBoProvider.get();
			if (suBo.open(suId)) {
				req.setAttribute("serviceUnit", suBo.getName());
			} else {
				req.setAttribute("serviceUnit", "none");
			}
		}

		// Contact information
		req.setAttribute("user", userBo);

		// Confirmation information
		req.setAttribute("confirmed", Boolean.toString(userBo.isConfirmed()));

		// Other information
		req.setAttribute("username", userBo.getGoogleUser().getNickname());
		req.setAttribute("logoutURL", userService.createLogoutURL("/jsp/loggedOut.jsp"));
	}
}
