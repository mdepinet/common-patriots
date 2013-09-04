package org.commonpatriots.data;

import java.util.Collection;
import java.util.Collections;

import org.commonpatriots.data.BaseDAO.CPDAOException;
import org.commonpatriots.proto.CPData.ServiceUnit;
import org.commonpatriots.util.CPUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ServiceUnitCollectionBo extends BaseBo<Collection<ServiceUnit>> {

	private Collection<ServiceUnit> data;

	@Inject
	public ServiceUnitCollectionBo(Provider<BaseDAO> daoProvider) {
		this.daoProvider = daoProvider;
	}

	@Override
	public void open() {
		open(CPUtil.<ServiceUnit>newArrayList());
	}

	public void openAll() {
		open(daoProvider.get().getAllServiceUnits());
	}

	@Override
	public boolean open(String serviceUnitId) {
		try {
			open(CPUtil.newArrayList(daoProvider.get().getServiceUnitById(serviceUnitId)));
			return true;
		} catch (CPDAOException ex) {
			open(CPUtil.<ServiceUnit>newArrayList());
			return false;
		}
	}

	@Override
	public void open(Collection<ServiceUnit> data) {
		this.data = data;
		isOpen = true;
	}

	@Override
	public void save() {
		// There's no use case for this yet.
		throw new UnsupportedOperationException("Cannot save a ServiceUnitCollection");
	}

	@Override
	public Collection<ServiceUnit> toDataObject() {
		checkIsOpen();
		return Collections.unmodifiableCollection(data);
	}
}
