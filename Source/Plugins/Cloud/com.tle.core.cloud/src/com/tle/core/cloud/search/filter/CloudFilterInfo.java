package com.tle.core.cloud.search.filter;

import java.util.List;

import com.tle.common.NameValue;

public class CloudFilterInfo
{
	private List<NameValue> languages;
	private List<NameValue> licences;
	private List<NameValue> publishers;
	private List<NameValue> educationLevels;

	public List<NameValue> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<NameValue> languages)
	{
		this.languages = languages;
	}

	public List<NameValue> getLicences()
	{
		return licences;
	}

	public void setLicences(List<NameValue> licences)
	{
		this.licences = licences;
	}

	public List<NameValue> getPublishers()
	{
		return publishers;
	}

	public void setPublishers(List<NameValue> publishers)
	{
		this.publishers = publishers;
	}

	public List<NameValue> getEducationLevels()
	{
		return educationLevels;
	}

	public void setEducationLevels(List<NameValue> educationLevels)
	{
		this.educationLevels = educationLevels;
	}
}
