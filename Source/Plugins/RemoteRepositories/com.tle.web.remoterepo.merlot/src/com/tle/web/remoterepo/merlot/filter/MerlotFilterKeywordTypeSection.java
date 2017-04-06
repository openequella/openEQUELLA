package com.tle.web.remoterepo.merlot.filter;

import com.tle.common.NameValue;
import com.tle.core.remoterepo.merlot.service.MerlotSearchParams.KeywordUse;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.remoterepo.merlot.MerlotRemoteRepoSearchEvent;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@SuppressWarnings("nls")
public class MerlotFilterKeywordTypeSection extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		SearchEventListener<MerlotRemoteRepoSearchEvent>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@ResourceHelper
	private PluginResourceHelper RESOURCES;

	@Component(name = "kc")
	private SingleSelectionList<NameValue> typeList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		final SimpleHtmlListModel<NameValue> listModel = new SimpleHtmlListModel<NameValue>();
		listModel.add(new BundleNameValue(RESOURCES.key("filter.type.all"), KeywordUse.ALL.name()));
		listModel.add(new BundleNameValue(RESOURCES.key("filter.type.any"), KeywordUse.ANY.name()));
		listModel.add(new BundleNameValue(RESOURCES.key("filter.type.phrase"), KeywordUse.EXACT_PHRASE.name()));
		typeList.setListModel(listModel);
		typeList.setAlwaysSelect(true);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("filter/merlotfiltertype.ftl", this);
	}

	@Override
	public void prepareSearch(SectionInfo info, MerlotRemoteRepoSearchEvent event) 
	{
		event.setKeywordUse(KeywordUse.valueOf(typeList.getSelectedValueAsString(info)));
	}

	public SingleSelectionList<NameValue> getTypeList()
	{
		return typeList;
	}
}
