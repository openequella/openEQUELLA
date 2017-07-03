/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.remoterepo.section;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.template.Decorations;

public class RemoteRepoListAllSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@Inject
	private RemoteRepoWebService remoteRepoWebService;

	@Inject
	private FederatedSearchService federatedSearchService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private BundleCache bundleCache;

	@PlugKey("remoterepos.heading")
	private static Label TITLE;
	@PlugKey("remoterepos.column.remoterepo")
	private static Label LABEL_REMOTE_REPO;

	@Component(name = "rr")
	private Table remoteReposTable;

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(TITLE);

		final TableState remoteReposTableState = remoteReposTable.getState(context);
		for( FederatedSearch fed : federatedSearchService.enumerateSearchable() )
		{
			if( !fed.isDisabled() )
			{
				final BundleLabel nameLabel = new BundleLabel(fed.getName(), bundleCache);
				final HtmlLinkState repolink = new HtmlLinkState(nameLabel, events.getNamedHandler("remoteRepo",
					fed.getUuid()));

				// TODO: better way to do the <BR>?
				final TableRow row = remoteReposTableState.addRow(new TableCell(repolink, new TextLabel("<br>", true),
					new BundleLabel(fed.getDescription(), bundleCache)));
				row.setSortData(nameLabel);
			}
		}
		return viewFactory.createResult("remoterepo.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		remoteReposTable.setColumnHeadings(LABEL_REMOTE_REPO);
		remoteReposTable.setColumnSorts(Sort.PRIMARY_ASC);
	}

	@EventHandlerMethod
	public void remoteRepo(SectionInfo info, String fedUuid)
	{
		remoteRepoWebService.forwardToSearch(info, federatedSearchService.getByUuid(fedUuid), true);
	}

	public Table getRemoteReposTable()
	{
		return remoteReposTable;
	}
}
