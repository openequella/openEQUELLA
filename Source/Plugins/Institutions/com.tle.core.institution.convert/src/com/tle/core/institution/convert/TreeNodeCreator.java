package com.tle.core.institution.convert;

import com.tle.common.institution.TreeNodeInterface;

public interface TreeNodeCreator<T extends TreeNodeInterface<T>>
{
	T createNode();
}
