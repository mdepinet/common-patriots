package org.commonpatriots.data;

import java.util.List;

import org.commonpatriots.CPException;
import org.commonpatriots.data.BaseDAO.CPDAOException;
import org.commonpatriots.proto.CPData.ContactInfo;
import org.commonpatriots.proto.CPData.ServiceUnit;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon;
import org.commonpatriots.proto.CPData.State;
import org.commonpatriots.util.CPUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ServiceUnitBo extends BaseBo<ServiceUnit> {

	private ServiceUnit.Builder data;
	private Provider<SubscriptionCollectionBo> subColBoProvider;

	@Inject
	public ServiceUnitBo(Provider<BaseDAO> daoProvider, Provider<SubscriptionCollectionBo> subColBoProvider) {
		this.daoProvider = daoProvider;
		this.subColBoProvider = subColBoProvider;
	}

	@Override
	public void open() {
		open(ServiceUnit.getDefaultInstance());
	}

	@Override
	public boolean open(String id) {
		CPUtil.checkPrecondition(id != null);
		try {
			open(daoProvider.get().getServiceUnitById(id));
			return true;
		} catch (CPDAOException ex) {
			open(ServiceUnit.newBuilder().setId(id).build());
			return false;
		}
	}

	public boolean openByName(String name) {
		CPUtil.checkPrecondition(name != null);
		try {
			open(daoProvider.get().getServiceUnitByName(name));
			return true;
		} catch (CPDAOException ex) {
			open(ServiceUnit.newBuilder().setName(name).build());
			return false;
		}
	}
	@Override
	public void open(ServiceUnit data) {
		this.data = ServiceUnit.newBuilder(data);
		isOpen = true;
	}

	public boolean openOwner(Polygon zone) throws CPException {
		try {
			open(daoProvider.get().getOwnerOfZone(zone));
			return true;
		} catch (CPDAOException ex) {
			throw new CPException("Could not find owner of zone " + zone, ex);
		}
	}

	public String create() {
		return daoProvider.get().createServiceUnit();
	}

	@Override
	public void save() {
		checkIsOpen();
		BaseDAO dao = daoProvider.get();
		ServiceUnit newData = data.build();
		ServiceUnit oldData = dao.getServiceUnitById(data.getId());
		if (!newData.equals(oldData)) {
			dao.upsertServiceUnit(newData);
		}
		isOpen = false;
	}

	public void delete() {
		checkIsOpen();
		SubscriptionCollectionBo subColBo = subColBoProvider.get();
		subColBo.open(data.getId());
		subColBo.delete();
		daoProvider.get().delete(data.getId());
		isOpen = false;
	}

	@Override
	public ServiceUnit toDataObject() {
		checkIsOpen();
		return data.build();
	}

	public String getName() {
		checkIsOpen();
		return data.getName();
	}

	public void setName(String name) {
		checkIsOpen();
		data.setName(name);
	}

	public String getColor() {
		checkIsOpen();
		return data.getColor();
	}
	
	public void setColor(String color) {
		checkIsOpen();
		data.setColor(color);
	}

	public String getEmail() {
		checkIsOpen();
		return data.hasContactInfo() ? data.getContactInfo().getEmail() : null;
	}
	
	public void setEmail(String email) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setEmail(email));
	}
	
	public String getPhone() {
		checkIsOpen();
		return data.hasContactInfo() ? data.getContactInfo().getPhone() : null;
	}
	
	public void setPhone(String phone) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setPhone(phone));
	}
	
	public String getAddress() {
		checkIsOpen();
		return data.hasContactInfo() ? data.getContactInfo().getAddress() : null;
	}
	
	public void setAddress(String address) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setAddress(address));
	}
	
	public String getCity() {
		checkIsOpen();
		return data.hasContactInfo() ? data.getContactInfo().getCity() : null;
	}
	
	public void setCity(String city) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setCity(city));
	}
	
	public State getState() {
		checkIsOpen();
		return data.hasContactInfo() ? data.getContactInfo().getState() : null;
	}
	
	public void setState(State state) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setState(state));
	}

	public String getZip() {
		checkIsOpen();
		int intVal = data.hasContactInfo() ? data.getContactInfo().getZip() : null;
		if (intVal > 99999) { // Must be 9 digits
			 return "" + intVal / 100000 + "-" + intVal % 100000;
		} else {
			return "" + intVal;
		}
	}
	
	public void setZip(String zip) {
		checkIsOpen();
		zip = zip.replaceAll("-", "");
		int parsedZip = Integer.parseInt(zip);
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setZip(parsedZip));
	}
	
	public void setContactInfo(ContactInfo info) {
		data.setContactInfo(info);
	}

	public boolean deletePolygon(Polygon poly) {
		checkIsOpen();
		boolean success = false;
		List<Polygon> zones = CPUtil.newLinkedList(data.getDistributionZonesList());
		if (poly.hasId()) {
			for (Polygon oldPoly : zones) {
				if (poly.getId() == oldPoly.getId()) {
					success = zones.remove(oldPoly);
					break;
				}
			}
		} else {
			success = zones.remove(poly);
		}
		data.clearDistributionZones();
		data.addAllDistributionZones(zones);
		return success;
	}

	public void addPolygon(Polygon poly) {
		checkIsOpen();
		poly = poly.toBuilder().setId(nextPolygonId()).build();
		data.addDistributionZones(poly);
	}

	public void updatePolygon(Polygon poly) {
		CPUtil.checkPrecondition(poly.hasId());
		deletePolygon(poly);
		data.addDistributionZones(poly);
	}

	public String getInfoFrameLoc() {
		checkIsOpen();
		return data.getInfoFrameLoc();
	}

	public void setInfoFrameLoc(String loc) {
		checkIsOpen();
		data.setInfoFrameLoc(loc);
	}

	public boolean servesLocation(double latitude, double longitude) {
		return getPolygonForLocation(latitude, longitude) != null;
	}

	public Polygon getPolygonForLocation(double latitude, double longitude) {
		for (Polygon poly : data.getDistributionZonesList()) {
			if (pointInsidePolygon(latitude, longitude, poly)) {
				return poly;
			}
		}
		return null;
	}

	private boolean pointInsidePolygon(double latitude, double longitude, Polygon poly) {
		boolean count = false;
		int numVertices = poly.getPointsCount();
		for (int i = 0, j = numVertices - 1; i < numVertices; j = i++) {
			if (poly.getPoints(j).getLatitude() == poly.getPoints(i).getLatitude()) {
				continue;
			}
			if (((poly.getPoints(i).getLatitude() > latitude) != (poly.getPoints(j).getLatitude() > latitude))
					&& (longitude < (poly.getPoints(j).getLongitude() - poly.getPoints(i).getLongitude())
						* (latitude - poly.getPoints(i).getLatitude())
						/ (poly.getPoints(j).getLatitude() - poly.getPoints(i).getLatitude())
						+ poly.getPoints(i).getLongitude())) {
				count = !count;
			}
		}
		return count;
	}

	private long nextPolygonId() {
		long highestId = 0L;
		for (Polygon poly : data.getDistributionZonesList()) {
			highestId = Math.max(highestId, poly.getId());
		}
		return ++highestId;
	}

}
