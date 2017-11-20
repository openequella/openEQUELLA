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

package com.tle.web.entities.section;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.collect.Maps;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.web.DebugSettings;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.MultiEditBox;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public abstract class AbstractEntityEditor<B extends EntityEditingBean, E extends BaseEntity, M extends AbstractEntityEditor<B, E, M>.AbstractEntityEditorModel>
	extends
		AbstractPrototypeSection<M>
	implements HtmlRenderer, EntityEditor<B, E>
{
	private static final String KEY_ERROR_TITLE = "title";

	@PlugKey("editor.label.title")
	private static Label LABEL_TITLE;
	@PlugKey("editor.label.description")
	private static Label LABEL_DESCRIPTION;

	@PlugKey("editor.error.title.mandatory")
	private static Label LABEL_ERROR_TITLE;
	@PlugKey("editor.warning.navigateaway")
	private static Label LABEL_WARNING_NAVIGATEAWAY;

	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	@Component(name = "t", stateful = false)
	private MultiEditBox title;
	@Inject
	@Component(name = "d", stateful = false)
	private MultiEditBox description;

	protected abstract AbstractEntityService<B, E> getEntityService();

	protected abstract E createNewEntity(SectionInfo info);

	protected abstract SectionRenderable renderFields(RenderEventContext context, EntityEditingSession<B, E> session);

	/**
	 * Populate form fields
	 * 
	 * @param info
	 * @param session
	 */
	protected abstract void loadFromSession(SectionInfo info, EntityEditingSession<B, E> session);

	/**
	 * Store form fields into editing session entity
	 * 
	 * @param info
	 * @param session
	 * @param validate
	 */
	protected abstract void saveToSession(SectionInfo info, EntityEditingSession<B, E> session, boolean validate);

	/**
	 * Check mandatory fields etc
	 * 
	 * @param info
	 * @param ent
	 * @param errors
	 */
	protected void validate(SectionInfo info, EntityEditingSession<B, E> session)
	{
		try
		{
			getEntityService().validate(session, session.getEntity());
		}
		catch( InvalidDataException ide )
		{
			session.getValidationErrors().putAll(ide.getErrorsAsMap());
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final M model = getModel(context);
		final EntityEditingSession<B, E> session = getEntityService().loadSession(model.getSessionId());

		model.setEntityUuid(session.getBean().getUuid());
		model.setErrors(session.getValidationErrors());
		model.setCustomEditor(renderFields(context, session));

		if( !DebugSettings.isAutoTestMode() )
		{
			context.getBody().addEventStatements(JSHandler.EVENT_BEFOREUNLOAD,
				new ReturnStatement(LABEL_WARNING_NAVIGATEAWAY));
		}

		return view.createResult("editcommon.ftl", context);
	}

	/**
	 * Called from Contrib section
	 */
	@Override
	public SectionRenderable renderEditor(RenderContext info)
	{
		return renderSection(info, this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		description.setSize(3);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	/**
	 * Override if you want to
	 */
	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return null;
	}

	@Override
	public final void create(SectionInfo info)
	{
		final EntityEditingSession<B, E> session = getEntityService().startNewSession(createNewEntity(info));
		startSession(info, session);
	}

	@Override
	public final void edit(SectionInfo info, String entityUuid, boolean clone)
	{
		AbstractEntityService<B, E> entityService = getEntityService();
		final EntityEditingSession<B, E> session = (clone ? entityService.cloneIntoSession(entityUuid)
			: entityService.startEditingSession(entityUuid));
		startSession(info, session);
	}

	@Override
	public final boolean save(SectionInfo info)
	{
		final M model = getModel(info);
		final EntityEditingSession<B, E> session = getEntityService().loadSession(model.getSessionId());
		saveToSessionPrivate(info, session, true);
		if( session.isValid() )
		{
			getEntityService().commitSession(session);
			model.setSessionId(null);
			return true;
		}
		return false;
	}

	@Override
	public final void cancel(SectionInfo info)
	{
		final M model = getModel(info);
		getEntityService().cancelSessionId(model.getSessionId());
		model.setSessionId(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <S extends EntityEditingSession<B, E>> S loadFromSession(SectionInfo info)
	{
		final String sessionId = getModel(info).getSessionId();
		if( sessionId != null )
		{
			final EntityEditingSession<B, E> session = getEntityService().loadSession(sessionId);
			loadFromSessionPrivate(info, session);
			return (S) session;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <S extends EntityEditingSession<B, E>> S saveToSession(SectionInfo info)
	{
		final String sessionId = getModel(info).getSessionId();
		if( sessionId != null )
		{
			final EntityEditingSession<B, E> session = getEntityService().loadSession(sessionId);
			saveToSessionPrivate(info, session, false);
			return (S) session;
		}
		return null;
	}

	@Override
	public B getEditedEntity(SectionInfo info)
	{
		return getModel(info).getEditedEntity();
	}

	private void startSession(SectionInfo info, EntityEditingSession<B, E> session)
	{
		loadFromSessionPrivate(info, session);
		getModel(info).setSessionId(session.getSessionId());
	}

	private void saveToSessionPrivate(SectionInfo info, EntityEditingSession<B, E> session, boolean validate)
	{
		final EntityEditingBean bean = session.getBean();
		if( validate )
		{
			session.setValid(validatePrivate(info, session));
		}
		else
		{
			session.getValidationErrors().clear();
		}

		// Save fields even if invalid, it wont be committed until the session
		// is valid
		bean.setName(title.getLanguageBundle(info));
		bean.setDescription(description.getLanguageBundle(info));

		saveToSession(info, session, validate);

		getEntityService().saveSession(session);
	}

	private void loadFromSessionPrivate(SectionInfo info, EntityEditingSession<B, E> session)
	{
		final B bean = session.getBean();
		final M model = getModel(info);
		model.setEditedEntity(bean);

		final LanguageBundleBean name = bean.getName();
		if( name != null )
		{
			title.setLanguageBundle(info, name);
		}

		final LanguageBundleBean desc = bean.getDescription();
		if( desc != null )
		{
			description.setLanguageBundle(info, desc);
		}

		loadFromSession(info, session);
	}

	private boolean validatePrivate(SectionInfo info, EntityEditingSession<B, E> session)
	{
		final Map<String, Object> errors = session.getValidationErrors();
		errors.clear();

		final LanguageBundleBean bundle = title.getLanguageBundle(info);
		if( LangUtils.isEmpty(bundle) )
		{
			errors.put(KEY_ERROR_TITLE, getTitleMandatoryErrorLabel().getText());
		}

		validate(info, session);
		return errors.isEmpty();
	}

	protected Label getTitleMandatoryErrorLabel()
	{
		return LABEL_ERROR_TITLE;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AbstractEntityEditorModel();
	}

	public MultiEditBox getTitle()
	{
		return title;
	}

	public MultiEditBox getDescription()
	{
		return description;
	}

	public Label getTitleLabel()
	{
		return LABEL_TITLE;
	}

	public Label getDescriptionLabel()
	{
		return LABEL_DESCRIPTION;
	}

	public Label getWarningNavigateAway()
	{
		return LABEL_WARNING_NAVIGATEAWAY;
	}

	public class AbstractEntityEditorModel
	{
		@Bookmarked(name = "s")
		private String sessionId;
		private String entityUuid;

		private SectionRenderable customEditor;
		private Map<String, Object> errors = Maps.newHashMap();

		// Just a cache
		private B editedEntity;

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}

		public SectionRenderable getCustomEditor()
		{
			return customEditor;
		}

		public void setCustomEditor(SectionRenderable customEditor)
		{
			this.customEditor = customEditor;
		}

		public Map<String, Object> getErrors()
		{
			return errors;
		}

		public void setErrors(Map<String, Object> errors)
		{
			this.errors = errors;
		}

		public B getEditedEntity()
		{
			return editedEntity;
		}

		public void setEditedEntity(B editedEntity)
		{
			this.editedEntity = editedEntity;
		}

		public String getEntityUuid()
		{
			return entityUuid;
		}

		public void setEntityUuid(String entityUuid)
		{
			this.entityUuid = entityUuid;
		}
	}
}
