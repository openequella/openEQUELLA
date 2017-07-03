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

package com.tle.web.connectors.export;

import static com.tle.common.collection.AttachmentConfigConstants.DISPLAY_MODE_KEY;
import static com.tle.common.collection.AttachmentConfigConstants.DISPLAY_MODE_THUMBNAIL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.settings.standard.CourseDefaultsSettings;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.connectors.dialog.LMSAuthDialog;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryTextFieldHint;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer;
import com.tle.web.selection.SelectableAttachment;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.attachments.AttachmentView;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService.AttachmentRowDisplay;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
@Bind
public class LMSExportSection extends AbstractContentSection<LMSExportSection.LMSExporterModel>
{
	private static final Logger LOGGER = Logger.getLogger(LMSExportSection.class);
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(LMSExportSection.class);
	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/lmsexporter.js"));
	private static final ExternallyDefinedFunction FUNCTION_CLICKABLE_LINES = new ExternallyDefinedFunction(
		"makeClickable", 2, INCLUDE);

	private static final ExternallyDefinedFunction FUNCTION_RESOURCE_SELECTED = new ExternallyDefinedFunction(
		"resourceSelected", 3, INCLUDE);
	private static final ExternallyDefinedFunction FUNCTION_LOCATION_SELECTED = new ExternallyDefinedFunction(
		"locationSelected", 1, INCLUDE);

	private static final ExternallyDefinedFunction FUNCTION_FILTER_COURSES = new ExternallyDefinedFunction(
		"filterCourses", 2, INCLUDE);
	private static final ExternallyDefinedFunction FUNCTION_SELECT_ALL_ATTACHMENTS = new ExternallyDefinedFunction(
		"selectAllAttachments", 0, INCLUDE);
	private static final ExternallyDefinedFunction FUNCTION_DO_FILTER = new ExternallyDefinedFunction("doFilter", 3,
		INCLUDE);
	private static final ExternallyDefinedFunction FUNCTION_SWALLOW_ENTER = new ExternallyDefinedFunction(
		"swallowEnter", 1, INCLUDE);

	@PlugKey("export.title")
	private static Label LABEL_TITLE;
	@PlugKey("export.error.nolocationsselected")
	private static Label LABEL_ERROR_NO_LOCATION_SELECTED;
	@PlugKey("export.error.noresourcesselected")
	private static Label LABEL_ERROR_NO_RESOURCES_SELECTED;
	@PlugKey("export.selectconnector")
	private static String KEY_SELECT;
	@PlugKey("export.label.filter")
	private static Label LABEL_FILTER_COURSES;
	@PlugKey("export.label.attachment.noactivations")
	private static Label LABEL_NO_ACTIVATIONS;

	@PlugKey("export.added.full.message")
	private static String KEY_PUBLISHED;
	@PlugKey("export.added.full.location.attachment")
	private static String KEY_LOCATION_ATTACHMENT;
	@PlugKey("export.added.full.location.summary")
	private static String KEY_LOCATION_SUMMARY;
	@PlugKey("export.added.full.location.package")
	private static String KEY_LOCATION_PACKAGE;

	@PlugKey("export.added.summary.singleresource.resourcesummary")
	private static Label LABEL_RESOURCE_SUMMARY;
	@PlugKey("export.added.summary.singleresource.contentpackage")
	private static Label LABEL_CONTENT_PACKAGE;
	@PlugKey("export.added.summary.singleresourcemultilocations")
	private static String KEY_SUMMARY_SINGLERESOURCE_MULTILOCATIONS;
	@PlugKey("export.added.summary.multiresourcessinglelocation")
	private static String KEY_SUMMARY_MULTIRESOURCES_SINGLELOCATION;
	@PlugKey("export.added.summary.multiresourcesmultilocations")
	private static String KEY_SUMMARY_MULTIRESOURCES_MULTILOCATIONS;
	@PlugKey("export.error.nothingtopush")
	private static Label LABEL_ERROR_NOTHING_TO_PUSH;

	@PlugKey("export.error.accessdenied")
	private static String ACCESS_DENIED;

