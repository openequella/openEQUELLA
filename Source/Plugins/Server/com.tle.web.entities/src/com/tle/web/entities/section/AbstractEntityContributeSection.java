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

import java.util.Collection;

import com.google.common.base.Throwables;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

@SuppressWarnings("nls")
@TreeIndexed
public abstract class AbstractEntityContributeSection<B extends EntityEditingBean, E extends BaseEntity, M extends AbstractEntityContributeSection<B, E, M>.EntityContributeModel>
	extends
		AbstractPrototypeSection<M>
	implements HtmlRenderer
{
	@PlugKey("editor.error.accessdenied")
	private static String KEY_ACCESS_DENIED;

	@TreeLookup
	private AbstractRootEntitySection<?> rootSection;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@PlugKey("editor.button.save")
	@Component(name = "sv", stateful = false)
	private Button saveButton;
	@PlugKey("editor.button.cancel")
	@Component(name = "cl", stateful = false)
	private Button cancelButton;

	protected abstract AbstractEntityService<B, E> getEntityService();

	protected abstract Label getCreatingLabel(SectionInfo info);

	protected abstract Label getEditingLabel(SectionInfo info);

	protected abstract EntityEditor<B, E> getEditor(SectionInfo info);

	// Can probably remove this
	protected abstract String getCreatePriv();

	// Can probably remove this
	protected abstract String getEditPriv();

	/**
	 * Only called once, on registered
	 * 
	 * @return A list of all possible editors
	 */
	protected abstract Collection<EntityEditor<B, E>> getAllEditors();

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final M model = getModel(context);

		model.setPageTitle(getPageTitle(context));

		final EntityEditor<B, E> ed = getEditorPrivate(context);
		if( ed != null )
		{
			// check edit priv
			final EntityEditingBean editedEnt = ed.getEditedEntity(context);
			if( editedEnt.getId() == 0 )
			{
				ensureCreatePriv(context);
			}
			else if( !canEdit(context, editedEnt) )
			{
				throw accessDenied(getEditPriv());
			}

			HelpAndScreenOptionsSection.addHelp(context, ed.renderHelp(context));
			model.setEditorRenderable(ed.renderEditor(context));
		}
		else
		{
			ensureCreatePriv(context);
		}

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult("body", view.createResult("editentity.ftl", context));
		return templateResult;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		for( EntityEditor<B, E> ed : getAllEditors() )
		{
			ed.register(tree, id);
		}

		saveButton.setClickHandler(events.getNamedHandler("save"));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
	}

	protected boolean canEdit(SectionInfo info, EntityEditingBean editedEnt)
	{
		// TODO: rather dodgy
		E entityTemplate;
		try
		{
			entityTemplate = getEntityService().getEntityClass().newInstance();
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
		entityTemplate.setId(editedEnt.getId());
		return getEntityService().canEdit(entityTemplate);
	}

	protected AccessDeniedException accessDenied(String priv)
	{
		return new AccessDeniedException(CurrentLocale.get(KEY_ACCESS_DENIED, priv));
	}

	protected boolean canCreate(SectionInfo info)
	{
		return getEntityService().canCreate();
	}

	protected void ensureCreatePriv(SectionInfo info)
	{
		if( !canCreate(info) )
		{
			throw accessDenied(CurrentLocale.get(KEY_ACCESS_DENIED, getCreatePriv()));
		}
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final EntityEditor<B, E> ed = getEditorPrivate(info);
		if( ed.save(info) )
		{
			returnFromEdit(info, false);
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		final EntityEditor<B, E> ed = getEditorPrivate(info);
		if( ed != null )
		{
			ed.cancel(info);
		}
		returnFromEdit(info, true);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		final M model = getModel(info);
		if( model.isEditing() )
		{
			rootSection.setModalSection(info, this);
		}
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void includeHandler(SectionInfo info)
	{
		final M model = getModel(info);
		if( model.isRendered() )
		{
			EntityEditor<B, E> ed = getEditorPrivate(info);
			if( ed != null )
			{
				ed.saveToSession(info);
			}
		}
	}

	@DirectEvent
	public void loadFromSession(SectionInfo info)
	{
		final M model = getModel(info);
		model.setRendered(true);

		final EntityEditor<B, E> ed = getEditorPrivate(info);
		if( ed != null )
		{
			ed.loadFromSession(info);
		}
	}

	public void returnFromEdit(SectionInfo info, boolean cancelled)
	{
		final M model = getModel(info);
		model.setEditor(null);
		model.setEditing(false);
	}

	private EntityEditor<B, E> getEditorPrivate(SectionInfo info)
	{
		EntityEditor<B, E> ed = getEditor(info);
		getModel(info).setEditor(ed);
		return ed;
	}

	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		ContentLayout.setLayout(info, ContentLayout.ONE_COLUMN);

		final M model = getModel(info);
		final Label pageTitle = getPageTitle(info);

		final EntityEditor<B, E> editor = getEditorPrivate(info);
		if( editor != null )
		{
			decorations.setContentBodyClass("entityedit");
			crumbs.setForcedLastCrumb(pageTitle);
		}

		model.setPageTitle(pageTitle);
		decorations.setTitle(pageTitle);
	}

	protected Label getPageTitle(SectionInfo info)
	{
		final M model = getModel(info);
		final EntityEditor<B, E> editor = getEditorPrivate(info);
		Label pageTitle = getCreatingLabel(info);
		if( editor != null )
		{
			final EntityEditingBean editedEnt = editor.getEditedEntity(info);
			final boolean editExisting = editedEnt.getId() != 0;
			model.setEditExisting(editExisting);
			pageTitle = (editExisting ? getEditingLabel(info) : pageTitle);
		}
		return pageTitle;
	}

	/**
	 * @param info
	 * @param type
	 */
	public void createNew(SectionInfo info)
	{
		final M model = getModel(info);
		model.setEditing(true);

		final EntityEditor<B, E> ed = getEditorPrivate(info);
		if( ed != null )
		{
			ed.create(info);
		}
	}

	/**
	 * @param info
	 * @param entityUuid
	 * @param type
	 */
	public void startEdit(SectionInfo info, String entityUuid, boolean clone)
	{
		final M model = getModel(info);
		final EntityEditor<B, E> ed = getEditorPrivate(info);
		model.setEditing(true);
		if( ed != null )
		{
			ed.edit(info, entityUuid, clone);
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new EntityContributeModel();
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public class EntityContributeModel
	{
		@Bookmarked(name = "ed")
		private boolean editing;
		@Bookmarked(stateful = false)
		private boolean rendered;

		private Label pageTitle;
		private boolean editExisting;
		private EntityEditor<B, E> editor;
		private SectionRenderable editorRenderable;

		public boolean isEditing()
		{
			return editing;
		}

		public void setEditing(boolean editing)
		{
			this.editing = editing;
		}

		public boolean isRendered()
		{
			return rendered;
		}

		public void setRendered(boolean rendered)
		{
			this.rendered = rendered;
		}

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public boolean isEditExisting()
		{
			return editExisting;
		}

		public void setEditExisting(boolean editExisting)
		{
			this.editExisting = editExisting;
		}

		public EntityEditor<B, E> getEditor()
		{
			return editor;
		}

		public void setEditor(EntityEditor<B, E> editor)
		{
			this.editor = editor;
		}

		public SectionRenderable getEditorRenderable()
		{
			return editorRenderable;
		}

		public void setEditorRenderable(SectionRenderable editorRenderable)
		{
			this.editorRenderable = editorRenderable;
		}
	}
}
