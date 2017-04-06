package com.tle.core.payment.converter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.common.NameValue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.payment.converter.xstream.SubscriptionPeriodXmlConverter;
import com.tle.core.payment.dao.PriceDao;
import com.tle.core.payment.dao.PricingTierDao;
import com.tle.core.payment.dao.SubscriptionPeriodDao;
import com.tle.core.services.entity.BaseEntityXmlConverter;
import com.tle.core.services.entity.EntityRegistry;
import com.tle.web.i18n.BundleNameValue;

@Bind
@Singleton
@SuppressWarnings("nls")
public class PriceConverter extends AbstractConverter<Price>
{
	private static final String PREFIX = "com.tle.core.payment.backend.";
	private static final String IMPORT_EXPORT_FOLDER = "price";

	@Inject
	private EntityRegistry registry;
	@Inject
	private PriceDao priceDao;
	@Inject
	private PricingTierDao tierDao;
	@Inject
	private SubscriptionPeriodDao periodDao;

	public static SubTemporaryFile getFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		for( PricingTier tier : tierDao.enumerateAll() )
		{
			for( Price price : priceDao.enumerateAllForSubscriptionTier(tier) )
			{
				priceDao.delete(price);
			}
		}
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile allPricesFolder = new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
		xmlHelper.writeExportFormatXmlFile(allPricesFolder, true);

		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new SubscriptionPeriodXmlConverter(periodDao));
		xstream.registerConverter(new BaseEntityXmlConverter(registry));

		for( PricingTier tier : tierDao.enumerateAllIncludingSystem() )
		{
			if( tier.isPurchase() )
			{
				exportPrice(allPricesFolder, priceDao.getForPurchaseTier(tier), xstream);
			}
			else
			{
				for( Price price : priceDao.enumerateAllForSubscriptionTier(tier) )
				{
					exportPrice(allPricesFolder, price, xstream);
				}
			}
		}
	}

	private void exportPrice(SubTemporaryFile allPricesFolder, Price price, XStream xstream)
	{
		final BucketFile bucketFolder = new BucketFile(allPricesFolder, price.getId());
		xmlHelper.writeXmlFile(bucketFolder, price.getId() + ".xml", price, xstream);
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile allPricesFolder = new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);

		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new SubscriptionPeriodXmlConverter(periodDao));
		xstream.registerConverter(new BaseEntityXmlConverter(registry));

		final List<String> entries = xmlHelper.getXmlFileList(allPricesFolder);

		for( String entry : entries )
		{
			final Price price = xmlHelper.readXmlFile(allPricesFolder, entry, xstream);
			price.setId(0);

			priceDao.save(price);
			priceDao.flush();
			priceDao.clear();
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
		return new BundleNameValue(PREFIX + "converter.price.name", getStringId());
	}

	@Override
	public String getStringId()
	{
		return "PRICE";
	}
}
