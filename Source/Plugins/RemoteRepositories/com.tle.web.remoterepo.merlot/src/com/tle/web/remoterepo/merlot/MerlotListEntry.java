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

package com.tle.web.remoterepo.merlot;

import java.util.List;

import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.merlot.service.impl.MerlotSearchResult;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.TextUtils;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class MerlotListEntry extends RemoteRepoListEntry<MerlotSearchResult>
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(MerlotListEntry.class);

	// @Inject
	// private MerlotWebService merlotWebService;

	@Override
	public Label getDescription()
	{
		final String desc = TextUtils.INSTANCE.ensureWrap(result.getDescription(), 200, -1, true);
		return new TextLabel(desc, true);
	}

	@Override
	public List<MetadataEntry> getMetadata()
	{
		final List<MetadataEntry> entries = super.getMetadata();

		// we need to choosy about what to display in the search results (hence
		// the commented out lines)

		final String detailUrl = result.getDetailUrl();
		if( detailUrl != null )
		{
			final HtmlLinkState state = new HtmlLinkState(new TextLabel(detailUrl), new SimpleBookmark(detailUrl));
			state.setTarget(HtmlLinkState.TARGET_BLANK);
			addField(entries, "result.detailurl", new LinkRenderer(state));
		}
		final String url = result.getUrl();
		if( url != null )
		{
			final HtmlLinkState state = new HtmlLinkState(new TextLabel(url), new SimpleBookmark(result.getUrl()));
			state.setTarget(HtmlLinkState.TARGET_BLANK);
			addField(entries, "result.url", new LinkRenderer(state));
		}

		/*
		 * //FIXME: no! not toString addField(entries, new
		 * KeyLabel(getKeyPrefix() + "result.creationdate"),
		 * result.getPublishedDate() == null ? null :
		 * result.getPublishedDate().toString()); //FIXME: no! not toString
		 * addField(entries, new KeyLabel(getKeyPrefix() +
		 * "result.modifieddate"), result.getModifiedDate() == null ? null :
		 * result.getModifiedDate().toString());
		 */

		// addField(entries, "result.creativecommons",
		// result.getCreativeCommons());

		/*
		 * addField( entries, "result.categories", result.getCategories() ==
		 * null ? null : Utils.join(result.getCategories().toArray(), ", "));
		 */
		/*
		 * addField( entries, "result.audiences", result.getAudiences() == null
		 * ? null : Utils .join(result.getAudiences().toArray(), ", "));
		 */
		/*
		 * List<String> langs = result.getLanguages(); List<String> niceLangs =
		 * new ArrayList<String>(); if( langs != null ) { for( String lang :
		 * langs ) { niceLangs.add(merlotWebService.lookupLanguage(info, lang));
		 * } addField(entries, "result.languages",
		 * Utils.join(niceLangs.toArray(), ", ")); }
		 */
		/*
		 * addLabelField(entries, new KeyLabel(getKeyPrefix() +
		 * "result.technicalrequirements"), new
		 * TextLabel(result.getTechnicalRequirements(), true));
		 * addField(entries, "result.community", result.getCommunity());
		 * addField(entries, "result.copyright", result.getCopyright());
		 * addField(entries, "result.cost", result.getCost()); addField(entries,
		 * "result.materialtype", result.getMaterialType()); addField(entries,
		 * "result.section508compliant", result.getSection508Compliant());
		 * addField(entries, "result.sourceavailable",
		 * result.getSourceAvailable()); addField(entries, "result.submitter",
		 * result.getSubmitter());
		 */
		return entries;
	}

	@Override
	protected String getKeyPrefix()
	{
		return resources.pluginId() + ".";
	}
}
