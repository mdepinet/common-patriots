package org.commonpatriots.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.commonpatriots.CPException;
import org.commonpatriots.data.BaseDAO.CPDAOException;
import org.commonpatriots.proto.CPData.Subscription;
import org.commonpatriots.util.CPUtil;
import org.commonpatriots.util.Pair;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class SubscriptionCollectionBo extends BaseBo<Collection<Subscription>> {

	private Collection<Subscription> data;
	private Provider<UserBo> userBoProvider;

	@Inject
	public SubscriptionCollectionBo(Provider<BaseDAO> daoProvider, Provider<UserBo> userBoProvider) {
		this.daoProvider = daoProvider;
		this.userBoProvider = userBoProvider;
	}

	@Override
	public void open() {
		open(CPUtil.<Subscription>newArrayList());
	}

	@Override
	public boolean open(String parentId) {
		try {
			open(daoProvider.get().getSubscriptionsByServiceUnitId(parentId));
			return true;
		} catch (CPDAOException ex) {
			open(CPUtil.<Subscription>newArrayList());
			return false;
		}
	}

	@Override
	public void open(Collection<Subscription> data) {
		this.data = data;
		isOpen = true;
	}

	@Override
	public void save() {
		checkIsOpen();
		daoProvider.get().upsertSubscriptions(data);
		data = null;
		isOpen = false;
	}

	public void delete() {
		checkIsOpen();
		BaseDAO dao = daoProvider.get();
		for (Subscription sub : data) {
			dao.delete(sub.getId());
		}
		isOpen = false;
	}

	@Override
	public Collection<Subscription> toDataObject() {
		checkIsOpen();
		return Collections.unmodifiableCollection(data);
	}

	public void addSubscription(Subscription sub) {
		checkIsOpen();
		data.add(sub);
	}

	public void removeSubscription(Subscription sub) {
		checkIsOpen();
		data.remove(sub);
	}

	public Collection<List<Subscription>> optimizeRoutes(
			int maxRoutes, int maxSubscriptionsPerRoute) throws CPException {
		Collection<Pair<Pair<Double, Double>, Subscription>> locations = getActivePoints();
		if (locations.size() > maxRoutes * maxSubscriptionsPerRoute) {
			throw new CPException("Too many subscriptions for specified maximums!");
		}
		Collection<Set<Pair<Pair<Double, Double>, Subscription>>> unorderedRoutes =
				separatePoints(locations, maxRoutes, maxSubscriptionsPerRoute);
		Collection<List<Subscription>> results = CPUtil.newArrayList();
		for (Set<Pair<Pair<Double, Double>, Subscription>> unorderedRoute : unorderedRoutes) {
			results.add(extractSubscriptions(orderRoute(unorderedRoute)));
		}
		return results;
	}

	private Collection<Pair<Pair<Double, Double>, Subscription>> getActivePoints() {
		List<Pair<Pair<Double, Double>, Subscription>> points = CPUtil.newArrayList();
		UserBo userBo = userBoProvider.get();
		for (Subscription sub : data) {
			if (sub.getActive() && sub.hasUserId()) {
				userBo.open(sub.getUserId());
				Pair<Double, Double> point = userBo.getLocation(true);
				if (point != null) {
					points.add(Pair.of(point, sub));
				}
			}
		}
		return points;
	}

	private Collection<Set<Pair<Pair<Double, Double>, Subscription>>> separatePoints(
			Collection<Pair<Pair<Double, Double>, Subscription>> points, int maxRoutes, int maxSubsPerRoute) {
		// TODO: This just uses Euclidean distances for now.  We could use driving distances, but it we probably need
		// premier maps to support the number of queries needed.
		// TODO
		return null;
	}

	private List<Pair<Pair<Double, Double>, Subscription>> orderRoute(
			Set<Pair<Pair<Double, Double>, Subscription>> unorderedRoute) {
		// TODO
		return null;
	}

	private List<Subscription> extractSubscriptions(List<Pair<Pair<Double, Double>, Subscription>> route) {
		List<Subscription> subs = CPUtil.newArrayListWithExpectedSize(route.size());
		for (Pair<Pair<Double, Double>, Subscription> stop : route) {
			subs.add(stop.second);
		}
		return subs;
	}
}
