package org.commonpatriots.data;

import org.commonpatriots.data.BaseDAO.CPDAOException;
import org.commonpatriots.proto.CPData.Subscription;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class SubscriptionBo extends BaseBo<Subscription> {

	private Subscription.Builder data;

	@Inject
	public SubscriptionBo(Provider<BaseDAO> daoProvider) {
		this.daoProvider = daoProvider;
	}

	@Override
	public void open() {
		open(Subscription.getDefaultInstance());
	}

	@Override
	public boolean open(String id) {
		try {
			open(daoProvider.get().getSubscriptionById(id));
			return true;
		} catch (CPDAOException ex) {
			open(Subscription.newBuilder().setId(id).build());
			return false;
		}
	}

	@Override
	public void open(Subscription data) {
		this.data = Subscription.newBuilder(data);
		isOpen = true;
	}

	@Override
	public void save() {
		checkIsOpen();
		daoProvider.get().upsertSubscription(data.build());
		isOpen = false;
	}

	@Override
	public Subscription toDataObject() {
		checkIsOpen();
		return data.build();
	}

	public String getServiceUnitId() {
		checkIsOpen();
		return data.getServiceUnitId();
	}

	public void setServiceUnitId(String unitId) {
		checkIsOpen();
		data.setServiceUnitId(unitId);
	}

	public String getUserId() {
		checkIsOpen();
		return data.getUserId();
	}

	public void setUserId(String userId) {
		checkIsOpen();
		data.setUserId(userId);
	}

	public int getNumFlags() {
		checkIsOpen();
		return data.getNumFlags();
	}

	public void setNumFlags(int numFlags) {
		checkIsOpen();
		data.setNumFlags(numFlags);
	}

	public boolean isActive() {
		checkIsOpen();
		return data.getActive();
	}

	public void setActive(boolean active) {
		checkIsOpen();
		data.setActive(active);
	}
}
