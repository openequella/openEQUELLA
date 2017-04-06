package com.tle.core.workflow.daily;

import com.google.common.collect.Multimap;
import com.tle.core.guice.BindFactory;
import com.tle.core.workflow.daily.NewItemFilter.NewItemOperation;

@BindFactory
public interface NewItemFactory
{
	NewItemFilter createFilter(Multimap<String, String> collectionMap);

	NewItemOperation createOperation(Multimap<String, String> collectionMap);
}