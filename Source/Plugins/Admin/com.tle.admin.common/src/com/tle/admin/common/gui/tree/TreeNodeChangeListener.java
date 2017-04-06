package com.tle.admin.common.gui.tree;

import java.util.EventListener;

import com.tle.common.LazyTreeNode;

public interface TreeNodeChangeListener extends EventListener
{
	void nodeSaved(LazyTreeNode node);
}