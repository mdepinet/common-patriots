package org.commonpatriots.frontend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.commonpatriots.data.UserBo;
import org.commonpatriots.proto.CPData.State;
import org.commonpatriots.util.Pair;
import org.commonpatriots.util.Strings;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class CommonPatriotsServlet extends HttpServlet {
	private Provider<UserBo> userBoProvider;
	
	@Inject
	public CommonPatriotsServlet(Provider<UserBo> userBoProvider) {
		this.userBoProvider = userBoProvider;
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
				getServletContext().getRequestDispatcher("/jsp/main.jsp").forward(req, resp);
			} catch (ServletException e) {
				e.printStackTrace();
			}
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
}
