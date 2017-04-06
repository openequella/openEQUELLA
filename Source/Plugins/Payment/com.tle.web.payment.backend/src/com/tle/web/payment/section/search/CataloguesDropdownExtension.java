package com.tle.web.payment.section.search;

import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.ItemStatus;
import com.tle.common.search.PresetSearch;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentIndexFields;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.WithinEntry;
import com.tle.web.itemadmin.WithinExtension;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@Bind
@TreeIndexed
public class CataloguesDropdownExtension extends AbstractPrototypeSection<Object> implements WithinExtension
{
	public static final String WITHIN_ID = "catalogueWithin"; //$NON-NLS-1$

	@PlugKey("search.query.catalgoues")
	private static Label LABEL_CATALOGUES;

	@PlugKey("search.query.where.live")
	private static String KEY_LIVE;
	@PlugKey("search.query.where.pending")
	private static String KEY_PENDING;
	@PlugKey("search.query.where.excluded")
	private static String KEY_EXCLUDED;

	@Inject
	private CatalogueService catalogueService;
	@Inject
	private PricingTierService tierService;

	@TreeLookup
	private ItemAdminQuerySection querySection;

	@Component(parameter = "cwl", supported = true)
	private SingleSelectionList<Void> catalogueWhereList;

	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Override
	public void register(String parentId, SectionTree tree)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		SimpleHtmlListModel<Void> listModel = new SimpleHtmlListModel<Void>();
		listModel.add(new VoidKeyOption(KEY_LIVE, KEY_LIVE));
		listModel.add(new VoidKeyOption(KEY_PENDING, KEY_PENDING));
		listModel.add(new VoidKeyOption(KEY_EXCLUDED, KEY_EXCLUDED));
		catalogueWhereList.setListModel(listModel);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		catalogueWhereList.addChangeEventHandler(querySection.getCollectionChangedHandler(tree));
	}

	@Override
	public void populateModel(SectionInfo info, List<WithinEntry> list)
	{
		List<BaseEntityLabel> listAll = catalogueService.listManageable();
		list.addAll(Lists.transform(listAll, new Function<BaseEntityLabel, WithinEntry>()
		{
			@Override
			public WithinEntry apply(BaseEntityLabel input)
			{
				return new WithinEntry(input, LABEL_CATALOGUES, WITHIN_ID, false, 1000);
			}
		}));
	}

	@Override
	public PresetSearch createDefaultSearch(SectionInfo info, WithinEntry selected)
	{
		PresetSearch createSearch;
		String where = catalogueWhereList.getSelectedValueAsString(info);

		if( KEY_PENDING.equals(where) )
		{
			createSearch = catalogueService.createSearch(selected.getBel().getUuid(), false);
			FreeTextQuery originalQuery = createSearch.getFreeTextQuery();
			FreeTextQuery pricingSetQuery = tierService.getPriceSetQuery(true);
			FreeTextQuery noPricingSetQuery = tierService.getPriceSetQuery(false);

			FreeTextBooleanQuery liveQuery = new FreeTextBooleanQuery(false, false);
			liveQuery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ITEMSTATUS, ItemStatus.LIVE.toString()));
			liveQuery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ITEMSTATUS, ItemStatus.REVIEW.toString()));

			FreeTextBooleanQuery notLiveQuery = new FreeTextBooleanQuery(true, false);
			notLiveQuery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ITEMSTATUS, ItemStatus.LIVE.toString()));
			notLiveQuery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ITEMSTATUS, ItemStatus.REVIEW.toString()));

			FreeTextBooleanQuery case1 = new FreeTextBooleanQuery(false, true, pricingSetQuery, notLiveQuery);
			FreeTextBooleanQuery case2 = new FreeTextBooleanQuery(false, true, noPricingSetQuery, liveQuery);
			FreeTextBooleanQuery case3 = new FreeTextBooleanQuery(false, true, noPricingSetQuery, notLiveQuery);

			FreeTextBooleanQuery combined = new FreeTextBooleanQuery(false, false, case1, case2, case3);

			createSearch = new PresetSearch(createSearch.getQuery(), new FreeTextBooleanQuery(false, true,
				originalQuery, combined), false);
		}
		else if( KEY_EXCLUDED.equals(where) )
		{
			createSearch = catalogueService.createSearch(selected.getBel().getUuid(), false);
			FreeTextFieldQuery blacklistedQuery = new FreeTextFieldQuery(PaymentIndexFields.FIELD_BLACKLISTED, selected
				.getBel().getUuid());
			createSearch = new PresetSearch(createSearch.getQuery(), blacklistedQuery, false);
		}
		else
		{
			createSearch = catalogueService.createLiveSearch(selected.getBel().getUuid());
		}
		return createSearch;
	}

	@Override
	public SectionRenderable render(RenderEventContext context)
	{
		WithinEntry selectedValue = querySection.getCollectionList().getSelectedValue(context);
		if( selectedValue != null && WITHIN_ID.equals(selectedValue.getTypeId()) )
		{
			return viewFactory.createResult("catalogue-where.ftl", this);
		}
		else
		{
			return null;
		}
	}

	public SingleSelectionList<Void> getCatalogueWhereList()
	{
		return catalogueWhereList;
	}

	public String getExculduedKey()
	{
		return KEY_EXCLUDED;
	}
}
