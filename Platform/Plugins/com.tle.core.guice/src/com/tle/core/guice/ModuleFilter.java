package com.tle.core.guice;

import java.util.List;

import com.google.inject.Module;

public interface ModuleFilter
{
	List<Module> filterModules(List<Module> modules);
}
