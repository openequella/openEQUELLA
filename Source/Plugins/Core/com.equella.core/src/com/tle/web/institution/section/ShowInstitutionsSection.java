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

package com.tle.web.institution.section;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.SchemaInfo;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.InstitutionSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;

@Bind
@SuppressWarnings("nls")
public class ShowInstitutionsSection extends AbstractPrototypeSection<ShowInstitutionsSection.Model>
	implements
		HtmlRenderer
{
	@PlugKey("institutions.list.title")
	private static Label TITLE_LABEL;

	@Inject
	private MigrationService migrationService;
	@Inject
	private InstitutionService institutionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private InstitutionSection institutionSection;

	@Override
	public SectionResult renderHtml(final RenderEventContext context)
	{
		final Model model = getModel(context);

		List<InstitutionDisplay> is = Lists.newArrayList(Collections2.transform(institutionService.getAvailableMap()
			.values(), new Function<Institution, InstitutionDisplay>()
		{
			@Override
			public InstitutionDisplay apply(Institution institution)
			{
				return new InstitutionDisplay(institution.getName(), institutionSection.getBadgeUrl(context,
					institution.getUniqueId()), new HtmlLinkState(new SimpleBookmark(institution.getUrl())));
			}
		}));
		Collections.sort(is, INSTITUTION_COMPARATOR);
		model.setInstitutions(is);

		int reqMigCount = 0;
		for( SchemaInfo si : migrationService.getMigrationsStatus().getSchemas().values() )
		{
			if( si.isMigrationRequired() )
			{
				reqMigCount++;
			}
		}
		model.setRequireMigrationCount(reqMigCount);

		Decorations decorations = Decorations.getDecorations(context);
		decorations.setMenuMode(MenuMode.HIDDEN);
		decorations.setTitle(TITLE_LABEL);

		return viewFactory.createResult("instlist.ftl", context);
	}

	@Override
	public Model instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private int requireMigrationCount;
		private List<InstitutionDisplay> institutions;

		public List<InstitutionDisplay> getInstitutions()
		{
			return institutions;
		}

		public void setInstitutions(List<InstitutionDisplay> institutions)
		{
			this.institutions = institutions;
		}

		public int getRequireMigrationCount()
		{
			return requireMigrationCount;
		}

		public void setRequireMigrationCount(int requireMigrationCount)
		{
			this.requireMigrationCount = requireMigrationCount;
		}
	}

	public static final class InstitutionDisplay
	{
		private final String name;
		private final String badgeUrl;
		private final HtmlLinkState loginLink;

		private InstitutionDisplay(String name, String badgeUrl, HtmlLinkState loginLink)
		{
			super();
			this.name = name;
			this.badgeUrl = badgeUrl;
			this.loginLink = loginLink;
		}

		public String getName()
		{
			return name;
		}

		public String getBadgeUrl()
		{
			return badgeUrl;
		}

		public HtmlLinkState getLoginLink()
		{
			return loginLink;
		}
	}

	public static final Comparator<InstitutionDisplay> INSTITUTION_COMPARATOR = new NumberStringComparator<InstitutionDisplay>()
	{
		@Override
		public String convertToString(InstitutionDisplay id)
		{
			return id.getName();
		}
	};
}
