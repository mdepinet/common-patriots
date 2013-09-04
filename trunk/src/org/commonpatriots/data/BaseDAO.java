package org.commonpatriots.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.commonpatriots.CPRuntimeException;
import org.commonpatriots.proto.CPData.ServiceUnit;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon;
import org.commonpatriots.proto.CPData.Subscription;
import org.commonpatriots.proto.CPData.User;
import org.commonpatriots.proto.CPData.User.UserType;
import org.commonpatriots.util.CPUtil;
import org.commonpatriots.util.Pair;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.protobuf.InvalidProtocolBufferException;

class BaseDAO {
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private static final MemcacheService cache = MemcacheServiceFactory.getMemcacheService();

	private static final int MAX_NUM_ADMINS = 10;

	private static final String USER_KIND = "User";
	private static final String SUBSCRIPTION_KIND = "Subscription";
	private static final String SERVICE_UNIT_KIND = "ServiceUnit";

	private static final String USER_PROTO_PROPERTY = "userProto";
	private static final String SUBSCRIPTION_PROTO_PROPERTY = "subscriptionProto";
	private static final String SERVICE_UNIT_PROTO_PROPERTY = "serviceUnitProto";

	// These are the only searchable properties.  These must be added to entities directly.
	private static final String USER_USER_PROPERTY = "userUser";
	private static final String USER_TYPE_PROPERTY = "userType";
	private static final String USER_SERVICE_UNIT_ID_PROPERTY = "userServiceUnitId";
	private static final String USER_EMAIL_PROPERTY = "userEmail";
	private static final String SERVICE_UNIT_NAME_PROPERTY = "serviceUnitName";
	private static final String SERVICE_UNIT_ZONES_PROPERTY = "serviceUnitZone";

	Pair<User, com.google.appengine.api.users.User> getUserById(String userId) {
		return getUserFromEntity(get(KeyFactory.stringToKey(userId)));
	}

	Pair<User, com.google.appengine.api.users.User> getUserByGoogleUser(com.google.appengine.api.users.User user) {
		Iterator<Entity> userEntities = datastore.prepare(new Query(USER_KIND)
			.setFilter(new Query.FilterPredicate(USER_USER_PROPERTY, Query.FilterOperator.EQUAL, user)).setKeysOnly())
			.asIterator(FetchOptions.Builder.withLimit(1));
		if (!userEntities.hasNext()) {
			throw new CPDAOException("Could not find entity with user " + user);
		} else {
			return getUserFromEntity(get(userEntities.next().getKey()));
		}
	}

	Pair<User, com.google.appengine.api.users.User> getUserByEmail(String email) {
		Iterator<Entity> userEntities = datastore.prepare(new Query(USER_KIND)
			.setFilter(new Query.FilterPredicate(USER_EMAIL_PROPERTY, Query.FilterOperator.EQUAL, email))
			.setKeysOnly()).asIterator(FetchOptions.Builder.withLimit(1));
		if (!userEntities.hasNext()) {
			throw new CPDAOException("Could not find entity with email " + email);
		} else {
			return getUserFromEntity(get(userEntities.next().getKey()));
		}
	}

	List<Pair<User, com.google.appengine.api.users.User>> getAdministrators() {
		Iterator<Entity> userEntities = datastore.prepare(new Query(USER_KIND).setFilter(new Query.FilterPredicate(
				USER_TYPE_PROPERTY, Query.FilterOperator.EQUAL, UserType.ADMINISTRATOR.name())).setKeysOnly())
				.asIterator(FetchOptions.Builder.withLimit(MAX_NUM_ADMINS));
		if (!userEntities.hasNext()) {
			throw new CPDAOException("Could not find any administrators!");
		} else {
			List<Pair<User, com.google.appengine.api.users.User>> admins = CPUtil.newLinkedList();
			List<Entity> fullEntities = get(userEntities);
			for (Entity userEntity : fullEntities) {
				admins.add(getUserFromEntity(userEntity));
			}
			return admins;
		}
	}

