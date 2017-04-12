package com.tle.core.payment.converter;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Maps;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.NameValue;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.payment.converter.initialiser.StoreEntitiesInitialiserCallback;
import com.tle.core.payment.converter.xstream.SaleXmlConverter;
import com.tle.core.payment.converter.xstream.SubscriptionPeriodXmlConverter;
import com.tle.core.payment.dao.SaleDao;
import com.tle.core.payment.dao.StoreHarvestInfoDao;
import com.tle.core.payment.dao.SubscriptionPeriodDao;
import com.tle.core.services.entity.BaseEntityXmlConverter;
import com.tle.core.services.entity.EntityRegistry;
import com.tle.core.services.entity.IdOnlyConverter;
import com.tle.web.i18n.BundleNameValue;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class SaleConverter extends AbstractConverter<Object>
{
	private static final String PREFIX = "com.tle.core.payment.backend.";
	private static final String IMPORT_EXPORT_FOLDER = "sale";
	private static final String HARVEST_INFO_FOLDER = "harvestinfo";

	@Inject
	private SaleDao saleDao;
	@Inject
	private SubscriptionPeriodDao periodDao;
	@Inject
	private StoreHarvestInfoDao harvestInfoDao;

	@Inject
	private EntityRegistry entityRegistry;

	public static SubTemporaryFile getFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
	}

	public static SubTemporaryFile getHarvestFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, HARVEST_INFO_FOLDER);
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		harvestInfoDao.deleteAll();
		saleDao.deleteAll();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(entityRegistry));
		xstream.registerConverter(new IdOnlyConverter(Item.class));
		xstream.registerConverter(new SubscriptionPeriodXmlConverter(periodDao));

		final SubTemporaryFile allSalesExportFolder = getFolder(staging);
		xmlHelper.writeExportFormatXmlFile(allSalesExportFolder, true);

		for( Sale sale : saleDao.enumerateAll() )
		{
			initialiserService.initialise(sale, new StoreEntitiesInitialiserCallback());

			final BucketFile bucketFolder = new BucketFile(allSalesExportFolder, sale.getId());
			xmlHelper.writeXmlFile(bucketFolder, sale.getId() + ".xml", sale, xstream);
		}

		xstream.registerConverter(new SaleXmlConverter(saleDao));

		final SubTemporaryFile allHarvestInfoFolder = new SubTemporaryFile(staging, HARVEST_INFO_FOLDER);
		xmlHelper.writeExportFormatXmlFile(allHarvestInfoFolder, true);
		for( StoreHarvestInfo hinfo : harvestInfoDao.enumerateAll() )
		{
			initialiserService.initialise(hinfo, new StoreEntitiesInitialiserCallback());

			final BucketFile bucketFolder = new BucketFile(allHarvestInfoFolder, hinfo.getId());
			xmlHelper.writeXmlFile(bucketFolder, hinfo.getId() + ".xml", hinfo, xstream);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(entityRegistry));
		xstream.registerConverter(new IdOnlyConverter(Item.class));
		xstream.registerConverter(new SubscriptionPeriodXmlConverter(periodDao));

		final Map<String, Sale> saleMap = Maps.newHashMap();

		final SubTemporaryFile allSalesImportFolder = getFolder(staging);
		for( String entry : xmlHelper.getXmlFileList(allSalesImportFolder) )
		{
			final Sale sale = xmlHelper.readXmlFile(allSalesImportFolder, entry, xstream);
			sale.setId(0);
			sale.setInstitution(institution);

			for( SaleItem saleItem : sale.getSales() )
			{
				saleItem.setId(0);
				saleItem.setSale(sale);
			}
			saleMap.put(sale.getUuid(), sale);

			saleDao.save(sale);
			saleDao.flush();
			saleDao.clear();
		}

		xstream.registerConverter(new SaleXmlConverter(saleDao));

		final SubTemporaryFile allHarvestImportFolder = getHarvestFolder(staging);
		for( String entry : xmlHelper.getXmlFileList(allHarvestImportFolder) )
		{
			final StoreHarvestInfo hinfo = xmlHelper.readXmlFile(allHarvestImportFolder, entry, xstream);

			harvestInfoDao.save(hinfo);
			harvestInfoDao.flush();
			harvestInfoDao.clear();
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
		return new BundleNameValue(PREFIX + "converter.sale.name", getStringId());
	}

	@Override
	public String getStringId()
	{
		return "SALE";
	}
}
