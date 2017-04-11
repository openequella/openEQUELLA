package com.tle.jpfclasspath.model;

import java.util.List;
import java.util.Set;

public interface IRepoModel extends IModel
{
	Set<String> getParentRepos();

	List<? extends IPluginModel> getJarModels();

	String getName();
}
