package com.tle.ant.dependencies;

import java.util.List;

import com.google.common.collect.Lists;

public class Dep
{
	private String groupId;
	private String artifactId;
	private String version;
	private String extension = "jar";
	private String classifier = ""; //$NON-NLS-1$
	private List<String> excludes = Lists.newArrayList();
	private List<String> jpfIncludes = Lists.newArrayList();
	private List<String> jpfExports = Lists.newArrayList();
	private String jpfFragment;
	private String rename;
	private boolean noSource;

	public Dep()
	{

	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}

	public String getArtifactId()
	{
		return artifactId;
	}

	public void setArtifactId(String artifactId)
	{
		this.artifactId = artifactId;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public List<String> getExcludes()
	{
		return excludes;
	}

	public void setExcludes(List<String> excludes)
	{
		this.excludes = excludes;
	}

	public List<String> getJpfIncludes()
	{
		return jpfIncludes;
	}

	public void setJpfIncludes(List<String> jpfIncludes)
	{
		this.jpfIncludes = jpfIncludes;
	}

	public boolean isNoSource()
	{
		return noSource;
	}

	public void setNoSource(boolean noSource)
	{
		this.noSource = noSource;
	}

	public List<String> getJpfExports()
	{
		return jpfExports;
	}

	public void setJpfExports(List<String> jpfExports)
	{
		this.jpfExports = jpfExports;
	}

	public String getClassifier()
	{
		return classifier;
	}

	public void setClassifier(String classifier)
	{
		this.classifier = classifier;
	}

	public String getExtension()
	{
		return extension;
	}

	public void setExtension(String extension)
	{
		this.extension = extension;
	}

	public String getJpfFragment()
	{
		return jpfFragment;
	}

	public void setJpfFragment(String jpfFragment)
	{
		this.jpfFragment = jpfFragment;
	}

	public String getRename()
	{
		return rename;
	}

	public void setRename(String rename)
	{
		this.rename = rename;
	}

}