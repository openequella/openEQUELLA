package com.tle.web.hierarchy.legacy;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.Check;
import com.tle.web.hierarchy.section.RootHierarchySection;
import com.tle.web.hierarchy.section.TopicDisplaySection;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.searching.section.RootSearchSection;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.search.AbstractQuerySection;

public abstract class LegacySearchServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private SectionsController controller;

	@SuppressWarnings("nls")
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		SectionInfo info;
		String topicUuid = getTopicParameter(request);
		if( !Check.isEmpty(topicUuid) )
		{
			info = controller.createForward(RootHierarchySection.HIERARCHYURL);
			TopicDisplaySection topicSection = info.lookupSection(TopicDisplaySection.class);
			topicSection.changeTopic(info, topicUuid);
		}
		else
		{
			info = controller.createForward(RootSearchSection.SEARCHURL);
			SearchQuerySection sqs = info.lookupSection(SearchQuerySection.class);
			String coluuid = getCollectionUuidsParam(request);
			if( !Check.isEmpty(coluuid) )
			{
				sqs.setCollection(info, coluuid);
			}
			String powuuid = getPowerSearchParam(request);
			String powxml = getPowerXmlParam(request);
			if( !Check.isEmpty(powuuid) )
			{
				sqs.setPowerSearch(info, powuuid, powxml);
			}
		}
		AbstractQuerySection<?, ?> querySection = info.lookupSection(AbstractQuerySection.class);
		AbstractSortOptionsSection sortOptions = info.lookupSection(AbstractSortOptionsSection.class);
		String queryString = getQueryString(request);
		if( queryString == null )
		{
			queryString = "";
		}
		querySection.setQuery(info, queryString);
		String sort = getSortParam(request);
		if( !Check.isEmpty(sort) )
		{
			sortOptions.getSortOptions().setSelectedStringValue(info, sort.toLowerCase());
		}
		redirect(request, response, info);
	}

	protected abstract void redirect(HttpServletRequest request, HttpServletResponse response, SectionInfo info)
		throws IOException;

	protected abstract String getSortParam(HttpServletRequest request);

	protected abstract String getQueryString(HttpServletRequest request);

	protected abstract String getPowerXmlParam(HttpServletRequest request);

	protected abstract String getPowerSearchParam(HttpServletRequest request);

	protected abstract String getCollectionUuidsParam(HttpServletRequest request);

	protected abstract String getTopicParameter(HttpServletRequest request);
}
