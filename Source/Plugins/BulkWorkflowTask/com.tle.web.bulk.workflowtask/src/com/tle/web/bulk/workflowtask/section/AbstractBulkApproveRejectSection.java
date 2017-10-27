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

package com.tle.web.bulk.workflowtask.section;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import com.google.inject.Inject;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.quota.settings.QuotaSettings;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.FunctionCallValidator;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileDrop;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.workflow.manage.TaskResultsDialog;

public abstract class AbstractBulkApproveRejectSection
	extends
		AbstractPrototypeSection<AbstractBulkApproveRejectSection.BulkApproveRejectModel>
	implements
		HtmlRenderer,
		BulkOperationExtension
{
	@Inject
	private TLEAclManager aclService;

	@AjaxFactory
	private AjaxGenerator ajax;
	@Inject
	private ConfigurationService configService;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService instituionService;
	@Inject
	private StagingService stagingService;

	@PlugKey("validation.mandatorymessage")
	private static Label LABEL_MANDATORY_MESSAGE;

	@Component
	private TextField commentField;

	@TreeLookup
	private TaskResultsDialog taskResultsDialog;

	private static final PluginResourceHelper URL_HELPER = ResourcesService
		.getResourceHelper(AbstractBulkApproveRejectSection.class);
	private static final IncludeFile INCLUDE = new IncludeFile(URL_HELPER.url("scripts/bulkop.dnd.js"));
	private static final JSCallAndReference JS_CLASS = new ExternallyDefinedFunction("WorkflowBulkOpDnd", INCLUDE);
	private static final ExternallyDefinedFunction DONE_UPLOAD = new ExternallyDefinedFunction(JS_CLASS,
		"dndUploadFinishedCallback", 0);

	private static final ExternallyDefinedFunction VALIDATOR = new ExternallyDefinedFunction(JS_CLASS,
		"validateMessage", 2);

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		BulkApproveRejectModel model = getModel(context);

		getDialog().getOkButton().getState(context).getHandler("click")
			.addValidator(new FunctionCallValidator(VALIDATOR, Jq.$(commentField), LABEL_MANDATORY_MESSAGE.getText()));

		return viewFactory.createResult("comments.ftl", context);
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// none
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		//TODO: remove all the current nasty Javascript and do server side validation, 
		// as well as rendering of currently uploaded files 
		return true;
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return getDialog().getModel(info).isShowOptions();
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
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

	@SuppressWarnings("rawtypes")
	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemId) throws Exception
	{
		return null;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		return true;
	}

	@Override
	public Class<BulkApproveRejectModel> getModelClass()
	{
		return BulkApproveRejectModel.class;
	}

	public TextField getCommentField()
	{
		return commentField;
	}

	public String getComment(SectionInfo info)
	{
		return commentField.getValue(info);
	}

	public void setTitle(SectionInfo info, String title)
	{
		getModel(info).setTitle(title);
	}

	public void setSubTitle(SectionInfo info, String subTitle)
	{
		getModel(info).setSubTitle(subTitle);
	}

	protected boolean hasPrivilege(String privilege)
	{
		return !aclService.filterNonGrantedPrivileges(Collections.singleton(privilege), true).isEmpty();
	}

	protected boolean isOnMyTaskPage(SectionInfo info)
	{
		String page = taskResultsDialog.getModel(info).getPage();
		if( page != null )
		{
			return page.equals("my-task");
		}
		return false;
	}

	protected TaskResultsDialog getDialog()
	{
		return taskResultsDialog;
	}

	public static class BulkApproveRejectModel
	{
		private String title;
		private String subTitle;
		private boolean mandatoryMessage;
		private boolean mustHaveMessage;

		public boolean isMandatoryMessage()
		{
			return mandatoryMessage;
		}

		public void setMandatoryMessage(boolean mandatoryMessage)
		{
			this.mandatoryMessage = mandatoryMessage;
		}

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}

		public String getSubTitle()
		{
			return subTitle;
		}

		public void setSubTitle(String subTitle)
		{
			this.subTitle = subTitle;
		}

		public boolean isMustHaveMessage()
		{
			return mustHaveMessage;
		}

		public void setMustHaveMessage(boolean mustHaveMessage)
		{
			this.mustHaveMessage = mustHaveMessage;
		}
	}
}
