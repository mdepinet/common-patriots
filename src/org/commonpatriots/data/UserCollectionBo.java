package org.commonpatriots.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.commonpatriots.data.BaseDAO.CPDAOException;
import org.commonpatriots.proto.CPData.User;
import org.commonpatriots.util.CPUtil;
import org.commonpatriots.util.Pair;
import org.commonpatriots.util.Strings;
import org.commonpatriots.util.Validation;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserCollectionBo extends BaseBo<Collection<Pair<User, com.google.appengine.api.users.User>>> {

	private Collection<Pair<User, com.google.appengine.api.users.User>> data;
	private Provider<UserBo> userBoProvider;

	@Inject
	public UserCollectionBo(Provider<BaseDAO> daoProvider, Provider<UserBo> userBoProvider) {
		this.daoProvider = daoProvider;
		this.userBoProvider = userBoProvider;
	}

	@Override
	public void open() {
		open(CPUtil.<Pair<User, com.google.appengine.api.users.User>>newArrayList());
	}

	public boolean openAdministrators() {
		try {
			open(daoProvider.get().getAdministrators());
			return true;
		} catch (CPDAOException ex) {
			open(CPUtil.<Pair<User, com.google.appengine.api.users.User>>newArrayList());
			return false;
		}
	}

	@Override
	public boolean open(String serviceUnitId) {
		try {
			open(daoProvider.get().getServiceUnitCoordinators(serviceUnitId));
			return true;
		} catch (CPDAOException ex) {
			open(CPUtil.<Pair<User, com.google.appengine.api.users.User>>newArrayList());
			return false;
		}
	}

	@Override
	public void open(Collection<Pair<User, com.google.appengine.api.users.User>> data) {
		this.data = data;
		isOpen = true;
	}

	@Override
	public void save() {
		// There's no use case for this yet.
		throw new UnsupportedOperationException("Cannot save a UserCollection");
	}

	@Override
	public Collection<Pair<User, com.google.appengine.api.users.User>> toDataObject() {
		checkIsOpen();
		return Collections.unmodifiableCollection(data);
	}

	public List<Pair<String, String>> getEmailsAndNames() {
		checkIsOpen();
		List<Pair<String, String>> emailsAndNames = CPUtil.newArrayList();
		UserBo userBo = userBoProvider.get();
		for (Pair<User, com.google.appengine.api.users.User> user : data) {
			userBo.open(user);
			String email = userBo.getEmail();
			userBo.close();
			String name = user.second.getNickname();
			if (!Strings.isNullOrEmpty(email) && Validation.isEmailAddress(email) && !Strings.isNullOrEmpty(name)) {
				emailsAndNames.add(Pair.of(email, name));
			}
		}
		return emailsAndNames;
	}
}
