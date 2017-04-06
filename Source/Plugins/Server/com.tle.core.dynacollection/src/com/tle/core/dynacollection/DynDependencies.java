package com.tle.core.dynacollection;

import java.util.Set;

import com.google.common.collect.Sets;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.DynaCollection;
import com.tle.core.migration.ClassDependencies;

public class DynDependencies extends ClassDependencies
{
	public static Set<Class<?>> dynamicCollection()
	{
		final Set<Class<?>> deps = Sets.newHashSet();
		deps.add(DynaCollection.class);
		deps.add(ItemDefinitionScript.class);
		deps.add(SchemaScript.class);
		deps.addAll(baseEntity());
		return deps;
	}
}
