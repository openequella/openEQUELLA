package com.tle.admin.gui.common;

import java.awt.Component;
import java.awt.event.KeyListener;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Nicholas Read
 */
public interface TreeWithViewInterface<NODE_TYPE extends DefaultMutableTreeNode>
{
	Component getComponent();

	void setup();

	void addNameListener(KeyListener listener);

	void load(NODE_TYPE element);

	void save(NODE_TYPE element);
}
