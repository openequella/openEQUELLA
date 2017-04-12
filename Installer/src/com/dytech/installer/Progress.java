/*
 * Created on Nov 9, 2004
 */
package com.dytech.installer;

/**
 * @author Nicholas Read
 */
public interface Progress
{
	void addMessage(String msg);

	void setWholeAmount(int i);

	void setCurrentAmount(int i);

	int getWholeAmount();

	int getCurrentAmount();

	void setCurrentMaximum(int maximum);

	int getCurrentMaximum();

	int getWholeMaximum();

	void setup(String title, int total);

	void popupMessage(String title, String message, boolean error);
}