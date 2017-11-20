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

package com.tle.web.institution.tab;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.google.common.collect.Lists;
import com.tle.beans.Institution;
import com.tle.common.PathUtils;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.institution.section.CloneSection;
import com.tle.web.institution.section.EditSection;
import com.tle.web.institution.section.ExportSection;
import com.tle.web.institution.section.ProgressSection;
import com.tle.web.institution.section.ProgressSection.ProgressRunnable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class AdminTab extends AbstractInstitutionTab<AdminTab.AdminTabModel>
{
	@PlugKey("institutions.admin.title")
	private static Label TITLE_LABEL;
	@PlugKey("institutions.admin.link.name")
	private static Label LINK_LABEL;

	// Table columns
	@PlugKey("column.institution")
	private static Label LABEL_INSTITUTION;
	@PlugKey("column.status")
	private static Label LABEL_STATUS;
	@PlugKey("column.actions")
	private static Label LABEL_ACTIONS;

	// Actions
	@PlugKey("institutions.admin.login")
	private static Label LABEL_LOGIN;
	@PlugKey("institutions.admin.delete")
	private static Label LABEL_DELETE;
	@PlugKey("institutions.admin.delete")
	private static String KEY_DELETE;
	@PlugKey("institutions.admin.confirm")
	private static String KEY_CONFIRM_DELETE;
	@PlugKey("institutions.admin.clone")
	private static Label LABEL_CLONE;
	@PlugKey("institutions.admin.dump")
	private static Label LABEL_EXPORT;
	@PlugKey("institutions.admin.disable")
	private static Label LABEL_DISABLE;
	@PlugKey("institutions.admin.enable")
	private static Label LABEL_ENABLE;
	@PlugKey("institutions.admin.edit")
	private static Label LABEL_EDIT;

	// Statuses
	@PlugKey("institutions.admin.status.disabled")
	private static Label LABEL_STATUS_DISABLED;
	@PlugKey("institutions.admin.status.enabled")
	private static Label LABEL_STATUS_ENABLED;
	@PlugKey("institutions.admin.status.invalid.")
	private static String KEY_STATUS_INVALID;

	@Inject
	private InstitutionService institutionService;
	@Inject
	private InstitutionImportService instImportService;
	@Inject
	private ExportSection exportSection;
	@Inject
	private EditSection editSection;
	@Inject
	private CloneSection cloneSection;

	@TreeLookup
	private ProgressSection progressSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component(name = "i")
	private Table institutionsTable;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerSections(exportSection, id);
		tree.registerSections(editSection, id);
		tree.registerSections(cloneSection, id);

		institutionsTable.setColumnHeadings(LABEL_INSTITUTION, LABEL_STATUS, LABEL_ACTIONS);
		institutionsTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.NONE);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		SectionResult first = renderFirstResult(context);
		if( first != null )
		{
			return first;
		}

		List<InstitutionStatus> institutions = Lists.newArrayList(institutionService.getAllInstitutions());
		Collections.sort(institutions, new NumberStringComparator<InstitutionStatus>()
		{
			@Override
			public String convertToString(InstitutionStatus t)
			{
				return t.getInstitution().getName();
			}
		});

		getModel(context).setInstitutions(institutions.size() > 0);

		TableState tableState = institutionsTable.getState(context);
		for( InstitutionStatus instStatus : institutions )
		{
			final Institution institution = instStatus.getInstitution();
			final long id = institution.getUniqueId();

			// Badge Cell
			TagState tagState = new TagState();
			tagState.setStyle("display: none;");
			tagState.addClass("insturl");
			SpanRenderer instUrlTag = new SpanRenderer(tagState, institution.getUrl());
			final TableCell badgeCell = new TableCell(viewFactory.createResultWithModelMap("tab/badgeCell.ftl", "name",
				institution.getName(), "badgeUrl", institutionSection.getBadgeUrl(context, id)), instUrlTag);
			badgeCell.addClass("badge-row");
			badgeCell.setSortData(institution.getName());

			// Status and actions
			Label status = null;
			final List<Object> actions = Lists.newArrayList();

			if( !instStatus.isValid() )
			{
				status = new KeyLabel(KEY_STATUS_INVALID + instStatus.getInvalidReason().name().toLowerCase());
			}

			if( institution.isEnabled() )
			{
				if( instStatus.isValid() )
				{
					status = LABEL_STATUS_ENABLED;
					actions.add(new HtmlLinkState(LABEL_LOGIN,
						new SimpleBookmark(PathUtils.urlPath(institution.getUrl(), "logon.do"))));
				}
				actions.add(actionLink(LABEL_DISABLE, "disableClicked", id));
			}
			else if( instStatus.isValid() )
			{
				status = LABEL_STATUS_DISABLED;
				actions.add(actionLink(LABEL_ENABLE, "enableClicked", id));
			}
			actions.add(actionLink(LABEL_EDIT, "editClicked", id));

			actions.add(new HtmlLinkState(LABEL_DELETE, events.getNamedHandler("deleteClicked", id)
				.addValidator(new Confirm(new KeyLabel(KEY_CONFIRM_DELETE, institution.getName())))));

			actions.add(actionLink(LABEL_CLONE, "cloneClicked", id));
			actions.add(actionLink(LABEL_EXPORT, "dumpClicked", id));

			// Put the status cell together
			final TableCell statusCell = new TableCell();
			statusCell.addClass("middle");
			statusCell.addContent(status);

			// And put the actions cell together
			final TableCell actionsCell = new TableCell();
			actionsCell.addClass("middle");
			for( Iterator<Object> iter = actions.iterator(); iter.hasNext(); )
			{
				actionsCell.addContent(iter.next());
				if( iter.hasNext() )
				{
					actionsCell.addContent(" | ");
				}
			}

			tableState.addRow(badgeCell, statusCell, actionsCell);
		}

		Decorations d = Decorations.getDecorations(context);
		d.setTitle(TITLE_LABEL);
		return viewFactory.createResult("tab/admin.ftl", context);
	}

	private HtmlLinkState actionLink(Label label, String handler, Object... values)
	{
		return new HtmlLinkState(label, events.getNamedHandler(handler, values));
	}

	@EventHandlerMethod
	public void editClicked(SectionInfo info, long institutionId)
	{
		editSection.setupEdit(info, institutionId);
	}

	@EventHandlerMethod
	public void deleteClicked(SectionInfo info, long institutionId)
	{
		final Institution i = institutionService.getInstitution(institutionId);

		InstitutionInfo instImp = instImportService.getInstitutionInfo(i);
		instImp.setFlags(new HashSet<String>());
		progressSection.setupProgress(info, instImportService.getConverterTasks(ConvertType.DELETE, instImp),
			KEY_DELETE, i, new ProgressRunnable()
			{
				@Override
				public void run(ListProgressCallback callback)
				{
					instImportService.delete(i, callback);
				}

				@Override
				public String getTaskName()
				{
					return "delete";
				}
			});
	}

	@EventHandlerMethod
	public void cloneClicked(SectionInfo info, int institutionId)
	{
		cloneSection.setupClone(info, institutionId);
	}

	@EventHandlerMethod
	public void dumpClicked(SectionInfo info, int institutionId)
	{
		exportSection.setupExport(info, institutionId);
	}

	@EventHandlerMethod
	public void enableClicked(SectionInfo info, int institutionId)
	{
		setEnabled(institutionId, true);
	}

	@EventHandlerMethod
	public void disableClicked(SectionInfo info, int institutionId)
	{
		setEnabled(institutionId, false);
	}

	private void setEnabled(long instId, boolean enabled)
	{
		institutionService.setEnabled(instId, enabled);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "admin";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AdminTabModel();
	}

	@Override
	public boolean shouldDefault(SectionInfo info)
	{
		return !institutionService.getAllInstitutions().isEmpty();
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public Label getName()
	{
		return LINK_LABEL;
	}

	public Table getInstitutionsTable()
	{
		return institutionsTable;
	}

	public static class AdminTabModel
	{
		private boolean institutions;

		public boolean hasInstitutions()
		{
			return institutions;
		}

		public void setInstitutions(boolean institutions)
		{
			this.institutions = institutions;
		}
	}
}
