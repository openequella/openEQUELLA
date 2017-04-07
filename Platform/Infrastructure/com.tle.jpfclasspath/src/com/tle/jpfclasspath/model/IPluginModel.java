package com.tle.jpfclasspath.model;

import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import com.tle.jpfclasspath.parser.ModelPluginManifest;

public interface IPluginModel extends IModel
{
	String DEFAULT_REGISTRY = "default";

	IJavaProject getJavaProject();

	boolean isFragmentModel();

	ModelPluginManifest getParsedManifest();

	List<IClasspathEntry> createClasspathEntries();

	String getRegistryName();

	boolean isJarModel();
}
