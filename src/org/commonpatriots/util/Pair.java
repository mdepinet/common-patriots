package org.commonpatriots.util;

public class Pair<T1, T2> {
	public T1 first;
	public T2 second;

	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
		return new Pair<T1, T2>(first, second);
	}

	public T1 getFirst() { // For use in JSP
		return first;
	}

	public T2 getSecond() { // For use in JSP
		return second;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Pair<?, ?>) {
			Pair<?, ?> other = (Pair<?, ?>) object;
			return (this.first == null ? other.first == null : this.first.equals(other.first))
					&& (this.second == null ? other.second == null : this.second.equals(other.second)); 
		} else {
			return false;
		}
	}
}
