package org.commonpatriots.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.commonpatriots.CPRuntimeException;

public final class CPUtil {

	public static boolean checkPrecondition(boolean condition) {
		if (condition) {
			return true;
		} else {
			throw new CPRuntimeException("Precondition check failed");
		}
	}

	public static <T> LinkedList<T> newLinkedList() {
		return new LinkedList<T>();
	}

	public static <T> LinkedList<T> newLinkedList(Iterable<T> items) {
		LinkedList<T> list = new LinkedList<T>();
		for (T item : items) {
			list.add(item);
		}
		return list;
	}

	@SafeVarargs
	public static <T> LinkedList<T> newLinkedList(T... items) {
		LinkedList<T> list = new LinkedList<T>();
		for (T item : items) {
			list.add(item);
		}
		return list;
	}

	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}

	public static <T> ArrayList<T> newArrayList(Iterable<T> items) {
		ArrayList<T> list = new ArrayList<T>();
		for (T item : items) {
			list.add(item);
		}
		return list;
	}

	@SafeVarargs
	public static <T> ArrayList<T> newArrayList(T... items) {
		ArrayList<T> list = new ArrayList<T>();
		for (T item : items) {
			list.add(item);
		}
		return list;
	}

	public static <T> ArrayList<T> newArrayListWithExpectedSize(int size) {
		return new ArrayList<T>(size);
	}

	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}
}
