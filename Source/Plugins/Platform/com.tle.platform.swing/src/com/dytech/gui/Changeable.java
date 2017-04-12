/*
 * Created on Oct 13, 2004
 */
package com.dytech.gui;

/**
 * @author Nicholas Read
 */
public interface Changeable
{
	/**
	 * Indicates whether a change has been detected since construction of the
	 * <code>Changeable</code> component, or the invocation of
	 * <code>clearChanges</code>.
	 * 
	 * @return true if a change has occured, else false.
	 */
	boolean hasDetectedChanges();

	/**
	 * Wipes any detected changes.
	 */
	void clearChanges();
}
