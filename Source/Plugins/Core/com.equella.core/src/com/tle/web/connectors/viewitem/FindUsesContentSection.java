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

package com.tle.web.connectors.viewitem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.common.base.Throwables;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorContent.ConnectorContentAttribute;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.connectors.dialog.LMSAuthDialog;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
@Bind
public class FindUsesContentSection extends AbstractContentSection<FindUsesContentSection.FindUsesModel>
{
	private static final Logger LOGGER = Logger.getLogger(FindUsesContentSection.class);
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(FindUsesContentSection.class);

	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/finduses.js"));
	private static final JSCallable SETUP_DETAILS = new ExternallyDefinedFunction("setupDetails", INCLUDE);

	@PlugKey("finduses.selectconnector")
	private static String KEY_SELECT;
	@PlugKey("general.error.nouser")
	private static String KEY_NO_USER;
	@PlugKey("finduses.title")
	private static Label PAGE_TITLE;
	@PlugKey("finduses.showversions")
	private static Label SHOW_VERSIONS;
	@PlugKey("finduses.dateadded")
	private static Label LABEL_DATE_ADDED;
	@PlugURL("images/finduses/droparrow.gif")
	private static String URL_DROP_ARROW;
	@PlugKey("finduses.showdetails")
	private static Label LABEL_SHOW_DETAILS;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorRepositoryService repositoryService;

	@Inject
	@Component
	private LMSAuthDialog authDialog;
	@Component(name = "cl")
	private SingleSelectionList<BaseEntityLabel> connectorsList;
	@Component(name = "use")
	private Table usageTable;
	@Component(name = "sa")
	private Checkbox showArchived;
	@Component(name = "sv")
	private Checkbox showAllVersion;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		if( connectorService.listForViewing().isEmpty()
			|| !itemInfo.hasPrivilege(ConnectorConstants.PRIV_FIND_USES_ITEM) )
		{
			return null;
		}

		ensureSingleSelection(context);

		final FindUsesModel model = getModel(context);

		final Connector connector = getConnector(context);
		if( connector != null )
		{
			if( repositoryService.isRequiresAuthentication(connector) )
			{
				model.setAuthRequired(true);

				authDialog.setConnectorUuid(context, connector.getUuid());
			}
			else
			{

				final ConnectorTerminology terms = repositoryService.getConnectorTerminology(connector.getLmsType());

				showArchived.setLabel(context, new KeyLabel(terms.getShowArchived()));

				if( !checkErrors(context) )
				{
					final List<ConnectorContent> usage = getUsage(context);
					if( usage != null && usage.size() > 0 )
					{
						model.setResults(true);

						final UnmodifiableAttachments attachments = new UnmodifiableAttachments(itemInfo.getItem());

						// column headings
						final TableState tableState = usageTable.getState(context);
						final TableHeaderRow header = tableState.setColumnHeadings(
							new KeyLabel(terms.getCourseHeading()), new KeyLabel(terms.getLocationHeading()),
							LABEL_DATE_ADDED, null);
						header.getCells().get(3).addClass("droparrow");

						for( ConnectorContent use : usage )
						{
							final String link = use.getCourseUrl();

							final Object course;
							final String courseName = use.getCourse();
							if( Check.isEmpty(link) )
							{
								course = courseName;
							}
							else
							{
								HtmlLinkState linkState = new HtmlLinkState(new SimpleBookmark(link));
								linkState.setLabel(new TextLabel(courseName));
								linkState.setTarget("_blank");
								course = linkState;
							}

							final ConnectorContentDisplay display = new ConnectorContentDisplay();
							final Date dateAdded = use.getDateAdded();
							display.setDateAdded(dateAdded);
							display.setDateModified(use.getDateModified());
							List<ConnectorContentAttribute> attributeList = use.getAttributeList();
							List<ConnectorContentAttribute> renderAttList = new ArrayList<ConnectorContentAttribute>();
							for( ConnectorContentAttribute attribute : attributeList )
							{
								renderAttList.add(new ConnectorContentAttribute(attribute.getLabelKey(),
									SectionUtils.convertToRenderer(attribute.getValue())));
							}
							display.setAttributes(renderAttList);
							display.setExternalTitle(new TextLabel(use.getExternalTitle()));

							display.setVersion(use.getVersion());
							final String attachmentUuid = use.getAttachmentUuid();
							if( !Check.isEmpty(attachmentUuid) )
							{
								final IAttachment attachment = attachments.getAttachmentByUuid(attachmentUuid);
								display.setAttachmentName(
									new TextLabel(attachment == null ? attachmentUuid : attachment.getDescription()));
							}
							SectionRenderable details = viewFactory.createResultWithModel("finduses/details.ftl",
								display);

							TableCell dropArrowCol = new TableCell(
								new ImageRenderer(URL_DROP_ARROW, LABEL_SHOW_DETAILS)).addClass("droparrow");

							String folderUrl = use.getFolderUrl();
							final Object folder;
							final String folderName = use.getFolder();

							if( Check.isEmpty(folderUrl) )
							{
								folder = folderName;
							}
							else
							{
								HtmlLinkState linkState = new HtmlLinkState(new SimpleBookmark(folderUrl));
								linkState.setLabel(new TextLabel(folderName));
								linkState.setTarget("_blank");
								folder = linkState;
							}

							final TableRow row = tableState.addRow(course, folder, details, dropArrowCol);
							row.addClass("detailsRow");
							row.setSortData(courseName, folderName, dateAdded);
						}
					}
				}
			}
		}