	List<Pair<User, com.google.appengine.api.users.User>> getServiceUnitCoordinators(String serviceUnitId) {
		Iterator<Entity> userEntities = datastore.prepare(new Query(USER_KIND).setFilter(CompositeFilterOperator.and(
				new Query.FilterPredicate(
				USER_TYPE_PROPERTY, Query.FilterOperator.EQUAL, UserType.SERVICE_UNIT_COORDINATOR.name()),
				new Query.FilterPredicate(USER_SERVICE_UNIT_ID_PROPERTY, Query.FilterOperator.EQUAL, serviceUnitId)))
				.setKeysOnly()).asIterator(FetchOptions.Builder.withLimit(10));
		if (!userEntities.hasNext()) {
			throw new CPDAOException("Could not find any service unit coordinators for service unit" + serviceUnitId);
		} else {
			List<Pair<User, com.google.appengine.api.users.User>> coordinators = CPUtil.newLinkedList();
			List<Entity> fullEntities = get(userEntities);
			for (Entity userEntity : fullEntities) {
				coordinators.add(getUserFromEntity(userEntity));
			}
			return coordinators;
		}
	}

	void upsertUser(User user, com.google.appengine.api.users.User googleUser) {
		put(toEntity(user, googleUser));
	}

	void upsertUsers(Iterable<Pair<User, com.google.appengine.api.users.User>> users) {
		List<Entity> userEntities = CPUtil.newLinkedList();
		for (Pair<User, com.google.appengine.api.users.User> user : users) {
			userEntities.add(toEntity(user.first, user.second));
		}
		put(userEntities);
	}

	Subscription getSubscriptionById(String subId) {
		return getSubscriptionFromEntity(get(KeyFactory.stringToKey(subId)));
	}

	List<Subscription> getSubscriptionsByServiceUnitId(String unitId) {
		// Not limited...
		Iterator<Entity> subEntities = datastore.prepare(
				new Query(SUBSCRIPTION_KIND).setAncestor(KeyFactory.stringToKey(unitId)).setKeysOnly()).asIterator();
		List<Subscription> subs = new ArrayList<>();
		List<Entity> fullEntities = get(subEntities);
		for (Entity subEntity : fullEntities) {
			subs.add(getSubscriptionFromEntity(subEntity));
		}
		return subs;
	}

	void upsertSubscription(Subscription sub) {
		put(toEntity(sub));
	}

	void upsertSubscriptions(Iterable<Subscription> subs) {
		List<Entity> subEntities = new ArrayList<Entity>();
		for (Subscription sub : subs) {
			subEntities.add(toEntity(sub));
		}
		put(subEntities);
	}

	ServiceUnit getServiceUnitById(String unitId) {
		return getServiceUnitFromEntity(get(KeyFactory.stringToKey(unitId)));
	}

	ServiceUnit getServiceUnitByName(String name) {
		Iterator<Entity> unitEntities = datastore.prepare(new Query(SERVICE_UNIT_KIND)
			.setFilter(new Query.FilterPredicate(SERVICE_UNIT_NAME_PROPERTY, Query.FilterOperator.EQUAL, name))
			.setKeysOnly()).asIterator(FetchOptions.Builder.withLimit(1));
		if (!unitEntities.hasNext()) {
			throw new CPDAOException("Could not find service unit entity with name " + name);
		} else {
			return getServiceUnitFromEntity(get(unitEntities.next().getKey()));
		}
	}

	ServiceUnit getOwnerOfZone(Polygon zone) {
		Iterator<Entity> unitEntities = datastore.prepare(new Query(SERVICE_UNIT_KIND)
			.setFilter(new Query.FilterPredicate(SERVICE_UNIT_ZONES_PROPERTY, Query.FilterOperator.EQUAL,
			new ShortBlob(zone.toByteArray()))).setKeysOnly()).asIterator(FetchOptions.Builder.withLimit(1));
		if (!unitEntities.hasNext()) {
			throw new CPDAOException("Could not find service unit entity with zone " + zone);
		} else {
			return getServiceUnitFromEntity(get(unitEntities.next().getKey()));
		}
	}

	List<ServiceUnit> getAllServiceUnits() {
		Iterator<Entity> unitIdEntities = datastore.prepare(new Query(SERVICE_UNIT_KIND).setKeysOnly()).asIterator();
		List<Entity> unitEntities = get(unitIdEntities);
		List<ServiceUnit> units = CPUtil.newArrayListWithExpectedSize(unitEntities.size());
		for (Entity unitEntity : unitEntities) {
			units.add(getServiceUnitFromEntity(unitEntity));
		}
		return units;
	}

	void upsertServiceUnit(ServiceUnit unit) {
		put(toEntity(unit));
	}

	void upsertServiceUnits(Iterable<ServiceUnit> units) {
		List<Entity> unitEntities = new ArrayList<Entity>();
		for (ServiceUnit unit : units) {
			unitEntities.add(toEntity(unit));
		}
		put(unitEntities);
	}

