package com.tle.web.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Throwables;

public class WizardMetadataMapper implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;

	private boolean mapNow;
	private String packageExtractedFolder;
	private final List<String> htmlMappedFiles = new ArrayList<String>();

	public String getPackageExtractedFolder()
	{
		return packageExtractedFolder;
	}

	public void setPackageExtractedFolder(String packageExtractedFolder)
	{
		this.packageExtractedFolder = packageExtractedFolder;
	}

	public List<String> getHtmlMappedFiles()
	{
		return htmlMappedFiles;
	}

	public void addHtmlMappedFile(String filename)
	{
		htmlMappedFiles.add(filename);
	}

	public void addHtmlMappedFiles(Collection<String> fileNames)
	{
		htmlMappedFiles.addAll(fileNames);
	}

	public boolean isMapNow()
	{
		return mapNow;
	}

	public void setMapNow(boolean mapNow)
	{
		this.mapNow = mapNow;
	}

	@Override
	protected WizardMetadataMapper clone()
	{
		try
		{
			return (WizardMetadataMapper) super.clone();
		}
		catch( CloneNotSupportedException e )
		{
			throw Throwables.propagate(e);
		}
	}
}
