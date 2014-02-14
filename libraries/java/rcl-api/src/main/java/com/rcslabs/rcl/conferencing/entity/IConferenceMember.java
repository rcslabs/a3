package com.rcslabs.rcl.conferencing.entity;

/**
 * A conference member (read-only interface).
 *
 */
public interface IConferenceMember extends Cloneable {

	String getPhoneNumber();

	String getUserName();

	IConferenceMember clone();

}