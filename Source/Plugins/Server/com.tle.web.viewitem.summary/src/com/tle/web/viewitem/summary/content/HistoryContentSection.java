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
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.workflow.events.StateChangeEvent;
import com.tle.core.workflow.events.WorkflowEvent;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.ItemStatusKeys;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
public class HistoryContentSection extends AbstractContentSection<HistoryContentSection.HistoryModel>
{
	public static final String VIEW_PRIVILEGE = "VIEW_HISTORY_ITEM";

	@PlugKey("summary.content.history.pagetitle")
	private static Label LABEL_TITLE;
	@PlugKey("summary.content.history.showcomment")
	private static Label LABEL_SHOWCOMMENT;
	@PlugKey("summary.content.history.unknownstepname")
	private static Label LABEL_UNKNOWNSTEP;
	@PlugKey("summary.content.history.event.")
	private static String KEY_EVENTPFX;
	@PlugKey("summary.content.history.column.event")
	private static Label LABEL_EVENT;
	@PlugKey("summary.content.history.column.user")
	private static Label LABEL_USER;
	@PlugKey("summary.content.history.column.date")
	private static Label LABEL_DATE;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajaxFactory;

	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@Component
	private SingleSelectionList<NameValue> detailsSelection;

	@PlugKey("summary.content.history.basicdetails")
	private static String basicKey;
	@PlugKey("summary.content.history.includeedits")
	private static String showEditsKey;
	@PlugKey("summary.content.history.all")
	private static String showAllKey;

	@Component
	@Inject
	private HistoryCommentDialog commentDialog;
	@Inject
	private DateRendererFactory dateRendererFactory;
	@Component(name = "h")
	private Table historyTable;

