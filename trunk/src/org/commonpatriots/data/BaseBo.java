package org.commonpatriots.data;

import org.commonpatriots.util.CPUtil;

import com.google.inject.Provider;

abstract class BaseBo<T> {
	protected Provider<BaseDAO> daoProvider;
	protected boolean isOpen;
	
	public abstract void open();
	public abstract boolean open(String id); // Returns true if match already exists, false otherwise
	public abstract void open(T data);
	
	public abstract void save();
	
	public abstract T toDataObject();

	public void close() {
		isOpen = false;
	}
	
	protected boolean checkIsOpen() {
		return CPUtil.checkPrecondition(isOpen);
	}
}
