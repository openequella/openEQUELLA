package com.tle.core.payment.converter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.NameValue;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.payment.converter.initialiser.StoreEntitiesInitialiserCallback;
import com.tle.core.payment.dao.CatalogueAssignmentDao;
import com.tle.core.payment.dao.CatalogueDao;
import com.tle.core.payment.dao.PricingTierAssignmentDao;
import com.tle.core.services.InstitutionImportService.ConvertType;
import com.tle.core.services.entity.BaseEntityXmlConverter;
import com.tle.core.services.entity.EntityRegistry;
import com.tle.core.services.entity.IdOnlyConverter;
import com.tle.core.services.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.web.i18n.BundleNameValue;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class PaymentConverter extends AbstractConverter<Object>
{
	private static final String PREFIX = "com.tle.core.payment.backend.";
	private static final String CAT_ASSIGN_FOLDER = "catassign";
	private static final String TIER_ASSIGN_FOLDER = "tierassign";

	@Inject
	private CatalogueDao catDao;
	@Inject
	private CatalogueAssignmentDao catAssDao;
	@Inject
	private PricingTierAssignmentDao tierAssDao;

	@Inject
	private EntityRegistry registry;

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( !params.hasFlag(ConverterParams.NO_ITEMS) )
		{
			super.addTasks(type, tasks, params);
		}
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		for( PricingTierAssignment tierAss : getTierAssList() )
		{
			tierAssDao.delete(tierAss);
		}

		for( CatalogueAssignment catAss : getCatAssList() )
		{
			catAssDao.delete(catAss);
		}
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(registry));
		xstream.registerConverter(new IdOnlyConverter(Item.class));

		final SubTemporaryFile allCatAssFolder = new SubTemporaryFile(staging, CAT_ASSIGN_FOLDER);
		xmlHelper.writeExportFormatXmlFile(allCatAssFolder, true);
		for( CatalogueAssignment catAss : getCatAssList() )
		{
			initialiserService.initialise(catAss, new StoreEntitiesInitialiserCallback());

			final long id = catAss.getId();
			final BucketFile bucketFolder = new BucketFile(allCatAssFolder, id);
			xmlHelper.writeXmlFile(bucketFolder, id + ".xml", catAss, xstream);
		}

		final SubTemporaryFile allTierAssFolder = new SubTemporaryFile(staging, TIER_ASSIGN_FOLDER);
		xmlHelper.writeExportFormatXmlFile(allTierAssFolder, true);
		for( PricingTierAssignment tierAss : getTierAssList() )
		{
			initialiserService.initialise(tierAss, new StoreEntitiesInitialiserCallback());

			final long id = tierAss.getId();
			final BucketFile bucketFolder = new BucketFile(allTierAssFolder, id);
			xmlHelper.writeXmlFile(bucketFolder, id + ".xml", tierAss, xstream);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final XStream xstream = xmlHelper.createXStream();
		xstream.registerConverter(new BaseEntityXmlConverter(registry));
		xstream.registerConverter(new IdOnlyConverter(Item.class));

		final SubTemporaryFile allCatAssFolder = new SubTemporaryFile(staging, CAT_ASSIGN_FOLDER);
		for( String entry : xmlHelper.getXmlFileList(allCatAssFolder) )
		{
			final CatalogueAssignment catAss = xmlHelper.readXmlFile(allCatAssFolder, entry, xstream);
			catAss.setId(0);

			// old2new items
			final Item item = catAss.getItem();
			item.setId(params.getItems().get(item.getId()));
			catAss.setItem(item);

			catAssDao.save(catAss);
			catAssDao.flush();
			catAssDao.clear();
		}

		final SubTemporaryFile allTierAssFolder = new SubTemporaryFile(staging, TIER_ASSIGN_FOLDER);
		for( String entry : xmlHelper.getXmlFileList(allTierAssFolder) )
		{
			final PricingTierAssignment tierAss = xmlHelper.readXmlFile(allTierAssFolder, entry, xstream);
			tierAss.setId(0);

			// old2new items
			final Item item = tierAss.getItem();
			item.setId(params.getItems().get(item.getId()));
			tierAss.setItem(item);

			tierAssDao.save(tierAss);
			tierAssDao.flush();
			tierAssDao.clear();
		}
	}

	private List<CatalogueAssignment> getCatAssList()
	{
		final List<CatalogueAssignment> catAssList = Lists.newArrayList();
		for( Catalogue cat : catDao.enumerateAll() )
		{
			for( CatalogueAssignment catAss : catAssDao.enumerateForCatalogue(cat) )
			{
				catAssList.add(catAss);
			}
		}
		return catAssList;
	}

	private List<PricingTierAssignment> getTierAssList()
	{
		return tierAssDao.enumerateAll();
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
		return new BundleNameValue(PREFIX + "converter.payment.name", getStringId());
	}

	@Override
	public String getStringId()
	{
		return "PAYMENT";
	}
}