	private final TagState eventsTable = new TagState();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		StatementHandler showHistoryTableFunc = new StatementHandler(ajaxFactory.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("reload"), ajaxFactory.getEffectFunction(EffectType.FADEOUTIN), "historyevents"));

		detailsSelection.setEventHandler(JSHandler.EVENT_CHANGE, showHistoryTableFunc);
		SimpleHtmlListModel<NameValue> listModel = new SimpleHtmlListModel<NameValue>();
		listModel.add(new BundleNameValue(basicKey, "basicKey"));
		listModel.add(new BundleNameValue(showEditsKey, "showEditsKey"));
		listModel.add(new BundleNameValue(showAllKey, "showAllKey"));
		detailsSelection.setListModel(listModel);
		detailsSelection.setAlwaysSelect(true);
		userLinkSection = userLinkService.register(tree, id);

		historyTable.setColumnHeadings(LABEL_EVENT, LABEL_USER, LABEL_DATE);
		historyTable.setColumnSorts(Sort.SORTABLE_ASC, Sort.SORTABLE_ASC, Sort.PRIMARY_ASC);
	}

	private boolean canView(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).hasPrivilege(VIEW_PRIVILEGE);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}

		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		final WorkflowStatus status = itemInfo.getWorkflowStatus();
		if( status == null )
		{
			return null;
		}

		showHistoryTable(context);

		addDefaultBreadcrumbs(context, itemInfo, LABEL_TITLE);
		displayBackButton(context);

		final Item item = itemInfo.getItem();
		final ModerationStatus modStatus = item.getModeration();
		if( modStatus != null )
		{
			final Date reviewDate = item.getModeration().getReviewDate();
			if( reviewDate != null )
			{
				HistoryModel model = getModel(context);
				model.setReviewDate(JQueryTimeAgo.timeAgoTag(reviewDate));
				model.setReviewTense(reviewDate.before(new Date()) ? "past" : "future");

				JQueryTimeAgo.enableFutureTimes(context);
			}
		}

		return viewFactory.createResult("viewitem/summary/content/history.ftl", context);
	}

	@EventHandlerMethod
	public void reload(SectionInfo info)
	{
		// noop
	}

	private void showHistoryTable(SectionInfo info)
	{
		final WorkflowStatus status = ParentViewItemSectionUtils.getItemInfo(info).getWorkflowStatus();

		final WorkflowEvent[] workflowEvents = status.getEvents();
		if( workflowEvents == null || workflowEvents.length == 0 )
		{
			return;
		}

		final TableState allEvents = historyTable.getState(info);

		boolean showEdits = false;
		boolean showAllDetails = false;

		String value = Strings.nullToEmpty(detailsSelection.getSelectedValueAsString(info));
		if( value.equals("showEditsKey") )
		{
			showEdits = true;
		}
		else if( value.equals("showAllKey") )
		{
			showEdits = true;
			showAllDetails = true;
		}

		final Map<String, WorkflowStep> refMap = status.getReferencedSteps();

		for( int i = workflowEvents.length - 1; i >= 0; i-- )
		{
			final WorkflowEvent event = workflowEvents[i];
			final Label atStepName = getStepName(event.getStep(), event.getStepName(), refMap);

			// Fudge the date a little so that events that occurred at
			// "the same time"
			// get preserved in order
			final Date eventDate = new Date(event.getDate().getTime() + i);

			Label label = null;

			switch( event.getIntType() )
			{
				case resetworkflow:
					if( showAllDetails )
					{
						label = s("reset");
					}
					break;

				case approved:
					if( showAllDetails )
					{
						label = s("accepted", atStepName);
					}
					break;

				case rejected:
					final String tostep = event.getTostep();
					if( Check.isEmpty(tostep) )
					{
						label = s("rejected", atStepName);
					}
					else if( showAllDetails )
					{
						label = s("rejectedtostep", atStepName, getStepName(tostep, event.getToStepName(), refMap));
					}
					break;

				case edit:
					if( showEdits )
					{
						label = s("edited");
					}
					break;

				case newversion:
					label = s("newversion");
					break;

				case workflowremoved:
					if( showAllDetails )
					{
						label = s("workflowremoved");
					}
					break;

				case comment:
					if( showAllDetails )
					{
						label = s("comment", atStepName);
					}
					break;

				case scriptComplete:
					if( showAllDetails )
					{
						label = s("scriptcomplete", atStepName);
					}
					break;

				case scriptError:
					label = s("scripterror", atStepName);
					break;

				case statechange:
					ItemStatus state = ((StateChangeEvent) event).getState();
					switch( state )
					{
						case DRAFT:
							label = s("statechanged.draft");
							break;

						case LIVE:
							label = s("statechanged.live");
							break;

						case MODERATING:
							if( showAllDetails )
							{
								label = s("statechanged.moderating");
							}
							break;
						case REJECTED:
							if( showAllDetails )
							{
								label = s("statechanged", new KeyLabel(ItemStatusKeys.get(state)));
							}
							break;

						default:
							label = s("statechanged", new KeyLabel(ItemStatusKeys.get(state)));
					}
					break;

				case clone:
					if( showEdits )
					{
						label = s("cloned");
					}
					break;

				case changeCollection:
					if( showEdits )
					{
						label = s("moved");
					}
					break;

				case contributed:
					label = s("contributed");
					break;
				case taskMove:
					label = s("move", getStepName(event.getTostep(), event.getToStepName(), refMap));
					break;

				default:
					// 'promoted' unloved?
					break;
			}

			if( label != null )
			{
				TableCell cell1;
				final String comment = event.getComment();
				if( !Check.isEmpty(comment) )
				{
					final HtmlLinkState hcs = new HtmlLinkState(new OverrideHandler(commentDialog.getOpenFunction(),
						event.getId()));
					hcs.setLabel(LABEL_SHOWCOMMENT);
					cell1 = new TableCell(label, " (", hcs, ")");
				}
				else
				{
					cell1 = new TableCell(label);
				}

				if( event.getIntType() == Type.scriptComplete || event.getIntType() == Type.scriptError )
				{
					allEvents.addRow(cell1, "", dateRendererFactory.createDateRenderer(eventDate)).setSortData(label,
						"", eventDate);
				}
				else
				{
					final HtmlLinkState userLink = userLinkSection.createLink(info, event.getUserid());
					allEvents.addRow(cell1, userLink, dateRendererFactory.createDateRenderer(eventDate)).setSortData(
						label, userLink.getLabel(), eventDate);
				}
			}
		}
	}

	@EventHandlerMethod
	public void backToSummaryClicked(SectionInfo info)
	{
		itemSummaryContentSection.setSummaryId(info, null);
	}

	private Label s(String keyPart, Label... values)
	{
		return new KeyLabel(KEY_EVENTPFX + keyPart, values);
	}

	private Label getStepName(String step, String stepname, Map<String, WorkflowStep> refMap)
	{
		if( !Check.isEmpty(step) )
		{
			WorkflowStep refStep = refMap.get(step);
			if( refStep != null )
			{
				return new BundleLabel(refStep.getDisplayName(), bundleCache);
			}
		}

		if( stepname != null )
		{
			return new TextLabel(stepname);
		}

		return LABEL_UNKNOWNSTEP;
	}

	@Override
	public Class<HistoryModel> getModelClass()
	{
		return HistoryModel.class;
	}

	public static class HistoryModel
	{
		private TagRenderer reviewDate;
		private String reviewTense;

		public TagRenderer getReviewDate()
		{
			return reviewDate;
		}

		public void setReviewDate(TagRenderer reviewDate)
		{
			this.reviewDate = reviewDate;
		}

		public String getReviewTense()
		{
			return reviewTense;
		}

		public void setReviewTense(String reviewTense)
		{
			this.reviewTense = reviewTense;
		}
	}

	public TagState getEventsTable()
	{
		return eventsTable;
	}

	public SingleSelectionList<NameValue> getDetailsSelection()
	{
		return detailsSelection;
	}

	public Table getHistoryTable()
	{
		return historyTable;
	}
}
