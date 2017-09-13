package com.tle.web.workflow.tasks.dialog;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.FileSystemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.generic.expression.RuntimeExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PartiallyApply;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.ReloadStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileDrop;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.workflow.servlet.WorkflowMessageServlet;
import com.tle.web.workflow.tasks.CurrentTaskSection;
import com.tle.web.workflow.tasks.comments.CommentsSection;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NonNullByDefault
public abstract class AbstractTaskActionDialog<M extends AbstractTaskActionDialog.AbstractTaskActionDialogModel> extends EquellaDialog<M>
{
	private static final PluginResourceHelper URL_HELPER = ResourcesService.getResourceHelper(AbstractTaskActionDialog.class);
	private static final IncludeFile INCLUDE = new IncludeFile(URL_HELPER.url("scripts/comments.js"));
	private static final JSCallAndReference WORKFLOW_COMMENTS_CLASS = new ExternallyDefinedFunction("WorkflowComments",
			INCLUDE, JQueryProgression.PRERENDER);
	private static final ExternallyDefinedFunction VALIDATE_FILE = new ExternallyDefinedFunction(WORKFLOW_COMMENTS_CLASS,
			"validateFile", 2);
	private static final ExternallyDefinedFunction REMOVE_STAGING_FILE = new ExternallyDefinedFunction(
			WORKFLOW_COMMENTS_CLASS, "removeStagingFile", 3);

	private JSAssignable validateFile;
	private JSExpression stagingUuidExpression;

	@Inject
	protected FileSystemService fileSystemService;
	@Inject
	protected StagingService stagingService;
	@Inject
	protected InstitutionService instituionService;

	@PlugKey("command.taskaction.attachedfiles")
	private static Label LABEL_ATTACHED_FILES;

	@Component
	private TextField commentField;
	@Component(name = "fd")
	@PlugKey("comments.upload")
	private FileDrop fileDrop;
	@Component(name = "u")
	@PlugKey("command.taskaction.uploadfile")
	private Button uploadButton;
	@Component(name = "o")
	private Button okButton;
	@Component(name = "c")
	@PlugKey("command.taskaction.cancel")
	private Button cancelButton;

	@ViewFactory
	protected FreemarkerFactory viewFactory;

	protected AbstractTaskActionDialog()
	{
		setAjax(true);
	}

	protected abstract ButtonRenderer.ButtonType getButtonType();

	protected abstract Label getButtonLabel();

	protected abstract CommentsSection.CommentType getActionType();

	public abstract Label getPostCommentHeading();


	protected Button getSaveButton()
	{
		return okButton;
	}

	@Nullable
	protected JSExpression getWorkflowStepExpression()
	{
		return null;
	}

	@Override
	public String getWidth()
	{
		return "696px";
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		setupModelForRender(context);
		return viewFactory.createResult("dialog/taskaction.ftl", this);
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "taskactiondialog";
	}

	protected void setupModelForRender(RenderContext context)
	{
		M model = getModel(context);
		String stagingFolderUuid = model.getStagingFolderUuid();
		if( stagingFolderUuid == null )
		{
			stagingFolderUuid = stagingService.createStagingArea().getUuid();
			model.setStagingFolderUuid(stagingFolderUuid);
		}

		try
		{
			FileEntry[] fileEntries = fileSystemService.enumerate(new StagingFile(stagingFolderUuid), null, null);
			ArrayList<HtmlLinkState> lists = Lists.newArrayList();
			ArrayList<HtmlLinkState> deleteFiles = Lists.newArrayList();
			int index = 0;
			for( FileEntry fileEntry : fileEntries )
			{
				String filename = fileEntry.getName();
				HtmlLinkState link = new HtmlLinkState(new TextLabel(filename),
						new SimpleBookmark(WorkflowMessageServlet.stagingUrl(stagingFolderUuid, filename)));
				link.setTarget("_blank");
				lists.add(link);
				HtmlLinkState removeBtn = new HtmlLinkState(new TextLabel(""));
				removeBtn.addClass("unselect");
				String htmlLiId = "sf_" + index;
				OverrideHandler removeJsHandler = new OverrideHandler(REMOVE_STAGING_FILE,
						ajaxEvents.getAjaxFunction("removeUploadedFile"), filename, htmlLiId);
				removeBtn.setClickHandler(removeJsHandler);
				deleteFiles.add(removeBtn);
				index++;
			}
			model.setStagingFiles(lists);
			model.setDeleteFiles(deleteFiles);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}

		fileDrop.setAjaxUploadUrl(context, new BookmarkAndModify(context, ajaxEvents.getModifier("dndUpload")));
		fileDrop.setValidateFile(context, validateFile);
	}

	@EventHandlerMethod
	public void ok(SectionInfo info, String comment, String stagingUuid, String workflowStep)
	{
		doSaveAction(info, comment, stagingUuid, workflowStep);
	}

