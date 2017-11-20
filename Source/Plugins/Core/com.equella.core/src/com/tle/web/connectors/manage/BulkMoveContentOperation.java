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

package com.tle.web.connectors.manage;

import static com.tle.web.sections.standard.RendererConstants.RADIO_CHECKBOX;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorOperationFactory;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.connectors.export.BaseLMSExportModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryTextFieldHint;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class BulkMoveContentOperation
	extends
		AbstractPrototypeSection<BulkMoveContentOperation.BulkMoveContentOperationModel>
	implements
		BulkOperationExtension
{
	private static final Logger LOGGER = Logger.getLogger(BulkMoveContentOperation.class);

	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(BulkMoveContentOperation.class);

	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/lmsexporter.js"));
	private static final ExternallyDefinedFunction FUNCTION_CLICKABLE_LINES = new ExternallyDefinedFunction(
		"makeClickable", 2, INCLUDE);

	private static final ExternallyDefinedFunction FUNCTION_FILTER_COURSES = new ExternallyDefinedFunction(
		"filterCourses", 2, INCLUDE);
	private static final ExternallyDefinedFunction FUNCTION_DO_FILTER = new ExternallyDefinedFunction("doFilter", 3,
		INCLUDE);
	private static final ExternallyDefinedFunction FUNCTION_SWALLOW_ENTER = new ExternallyDefinedFunction(
		"swallowEnter", 1, INCLUDE);

	@PlugKey("operation.move")
	private static String KEY_NAME;
	@PlugKey("connector.opresults.status")
	private static String KEY_STATUS;
	@PlugKey("export.label.filter")
	private static Label LABEL_FILTER_COURSES;

	private static final String BULK_VALUE = "move";

	@Component
	private Tree folderTree;
	@Component(name = "fs"/* , stateful = false */)
	private MappedBooleans folderSelections;
	@Component(name = "sa")
	private Checkbox showArchived;
	@Component(name = "fb", stateful = false, ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
	private TextField filterBox;

	@TreeLookup
	private ConnectorManagementQuerySection querySection;
	@TreeLookup
	private ConnectorBulkResultsDialog dialog;

	@Inject
	private ConnectorRepositoryService repositoryService;

	@ViewFactory
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		checkErrors(context);

		ConnectorTerminology terms = repositoryService.getConnectorTerminology(getConnector(context).getLmsType());
		showArchived.setLabel(context, new KeyLabel(terms.getShowArchivedLocations()));

		filterBox.setValue(context, null);

		return viewFactory.createResult("edit/move-tree.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		JSExpression boundKeyup = Js.methodCall(Jq.$(filterBox), Js.function("keyup"),
			Js.function(Js.call_s(FUNCTION_FILTER_COURSES, Jq.$(folderTree), Jq.$(filterBox))));
		ScriptVariable event = Js.var("event");
		JSExpression boundKeydownAndKeyup = Js.methodCall(boundKeyup, Js.function("keydown"),
			Js.function(Js.call_s(FUNCTION_SWALLOW_ENTER, event), event));
		filterBox.addReadyStatements(Js.statement(boundKeydownAndKeyup));
		filterBox.addTagProcessor(new JQueryTextFieldHint(LABEL_FILTER_COURSES, filterBox));

		folderTree.setModel(new CourseTreeModel());
		folderTree.setLazyLoad(true);
		folderTree.setAllowMultipleOpenBranches(true);

		showArchived.addReadyStatements(CallAndReferenceFunction
			.get(Js.function(Js.call_s(FUNCTION_DO_FILTER, Jq.$(folderTree), Jq.$(filterBox), true)), showArchived));

		showArchived.addClickStatements(new OverrideHandler(events.getSubmitValuesFunction(("loadTree")), false));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		folderTree.addReadyStatements(
			Js.call_s(FUNCTION_CLICKABLE_LINES, Jq.$(folderTree), ajax.getAjaxUpdateDomFunction(tree, null, null,
				ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), dialog.getFooterId().getElementId(null))));
		super.treeFinished(id, tree);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new BulkMoveContentOperationModel();
	}

	@EventHandlerMethod
	public void loadTree(SectionInfo info, boolean clearFolders)
	{
		folderSelections.clearChecked(info);
	}

	// returns true if errors exists
	private boolean checkErrors(SectionInfo info)
	{
		// ensure courses NOW. catch errors before render time
		final BaseLMSExportModel model = getModel(info);
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

	protected List<ConnectorCourse> getCourses(SectionInfo info) throws LmsUserNotFoundException
	{
		final BaseLMSExportModel model = getModel(info);
		List<ConnectorCourse> courses = model.getCoursesCache();
		if( courses == null )
		{
			final Connector connector = getConnector(info);
			if( connector != null )
			{
				courses = repositoryService.getModifiableCourses(connector, CurrentUser.getUsername(),
					showArchived.isChecked(info), true);
				model.setCoursesCache(courses);
			}
		}
		return courses;
	}

	@Nullable
	protected Connector getConnector(SectionInfo info)
	{
		final BaseLMSExportModel model = getModel(info);
		Connector connector = model.getConnector();
		if( connector == null )
		{
			connector = querySection.getConnector(info);
			model.setConnector(connector);
		}
		return connector;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		options.add(new KeyOption<OperationInfo>(KEY_NAME, BULK_VALUE, new OperationInfo(this, BULK_VALUE)));
	}

	@Override
	public BeanLocator<? extends BulkOperationExecutor> getExecutor(SectionInfo info, String operationId)
	{
		String folderAndCourseId = folderSelections.getFirstChecked(info);
		final String[] components = folderAndCourseId.split("\\$");
		final String courseId = components[0];
		final String folderId = components[1];

		return new FactoryMethodLocator<BulkOperationExecutor>(MoveContentOperationExecutorFactory.class, "create",
			courseId, folderId);
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(KEY_STATUS, new KeyLabel(KEY_NAME + ".title"));
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		return !folderSelections.getCheckedSet(info).isEmpty();
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return validateOptions(info, operationId);
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	public Tree getFolderTree()
	{
		return folderTree;
	}

	public MappedBooleans getFolderSelections()
	{
		return folderSelections;
	}

	public Checkbox getShowArchived()
	{
		return showArchived;
	}

	public TextField getFilterBox()
	{
		return filterBox;
	}

	@BindFactory
	public interface MoveContentOperationExecutorFactory
	{
		MoveContentOperationExecutor create(@Assisted("courseId") String courseId,
			@Assisted("locationId") String locationId);
	}

	public static class MoveContentOperationExecutor implements BulkOperationExecutor
	{
		private static final long serialVersionUID = 1L;

		private final String courseId;
		private final String locationId;

		@Inject
		public MoveContentOperationExecutor(@Assisted("courseId") String courseId,
			@Assisted("locationId") String locationId)
		{
			this.courseId = courseId;
			this.locationId = locationId;
		}

		@Inject
		private ConnectorOperationFactory operationFactory;

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{operationFactory.createMove(courseId, locationId)};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.connectors.bulk.movecontent.title";
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
					// can't happen, this is checked in render
					return Lists.newArrayList();
				}
			}
			else if( id.contains("$") )
			{
				final String[] ids = id.split("\\$");
				final String courseId = ids[0];
				final String folderId = ids[1];
				return Lists.transform(repositoryService.getFoldersForFolder(connector, CurrentUser.getUsername(),
					courseId, folderId, true), new FolderToTreeNodeTransform(info));
			}

			return Lists.transform(
				repositoryService.getFoldersForCourse(connector, CurrentUser.getUsername(), id, true),
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

		@NonNullByDefault(false)
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
			HtmlBooleanState booleanState = folderSelections.getBooleanState(info, getId());
			booleanState.setDefaultRenderer(RADIO_CHECKBOX);
			return booleanState;
		}
	}

	public static class BulkMoveContentOperationModel extends BaseLMSExportModel
	{
		// Nothing to declare
	}

	@Override
	public boolean hasExtraNavigation(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public Collection<Button> getExtraNavigation(SectionInfo info, String operationId)
	{
		return null;
	}

	@Override
	public boolean hasPreview(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemUuid)
	{
		return null;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		return true;
	}
}
