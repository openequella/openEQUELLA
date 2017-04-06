package com.tle.web.remoterepo.section;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class RemoteRepoQuerySection<E extends RemoteRepoSearchEvent<E>>
	extends
		AbstractQuerySection<RemoteRepoQuerySection.RemoteRepoQueryModel, E>
{
	@PlugKey("query.search")
	private static Label SEARCH_LABEL;
	@PlugKey("query.blank")
	private static Label BLANK_QUERY_LABEL;

	@SuppressWarnings("rawtypes")
	@TreeLookup
	private RemoteRepoResultsSection resultsSection;

	@Inject
	private RemoteRepoWebService repoWebService;

	@ViewFactory
	private FreemarkerFactory rrView;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setTitle(CurrentLocale.get(repoWebService.getRemoteRepository(context).getName()));
		return rrView.createResult("query.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		searchButton.setLabel(SEARCH_LABEL);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		final JSValidator validator = createValidator();
		final JSHandler restartSearch = new StatementHandler(resultsSection.getRestartSearchHandler(tree));
		if( validator != null )
		{
			restartSearch.addValidator(validator);
		}
		searchButton.setClickHandler(restartSearch);
	}

	@Override
	public void prepareSearch(SectionInfo info, E event) throws Exception
	{
		String q = getParsedQuery(info);
		if( Check.isEmpty(q) )
		{
			event.setInvalid(true);
			event.stopProcessing();
		}
		else
		{
			super.prepareSearch(info, event);
		}
	}

	protected JSValidator createValidator()
	{
		return queryField.createNotBlankValidator().setFailureStatements(Js.alert_s(BLANK_QUERY_LABEL));
	}

	@Override
	public Class<RemoteRepoQueryModel> getModelClass()
	{
		return RemoteRepoQueryModel.class;
	}

	public static class RemoteRepoQueryModel
	{
		private String title;

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}
	}
}