	@Inject
	private PluginTracker<SelectableAttachment> selectableAttachments;
	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorRepositoryService repositoryService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ViewAttachmentWebService viewAttachmentWebService;
	@Inject
	private ConfigurationService systemConstantsService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	@Component
	private LMSAuthDialog authDialog;
	@PlugKey("export.label.selectsummary")
	@Component(name = "ss")
	private Checkbox selectSummary;
	@PlugKey("export.label.selectpackage")
	@Component(name = "sp")
	private Checkbox selectContentPackage;
	@Component(name = "as")
	private MappedBooleans attachmentSelections;
	@Component(name = "cl")
	private SingleSelectionList<BaseEntityLabel> connectorsList;
	@Component
	private Tree folderTree;
	@Component(name = "fs")
	private MappedBooleans folderSelections;
	@PlugKey("export.button.export")
	@Component
	private Button publishButton;
	@Component(name = "sa")
	private Checkbox showArchived;
	@Component(name = "fb", stateful = false, ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
	private TextField filterBox;
	@PlugKey("export.label.selectallattachments")
	@Component(name = "saa", stateful = false, ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
	private Checkbox selectAllAttachments;

	private boolean showStructuredView;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( getConnectors(context).isEmpty() )
		{
			return null;
		}

		ensureSingleSelection(context);
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		addDefaultBreadcrumbs(context, itemInfo, LABEL_TITLE);

		// !! This needs to be tied to something that is always rendered.
		publishButton.addReadyStatements(context,
			viewAttachmentWebService.createShowDetailsFunction(itemInfo.getItemId(), ".attachments-browse"));
		final LMSExporterModel model = getModel(context);

		final Connector connector = getConnector(context);
		if( connector != null && repositoryService.isRequiresAuthentication(connector) )
		{
			model.setAuthRequired(true);

			authDialog.setConnectorUuid(context, connector.getUuid());
		}
		else
		{
			if( !checkErrors(context) )
			{
				checkAttachmentLayout(context);
				model.setConnectorSelected(connectorsList.getSelectedValue(context) != null);

				final ViewableItem viewableItem = itemInfo.getViewableItem();
				final List<AttachmentRowDisplay> attachmentRowDisplays = viewAttachmentWebService.createViewsForItem(
					context, viewableItem, new SimpleElementId("lms-tree-ajax"), false, true, !isShowStructuredView(), false);
				context.setAttribute("showCopyrightAttachments", true);
				viewAttachmentWebService.filterAttachmentDisplays(context, attachmentRowDisplays);

				List<SelectableAttachment> beanList = selectableAttachments.getBeanList();
				model.setCopyrighted(beanList.stream().anyMatch(sa -> sa.isItemCopyrighted(itemInfo.getItem())));

				for( AttachmentRowDisplay row : attachmentRowDisplays )
				{
					// prepend a checkbox
					final AttachmentView attachmentView = row.getAttachmentView();
					if( attachmentView != null )
					{
						final TagRenderer rowLi = row.getRow();
						String attachmentUuid = attachmentView.getAttachment().getUuid();
						CheckboxRenderer attachmentCheckbox = new CheckboxRenderer(
							attachmentSelections.getBooleanState(context, attachmentUuid));
						if( model.isCopyrighted() )
						{
							HtmlComponentState checkBoxState = attachmentCheckbox.getHtmlState();
							if( beanList.stream().noneMatch(sa -> sa.canBePushed(attachmentUuid)) )
							{
								checkBoxState.setDisabled(true);
								checkBoxState.setTitle(LABEL_NO_ACTIVATIONS);
							}
							else
							{
								checkBoxState
									.setClickHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(getTree(), this,
										events.getEventHandler("filterCourses"), "lms-tree-ajax")));
							}
							if( !Check.isEmpty(attachmentSelections.getCheckedSet(context)) )
							{
								filterCourses(context);
							}
						}
						rowLi.setNestedRenderable(
							CombinedRenderer.combineResults(attachmentCheckbox, rowLi.getNestedRenderable()));
					}
				}

				model.setAttachmentRows(attachmentRowDisplays);

				final ImsAttachment imsAttachment = new UnmodifiableAttachments(itemInfo.getItem()).getIms();
				if( imsAttachment != null )
				{
					model.setContentPackage(true);
				}

				if( connector != null )
				{
					ConnectorTerminology terms = repositoryService.getConnectorTerminology(connector.getLmsType());
					model.setTerms(terms);
					showArchived.setLabel(context, new KeyLabel(terms.getShowArchivedLocations()));
					selectSummary.setDisplayed(context,
						(connector.getAttribute(ConnectorConstants.SHOW_SUMMARY_KEY, true) && !model.isCopyrighted()));
				}

				if( !selectSummary.isDisplayed(context) && !model.isContentPackage()
					&& Check.isEmpty(model.getAttachmentRows()) )
				{
					model.setError(LABEL_ERROR_NOTHING_TO_PUSH);
				}
			}
		}

