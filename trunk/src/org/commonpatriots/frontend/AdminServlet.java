package org.commonpatriots.frontend;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.commonpatriots.data.ServiceUnitBo;
import org.commonpatriots.data.ServiceUnitCollectionBo;
import org.commonpatriots.data.UserBo;
import org.commonpatriots.proto.CPData.ServiceUnit;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon.Point;
import org.commonpatriots.proto.CPData.State;
import org.commonpatriots.proto.CPData.User.UserType;
import org.commonpatriots.util.Strings;
import org.commonpatriots.util.Validation;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class AdminServlet extends HttpServlet {
	private Provider<UserBo> userBoProvider;
	private Provider<ServiceUnitCollectionBo> sucBoProvider;
	private Provider<ServiceUnitBo> serviceUnitBoProvider;

	@Inject
	public AdminServlet(Provider<UserBo> userBoProvider, Provider<ServiceUnitCollectionBo> sucBoProvider,
			Provider<ServiceUnitBo> serviceUnitBoProvider) {
		this.userBoProvider = userBoProvider;
		this.sucBoProvider = sucBoProvider;
		this.serviceUnitBoProvider = serviceUnitBoProvider;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			resp.sendRedirect(userService.createLoginURL("/main.jsp"));
		} else {
			// Authentication
			UserBo userBo = userBoProvider.get();
			if (!userBo.open(user) || userBo.getType() != UserType.ADMINISTRATOR || !userBo.isConfirmed()) {
				// Make Mike an admin if he's not already
				if ("localhost".equals(req.getServerName()) && ("mikedabomb000".equals(user.getEmail()) || "mikedabomb000@gmail.com".equals(user.getEmail()))) {
					userBo.setType(UserType.ADMINISTRATOR);
					userBo.setConfirmed(true);
					userBo.save();

					resp.setContentType("text/html");
					ServletOutputStream out = resp.getOutputStream();
					out.println("Welcome, Mike.  You've been authorized.");
					out.println("<a href='/admin'>Refresh</a>");
					return;
				} 
				resp.setContentType("text/html");
				ServletOutputStream out = resp.getOutputStream();
				out.println("You are not authorized to view this page.");
				out.println("<a href='/commonpatriots'>Return to main page</a>");
				return;
			}

			// Handle special paths (AJAX)
			String path = req.getPathInfo();
			if (Strings.isNullOrEmpty(path)) {
				handleDefaultGetRequest(req, resp, user, userService);
				return;
			}
			switch(path) {
				case "serviceUnit":
					handleServiceUnitGetRequest(req, resp);
					break;
				case "polygon":
					handlePolygonGetRequest(req, resp);
					break;
				default:
					handleDefaultGetRequest(req, resp, user, userService);
			}
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			resp.sendRedirect(userService.createLoginURL("/main.jsp"));
		}
		// Authentication
		UserBo userBo = userBoProvider.get();
		if (!userBo.open(user) || userBo.getType() != UserType.ADMINISTRATOR || !userBo.isConfirmed()) {
			resp.setContentType("text/html");
			ServletOutputStream out = resp.getOutputStream();
			out.println("You are not authorized to view this page.");
			out.println("<a href='/commonpatriots'>Return to main page</a>");
			return;
		}

		String path = req.getPathInfo();
		if (Strings.isNullOrEmpty(path)) {
			return;
		}
		path = path.substring(1); // Remove leading forward slash
		switch(path) {
			case "serviceUnit":
				handleServiceUnitPostRequest(req, resp);
				break;
			case "polygon":
				handlePolygonPostRequest(req, resp);
				break;
			default:
				// Do nothing
		}
	}

	private void handleDefaultGetRequest(HttpServletRequest req, HttpServletResponse resp, User user,
			UserService userService) throws IOException {
		String latLng = req.getHeader("X-AppEngine-CityLatLong");
		if (latLng != null && latLng.matches("[-]?\\d+[.]?\\d+,[-]?\\d+[.]?\\d+")) {
			req.setAttribute("latitude", latLng.substring(0,latLng.indexOf(",")));
			req.setAttribute("longitude", latLng.substring(latLng.indexOf(",") + 1));
		} else {
			// For local executions (where X-AppEngine-CityLatLong is unavailable)
			req.setAttribute("latitude", 30.35973);
			req.setAttribute("longitude", -97.75153);
		}
		req.setAttribute("username", user.getNickname());
		req.setAttribute("logoutURL", userService.createLogoutURL("/jsp/loggedOut.jsp"));
		ServiceUnitCollectionBo sucBo = sucBoProvider.get();
		sucBo.openAll();
		req.setAttribute("serviceUnits", sucBo.toDataObject());
		try {
			getServletContext().getRequestDispatcher("/jsp/admin.jsp").forward(req, resp);
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

	private void handleServiceUnitGetRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = req.getParameter("id");
		String name = req.getParameter("name");
		if (Strings.isNullOrEmpty(id) && Strings.isNullOrEmpty(name)) {
			ServiceUnitCollectionBo sucBo = sucBoProvider.get();
			sucBo.openAll();
			Collection<ServiceUnit> units = sucBo.toDataObject();
			StringBuffer output = new StringBuffer();
			for (ServiceUnit unit : units) {
				output.append(unit.getName());
				output.append("\n");
			}
			resp.getOutputStream().print(output.toString());
			return;
		} else {
			ServiceUnitBo suBo = serviceUnitBoProvider.get();
			if (!suBo.open(id)) {
				if (!suBo.openByName(name)) {
					return;
				}
			}
			// TODO: What do we actually want to return here?
			return;
		}
	}

	private void handleServiceUnitPostRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		/*
		 * 0 = Unchanged
		 * 1 = Success
		 * 2 = Bad request
		 */
		String id = req.getParameter("id");
		boolean delete = "true".equals(req.getParameter("delete"));
		if (Strings.isNullOrEmpty(id)) {
			ServiceUnitBo suBo = serviceUnitBoProvider.get();
			resp.getOutputStream().println(suBo.create());
			return;
		} else {
			ServiceUnitBo suBo = serviceUnitBoProvider.get();
			if ((!Strings.isNullOrEmpty(id) && suBo.open(id))) {
				if (delete) {
					suBo.delete();
				} else {
					String name = req.getParameter("name");
					String email = req.getParameter("email");
					String phone = req.getParameter("phone");
					String address = req.getParameter("address");
					String city = req.getParameter("city");
					String state = req.getParameter("state");
					String zip = req.getParameter("zip");
					String color = req.getParameter("color");
					String infoFrameLoc = req.getParameter("infoFrameLoc");
					int result = 0;
					resp.setContentType("text/plain");
					ServletOutputStream out = resp.getOutputStream();
					if (result != 2 && !Strings.isNullOrEmpty(name)) {
						if (Validation.isName(name)) {
							if (name.equals(suBo.getName())) {
								// Unchanged
							} else {
								suBo.setName(name);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(email)) {
						if (Validation.isEmailAddress(email)) {
							if (email.equals(suBo.getEmail())) {
								// Unchanged
							} else {
								suBo.setEmail(email);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(phone)) {
						if (Validation.isPhoneNumber(phone)) {
							if (phone.equals(suBo.getPhone())) {
								// Unchanged
							} else {
								suBo.setPhone(phone);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(color)) {
						if (Validation.isColor(color)) {
							if (color.equals(suBo.getColor())) {
								// Unchanged
							} else {
								suBo.setColor(color);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(address)) {
						if (Validation.isAddress(address)) {
							if (address.equals(suBo.getAddress())) {
								// Unchanged
							} else {
								suBo.setAddress(address);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(city)) {
						if (Validation.isCity(city)) {
							if (city.equals(suBo.getCity())) {
								// Unchanged
							} else {
								suBo.setCity(city);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(state)) {
						try {
							if (State.valueOf(state) == suBo.getState()) {
								// Unchanged
							} else {
								suBo.setState(State.valueOf(state));
								result = 1;
							}
						} catch (IllegalArgumentException ex) {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(zip)) {
						if (Validation.isZip(zip)) {
							if (zip.equals(suBo.getZip())) {
								// Unchanged
							} else {
								suBo.setZip(zip);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					if (result != 2 && !Strings.isNullOrEmpty(infoFrameLoc)) {
						if (Validation.isURL(infoFrameLoc)) {
							if (infoFrameLoc.equals(suBo.getInfoFrameLoc())) {
								// Unchanged
							} else {
								suBo.setInfoFrameLoc(infoFrameLoc);
								result = 1;
							}
						} else {
							result = 2;
						}
					}
					out.print("" + result);
					if (result == 1) {
						suBo.save();
					}
					return;
				}
			} else {
				// Failed to find matching service unit
				resp.getOutputStream().print("2");
				return;
			}
		}
	}

	private void handlePolygonGetRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.sendRedirect("/polygons");
		return;
	}

	private void handlePolygonPostRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = req.getParameter("id");
		String name = req.getParameter("name");
		boolean delete = "true".equals(req.getParameter("delete"));
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();
		if (Strings.isNullOrEmpty(id) && Strings.isNullOrEmpty(name)) {
			return;
		} else {
			ServiceUnitBo suBo = serviceUnitBoProvider.get();
			if ((!Strings.isNullOrEmpty(id) && suBo.open(id))
					|| (!Strings.isNullOrEmpty(name) && suBo.openByName(name))) {
				String polygonString = req.getParameter("polygon");
				if (Strings.isNullOrEmpty(polygonString)) {
					out.print("false");
					return;
				}
				Polygon poly = parsePolygon(polygonString);
				if (delete) {
					suBo.deletePolygon(poly);
					out.print("true");
				} else {
					if (poly.hasId()) {
						suBo.updatePolygon(poly);
					} else {
						suBo.addPolygon(poly);
					}
					out.print(Strings.isNullOrEmpty(suBo.getColor()) ? "#000000" : suBo.getColor());
				}
				suBo.save();
			}
			else {
				out.print("false");
				// Failed to find service unit
			}
		}
	}

	private Polygon parsePolygon(String polyString) {
		String[] polyAndId = polyString.split("=");
		String[] points = polyAndId[0].split("\n");
		Polygon.Builder builder = Polygon.newBuilder();
		if (polyAndId.length > 1) {
			try {
				builder.setId(Long.parseLong(polyAndId[1]));
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Not a valid polygon:\n" + polyString);
			}
		}
		for (int i = 0; i < points.length; i++) {
			String[] coords = points[i].split("\\s");
			if (coords.length < 2) {
				throw new IllegalArgumentException("Not a valid polygon:\n" + polyString);
			}
			try {
				builder.addPoints(Point.newBuilder().setLatitude(Double.parseDouble(coords[0]))
						.setLongitude(Double.parseDouble(coords[1])));
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Not a valid polygon:\n" + polyString);
			}
		}
		return builder.build();
	}
}
