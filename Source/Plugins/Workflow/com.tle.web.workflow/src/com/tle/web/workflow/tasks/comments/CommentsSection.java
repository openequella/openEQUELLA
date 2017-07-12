package com.tle.web.workflow.tasks.comments;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.quota.settings.QuotaSettings;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.workflow.service.WorkflowService;
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
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PartiallyApply;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileDrop;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.Decorations;
import com.tle.web.workflow.tasks.CurrentTaskSection;

@NonNullByDefault
@SuppressWarnings("nls")
public class CommentsSection extends AbstractPrototypeSection<CommentsSection.Model>
	implements
		HtmlRenderer,
		ReadyToRespondListener
{
	private JSAssignable validateFile;

	public enum CommentType
	{
		REJECT, COMMENT, SHOW, ACCEPT
	}

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@PlugKey("reject.original")
	private static Label LABEL_ORIGINAL;

	@PlugKey("comments.title.comments")
	private static Label LABEL_COMMENTS_TITLE;
	@PlugKey("comments.title.approving")
	private static Label LABEL_APPROVING_TITLE;
	@PlugKey("comments.title.rejecting")
	private static Label LABEL_REJECTING_TITLE;

	@PlugKey("comments.rejectmsg")
	private static Label LABEL_REJECTMSG;
	@PlugKey("comments.commentmsg")
	private static Label LABEL_COMMENTMSG;
	@PlugKey("comments.acceptmsg")
	private static Label LABEL_ACCEPTMSG;
	@PlugKey("comments.entermsg")
	private static Label LABEL_ENTERMSG;
	@PlugKey("comments.entermsg.withfiles")
	private static Label LABEL_ENTERMSG_WITHFILES;

	@Component
	private TextField commentField;
	@Component
	@PlugKey("comments.submit")
	private Button submitButton;
	@Component(name = "fd")
	@PlugKey("comments.upload")
	private FileDrop fileDrop;

	@Component
	@PlugKey("comments.cancel")
	private Button cancelButton;
	@Component
	@PlugKey("comments.close")
	private Button closeButton;
	@Component
	@PlugKey("comments.uploadfile")
	private Button uploadButton;

	@Component
	private SingleSelectionList<WorkflowStep> rejectSteps;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private FileSystemService fileSystemService;
	@TreeLookup
	private CurrentTaskSection currentTaskSection;

	@Inject
	private ViewCommentsSection viewCommentsSection;
	@Inject
	private WorkflowService workflowService;

	@AjaxFactory
	private AjaxGenerator ajax;
	@Inject
	private ConfigurationService configService;
	@Inject
	private StagingService stagingService;
	@Inject
	private InstitutionService instituionService;

	private static final PluginResourceHelper URL_HELPER = ResourcesService.getResourceHelper(CommentsSection.class);
	private static final IncludeFile INCLUDE = new IncludeFile(URL_HELPER.url("scripts/comments.js"));
	private static final JSCallAndReference WORKFLOW_COMMENTS_CLASS = new ExternallyDefinedFunction("WorkflowComments",
		INCLUDE, JQueryProgression.PRERENDER);
	private static final ExternallyDefinedFunction VALIDATE_FILE = new ExternallyDefinedFunction(WORKFLOW_COMMENTS_CLASS,
		"validateFile", 2);
	private static final ExternallyDefinedFunction removeStagingFile = new ExternallyDefinedFunction(
		WORKFLOW_COMMENTS_CLASS, "removeStagingFile", 3);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(viewCommentsSection, id);
		submitButton.setClickHandler(events.getNamedHandler("submit"));
		SubmitValuesHandler cancelHandler = events.getNamedHandler("cancel");
		cancelButton.setClickHandler(cancelHandler);
		closeButton.setClickHandler(cancelHandler);
		rejectSteps.setListModel(new StepListModel());
		rejectSteps.setDisplayed(false);
		UpdateDomFunction updateProgressArea = ajax.getAjaxUpdateDomFunction(tree, this, null,
				ajax.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE), "uploaded");
		validateFile = Js.functionValue(Js.call(VALIDATE_FILE, PartiallyApply.partial(updateProgressArea, 0)));
	}

	/**
	 * @throws IOException
	 */
	@AjaxMethod
	public boolean removeUploadedFile(SectionInfo info, String filename) throws IOException
	{
		StagingFile staging = new StagingFile(getModel(info).getStagingFolderUuid());
		fileSystemService.removeFile(staging, filename);
		return true;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws IOException
	{
		Label title = null;
		Model model = getModel(context);
		model.setDisplayUploadButton(false);
		model.setMandatory(true);
		model.setPostAllowed(true);
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
				String urlPath = PathUtils.urlPath("workflow/message/$/", stagingFolderUuid, filename);
				HtmlLinkState link = new HtmlLinkState(new TextLabel(filename),
					new SimpleBookmark(instituionService.institutionalise(urlPath)));
				lists.add(link);
				HtmlLinkState removeBtn = new HtmlLinkState(new TextLabel(""));
				removeBtn.addClass("unselect");
				String htmlLiId = "sf_" + index;
				OverrideHandler removeJsHandler = new OverrideHandler(removeStagingFile,
					ajax.getAjaxFunction("removeUploadedFile"), filename, htmlLiId);
				removeBtn.setClickHandler(removeJsHandler);
				deleteFiles.add(removeBtn);
				index++;
			}
			model.setStagingFiles(lists);
			model.setDeleteFiles(deleteFiles);
		}
		catch( IOException e )
		{
			throw e;
		}

		fileDrop.setAjaxUploadUrl(context, new BookmarkAndModify(context, ajax.getModifier("dndUpload")));
		fileDrop.setValidateFile(context, validateFile);
