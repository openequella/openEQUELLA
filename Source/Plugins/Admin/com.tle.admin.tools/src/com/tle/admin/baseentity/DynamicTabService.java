package com.tle.admin.baseentity;

import javax.swing.JComponent;

public interface DynamicTabService
{
	void addTab(JComponent component, String tabTitle, int index);

	void removeTab(JComponent component);
}
