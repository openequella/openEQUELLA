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

package com.tle.web.htmleditor.settings.section;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.htmleditor.HtmlEditorPluginConstants;
import com.tle.core.htmleditor.service.HtmlEditorPluginEditingBean;
import com.tle.core.htmleditor.service.HtmlEditorPluginService;
import com.tle.core.security.TLEAclManager;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class ShowHtmlEditorPluginsSection
	extends
		AbstractShowEntitiesSection<HtmlEditorPlugin, ShowHtmlEditorPluginsSection.ShowHtmlEditorPluginsModel>
	implements
		ModalHtmlEditorSettingsSection
{
	private static final Logger LOGGER = Logger.getLogger(ShowHtmlEditorPluginsSection.class);

	@PlugKey("settings.plugins.front.link")
	private static Label LABEL_SETTING_LINK;
	@PlugKey("settings.plugins.front.preamble")
	private static Label LABEL_SETTING_BLURB;

	@PlugKey("settings.plugins.title")
	private static Label LABEL_TITLE;
	@PlugKey("settings.plugins.label.noplugins")
	private static Label LABEL_PLUGINS_NONE;
	@PlugKey("settings.plugins.confirm.delete")
	private static Label LABEL_PLUGINS_DELETE_CONFIRM;
	@PlugKey("settings.plugins.column.pluginname")
	private static Label LABEL_PLUGINS_PLUGINNAME;
	@PlugKey("settings.plugins.column.pluginauthor")
	private static Label LABEL_PLUGINS_PLUGINAUTHOR;

	@Component(name = "pf")
	private FileUpload uploadPluginFile;
	@PlugKey("settings.plugins.button.uploadplugin")
	@Component(name = "pb")
	private Button uploadPluginButton;

	@Inject
	private HtmlEditorPluginService htmleditorPluginService;
	@Inject
	private TLEAclManager aclService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	private JSCallable updateFunction;

	private final Cache<String, UploadResult> UPLOADS = CacheBuilder.newBuilder()
		.expireAfterWrite(15, TimeUnit.SECONDS).concurrencyLevel(4).build();

	@Override
	protected SectionRenderable renderTop(RenderEventContext context)
	{
		final String uploadId = UUID.randomUUID().toString();
		final BookmarkAndModify ajaxUploadUrl = new BookmarkAndModify(context, ajax.getModifier("uploadPlugin",
			uploadId));
		uploadPluginFile.setAjaxUploadUrl(context, ajaxUploadUrl);
		uploadPluginFile.setAjaxAfterUpload(context, Js.call_s(updateFunction, uploadId));

		final ShowHtmlEditorPluginsModel model = getModel(context);
		model.setCanAdd(!aclService.filterNonGrantedPrivileges(HtmlEditorPluginConstants.PRIV_CREATE_HTMLEDITOR_PLUGIN)
			.isEmpty());

		return viewFactory.createResult("setting/uploadplugin.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		updateFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("checkUpload"), getAjaxId());
	}

	@AjaxMethod
	public SectionRenderable uploadPlugin(SectionInfo info, @Nullable String uploadId)
	{
		String errorMessage = null;
		final String uploadsId = uploadId != null ? uploadId : CurrentUser.getSessionID();

		try {
			htmleditorPluginService.uploadPlugin(uploadPluginFile.getInputStream(info));
		}
		catch( Exception ipe )
		{
			LOGGER.warn("Plugin upload error", ipe);
			errorMessage = (ipe.getMessage() == null ? ipe.getClass().getName() : ipe.getMessage());
			final Throwable cause = ipe.getCause();
			if( cause != null )
			{
				errorMessage += ": " + cause.getMessage();
			}
		}
		UPLOADS.put(uploadId, new UploadResult(errorMessage));
		return new SimpleSectionResult(true);
	}

	/**
	 * This method ensures uploadPlugin doesn't complete before the file is
	 * there to display.
	 * 
	 * @param info
	 * @param newFileUuid
	 */
	@EventHandlerMethod
	public void checkUpload(SectionInfo info, String newFileUuid)
	{
		final long timedout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
		UploadResult up = null;
		while( up == null && System.currentTimeMillis() < timedout )
		{
			up = UPLOADS.getIfPresent(newFileUuid);
			try
			{
				Thread.sleep(250);
			}
			catch( InterruptedException e )
			{
				return;
			}
		}
		UPLOADS.invalidate(newFileUuid);
		if( up != null )
		{
			String errorMessage = up.getErrorMessage();
			if( errorMessage != null )
			{
				getModel(info).getErrors().put("plugin", errorMessage);
				// info.preventGET();
			}
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShowHtmlEditorPluginsModel();
	}

	@Override
	public void startSession(SectionInfo info)
	{
		// No
	}

	public FileUpload getUploadPluginFile()
	{
		return uploadPluginFile;
	}

	public Button getUploadPluginButton()
	{
		return uploadPluginButton;
	}

	@Override
	protected AbstractEntityService<HtmlEditorPluginEditingBean, HtmlEditorPlugin> getEntityService()
	{
		return htmleditorPluginService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	protected Label getAddLabel()
	{
		return null;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_PLUGINS_PLUGINNAME;
	}

	@Override
	protected void addDynamicColumnHeadings(SectionInfo info, TableHeaderRow header)
	{
		super.addDynamicColumnHeadings(info, header);
		header.addCell(LABEL_PLUGINS_PLUGINAUTHOR);
	}

	@Override
	protected void addDynamicColumnData(SectionInfo info, HtmlEditorPlugin ent, SelectionsTableSelection row)
	{
		super.addDynamicColumnData(info, ent, row);
		row.addColumn(ent.getAuthor());
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_PLUGINS_NONE;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, HtmlEditorPlugin entity)
	{
		return LABEL_PLUGINS_DELETE_CONFIRM;
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return false;
	}

	@Override
	protected boolean canEdit(SectionInfo info, HtmlEditorPlugin ent)
	{
		return false;
	}

	@Override
	protected boolean canClone(SectionInfo info, HtmlEditorPlugin ent)
	{
		return false;
	}

	@Override
	protected boolean isInUse(SectionInfo info, HtmlEditorPlugin entity)
	{
		return false;
	}

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		crumbs.setForcedLastCrumb(LABEL_TITLE);
		decorations.setTitle(LABEL_TITLE);
		decorations.setContentBodyClass("htmleditor");
	}

	@Override
	@Nullable
	public SettingInfo getSettingInfo(SectionInfo info)
	{
		if( !aclService.filterNonGrantedPrivileges(HtmlEditorPluginConstants.PRIV_CREATE_HTMLEDITOR_PLUGIN,
			HtmlEditorPluginConstants.PRIV_EDIT_HTMLEDITOR_PLUGIN,
			HtmlEditorPluginConstants.PRIV_DELETE_HTMLEDITOR_PLUGIN).isEmpty() )
		{
			return new SettingInfo("plugins", LABEL_SETTING_LINK, LABEL_SETTING_BLURB);
		}

		return null;
	}

	@NonNullByDefault(false)
	private static class UploadResult
	{
		private final String errorMessage;

		public UploadResult(String errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}
	}

	@NonNullByDefault(false)
	public static class ShowHtmlEditorPluginsModel extends AbstractShowEntitiesSection.AbstractShowEntitiesModel
	{
		private boolean canAdd;
		private final Map<String, Object> errors = Maps.newHashMap();

		public boolean isCanAdd()
		{
			return canAdd;
		}

		public void setCanAdd(boolean canAdd)
		{
			this.canAdd = canAdd;
		}

		public Map<String, Object> getErrors()
		{
			return errors;
		}
	}
}
