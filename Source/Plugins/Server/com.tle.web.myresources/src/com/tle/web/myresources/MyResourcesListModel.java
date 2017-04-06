package com.tle.web.myresources;

import java.util.List;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@Bind
public class MyResourcesListModel extends SimpleHtmlListModel<MyResourcesSubSearch>
{
	@Inject
	private MyResourcesService myResourcesService;

	public List<MyResourcesSubSearch> createSearches()
	{
		List<MyResourcesSubSearch> listSearches = myResourcesService.listSearches();
		addAll(listSearches);
		return listSearches;
	}

	public void register(SectionTree tree, String parentId)
	{
		List<MyResourcesSubSearch> subSearches = createSearches();
		for( MyResourcesSubSearch subSearch : subSearches )
		{
			subSearch.register(tree, parentId);
		}
	}

	@Override
	protected Option<MyResourcesSubSearch> convertToOption(MyResourcesSubSearch subSearch)
	{
		return new KeyOption<MyResourcesSubSearch>(subSearch.getNameKey(), subSearch.getValue(), subSearch)
		{
			@Override
			public boolean isDisabled()
			{
				return !getObject().canView();
			}
		};
	}

	@Override
	public MyResourcesSubSearch getValue(SectionInfo info, String value)
	{
		MyResourcesSubSearch val = super.getValue(info, value);
		return val == null ? getValue(info, getDefaultValue(info)) : val;
	}
}
