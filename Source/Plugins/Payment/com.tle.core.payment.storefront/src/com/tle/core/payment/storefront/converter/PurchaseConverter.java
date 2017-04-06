package com.tle.core.payment.storefront.converter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.NameValue;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.EntityInitialiserCallback;
import com.tle.core.payment.storefront.dao.PurchaseDao;
import com.tle.core.payment.storefront.dao.PurchasedContentDao;
import com.tle.core.services.entity.BaseEntityXmlConverter;
import com.tle.core.services.entity.EntityRegistry;
import com.tle.core.services.entity.IdOnlyConverter;
import com.tle.web.i18n.BundleNameValue;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class PurchaseConverter extends AbstractConverter<Object>
{
	private static final String PREFIX = "com.tle.core.payment.storefront.";
	private static final String IMPORT_EXPORT_FOLDER = "purchase";
	private static final String IMPORT_EXPORT_FOLDER2 = "purchase_content";

	@Inject
	private PurchaseDao dao;
	@Inject
	private PurchasedContentDao contentDao;
	@Inject
	private EntityRegistry entityRegistry;

	public static SubTemporaryFile getFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
	}

	public static SubTemporaryFile getContentFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER2);
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		dao.deleteAll();
		contentDao.deleteAll();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(entityRegistry));
		xstream.registerConverter(new IdOnlyConverter(Item.class));

		final SubTemporaryFile allPurchasesExportFolder = getFolder(staging);
		xmlHelper.writeExportFormatXmlFile(allPurchasesExportFolder, true);
		for( Purchase purchase : dao.enumerateAll() )
		{
			initialiserService.initialise(purchase, new EntityInitialiserCallback());
			purchase.setInstitution(null);

			final BucketFile bucketFolder = new BucketFile(allPurchasesExportFolder, purchase.getId());
			xmlHelper.writeXmlFile(bucketFolder, purchase.getId() + ".xml", purchase, xstream);
		}

		final SubTemporaryFile allContentExportFolder = getContentFolder(staging);
		xmlHelper.writeExportFormatXmlFile(allContentExportFolder, true);
		for( PurchasedContent content : contentDao.enumerateAll() )
		{
			initialiserService.initialise(content, new EntityInitialiserCallback());
			content.setInstitution(null);

			final BucketFile bucketFolder = new BucketFile(allContentExportFolder, content.getId());
			xmlHelper.writeXmlFile(bucketFolder, content.getId() + ".xml", content, xstream);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(entityRegistry));
		xstream.registerConverter(new IdOnlyConverter(Item.class));

		final SubTemporaryFile allPurchasesImportFolder = getFolder(staging);
		for( String entry : xmlHelper.getXmlFileList(allPurchasesImportFolder) )
		{
			final Purchase purchase = xmlHelper.readXmlFile(allPurchasesImportFolder, entry, xstream);
			purchase.setId(0);
			purchase.setInstitution(institution);

			for( PurchaseItem purchaseItem : purchase.getPurchaseItems() )
			{
				purchaseItem.setId(0);
				purchaseItem.setPurchase(purchase);
			}

			dao.save(purchase);
			dao.flush();
			dao.clear();
		}

		final SubTemporaryFile allContentImportFolder = getContentFolder(staging);
		final List<String> contentEntries = xmlHelper.getXmlFileList(allContentImportFolder);

		for( String entry : contentEntries )
		{
			final PurchasedContent content = xmlHelper.readXmlFile(allContentImportFolder, entry, xstream);
			content.setId(0);
			content.setInstitution(institution);

			contentDao.save(content);
			contentDao.flush();
			contentDao.clear();
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
		return new BundleNameValue(PREFIX + "converter.purchase.name", getStringId());
	}

	@Override
	public String getStringId()
	{
		return "PURCHASE";
	}
}