	String createServiceUnit() {
		Entity newSU = toEntity(ServiceUnit.getDefaultInstance());
		return KeyFactory.keyToString(put(newSU));
	}

	private Entity toEntity(User user, com.google.appengine.api.users.User googleUser) {
		Entity userEntity = user.hasId() ? new Entity(KeyFactory.stringToKey(user.getId())) : new Entity(USER_KIND);
		if (user.hasType()) {
			userEntity.setProperty(USER_TYPE_PROPERTY, user.getType().name());
		}
		if (user.hasServiceUnitId()) {
			userEntity.setProperty(USER_SERVICE_UNIT_ID_PROPERTY, user.getServiceUnitId());
		}
		if (user.hasContactInfo() && user.getContactInfo().hasEmail()) {
			userEntity.setProperty(USER_EMAIL_PROPERTY, user.getContactInfo().getEmail());
		} else if (googleUser != null) {
			String email = googleUser.getEmail();
			if (!email.contains("@")) {
				email += "@" + googleUser.getAuthDomain();
			}
			userEntity.setProperty(USER_EMAIL_PROPERTY, email);
		}
		user = User.newBuilder(user).clearId().clearType().clearServiceUnitId().build();
		userEntity.setProperty(USER_PROTO_PROPERTY, new Blob(user.toByteArray()));
		if (googleUser != null) {
			userEntity.setProperty(USER_USER_PROPERTY, googleUser);
		}
		return userEntity;
	}

	private Pair<User, com.google.appengine.api.users.User> getUserFromEntity(Entity userEntity) {
		if (!userEntity.getKind().equals(USER_KIND)) {
			throw new CPDAOException(KeyFactory.keyToString(userEntity.getKey()) + " is not the key of a user!");
		}
		User.Builder builder = User.newBuilder().setId(KeyFactory.keyToString(userEntity.getKey()));
		try {
			builder.mergeFrom(((Blob) userEntity.getProperty(USER_PROTO_PROPERTY)).getBytes());
		} catch (InvalidProtocolBufferException e) {
			throw new CPRuntimeException("Invalid User proto read from datastore!", e);
		}
		if (userEntity.hasProperty(USER_TYPE_PROPERTY)) {
			builder.setType(UserType.valueOf((String) userEntity.getProperty(USER_TYPE_PROPERTY)));
		}
		if (userEntity.hasProperty(USER_SERVICE_UNIT_ID_PROPERTY)) {
			builder.setServiceUnitId((String) userEntity.getProperty(USER_SERVICE_UNIT_ID_PROPERTY));
		}
		com.google.appengine.api.users.User googleUser = null;
		if (userEntity.hasProperty(USER_USER_PROPERTY)) {
			googleUser = (com.google.appengine.api.users.User) userEntity.getProperty(USER_USER_PROPERTY);
		}
		return Pair.of(builder.build(), googleUser);
	}

	private Entity toEntity(Subscription sub) {
		if (!sub.hasServiceUnitId()) {
			throw new IllegalArgumentException("Subscriptions must have service units to be upserted!");
		}
		Key parent = KeyFactory.stringToKey(sub.getServiceUnitId());
		Entity subEntity = sub.hasId()
				? new Entity(KeyFactory.stringToKey(sub.getId())) : new Entity(SUBSCRIPTION_KIND, parent);
		sub = Subscription.newBuilder(sub).clearId().clearServiceUnitId().build();
		subEntity.setProperty(SUBSCRIPTION_PROTO_PROPERTY, new Blob(sub.toByteArray()));
		return subEntity;
	}

	private Subscription getSubscriptionFromEntity(Entity subEntity) {
		if (!subEntity.getKind().equals(SUBSCRIPTION_KIND)) {
			throw new CPDAOException(KeyFactory.keyToString(subEntity.getKey())
					+ " is not the key of a subscription!");
		}
		Subscription.Builder builder = Subscription.newBuilder().setId(KeyFactory.keyToString(subEntity.getKey()));
		builder.setServiceUnitId(KeyFactory.keyToString(subEntity.getKey().getParent()));
		try {
			builder.mergeFrom(((Blob) subEntity.getProperty(SUBSCRIPTION_PROTO_PROPERTY)).getBytes());
		} catch (InvalidProtocolBufferException e) {
			throw new CPRuntimeException("Invalid Subscription proto read from datastore!", e);
		}
		return builder.build();
	}