	protected void doSaveAction(SectionInfo info, String comment, String stagingUuid, String workflowStep)
	{
		StagingFile stagingFolder = new StagingFile(stagingUuid);
		long countFiles = fileSystemService.countFiles(stagingFolder, null);

		// when approving with attached files, comment msg cannot be empty
		/*
		boolean acceptedWithFiles = Check.isEmpty(comment) && countFiles > 1;
		if( acceptedWithFiles )
		{
			model.setErrorMessage(LABEL_ENTERMSG_WITHFILES);
			info.preventGET();
			return;
		}*/

		CurrentTaskSection currentTaskSection = info.lookupSection(CurrentTaskSection.class);
		if( !currentTaskSection.doComment(info, getActionType(), workflowStep, comment, stagingUuid) )
		{
			//commentField.setValue(info, null);
			closeDialog(info, new ReloadHandler());
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info, String stagingUuid)
	{
		StagingFile temp = new StagingFile(stagingUuid);
		fileSystemService.removeFile(temp);
		//SectionUtils.clearModel(info, this);
		//closeDialog(info, new ReloadHandler());
	}

	@AjaxMethod
	public DndUploadResponse dndUpload(SectionInfo info) throws IOException
	{
		M model = getModel(info);
		String stagingFolderUuid = model.getStagingFolderUuid();
		String filename = fileDrop.getFilename(info);

		String fn = "";
		if( !Check.isEmpty(filename) )
		{
			fn = filename.toLowerCase();
		}
		InputStream stream = fileDrop.getInputStream(info);

		StagingFile staging = new StagingFile(stagingFolderUuid);

		// overwrite existing file
		if( fileSystemService.fileExists(staging, fn) )
		{
			fileSystemService.removeFile(staging, fn);
		}
		fileSystemService.write(staging, fn, stream, false);
		String url = WorkflowMessageServlet.stagingUrl(stagingFolderUuid, fn);
		return new DndUploadResponse(stagingFolderUuid, url);
	}

	@AjaxMethod
	public boolean removeUploadedFile(SectionInfo info, String filename) throws IOException
	{
		StagingFile staging = new StagingFile(getModel(info).getStagingFolderUuid());
		fileSystemService.removeFile(staging, filename);
		return true;
	}

	protected JSHandler getSaveHandler()
	{
		final JSCallable commandExec = addParentCallable(events.getSubmitValuesFunction("ok"));
		final ScriptVariable stagingUuid = new ScriptVariable("stagingUuid");
		final ScriptVariable comment = new ScriptVariable("comment");
		final ScriptVariable workflowStep = new ScriptVariable("workflowStep");
		final SimpleFunction execFunc = new SimpleFunction("ok", this,
				StatementBlock.get(Js.call_s(commandExec, comment, stagingUuid), Js.call_s(getCloseFunction())), comment, stagingUuid);
		return new OverrideHandler(execFunc, commentField.createGetExpression(), stagingUuidExpression, getWorkflowStepExpression());
	}

	protected JSHandler getCancelHandler()
	{
		final JSCallable commandExec = //addParentCallable(
				events.getSubmitValuesFunction("cancel");
		//);
		final ScriptVariable stagingUuid = new ScriptVariable("stagingUuid");
		final SimpleFunction execFunc = new SimpleFunction("cancel", this,
				StatementBlock.get(Js.call_s(commandExec, stagingUuid), Js.call_s(getCloseFunction()), new ReloadStatement()), stagingUuid);
		return new OverrideHandler(execFunc, stagingUuidExpression);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		stagingUuidExpression = new RuntimeExpression(){
			@Override
			protected JSExpression createExpression(RenderContext info)
			{
				final M model = getModel(info);
				String stagingFolderUuid = model.getStagingFolderUuid();
				if( stagingFolderUuid == null )
				{
					stagingFolderUuid = stagingService.createStagingArea().getUuid();
					model.setStagingFolderUuid(stagingFolderUuid);
				}
				return new StringExpression(stagingFolderUuid);
			}
		};

		okButton.setClickHandler(getSaveHandler());
		okButton.setComponentAttribute(ButtonRenderer.ButtonType.class, getButtonType());
		okButton.setLabel(getButtonLabel());

		cancelButton.setClickHandler(getCancelHandler());

		UpdateDomFunction updateProgressArea = ajaxEvents.getAjaxUpdateDomFunction(tree, this, null,
				ajaxEvents.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE), "uploaded");
		validateFile = Js.functionValue(Js.call(VALIDATE_FILE, PartiallyApply.partial(updateProgressArea, 0)));

		super.treeFinished(id, tree);
	}

	@Override
	public M instantiateDialogModel(SectionInfo info)
	{
		return (M)new AbstractTaskActionDialogModel();
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		List<Button> buttons = new ArrayList<>();
		buttons.add(getSaveButton());
		buttons.add(cancelButton);
		return buttons;
	}

	public static class DndUploadResponse
	{
		public String stagingUuid;
		public String stagingFileUrl;

		public DndUploadResponse(String stagingUuid, String stagingFileUrl)
		{
			this.stagingUuid = stagingUuid;
			this.stagingFileUrl = stagingFileUrl;
		}
	}

	public static class AbstractTaskActionDialogModel extends DialogModel
	{
		@Bookmarked
		private String stagingFolderUuid;
		private ArrayList<HtmlLinkState> stagingFiles;
		private ArrayList<HtmlLinkState> deleteFiles;
		private Label errorMessage;

		public String getStagingFolderUuid()
		{
			return stagingFolderUuid;
		}

		public void setStagingFolderUuid(String stagingFolderUuid)
		{
			this.stagingFolderUuid = stagingFolderUuid;
		}

		public ArrayList<HtmlLinkState> getStagingFiles()
		{
			return stagingFiles;
		}

		public void setStagingFiles(ArrayList<HtmlLinkState> stagingFiles)
		{
			this.stagingFiles = stagingFiles;
		}

		public ArrayList<HtmlLinkState> getDeleteFiles()
		{
			return deleteFiles;
		}

		public void setDeleteFiles(ArrayList<HtmlLinkState> deleteFiles)
		{
			this.deleteFiles = deleteFiles;
		}

		public Label getErrorMessage()
		{
			return errorMessage;
		}

		public void setErrorMessage(Label errorMessage)
		{
			this.errorMessage = errorMessage;
		}
	}

	public TextField getCommentField()
	{
		return commentField;
	}

	public FileDrop getFileDrop()
	{
		return fileDrop;
	}

	public Button getUploadButton()
	{
		return uploadButton;
	}

	public Label getAttachedFilesLabel()
	{
		return LABEL_ATTACHED_FILES;
	}
}
