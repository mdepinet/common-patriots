package org.commonpatriots.frontend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.commonpatriots.data.UserBo;
import org.commonpatriots.data.UserCollectionBo;
import org.commonpatriots.mail.CPMailer;
import org.commonpatriots.proto.CPData.State;
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
public class CommonPatriotsServlet extends HttpServlet {
	private Provider<UserBo> userBoProvider;
	private Provider<UserCollectionBo> userCollectionBoProvider;
	
	@Inject
	public CommonPatriotsServlet(Provider<UserBo> userBoProvider,
			Provider<UserCollectionBo> userCollectionBoProvider) {
		this.userBoProvider = userBoProvider;
		this.userCollectionBoProvider = userCollectionBoProvider;
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String path = req.getServletPath();
		if (!Strings.isNullOrEmpty(path) && path.contains("forms")) {
			handleGetForm(req, resp);
			return;
		}
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) { // Anonymous access
			req.setAttribute("loginURL", userService.createLoginURL("/commonpatriots"));
			setRequestPosition(req);
			try {
				getServletContext().getRequestDispatcher("/jsp/main.jsp").forward(req, resp);
			} catch (ServletException e) {
				e.printStackTrace();
			}
			return;
		} else {
			UserBo userBo = userBoProvider.get();
			boolean save = false;
			if (!userBo.open(user) && "US".equals(req.getHeader("X-AppEngine-Country"))) {
				userBo.setState(State.valueOf(req.getHeader("X-AppEngine-Region").toUpperCase()));
				userBo.setCity(Strings.formatForUser(req.getHeader("X-AppEngine-City")));
				String email = user.getEmail();
				if (!email.contains("@"))  {
					email += "@" + user.getAuthDomain();
				}
				userBo.setEmail(email);
				save = true;
			}
			Pair<Double, Double> position = userBo.getLocation(false);
			if (position != null) {
				req.setAttribute("latitude", position.first);
				req.setAttribute("longitude", position.second);
			} else {
				setRequestPosition(req);
			}
			if (save) {
				userBo.save();
			}
			req.setAttribute("username", user.getNickname());
			req.setAttribute("logoutURL", userService.createLogoutURL("/jsp/loggedOut.jsp"));
			try {
				String page = "/jsp/" + ("true".equals(req.getParameter("mapOnly")) ? "mapOnly" : "main") + ".jsp";
				getServletContext().getRequestDispatcher(page).forward(req, resp);
			} catch (ServletException e) {
				e.printStackTrace();
			}
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String path = req.getServletPath();
		if (Strings.isNullOrEmpty(path) || !path.contains("forms")) {
			resp.sendError(500);
			resp.getOutputStream().print("Could not identify form submission");
		}
		String[] pathParts = path.split("/");
		String formId = pathParts[pathParts.length - 1];
		switch(formId) {
			case "requestServiceSubmit":
				double latitude = -1;
				double longitude = -1;
				String lat = req.getParameter("lat");
				String lng = req.getParameter("lng");
				if (!Strings.isNullOrEmpty(lat) && lat.matches("[-]?\\d+[.]?\\d+")
						&& !Strings.isNullOrEmpty(lng) && lng.matches("[-]?\\d+[.]?\\d+")) {
					latitude =  Double.parseDouble(lat);
					longitude = Double.parseDouble(lng);
				}
				String originalQuery = req.getParameter("query");
				String address = req.getParameter("addr");
				String name = req.getParameter("name");
				String email = req.getParameter("email");
				if (!Strings.isNullOrEmpty(email) && !Strings.isNullOrEmpty(address)
						&& Validation.isEmailAddress(email)
						&& (Validation.isAddress(address) || Validation.isAddressExtended(address))) {
					UserCollectionBo bo = userCollectionBoProvider.get();
					bo.openAdministrators();
					CPMailer.sendServiceRequestMessage(bo.getEmailsAndNames(),
							email, name, address, originalQuery, latitude, longitude);
					resp.setContentType("text/plain");
					resp.getOutputStream().print("Your message was sent. " +
							" We'll take a look and get back to you as soon as possible!");
				} else {
					resp.sendError(500);
					resp.getOutputStream().print("Service Requests must have a valid address and email");
				}
				break;
			default:
				resp.sendError(500);
				resp.getOutputStream().print("Invalid form id for submission");
		}
	}

	private void setRequestPosition(HttpServletRequest req) {
		String latLng = req.getHeader("X-AppEngine-CityLatLong");
		if (latLng != null && latLng.matches("[-]?\\d+[.]?\\d+,[-]?\\d+[.]?\\d+")) {
			req.setAttribute("latitude", latLng.substring(0,latLng.indexOf(",")));
			req.setAttribute("longitude", latLng.substring(latLng.indexOf(",") + 1));
		} else {
			// For local executions (where X-AppEngine-CityLatLong is unavailable)
			req.setAttribute("latitude", 30.35973);
			req.setAttribute("longitude", -97.75153);
		}
	}

	private void handleGetForm(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String[] pathParts = req.getServletPath().split("/");
		String requestedFormId = pathParts[pathParts.length - 1];
		switch(requestedFormId) {
			case "requestService":
				String lat = req.getParameter("lat");
				String lng = req.getParameter("lng");
				String queryString = req.getParameter("q");
				if (!Strings.isNullOrEmpty(lat) && lat.matches("[-]?\\d+[.]?\\d+")
						&& !Strings.isNullOrEmpty(lng) && lng.matches("[-]?\\d+[.]?\\d+")) {
					req.setAttribute("lat", Double.parseDouble(lat));
					req.setAttribute("lng", Double.parseDouble(lng));
				}
				req.setAttribute("query", queryString);
				UserService userService = UserServiceFactory.getUserService();
				User user = userService.getCurrentUser();
				UserBo bo = userBoProvider.get();
				if (user != null) {
					req.setAttribute("nickname", user.getNickname());
					req.setAttribute("email", user.getEmail());
				}
				if (bo.open(user)) {
					req.setAttribute("email", bo.getEmail());
				}
				try {
					getServletContext().getRequestDispatcher("/jsp/forms/requestService.jsp").forward(req, resp);
				} catch (ServletException e) {
					e.printStackTrace();
				}
				break;
			default:
				resp.sendError(500);
				resp.getOutputStream().print("Illegal form id");
				break;
		}
	}
}
