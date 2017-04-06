package com.tle.core.security;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;

public interface SecurityPostProcessor
{
	void postProcess(Node node, String target, Object domainObj, TargetList oldTL, TargetList newTL);
}
