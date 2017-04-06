package com.tle.web.service.oai;

import java.util.Properties;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class OAIModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		// provides methods
	}

	@SuppressWarnings("nls")
	@Provides
	@Named("oaiProps")
	Properties provideProperties()
	{
		Properties props = new Properties();
		props.put("AbstractCatalog.oaiCatalogClassName", OAICatalog.class.getName());
		props.put("AbstractCatalog.recordFactoryClassName", XMLRecordFactory.class.getName());
		props.put("Identify.repositoryName", "EQUELLA");
		props.put("AbstractCatalog.granularity", "YYYY-MM-DDThh:mm:ssZ");
		props.put("Identify.earliestDatestamp", "1998-01-01T00:00:00Z");
		props.put("Identify.deletedRecord", "transient");
		props.put("OAIHandler.urlEncodeSetSpec", "false");
		return props;
	}

	@SuppressWarnings({"nls", "deprecation"})
	@Provides
	@Named("oaiLegacyProps")
	Properties provideLegacyProperties()
	{
		Properties props = new Properties();
		props.put("AbstractCatalog.oaiCatalogClassName", com.tle.web.service.oai.legacy.OAICatalog.class.getName());
		props.put("AbstractCatalog.recordFactoryClassName",
			com.tle.web.service.oai.legacy.XMLRecordFactory.class.getName());
		props.put("Identify.repositoryName", "The Learning Edge");
		props.put("AbstractCatalog.granularity", "YYYY-MM-DDThh:mm:ssZ");
		props.put("Identify.earliestDatestamp", "1998-01-01T00:00:00Z");
		props.put("Identify.deletedRecord", "transient");
		return props;
	}
}
