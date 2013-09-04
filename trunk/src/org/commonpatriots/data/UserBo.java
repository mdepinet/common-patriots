package org.commonpatriots.data;

import org.commonpatriots.data.BaseDAO.CPDAOException;
import org.commonpatriots.proto.CPData.ContactInfo;
import org.commonpatriots.proto.CPData.State;
import org.commonpatriots.proto.CPData.User;
import org.commonpatriots.proto.CPData.User.UserType;
import org.commonpatriots.util.Pair;
import org.commonpatriots.util.Strings;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserBo extends BaseBo<User> {
	private User.Builder data;
	private com.google.appengine.api.users.User googleUser;

	@Inject
	public UserBo(Provider<BaseDAO> daoProvider) {
		this.daoProvider = daoProvider;
	}

	@Override
	public void open() {
		open(User.getDefaultInstance());
	}

	@Override
	public boolean open(String id) {
		try {
			open(daoProvider.get().getUserById(id));
			return true;
		} catch (CPDAOException ex) {
			open(User.newBuilder().setId(id).build());
			return false;
		}
	}

	public boolean openByEmail(String email) {
		try {
			open(daoProvider.get().getUserByEmail(email));
			return true;
		} catch (CPDAOException ex) {
			open(User.newBuilder().setContactInfo(ContactInfo.newBuilder().setEmail(email)).build());
			return false;
		}
	}

	public boolean open(com.google.appengine.api.users.User googleUser) {
		try {
			open(daoProvider.get().getUserByGoogleUser(googleUser));
			return true;
		} catch (CPDAOException ex) {
			open(Pair.of(User.getDefaultInstance(), googleUser));
			return false;
		}
	}

	@Override
	public void open(User data) {
		open(Pair.of(data, (com.google.appengine.api.users.User) null));
	}

	public void open(Pair<User, com.google.appengine.api.users.User> pair) {
		this.data = User.newBuilder(pair.first);
		this.googleUser = pair.second;
		isOpen = true;
	}

	@Override
	public void save() {
		checkIsOpen();
		daoProvider.get().upsertUser(data.build(), googleUser);
		isOpen = false;
	}

	@Override
	public User toDataObject() {
		checkIsOpen();
		return data.build();
	}

	public com.google.appengine.api.users.User getGoogleUser() {
		return googleUser;
	}

	public void setGoogleUser(com.google.appengine.api.users.User googleUser) {
		this.googleUser = googleUser;
	}

	public String getEmail() {
		checkIsOpen();
		String googleUserEmail = googleUser != null ? googleUser.getEmail() : null;
		if (!Strings.isNullOrEmpty(googleUserEmail) && !googleUserEmail.contains("@")) {
			googleUserEmail += "@" + googleUser.getAuthDomain();
		}
		return data.hasContactInfo() ? data.getContactInfo().getEmail()
				: !Strings.isNullOrEmpty(googleUserEmail) ? googleUserEmail : null;
	}

	public void setEmail(String email) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setEmail(email.toLowerCase()));
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

	public String getStateString() {
		State state = getState();
		if (state != null) {
			return state.name();
		} else {
			return "";
		}
	}
	
	public void setState(State state) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setState(state));
	}

	public int getZip() {
		checkIsOpen();
		return data.hasContactInfo() ? data.getContactInfo().getZip() : 0;
	}

	public void setZip(int zip) {
		checkIsOpen();
		data.setContactInfo(ContactInfo.newBuilder(data.getContactInfo()).setZip(zip));
	}

	public Pair<Double, Double> getLocation(boolean useGeocoder) {
		checkIsOpen();
		if (!data.hasContactInfo()) {
			return null;
		}
		ContactInfo info = data.getContactInfo();
		if (info.hasLatitude() && info.hasLongitude()) {
			return Pair.of(info.getLatitude(), info.getLongitude());
		} else if (useGeocoder){
//			String queryLocation = getFullAddressString();
			// TODO: Query Google maps API for the LatLng, extract from xml, and save and return it
			return null;
		} else {
			return null;
		}
	}

	public String getFullAddressString() {
		return getAddress() + ", " + getCity() + ", " + getState().name() + " " + getZip();
	}

	public void setContactInfo(ContactInfo info) {
		data.setContactInfo(info);
	}

	public String getServiceUnitId() {
		checkIsOpen();
		return data.getServiceUnitId();
	}

	public void setServiceUnitId(String unitId) {
		checkIsOpen();
		if (unitId == null) {
			data.clearServiceUnitId();
		} else {
			data.setServiceUnitId(unitId);
		}
	}

	public UserType getType() {
		checkIsOpen();
		return data.getType();
	}

	public void setType(UserType type) {
		checkIsOpen();
		data.setType(type);
	}

	public boolean isConfirmed() {
		return data.getConfirmed();
	}

	public void setConfirmed(boolean confirmed) {
		checkIsOpen();
		data.setConfirmed(confirmed);
	}
}
