package com.tle.core.payment.converter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.NameValue;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.payment.converter.initialiser.StoreEntitiesInitialiserCallback;
import com.tle.core.payment.dao.SubscriptionPeriodDao;
import com.tle.web.i18n.BundleNameValue;

@Bind
@Singleton
@SuppressWarnings("nls")
public class SubscriptionPeriodConverter extends AbstractConverter<Object>
{
	private static final String PREFIX = "com.tle.core.payment.backend.";
	private static final String IMPORT_EXPORT_FOLDER = "subsperiod";

	@Inject
	private SubscriptionPeriodDao spDao;

	public static SubTemporaryFile getFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		spDao.deleteAll();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile allPeriodsExportFolder = new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
		xmlHelper.writeExportFormatXmlFile(allPeriodsExportFolder, true);

		List<SubscriptionPeriod> periods = spDao.enumerateAll();
		for( SubscriptionPeriod period : periods )
		{
			initialiserService.initialise(period, new StoreEntitiesInitialiserCallback());

			final BucketFile bucketFolder = new BucketFile(allPeriodsExportFolder, period.getId());
			xmlHelper.writeXmlFile(bucketFolder, period.getId() + ".xml", period);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile allPeriodsImportFolder = new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
		final XStream xstream = xmlHelper.createXStream();
		final List<String> entries = xmlHelper.getXmlFileList(allPeriodsImportFolder);

		for( String entry : entries )
		{
			final SubscriptionPeriod period = xmlHelper.readXmlFile(allPeriodsImportFolder, entry, xstream);
			period.setInstitution(institution);
			period.setId(0);

			LanguageBundle languageBundle = period.getName();
			languageBundle.setId(0);
			Map<String, LanguageString> langStrings = languageBundle.getStrings();
			for( LanguageString ls : langStrings.values() )
			{
				ls.setId(0);
			}

			spDao.save(period);
			spDao.flush();
			spDao.clear();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	@Override
	protected NameValue getStandardTask()
	{
		return new BundleNameValue(PREFIX + "converter.period.name", getStringId());
	}

	@Override
	public String getStringId()
	{
		return "SUBSPERIOD";
	}
}
