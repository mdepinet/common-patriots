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
public class ServiceUnitServlet extends HttpServlet {
	private Provider<ServiceUnitBo> serviceUnitBoProvider;

	@Inject
	public ServiceUnitServlet(Provider<ServiceUnitBo> serviceUnitBoProvider) {
		this.serviceUnitBoProvider = serviceUnitBoProvider;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = req.getParameter("id");
		String name = req.getParameter("name");
		String latitude = req.getParameter("lat");
		String longitude = req.getParameter("lng");
		if (Strings.isNullOrEmpty(id) && Strings.isNullOrEmpty(name)) {
			resp.getOutputStream().print("true;;;;;;;");
			return;
		} else {
			ServiceUnitBo suBo = serviceUnitBoProvider.get();
			if (!Strings.isNullOrEmpty(id) && !suBo.open(id)) {
				if (!Strings.isNullOrEmpty(name) && !suBo.openByName(name)) {
					resp.getOutputStream().print("true;Not Found;;;;;;"); // No service unit found
					return;
				}
			} else if (!Strings.isNullOrEmpty(name) && !suBo.openByName(name)) {
				resp.getOutputStream().print("true;Not Found;;;;;;"); // No service unit found
				return;
			}
			StringBuffer output = new StringBuffer();
			if (Strings.isNullOrEmpty(latitude) || Strings.isNullOrEmpty(longitude) ||
					!latitude.matches("[-]?\\d+[.]?\\d+") || !longitude.matches("[-]?\\d+[.]?\\d+")) {
				output.append("true;");
			} else {
				output.append(suBo.servesLocation(Double.parseDouble(latitude), Double.parseDouble(longitude))
					? "true;" : "false;");
			}
			output.append(suBo.getName());
			output.append(";");
			output.append(suBo.getEmail());
			output.append(";");
			output.append(suBo.getPhone());
			output.append(";");
			output.append(suBo.getAddress());
			output.append(";");
			output.append(suBo.getCity());
			output.append(", ");
			output.append(suBo.getState());
			output.append(" ");
			output.append(suBo.getZip());
			output.append(";");
			output.append(suBo.getInfoFrameLoc());
			output.append(";");
			resp.getOutputStream().print(output.toString());
		}
	}
}