	private Entity toEntity(ServiceUnit unit) {
		Entity unitEntity = unit.hasId() ?
				new Entity(KeyFactory.stringToKey(unit.getId())) : new Entity(SERVICE_UNIT_KIND);
		List<Polygon> zones = unit.getDistributionZonesList();
		String name = unit.hasName() ? unit.getName() : null;
		unit = ServiceUnit.newBuilder(unit).clearId().clearDistributionZones().clearName().build();
		unitEntity.setProperty(SERVICE_UNIT_PROTO_PROPERTY, new Blob(unit.toByteArray()));
		// Store distribution zones and name separately so they're searchable
		if (name != null && ! "".equals(name)) {
			unitEntity.setProperty(SERVICE_UNIT_NAME_PROPERTY, name);
		}
		if (zones != null && !zones.isEmpty()) {
			List<ShortBlob> blobs = CPUtil.newLinkedList();
			for (Polygon zone : zones) {
				blobs.add(new ShortBlob(zone.toByteArray()));
			}
			unitEntity.setProperty(SERVICE_UNIT_ZONES_PROPERTY, blobs);
		}
		return unitEntity;
	}

	@SuppressWarnings("unchecked")
	private ServiceUnit getServiceUnitFromEntity(Entity unitEntity) {
		if (!unitEntity.getKind().equals(SERVICE_UNIT_KIND)) {
			throw new CPDAOException(KeyFactory.keyToString(unitEntity.getKey())
					+ " is not the key of a service unit!");
		}
		ServiceUnit.Builder builder = ServiceUnit.newBuilder().setId(KeyFactory.keyToString(unitEntity.getKey()));
		try {
			builder.mergeFrom(((Blob) unitEntity.getProperty(SERVICE_UNIT_PROTO_PROPERTY)).getBytes());
		} catch (InvalidProtocolBufferException e) {
			throw new CPRuntimeException("Invalid ServiceUnit read from datastore!", e);
		}
		if (unitEntity.hasProperty(SERVICE_UNIT_NAME_PROPERTY)) {
			builder.setName((String) unitEntity.getProperty(SERVICE_UNIT_NAME_PROPERTY));
		}
		if (unitEntity.hasProperty(SERVICE_UNIT_ZONES_PROPERTY)) {
			Collection<ShortBlob> blobs = (Collection<ShortBlob>) unitEntity.getProperty(SERVICE_UNIT_ZONES_PROPERTY);
			for (ShortBlob blob : blobs) {
				try {
					builder.addDistributionZones(Polygon.parseFrom(blob.getBytes()));
				} catch (InvalidProtocolBufferException e) {
					throw new CPRuntimeException("Invalid Polygon read from datastore!", e);
				}
			}
		}
		return builder.build();
	}

	private Entity get(Key key) {
		if (cache.contains(key)) {
			return (Entity) cache.get(key);
		} else {
			try {
				Entity result = datastore.get(key);
				cache.put(key, result);
				return result;
			} catch (EntityNotFoundException e) {
				throw new CPDAOException("Failed to find Entity with key " + key);
			}
		}
	}

	private List<Entity> get(Collection<Key> keys) {
		Map<Key, Object> fromCache = cache.getAll(keys);
		List<Entity> entities = CPUtil.newLinkedList();
		for (Object cached : fromCache.values()) {
			entities.add((Entity) cached);
		}
		List<Key> stillNeeded = CPUtil.newLinkedList(keys);
		stillNeeded.removeAll(fromCache.keySet());
		Map<Key, Entity> notCached = datastore.get(stillNeeded);
		cache.putAll(notCached);
		for (Entity entity : notCached.values()) {
			entities.add(entity);
		}
		return entities;
	}

	private List<Entity> get(Iterator<Entity> keys) {
		List<Key> extractedKeys = CPUtil.newLinkedList();
		while (keys.hasNext()) {
			extractedKeys.add(keys.next().getKey());
		}
		return get(extractedKeys);
	}

	private Key put(Entity entity) {
		cache.put(entity.getKey(), entity);
		return datastore.put(entity);
	}

	private void put(Collection<Entity> entities) {
		Map<Key, Entity> entityMap = CPUtil.newHashMap();
		for (Entity entity : entities) {
			entityMap.put(entity.getKey(), entity);
		}
		cache.putAll(entityMap);
		datastore.put(entities);
	}

	void delete(String id) {
		delete(KeyFactory.stringToKey(id));
	}
	private void delete(Key key) {
		cache.delete(key);
		datastore.delete(key);
	}

	class CPDAOException extends RuntimeException {
		public CPDAOException(String msg, Throwable t) {
			super(msg, t);
		}
		public CPDAOException(String msg) {
			super(msg);
		}
	}
}
