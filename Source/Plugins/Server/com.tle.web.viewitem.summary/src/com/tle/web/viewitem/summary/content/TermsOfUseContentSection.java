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

package com.tle.web.viewitem.summary.content;

import java.util.Date;

import javax.inject.Inject;

import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.core.item.service.DrmService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.viewitem.I18nDRM;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
public class TermsOfUseContentSection extends AbstractContentSection<TermsOfUseContentSection.TermsOfUseSummaryModel>
{
	private static final String DIGITAL_RIGHTS_ITEM = "DIGITAL_RIGHTS_ITEM";

	@PlugKey("summary.content.termsofuse.pagetitle")
	private static Label TITLE_LABEL;
	@PlugKey("summary.content.termsofuse.agreements.user")
	private static Label LABEL_USER;
	@PlugKey("summary.content.termsofuse.agreements.date")
	private static Label LABEL_DATE;
	@PlugKey("summary.content.termsofuse.agreements.title")
	private static String KEY_AGREEMENTS;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private DrmService drmService;
	@Inject
	private UserLinkService userLinkService;
	@Inject
	private DateRendererFactory dateRendererFactory;

	private UserLinkSection userLinkSection;

	@Component
	private Table agreementsTable;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);
		agreementsTable.setColumnHeadings(LABEL_USER, LABEL_DATE);
		agreementsTable.setColumnSorts(Sort.SORTABLE_ASC, Sort.PRIMARY_ASC);
	}

	@Override
	public SectionResult renderHtml(final RenderEventContext info) throws Exception
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		final Item item = itemInfo.getItem();
		final DrmSettings settings = item.getDrmSettings();

		if( settings == null )
		{
			throw new Error("No DRM settings on item");
		}

		final TermsOfUseSummaryModel model = getModel(info);

		model.setDrm(new I18nDRM(settings));

		if( itemInfo.hasPrivilege(DIGITAL_RIGHTS_ITEM) )
		{
			int agreementsCount = 0;
			model.setShowAgreements(true);

			final TableState agreementsTableState = agreementsTable.getState(info);

			// Pronounced 'assept'...
			for( DrmAcceptance accept : drmService.enumerateAgreements(item) )
			{
				final Date date = accept.getDate();
				TableRow row = agreementsTableState.addRow(userLinkSection.createLink(info, accept.getUser()),
					dateRendererFactory.createDateRenderer(date));
				row.setSortData(null, date); // first column = use the actual
												// link.
				agreementsCount++;
			}

			model.setAgreementsLabel(new PluralKeyLabel(KEY_AGREEMENTS, agreementsCount));
			model.setAgreementsCount(agreementsCount);
		}

		displayBackButton(info);

		addDefaultBreadcrumbs(info, itemInfo, TITLE_LABEL);

		return viewFactory.createResult("viewitem/summary/content/termsofuse.ftl", info);
	}

	@Override
	public Class<TermsOfUseSummaryModel> getModelClass()
	{
		return TermsOfUseSummaryModel.class;
	}

	public Table getAgreementsTable()
	{
		return agreementsTable;
	}

	public static class TermsOfUseSummaryModel
	{
		private I18nDRM drm;
		private boolean showAgreements;
		private Label agreementsLabel;
		private int agreementsCount;

		public I18nDRM getDrm()
		{
			return drm;
		}

		public void setDrm(I18nDRM drm)
		{
			this.drm = drm;
		}

		public boolean isShowAgreements()
		{
			return showAgreements;
		}

		public void setShowAgreements(boolean showAgreements)
		{
			this.showAgreements = showAgreements;
		}

		public Label getAgreementsLabel()
		{
			return agreementsLabel;
		}

		public void setAgreementsLabel(Label agreementsLabel)
		{
			this.agreementsLabel = agreementsLabel;
		}

		public int getAgreementsCount()
		{
			return agreementsCount;
		}

		public void setAgreementsCount(int agreementsCount)
		{
			this.agreementsCount = agreementsCount;
		}
	}
}