//		fileDrop.setAjaxMethod(context, ajax.getAjaxFunction("dndUpload"));
//		fileDrop.setRemoveFileMethod(context, ajax.getAjaxFunction("removeUploadedFile"));
//		fileDrop.setMaxFiles(context, -1);
//		fileDrop.setUploadFinishedCallback(context, DONE_UPLOAD);
//		Collection<String> banned = configService.getProperties(new QuotaSettings()).getBannedExtensions();
//		fileDrop.setBanned(context, banned);
//		fileDrop.setShowAsButton(context, true);

//		if (true) throw new Error("FIXME - filedrop");

		ItemTaskId itemTaskId = currentTaskSection.getCurrentState(context).getItemTaskId();
		viewCommentsSection.setMessages(context, workflowService.getMessages(itemTaskId));
		switch( model.getCommentType() )
		{
			case REJECT:
				title = LABEL_REJECTING_TITLE;
				model.setDisplayUploadButton(true);
				rejectSteps.setDisplayed(context, !rejectSteps.getListModel().getOptions(context).isEmpty());
				model.setPostCommentHeading(LABEL_REJECTMSG);
				break;
			case COMMENT:
				title = LABEL_COMMENTS_TITLE;
				model.setPostCommentHeading(LABEL_COMMENTMSG);
				break;
			case ACCEPT:
				model.setDisplayUploadButton(true);
				title = LABEL_APPROVING_TITLE;
				model.setPostCommentHeading(LABEL_ACCEPTMSG);
				model.setMandatory(false);
				break;
			case SHOW:
				title = LABEL_COMMENTS_TITLE;
				model.setPostAllowed(false);
				break;
		}
		Decorations.getDecorations(context).setTitle(title);
		model.setPageTitle(title);

		return viewFactory.createResult("comments.ftl", this);
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		StagingFile temp = new StagingFile(getModel(info).getStagingFolderUuid());
		fileSystemService.removeFile(temp);
		SectionUtils.clearModel(info, this);
	}

	@EventHandlerMethod
	public void submit(SectionInfo info)
	{
		Model model = getModel(info);
		String commentMsg = commentField.getValue(info);
		CommentType commentType = model.getCommentType();
		if( (commentType == CommentType.REJECT || commentType == CommentType.COMMENT) && Check.isEmpty(commentMsg) )
		{
			model.setErrorMessage(LABEL_ENTERMSG);
			info.preventGET();
			return;
		}

		StagingFile stagingFolder = new StagingFile(getModel(info).getStagingFolderUuid());
		String messageUuid = stagingFolder.getUuid();
		long countFiles = fileSystemService.countFiles(stagingFolder, null);

		// when approving with attached files, comment msg cannot be empty
		boolean acceptedWithFiles = Check.isEmpty(commentMsg) && commentType == CommentType.ACCEPT && countFiles > 1;
		if( acceptedWithFiles )
		{
			model.setErrorMessage(LABEL_ENTERMSG_WITHFILES);
			info.preventGET();
			return;
		}

		if( !currentTaskSection.doComment(info, commentType, rejectSteps.getSelectedValueAsString(info), commentMsg,
			messageUuid) )
		{
			commentField.setValue(info, null);
		}
	}

	class DndUploadResponse
	{
		public String stagingUuid;
		public String stagingFileUrl;

		public DndUploadResponse(String stagingUuid, String stagingFileUrl)
		{
			this.stagingUuid = stagingUuid;
			this.stagingFileUrl = stagingFileUrl;
		}

	}

	@AjaxMethod
	public DndUploadResponse dndUpload(SectionInfo info) throws IOException
	{
		Model model = getModel(info);
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
		model.setMandatory(true);
		String url = instituionService
			.institutionalise(PathUtils.urlPath("workflow/message/$/", stagingFolderUuid, fn));
		return new DndUploadResponse(stagingFolderUuid, url);
	}

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		if( !redirect && getModel(info).getCommentType() != null )
		{
			currentTaskSection.setModal(info, this);
		}
	}

	public Button getSubmitButton()
	{
		return submitButton;
	}

	public FileDrop getFileDrop()
	{
		return fileDrop;
	}

	public TextField getCommentField()
	{
		return commentField;
	}

	public class StepListModel extends DynamicHtmlListModel<WorkflowStep>
	{
		@Override
		protected Iterable<WorkflowStep> populateModel(SectionInfo info)
		{
			WorkflowStep step = currentTaskSection.getCurrentStep(info);
			List<WorkflowStep> steps = new ArrayList<WorkflowStep>(step.getRejectPoints());
			if( !steps.isEmpty() )
			{
				steps.add(null);
			}
			return steps;
		}

		@Override
		protected Option<WorkflowStep> convertToOption(SectionInfo info, @Nullable WorkflowStep obj)
		{
			if( obj == null )
			{
				return new LabelOption<WorkflowStep>(LABEL_ORIGINAL, "", null);
			}
			return new NameValueOption<WorkflowStep>(
				new BundleNameValue(obj.getDisplayName(), obj.getUuid(), bundleCache), obj);
		}
	}

	public SingleSelectionList<WorkflowStep> getRejectSteps()
	{
		return rejectSteps;
	}

	public void doComment(SectionInfo info, CommentType type)
	{
		getModel(info).setCommentType(type);
	}

	public boolean isCommenting(SectionInfo info)
	{
		return getModel(info).getCommentType() != null;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@NonNullByDefault(false)
	public static class Model
	{
		@Bookmarked
		private CommentType commentType;
		@Bookmarked
		private String stagingFolderUuid;
		private boolean postAllowed;
		private boolean mandatory;
		private boolean displayUploadButton;
		private Label errorMessage;
		private Label postCommentHeading;
		private Label commentHeading;
		private Label pageTitle;
		private ArrayList<HtmlLinkState> stagingFiles;
		private ArrayList<HtmlLinkState> deleteFiles;

		public CommentType getCommentType()
		{
			return commentType;
		}

		public void setCommentType(CommentType commentType)
		{
			this.commentType = commentType;
		}

		public boolean isPostAllowed()
		{
			return postAllowed;
		}

		public void setPostAllowed(boolean postAllowed)
		{
			this.postAllowed = postAllowed;
		}

		public Label getPostCommentHeading()
		{
			return postCommentHeading;
		}

		public void setPostCommentHeading(Label postCommentHeading)
		{
			this.postCommentHeading = postCommentHeading;
		}

		public Label getCommentHeading()
		{
			return commentHeading;
		}

		public void setCommentHeading(Label commentHeading)
		{
			this.commentHeading = commentHeading;
		}

		public Label getErrorMessage()
		{
			return errorMessage;
		}

		public void setErrorMessage(Label errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		public boolean isMandatory()
		{
			return mandatory;
		}

		public void setMandatory(boolean mandatory)
		{
			this.mandatory = mandatory;
		}

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public boolean isDisplayUploadButton()
		{
			return displayUploadButton;
		}

		public void setDisplayUploadButton(boolean displayUploadButton)
		{
			this.displayUploadButton = displayUploadButton;
		}

		public String getStagingFolderUuid()
		{
			return this.stagingFolderUuid;
		}

		public void setStagingFolderUuid(String stagingFolder)
		{
			this.stagingFolderUuid = stagingFolder;
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

	}

	public Button getCloseButton()
	{
		return closeButton;
	}

	public Button getUploadButton()
	{
		return uploadButton;
	}

	public ViewCommentsSection getViewCommentsSection()
	{
		return viewCommentsSection;
	}
}