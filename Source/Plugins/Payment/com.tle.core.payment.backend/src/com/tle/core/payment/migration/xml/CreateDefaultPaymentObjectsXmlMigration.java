package com.tle.core.payment.migration.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.payment.entity.SubscriptionPeriod.DurationUnit;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.payment.converter.SubscriptionPeriodConverter;

@Bind
@Singleton
public class CreateDefaultPaymentObjectsXmlMigration extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		final SubTemporaryFile subsFolder = SubscriptionPeriodConverter.getFolder(staging);
		if( !fileExists(subsFolder, "") )
		{
			final List<SubscriptionPeriod> periods = getDefaultPeriods(instInfo.getInstitution());
			xmlHelper.writeExportFormatXmlFile(subsFolder, true);

			int i = 0;
			for( SubscriptionPeriod period : periods )
			{
				i++;
				final BucketFile bucketFolder = new BucketFile(subsFolder, i);
				xmlHelper.writeXmlFile(bucketFolder, i + ".xml", period);
			}
		}
	}

	private static List<SubscriptionPeriod> getDefaultPeriods(Institution i)
	{
		final List<SubscriptionPeriod> periods = Lists.newArrayList();
		// i18n? What's the point? Also, how does one edit this?
		periods.add(createPeriod(i, "Week", 1, DurationUnit.WEEKS));
		periods.add(createPeriod(i, "Month", 1, DurationUnit.MONTHS));
		periods.add(createPeriod(i, "3 Months", 3, DurationUnit.MONTHS));
		periods.add(createPeriod(i, "6 Months", 6, DurationUnit.MONTHS));
		periods.add(createPeriod(i, "Year", 1, DurationUnit.YEARS));
		return periods;
	}

	private static SubscriptionPeriod createPeriod(Institution i, String name, int duration, DurationUnit unit)
	{
		final SubscriptionPeriod p = new SubscriptionPeriod();
		p.setInstitution(i);
		p.setUuid(UUID.randomUUID().toString());
		p.setName(createName(name));
		p.setDuration(duration);
		p.setDurationUnit(unit);
		return p;
	}

	private static LanguageBundle createName(String name)
	{
		LanguageBundle bundle = new LanguageBundle();
		Map<String, LanguageString> strings = new HashMap<String, LanguageString>();
		LanguageString langString = new LanguageString();
		langString.setBundle(bundle);
		langString.setLocale("en");
		langString.setText(name);
		strings.put("en", langString);
		bundle.setStrings(strings);
		return bundle;
	}
}
