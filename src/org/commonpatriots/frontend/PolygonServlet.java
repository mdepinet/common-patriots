package org.commonpatriots.frontend;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.commonpatriots.data.ServiceUnitBo;
import org.commonpatriots.data.ServiceUnitCollectionBo;
import org.commonpatriots.proto.CPData.ServiceUnit;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon.Point;
import org.commonpatriots.util.CPUtil;
import org.commonpatriots.util.Strings;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PolygonServlet extends HttpServlet {
	private Provider<ServiceUnitCollectionBo> sucBoProvider;
	private Provider<ServiceUnitBo> serviceUnitBoProvider;

	@Inject
	public PolygonServlet(Provider<ServiceUnitCollectionBo> sucBoProvider,
			Provider<ServiceUnitBo> serviceUnitBoProvider) {
		this.sucBoProvider = sucBoProvider;
		this.serviceUnitBoProvider = serviceUnitBoProvider;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (maybeHandleLocationQuery(req, resp)) {
			return;
		}
		String suid = req.getParameter("suid");
		Collection<ServiceUnit> units = null;
		if (!Strings.isNullOrEmpty(suid)) {
			ServiceUnitBo subo = serviceUnitBoProvider.get();
			if (subo.open(suid)) {
				units = CPUtil.newLinkedList(subo.toDataObject());
			}
		}
		if (units == null) {
			ServiceUnitCollectionBo sucBo = sucBoProvider.get();
			sucBo.openAll();
			units = sucBo.toDataObject();
		}
		StringBuffer output = new StringBuffer();
		for (ServiceUnit unit : units) {
			for (Polygon poly : unit.getDistributionZonesList()) {
				output.append(polygonToString(poly));
				output.append("=");
				output.append(unit.getName());
				output.append("=");
				output.append(!Strings.isNullOrEmpty(unit.getColor()) ? unit.getColor() : "#000000");
				output.append("=");
				output.append(poly.getId());
				output.append(";");
			}
		}
		resp.setContentType("text/plain");
		resp.getOutputStream().print(output.toString());
	}

	private String polygonToString(Polygon poly) {
		StringBuffer buff = new StringBuffer();
		for (Point p : poly.getPointsList()) {
			buff.append(p.getLatitude());
			buff.append(" ");
			buff.append(p.getLongitude());
			buff.append("\n");
		}
		return buff.toString();
	}

	private boolean maybeHandleLocationQuery(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String lat = req.getParameter("lat");
		String lng = req.getParameter("lng");
		if (!Strings.isNullOrEmpty(lat) && lat.matches("[-]?\\d+[.]?\\d+")
				&& !Strings.isNullOrEmpty(lng) && lng.matches("[-]?\\d+[.]?\\d+")) {
			double latitude = Double.parseDouble(lat);
			double longitude = Double.parseDouble(lng);
			ServiceUnitCollectionBo sucBo = sucBoProvider.get();
			ServiceUnitBo bo = serviceUnitBoProvider.get();
			sucBo.openAll();
			Collection<ServiceUnit> units = sucBo.toDataObject();
			StringBuffer output = new StringBuffer();
			for (ServiceUnit unit : units) {
				bo.open(unit);
				Polygon serviceArea = bo.getPolygonForLocation(latitude, longitude);
				if (serviceArea == null) {
					continue;
				}
				output.append(polygonToString(serviceArea));
				output.append("=");
				output.append(unit.getName());
				output.append("=");
				output.append(!Strings.isNullOrEmpty(unit.getColor()) ? unit.getColor() : "#000000");
				output.append("=");
				output.append(serviceArea.getId());
				output.append(";");
			}
			resp.getOutputStream().print(output.toString());
			return true;
		}
		return false;
	}
}