		filterBox.setValue(context, null);
		return viewFactory.createResult("lmsexporter.ftl", this);
	}

	/*
	 * @EventHandlerMethod public void filterCourses(SectionInfo info) {
	 * LMSExporterModel model = getModel(info); model.setFilteredCourses(null);
	 * model.setCoursesCache(null); }
	 */

	@EventHandlerMethod
	public void filterCourses(SectionInfo info)
	{
		List<String> activatedCourses = null;
		for( String attachment : attachmentSelections.getCheckedSet(info) )
		{
			CourseDefaultsSettings courseSettings = systemConstantsService.getProperties(new CourseDefaultsSettings());
			if( !courseSettings.isPortionRestrictionsEnabled() )
			{
				LMSExporterModel model = getModel(info);
				model.setFilteredCourses(null);
				model.setCoursesCache(null);
				return;
			}
			List<String> thisAttachment = new ArrayList<String>();
			for( SelectableAttachment bean : selectableAttachments.getBeanList() )
			{
				thisAttachment.addAll(bean.getApplicableCourseCodes(attachment));
			}
			if( activatedCourses == null )
			{
				activatedCourses = new ArrayList<String>();
				activatedCourses.addAll(thisAttachment);
			}
			else
			{
				activatedCourses.retainAll(thisAttachment);
			}
		}

		LMSExporterModel model = getModel(info);
		model.setFilteredCourses(activatedCourses);
		if( activatedCourses != null )
		{
			model.setCoursesCache(null);
		}
	}

	private void checkAttachmentLayout(SectionInfo info)
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		ItemDefinition collection = itemInfo.getItemdef();
		setShowStructuredView(true);

		for( SummarySectionsConfig ssc : collection.getItemSummaryDisplayTemplate().getConfigList() )
		{
			if( ssc.getValue().contains("attachmentsSection") )
			{
				String configxml = ssc.getConfiguration();
				if( !Check.isEmpty(configxml) )
				{
					PropBagEx xml = new PropBagEx(configxml);
					setShowStructuredView(!xml.getNode(DISPLAY_MODE_KEY).equals(DISPLAY_MODE_THUMBNAIL));
					break;
				}
			}
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		// JSCallable filterCoursesCallback = CallAndReferenceFunction.get();
		JSExpression boundKeyup = Js.methodCall(Jq.$(filterBox), Js.function("keyup"),
			Js.function(Js.call_s(FUNCTION_FILTER_COURSES, Jq.$(folderTree), Jq.$(filterBox))));
		ScriptVariable event = Js.var("event");
		JSExpression boundKeydownAndKeyup = Js.methodCall(boundKeyup, Js.function("keydown"),
			Js.function(Js.call_s(FUNCTION_SWALLOW_ENTER, event), event));
		filterBox.addReadyStatements(Js.statement(boundKeydownAndKeyup));
		filterBox.addTagProcessor(new JQueryTextFieldHint(LABEL_FILTER_COURSES, filterBox));

		JSExpression boundClick = Js.methodCall(Jq.$(selectAllAttachments), Js.function("click"),
			Js.function(Js.call_s(FUNCTION_SELECT_ALL_ATTACHMENTS, Jq.$(selectAllAttachments))));
		selectAllAttachments.addReadyStatements(Js.statement(boundClick));

		connectorsList.setListModel(new ConnectorsListModel());
		connectorsList.addChangeEventHandler(new OverrideHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("loadTree"), "lms-tree-ajax"), true));
		connectorsList.setAlwaysSelect(false);

		SimpleValidator locationValidator = new SimpleValidator(
			Js.call(FUNCTION_LOCATION_SELECTED, folderSelections.getElementsExpression()));
		locationValidator.setFailureStatements(Js.alert_s(LABEL_ERROR_NO_LOCATION_SELECTED));

		FunctionCallExpression resourceSelected = Js.call(FUNCTION_RESOURCE_SELECTED,
			attachmentSelections.getElementsExpression(), Jq.$(selectSummary), Jq.$(selectContentPackage));

		SimpleValidator resourceValidator = new SimpleValidator(resourceSelected);
		resourceValidator.setFailureStatements(Js.alert_s(LABEL_ERROR_NO_RESOURCES_SELECTED));

		publishButton.addClickStatements(
			events.getNamedHandler("publish").addValidator(locationValidator).addValidator(resourceValidator));

		folderTree.setModel(new CourseTreeModel());
		folderTree.setLazyLoad(true);
		folderTree.setAllowMultipleOpenBranches(true);
		folderTree.addReadyStatements(Js.call_s(FUNCTION_CLICKABLE_LINES, Jq.$(folderTree), null));

		final JSCallAndReference doFilterCall = CallAndReferenceFunction
			.get(Js.function(Js.call_s(FUNCTION_DO_FILTER, Jq.$(folderTree), Jq.$(filterBox), true)), showArchived);
		showArchived.addClickStatements(new OverrideHandler(ajax.getAjaxUpdateDomFunctionWithCallback(tree, this,
			events.getEventHandler("loadTree"), doFilterCall, "lms-tree-container"), false));
		showArchived.addReadyStatements(doFilterCall);

		authDialog.setOkCallback(new ReloadFunction());
	}

	// @DirectEvent
	private void ensureSingleSelection(SectionInfo info)
	{
		final LMSExporterModel model = getModel(info);
		final List<BaseEntityLabel> exportable = getConnectors(info);

		if( exportable.size() == 1 )
		{
			model.setSingleConnectorName(new BundleLabel(exportable.get(0).getBundleId(), bundleCache));
		}

		if( Check.isEmpty(connectorsList.getSelectedValueAsString(info)) )
		{
			if( exportable.size() == 1 )
			{
				connectorsList.setSelectedStringValue(info, exportable.get(0).getUuid());
			}
		}
	}

	@EventHandlerMethod
	public void loadTree(SectionInfo info, boolean clearFolders)
	{
		final Connector connector = getConnector(info);
		if( connector != null )
		{
			if( repositoryService.isRequiresAuthentication(connector) )
			{
				//just render the Authorise button
				return;
			}
		}

		if( clearFolders )
		{
			selectSummary.setChecked(info, false);
			selectContentPackage.setChecked(info, false);
			attachmentSelections.clearChecked(info);
			folderSelections.clearChecked(info);
		}

		checkErrors(info);
	}

	// returns true if errors exists
	private boolean checkErrors(SectionInfo info)
	{
		// ensure courses NOW. catch errors before render time
		final LMSExporterModel model = getModel(info);
		try
		{
			getCourses(info);
		}
		catch( LmsUserNotFoundException lms )
		{
			model.setError(new TextLabel(lms.getMessage()));
		}
		catch( Exception e )
		{
			LOGGER.error("Error loading course tree", e);
			model.setError(new TextLabel(e.getMessage()));
		}

		return model.getError() != null;
	}

	@EventHandlerMethod
	public void publish(SectionInfo info)
	{
		ensurePriv(info);

		final Set<String> selectedFolders = folderSelections.getCheckedSet(info);
		final Set<String> selectedResources = attachmentSelections.getCheckedSet(info);

		if( selectedFolders.isEmpty() )
		{
			receiptService.setReceipt(LABEL_ERROR_NO_LOCATION_SELECTED);
			return;
		}

		// if not attachments and not summary and not package
		final boolean selectedSummary = selectSummary.isChecked(info);
		final boolean selectedContentPackage = selectContentPackage.isChecked(info);
		if( selectedResources.isEmpty() && !selectedSummary && !selectedContentPackage )
		{
			receiptService.setReceipt(LABEL_ERROR_NO_RESOURCES_SELECTED);
			return;
		}

		final Item item = ParentViewItemSectionUtils.getItemInfo(info).getItem();
		final UnmodifiableAttachments attachments = new UnmodifiableAttachments(item);

		final BaseEntityLabel value = connectorsList.getSelectedValue(info);
		// Can it be null??
		if( value != null )
		{
			final Connector connector = connectorService.getByUuid(value.getUuid());

			final int numLocations = selectedFolders.size();
			int numResources = selectedResources.size();
			if( selectedSummary )
			{
				numResources++;
			}
			if( selectedContentPackage )
			{
				numResources++;
			}
			final boolean useSummaryMessage = (numResources * numLocations > 4);
			final List<Label> locations = Lists.newArrayList();
			String singleResource = null;
			String singleCourse = null;
			String singleFolder = null;

			for( String folderAndCourseId : selectedFolders )
			{
				final String[] components = folderAndCourseId.split("\\$");
				final String courseId = components[0];
				final String folderId = components[1];

				// Can it be empty??
				if( !Check.isEmpty(folderId) )
				{

					for( String attachmentUuid : selectedResources )
					{
						final IAttachment attachment = attachments.getAttachmentByUuid(attachmentUuid);

						final ConnectorFolder folder = publish(info, connector, folderId, courseId, item, attachment);
						if( !useSummaryMessage )
						{
							locations.add(new KeyLabel(KEY_LOCATION_ATTACHMENT, attachment.getDescription(),
								folder.getName(), folder.getCourse().getName()));
						}
						else if( numResources == 1 )
						{
							singleResource = attachment.getDescription();
						}
						else if( numLocations == 1 )
						{
							singleFolder = folder.getName();
							singleCourse = folder.getCourse().getName();
						}
					}
					if( selectedSummary )
					{
						final ConnectorFolder folder = publish(info, connector, folderId, courseId, item, null);
						if( !useSummaryMessage )
						{
							locations.add(
								new KeyLabel(KEY_LOCATION_SUMMARY, folder.getName(), folder.getCourse().getName()));
						}
						else if( numResources == 1 )
						{
							singleResource = LABEL_RESOURCE_SUMMARY.getText();
						}
						else if( numLocations == 1 )
						{
							singleFolder = folder.getName();
							singleCourse = folder.getCourse().getName();
						}
					}
					if( selectedContentPackage )
					{
						final ImsAttachment attachment = attachments.getIms();

						final ConnectorFolder folder = publish(info, connector, folderId, courseId, item, attachment);
						if( !useSummaryMessage )
						{
							locations.add(
								new KeyLabel(KEY_LOCATION_PACKAGE, folder.getName(), folder.getCourse().getName()));
						}
						else if( numResources == 1 )
						{
							singleResource = LABEL_CONTENT_PACKAGE.getText();
						}
						else if( numLocations == 1 )
						{
							singleFolder = folder.getName();
							singleCourse = folder.getCourse().getName();
						}
					}
				}
			}

			final Label receipt;
			if( !useSummaryMessage )
			{
				final StringBuilder locs = new StringBuilder();
				for( Label location : locations )
				{
					locs.append(location.getText());
				}
				receipt = new KeyLabel(KEY_PUBLISHED, locs.toString());
			}
			else if( numResources == 1 )
			{
				receipt = new KeyLabel(KEY_SUMMARY_SINGLERESOURCE_MULTILOCATIONS, singleResource, numLocations);
			}
			else if( numLocations == 1 )
			{
				receipt = new KeyLabel(KEY_SUMMARY_MULTIRESOURCES_SINGLELOCATION, numResources, singleFolder,
					singleCourse);
			}
			else
			{
				receipt = new KeyLabel(KEY_SUMMARY_MULTIRESOURCES_MULTILOCATIONS, numResources, numLocations);
			}
			receiptService.setReceipt(receipt);
			loadTree(info, true);
		}
	}

	private ConnectorFolder publish(SectionInfo info, Connector connector, String folderId, String courseId, Item item,
		IAttachment attachment)
	{
		ensurePriv(info);

		try
		{
			return repositoryService.addItemToCourse(connector, CurrentUser.getUsername(), courseId, folderId, item,
				attachment, info);
		}
		catch( LmsUserNotFoundException e )
		{
			// Not going to happen, this would have already happened
			// when populating the tree.
			throw Throwables.propagate(e);
		}
	}

	private void ensurePriv(SectionInfo info)
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		if( !itemInfo.hasPrivilege(ConnectorConstants.PRIV_EXPORT_TO_LMS_ITEM) )
		{
			throw new AccessDeniedException(CurrentLocale.get(ACCESS_DENIED));
		}
	}

	protected List<BaseEntityLabel> getConnectors(SectionInfo info)
	{
		ensurePriv(info);
		final LMSExporterModel model = getModel(info);

		List<BaseEntityLabel> connectors = model.getConnectorsCache();
		if( connectors == null )
		{
			connectors = Lists.newArrayList();
			List<BaseEntityLabel> allForUser = connectorService.listExportable();
			for( BaseEntityLabel bel : allForUser )
			{
				Connector connector = connectorService.getByUuid(bel.getUuid());

				if( repositoryService.supportsExport(connector.getLmsType()) )
				{
					connectors.add(bel);
				}
			}

			model.setConnectorsCache(connectors);
		}
		return connectors;
	}

	protected Connector getConnector(SectionInfo info)
	{
		final LMSExporterModel model = getModel(info);
		Connector connector = model.getConnector();
		if( connector == null )
		{
			final BaseEntityLabel connectorLabel = connectorsList.getSelectedValue(info);
			if( connectorLabel != null )
			{
				final String uuid = connectorLabel.getUuid();
				connector = connectorService.getByUuid(uuid);
				model.setConnector(connector);
			}
		}
		return connector;
	}

	protected List<ConnectorCourse> getCourses(SectionInfo info) throws LmsUserNotFoundException
	{
		final LMSExporterModel model = getModel(info);
		List<ConnectorCourse> courses = model.getCoursesCache();
		if( courses == null )
		{
			final Connector connector = getConnector(info);
			if( connector != null )
			{
				courses = repositoryService.getModifiableCourses(connector, CurrentUser.getUsername(),
					showArchived.isChecked(info), false);
				if( model.getFilteredCourses() != null )
				{
					List<ConnectorCourse> filtered = courses.stream()
						.filter(c -> model.getFilteredCourses().contains(c.getCourseCode()))
						.collect(Collectors.toList());
					courses = filtered;
				}
				model.setCoursesCache(courses);
			}
		}
		return courses;
	}

	@Override
	public Class<LMSExporterModel> getModelClass()
	{
		return LMSExporterModel.class;
	}

	public Checkbox getSelectSummary()
	{
		return selectSummary;
	}

	public Checkbox getSelectContentPackage()
	{
		return selectContentPackage;
	}

	public SingleSelectionList<BaseEntityLabel> getConnectorsList()
	{
		return connectorsList;
	}

	public LMSAuthDialog getAuthDialog()
	{
		return authDialog;
	}

	public Tree getFolderTree()
	{
		return folderTree;
	}

	public Button getPublishButton()
	{
		return publishButton;
	}

	public Checkbox getShowArchived()
	{
		return showArchived;
	}

	public TextField getFilterBox()
	{
		return filterBox;
	}

	public Checkbox getSelectAllAttachments()
	{
		return selectAllAttachments;
	}

	public static class LMSExporterModel extends BaseLMSExportModel
	{
		private Label singleConnectorName;
		private List<BaseEntityLabel> connectorsCache;
		private boolean authRequired;
		private boolean connectorSelected;
		private boolean contentPackage;
		private List<AttachmentRowDisplay> attachmentRows;
		private ConnectorTerminology terms;
		private List<String> filteredCourses;
		private boolean copyrighted;

		public Label getSingleConnectorName()
		{
			return singleConnectorName;
		}

		public void setSingleConnectorName(Label singleConnectorName)
		{
			this.singleConnectorName = singleConnectorName;
		}

		public List<BaseEntityLabel> getConnectorsCache()
		{
			return connectorsCache;
		}

		public void setConnectorsCache(List<BaseEntityLabel> connectorsCache)
		{
			this.connectorsCache = connectorsCache;
		}

		public boolean isAuthRequired()
		{
			return authRequired;
		}

		public void setAuthRequired(boolean authRequired)
		{
			this.authRequired = authRequired;
		}

		public boolean isConnectorSelected()
		{
			return connectorSelected;
		}

		public void setConnectorSelected(boolean connectorSelected)
		{
			this.connectorSelected = connectorSelected;
		}

		public boolean isContentPackage()
		{
			return contentPackage;
		}

		public void setContentPackage(boolean contentPackage)
		{
			this.contentPackage = contentPackage;
		}

		public List<AttachmentRowDisplay> getAttachmentRows()
		{
			return attachmentRows;
		}

		public void setAttachmentRows(List<AttachmentRowDisplay> attachmentRows)
		{
			this.attachmentRows = attachmentRows;
		}

		public ConnectorTerminology getTerms()
		{
			return terms;
		}

		public void setTerms(ConnectorTerminology terms)
		{
			this.terms = terms;
		}

		public List<String> getFilteredCourses()
		{
			return filteredCourses;
		}

		public void setFilteredCourses(List<String> filteredCourses)
		{
			this.filteredCourses = filteredCourses;
		}

		public boolean isCopyrighted()
		{
			return copyrighted;
		}

		public void setCopyrighted(boolean copyrighted)
		{
			this.copyrighted = copyrighted;
		}
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

	public class CourseTreeModel implements HtmlTreeModel
	{
		@Override
		public List<HtmlTreeNode> getChildNodes(SectionInfo info, String id)
		{
			final Connector connector = getConnector(info);
			if( Check.isEmpty(id) )
			{
				try
				{
					return Lists.transform(getCourses(info), new FolderToTreeNodeTransform(info));
				}
				catch( LmsUserNotFoundException e )
				{
					throw Throwables.propagate(e);
				}
			}
			else if( id.contains("$") )
			{
				final String[] ids = id.split("\\$");
				final String courseId = ids[0];
				final String folderId = ids[1];
				return Lists.transform(repositoryService.getFoldersForFolder(connector, CurrentUser.getUsername(),
					courseId, folderId, false), new FolderToTreeNodeTransform(info));
			}

			return Lists.transform(
				repositoryService.getFoldersForCourse(connector, CurrentUser.getUsername(), id, false),
				new FolderToTreeNodeTransform(info));
		}
	}

	private class FolderToTreeNodeTransform implements Function<ConnectorFolder, HtmlTreeNode>
	{
		private final SectionInfo info;

		public FolderToTreeNodeTransform(SectionInfo info)
		{
			this.info = info;
		}

		@Override
		public HtmlTreeNode apply(ConnectorFolder input)
		{
			return new LmsFolderTreeNode(info, input);
		}
	}

	public class LmsFolderTreeNode implements HtmlTreeNode
	{
		private final SectionInfo info;
		private final ConnectorFolder connectorFolder;

		protected LmsFolderTreeNode(SectionInfo info, ConnectorFolder connectorFolder)
		{
			this.info = info;
			this.connectorFolder = connectorFolder;
		}

		@Override
		public String getId()
		{
			return (connectorFolder instanceof ConnectorCourse ? connectorFolder.getId()
				: connectorFolder.getCourse().getId() + "$" + connectorFolder.getId());
		}

		@Override
		public SectionRenderable getRenderer()
		{
			if( connectorFolder instanceof ConnectorCourse )
			{
				return viewFactory.createResultWithModel("tree/courseline.ftl", this);
			}
			return viewFactory.createResultWithModel("tree/folderline.ftl", this);
		}

		@Override
		public Label getLabel()
		{
			return new TextLabel(connectorFolder.getName());
		}

		@Override
		public boolean isLeaf()
		{
			return connectorFolder.isLeaf();
		}

		public HtmlBooleanState getCheck()
		{
			return folderSelections.getBooleanState(info, getId());
		}
	}

	public boolean isShowStructuredView()
	{
		return showStructuredView;
	}

	public void setShowStructuredView(boolean showStructuredView)
	{
		this.showStructuredView = showStructuredView;
	}
}
