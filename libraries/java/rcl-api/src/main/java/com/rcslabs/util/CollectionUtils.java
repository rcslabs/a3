package com.rcslabs.util;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionUtils {
	private CollectionUtils() {}

	/**
	 * A collection group operation.
	 *
	 * @param <T> type of collection element
	 */
	public static interface Operation <T> {
		
		/**
		 * Operation that should be performed for each element in collection.
		 * @param element an element of collection
		 * @return if false, the iteration should break on this current element
		 */
		boolean run(T element);
	}
	
	/**
	 * Runs an operation over each element in collection
	 * @param collection a target collection
	 * @param operation an operation to run
	 */
	public static <T> void forEach(Collection<T> collection, Operation<T> operation) {
		for(T element : collection) {
			if(!operation.run(element)) break;
		}
	}
	
	/**
	 * A collection criteria.
	 *
	 * @param <T> type of collection element
	 */
	public static interface Criteria <T> {
		
		/**
		 * Returns true if an element satisfies the criteria, false otherwise.
		 */
		boolean satisfies(T element);
	}

	/**
	 * Returns a sub-collection for the specified collection.
	 * @param collection a target collection
	 * @param criteria a criteria to choose elements
	 * @return the resulting sub-collection
	 */
	public static <T> Collection<T> subCollection(Collection<T> collection, Criteria<T> criteria) {
		Collection<T> ret = new ArrayList<T>();
		for(T element : collection) {
			if(criteria.satisfies(element)) {
				ret.add(element);
			}
		}
		
		return ret;
	}
	
	/**
	 * Returns a first element from collection that satisfies the specified criteria.
	 * 
	 * @param collection a target collection
	 * @param criteria a criteria to choose element
	 * @return a first found element or null, if no elements match the criteria
	 */
	public static <T> T findSingle(Collection<T> collection, Criteria<T> criteria) {
		return findSingle(collection, criteria, null);
	}
	
	/**
	 * Returns a first element from collection that satisfies the specified criteria
	 * or default value, if no element satisfies.
	 * 
	 * @param collection a target collection
	 * @param criteria a criteria to choose element
	 * @param defaultValue a default value if no element found
	 * @return a first found element or defaultValue, if no elements match the criteria
	 */
	public static <T> T findSingle(Collection<T> collection, Criteria<T> criteria, T defaultValue) {
		for(T element : collection) {
			if(criteria.satisfies(element)) {
				return element;
			}
		}
		return defaultValue;
	}
	
	/**
	 * Runs an operation over each element in collection,
	 * that meets the specified criteria.
	 * 
	 * @param collection a collection
	 * @param criteria a criteria
	 * @param operation an operation to run
	 */
	public static <T> void forEach(
			Collection<T> collection, 
			Criteria<T> criteria, 
			Operation<T> operation) 
	{
		for(T element : collection) {
			if(criteria.satisfies(element)) {
				if(!operation.run(element)) break;
			}
		}
	}
}