		addDefaultBreadcrumbs(context, itemInfo, PAGE_TITLE);
		displayBackButton(context);

		return viewFactory.createResult("finduses/summary.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		connectorsList.setListModel(new ConnectorsListModel());
		UpdateDomFunction ajaxUpdateDomFunction = ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("loadUsage"), "lms-table-ajax");
		OverrideHandler findFunc = new OverrideHandler(ajaxUpdateDomFunction);
		connectorsList.addChangeEventHandler(findFunc);
		connectorsList.setAlwaysSelect(false);

		usageTable.addReadyStatements(Js.call_s(SETUP_DETAILS, Jq.$(usageTable)));
		usageTable.setColumnSorts(Sort.SORTABLE_ASC, Sort.SORTABLE_ASC, Sort.PRIMARY_ASC);

		showAllVersion.setLabel(SHOW_VERSIONS);

		showArchived.addClickStatements(findFunc);
		showAllVersion.addClickStatements(findFunc);

		authDialog.setOkCallback(new ReloadFunction());
	}

	@EventHandlerMethod
	public void loadUsage(SectionInfo info)
	{
		// ensure usages NOW. catch errors before render time
		final Connector connector = getConnector(info);
		if( connector != null )
		{
			if( !repositoryService.isRequiresAuthentication(connector) )
			{
				checkErrors(info);
			}
		}
	}

	// returns true if errors exists
	private boolean checkErrors(SectionInfo info)
	{
		final FindUsesModel model = getModel(info);

		try
		{
			getUsage(info);
		}
		catch( Exception e )
		{
			final Throwable t = Throwables.getRootCause(e);
			LOGGER.error("Error loading usages", t);
			if( t instanceof LmsUserNotFoundException )
			{
				model.setError(CurrentLocale.get(KEY_NO_USER, ((LmsUserNotFoundException) t).getUsername()));
			}
			else
			{
				model.setError(e.getLocalizedMessage());
			}
		}

		return !Check.isEmpty(model.getError());
	}

	// @DirectEvent
	private void ensureSingleSelection(SectionInfo info)
	{
		final FindUsesModel model = getModel(info);
		final List<BaseEntityLabel> exportable = getConnectors(info);

		if( Check.isEmpty(connectorsList.getSelectedValueAsString(info)) )
		{
			if( exportable.size() == 1 )
			{
				connectorsList.setSelectedStringValue(info, exportable.get(0).getUuid());
			}
		}

		if( exportable.size() == 1 )
		{
			model.setSingleConnectorName(new BundleLabel(exportable.get(0).getBundleId(), bundleCache));
			loadUsage(info);
		}
	}

	protected List<ConnectorContent> getUsage(SectionInfo info) throws LmsUserNotFoundException
	{
		LOGGER.trace("getUsage enter");
		final FindUsesModel model = getModel(info);
		List<ConnectorContent> content = model.getContentCache();
		if( content == null )
		{
			LOGGER.trace("No current content cache");
			final Connector connector = getConnector(info);
			if( connector != null )
			{
				Item item = ParentViewItemSectionUtils.getItemInfo(info).getItem();
				LOGGER.trace("Calling findUsages for connector");
				content = repositoryService.findUsages(connector, CurrentUser.getUsername(), item,
					showArchived.isChecked(info), showAllVersion.isChecked(info));

				model.setContentCache(content);
			}
		}
		return content;
	}

	protected Connector getConnector(SectionInfo info)
	{
		LOGGER.trace("getConnector enter");
		final FindUsesModel model = getModel(info);
		Connector connector = model.getConnector();
		if( connector == null )
		{
			LOGGER.trace("No current connector loaded");
			final BaseEntityLabel connectorLabel = connectorsList.getSelectedValue(info);
			if( connectorLabel != null )
			{
				final String uuid = connectorLabel.getUuid();
				LOGGER.trace("Loading connector");
				connector = connectorService.getByUuid(uuid);
				model.setConnector(connector);
				LOGGER.trace("Connector loaded");
			}
			else
			{
				LOGGER.trace("No connector selected");
			}
		}
		return connector;
	}

	public SingleSelectionList<BaseEntityLabel> getConnectorsList()
	{
		return connectorsList;
	}

	protected List<BaseEntityLabel> getConnectors(SectionInfo info)
	{
		final FindUsesModel model = getModel(info);
		List<BaseEntityLabel> connectors = model.getConnectorsCache();
		if( connectors == null )
		{
			connectors = connectorService.listForViewing();
			model.setConnectorsCache(connectors);
		}
		return connectors;
	}

	public class ConnectorsListModel extends DynamicHtmlListModel<BaseEntityLabel>
	{
		public ConnectorsListModel()
		{
			setSort(true);
		}

		@Override
		protected Option<BaseEntityLabel> getTopOption()
		{
			return new KeyOption<BaseEntityLabel>(KEY_SELECT, "", null);
		}

		@Override
		protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
		{
			return getConnectors(info);
		}

		@Override
		protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel bent)
		{
			return new NameValueOption<BaseEntityLabel>(
				new BundleNameValue(bent.getBundleId(), bent.getUuid(), bundleCache), bent);
		}
	}

	@Override
	public Class<FindUsesModel> getModelClass()
	{
		return FindUsesModel.class;
	}

	public LMSAuthDialog getAuthDialog()
	{
		return authDialog;
	}

	public Table getUsageTable()
	{
		return usageTable;
	}

	public Checkbox getShowArchived()
	{
		return showArchived;
	}

	public Checkbox getShowAllVersion()
	{
		return showAllVersion;
	}

	public static class FindUsesModel
	{
		private boolean authRequired;
		private Label singleConnectorName;
		private List<BaseEntityLabel> connectorsCache;
		private List<ConnectorContent> contentCache;
		private Connector connector;
		private String error;
		private boolean results;

		public boolean isAuthRequired()
		{
			return authRequired;
		}

		public void setAuthRequired(boolean authRequired)
		{
			this.authRequired = authRequired;
		}

		public Label getSingleConnectorName()
		{
			return singleConnectorName;
		}

		public void setSingleConnectorName(Label singleConnectorName)
		{
			this.singleConnectorName = singleConnectorName;
		}

		protected List<BaseEntityLabel> getConnectorsCache()
		{
			return connectorsCache;
		}

		protected void setConnectorsCache(List<BaseEntityLabel> connectorsCache)
		{
			this.connectorsCache = connectorsCache;
		}

		protected List<ConnectorContent> getContentCache()
		{
			return contentCache;
		}

		protected void setContentCache(List<ConnectorContent> contentCache)
		{
			this.contentCache = contentCache;
		}

		public String getError()
		{
			return error;
		}

		protected void setError(String error)
		{
			this.error = error;
		}

		public Connector getConnector()
		{
			return connector;
		}

		protected void setConnector(Connector connector)
		{
			this.connector = connector;
		}

		public boolean isResults()
		{
			return results;
		}

		public void setResults(boolean results)
		{
			this.results = results;
		}

	}

	public static class ConnectorContentDisplay
	{
		private Date dateAdded;
		private Date dateModified;
		private int version;
		private Label attachmentName;
		private Label externalTitle;
		private List<ConnectorContentAttribute> attributes;

		public int getVersion()
		{
			return version;
		}

		public void setVersion(int version)
		{
			this.version = version;
		}

		public Label getAttachmentName()
		{
			return attachmentName;
		}

		public void setAttachmentName(Label attachmentName)
		{
			this.attachmentName = attachmentName;
		}

		public Label getExternalTitle()
		{
			return externalTitle;
		}

		public void setExternalTitle(Label externalTitle)
		{
			this.externalTitle = externalTitle;
		}

		public List<ConnectorContentAttribute> getAttributes()
		{
			return attributes;
		}

		public void setAttributes(List<ConnectorContentAttribute> attributes)
		{
			this.attributes = attributes;
		}

		public Date getDateAdded()
		{
			return dateAdded;
		}

		public void setDateAdded(Date dateAdded)
		{
			this.dateAdded = dateAdded;
		}

		public Date getDateModified()
		{
			return dateModified;
		}

		public void setDateModified(Date dateModified)
		{
			this.dateModified = dateModified;
		}
	}
}
