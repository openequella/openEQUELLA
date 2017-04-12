package com.tle.admin.gui.common;

import java.awt.Component;
import java.awt.event.KeyListener;

import com.dytech.gui.Changeable;

/**
 * @author Nicholas Read
 */
public interface ListWithViewInterface<LIST_TYPE> extends Changeable
{
	Component getComponent();

	void setup();

	void addNameListener(KeyListener listener);

	void load(LIST_TYPE element);

	void save(LIST_TYPE element);
}
