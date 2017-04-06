package com.tle.core.payment.storefront.converter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.common.NameValue;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.OrderHistory;
import com.tle.common.payment.storefront.entity.OrderItem;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.EntityInitialiserCallback;
import com.tle.core.payment.storefront.dao.OrderDao;
import com.tle.core.services.entity.BaseEntityXmlConverter;
import com.tle.core.services.entity.EntityRegistry;
import com.tle.web.i18n.BundleNameValue;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class OrderConverter extends AbstractConverter<Object>
{
	private static final String PREFIX = "com.tle.core.payment.storefront.";
	private static final String IMPORT_EXPORT_FOLDER = "order";

	@Inject
	private OrderDao dao;
	@Inject
	private EntityRegistry entityRegistry;

	public static SubTemporaryFile getFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		dao.deleteAll();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(entityRegistry));

		final SubTemporaryFile allOrdersExportFolder = new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
		xmlHelper.writeExportFormatXmlFile(allOrdersExportFolder, true);
		for( Order order : dao.enumerateAll() )
		{
			// I hate Hibernate. With a passion.
			initialiserService.initialise(order, new EntityInitialiserCallback());
			order.setInstitution(null);

			final BucketFile bucketFolder = new BucketFile(allOrdersExportFolder, order.getId());
			xmlHelper.writeXmlFile(bucketFolder, order.getId() + ".xml", order, xstream);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(entityRegistry));

		final SubTemporaryFile allOrdersImportFolder = new SubTemporaryFile(staging, IMPORT_EXPORT_FOLDER);
		final List<String> entries = xmlHelper.getXmlFileList(allOrdersImportFolder);
		for( String entry : entries )
		{
			final Order order = xmlHelper.readXmlFile(allOrdersImportFolder, entry, xstream);
			order.setInstitution(institution);
			order.setId(0);

			for( OrderStorePart orderStore : order.getStoreParts() )
			{
				orderStore.setId(0);
				orderStore.setOrder(order);

				for( OrderItem orderItem : orderStore.getOrderItems() )
				{
					orderItem.setId(0);
					orderItem.setOrderStorePart(orderStore);
				}
			}

			for( OrderHistory history : order.getHistory() )
			{
				history.setId(0);
				history.setOrder(order);
			}

			dao.save(order);
			dao.flush();
			dao.clear();
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
		return new BundleNameValue(PREFIX + "converter.order.name", getStringId());
	}

	@Override
	public String getStringId()
	{
		return "ORDER";
	}
}
