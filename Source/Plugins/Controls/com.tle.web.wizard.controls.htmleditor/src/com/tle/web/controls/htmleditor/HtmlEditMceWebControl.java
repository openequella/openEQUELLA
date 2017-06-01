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

package com.tle.web.controls.htmleditor;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.scripting.ScriptContextFactory;
import com.tle.common.wizard.controls.htmleditmce.HtmlEditMceControl;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.mypages.parse.ConvertHtmlService;
import com.tle.mypages.parse.conversion.StagingConversion;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.HtmlEditorControl;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.controls.WebControlModel;

@SuppressWarnings("nls")
@Bind
public class HtmlEditMceWebControl extends AbstractWebControl<HtmlEditMceWebControl.HtmlEditMceWebControlModel>
{
	private HtmlEditorControl editor;
	private CCustomControl htmlEditControl;

	@EventFactory
	private EventGenerator events;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private ConvertHtmlService converter;

	@Component
	private Link editLink;

	private HtmlEditMceControl definitionControl;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		LERepository repo = getRepository();
		String wizId = repo.getWizid();
		ItemId itemId = repo.getItem().getItemId();

		StagingConversion stagingConversion = new StagingConversion(true, itemId, wizId, repo.getStagingid());
		String html = htmlEditControl.getValue();
		if( !Check.isEmpty(html) )
		{
			html = converter.convert(new StringReader(html), true, stagingConversion.getConversions());
		}

		final HtmlEditMceControl tempDefinitionControl = getDefinitionControl();

		final Map<Class<?>, Set<String>> searchableUuids = new HashMap<Class<?>, Set<String>>();
		searchableUuids.put(ItemDefinition.class, tempDefinitionControl.getCollectionsUuids());
		searchableUuids.put(DynaCollection.class, tempDefinitionControl.getDynaCollectionsUuids());
		searchableUuids.put(PowerSearch.class, tempDefinitionControl.getSearchUuids());

		final Set<String> contributableUuids = tempDefinitionControl.getContributableUuids();

		final Map<String, String> properties = new HashMap<String, String>();
		properties.put("sessionId", wizId);
		properties.put("pageId", HtmlEditorService.CONTENT_DIRECTORY);
		properties.put("html", html);
		properties.put("rows", Integer.toString(getSize1()));
		properties.put("width", "300px");
		properties.put("height", "300px");

		editor.setData(context, properties, tempDefinitionControl.isRestrictCollections(),
			tempDefinitionControl.isRestrictDynacolls(), tempDefinitionControl.isRestrictSearches(),
			tempDefinitionControl.isRestrictContributables(), searchableUuids, contributableUuids, getFormName(),
			new HtmlControlScriptContextFactory(repo));

		HtmlEditMceWebControlModel model = getModel(context);
		model.setEditor(SectionUtils.renderSection(context, editor));
		model.setStaticHtml(html);

		final boolean lazyLoad = tempDefinitionControl.isLazyLoad();
		model.setLazyLoad(lazyLoad);

		if( isEnabled() && lazyLoad )
		{
			addDisabler(context, editLink);
		}
		if( (isEnabled() && !lazyLoad) || (lazyLoad && model.isEditing()) )
		{
			addDisabler(context, editor);
		}

		// http://dev.equella.com/issues/5612
		// we can probably remove this if Chrome sorts out the problem
		context.getResponse().addHeader("X-XSS-Protection", "0");

		return viewFactory.createResult("htmleditwebcontrol.ftl", context);
	}

	@EventHandlerMethod
	public void toggleEditing(SectionContext context)
	{
		final HtmlEditMceWebControlModel model = getModel(context);
		final boolean editing = model.isEditing();
		if( editing )
		{
			// user locked the control -> save the current contents
			doEdits(context);
		}
		else
		{
			// stop the clobbering
			model.setDontSave(true);
		}

		model.setEditing(!editing);
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		htmlEditControl = (CCustomControl) control;
		super.setWrappedControl(control);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		HtmlEditMceWebControlModel model = getModel(info);
		if( !getDefinitionControl().isLazyLoad() || (model.isEditing() && !model.isDontSave()) )
		{
			LERepository repo = getRepository();
			String wizId = repo.getWizid();
			ItemId itemId = repo.getItem().getItemId();

			StagingConversion conversion = new StagingConversion(false, itemId, wizId, repo.getStagingid());
			String htmlText = editor.getHtml(info);
			if( !Check.isEmpty(htmlText) )
			{
				htmlText = converter.convert(new StringReader(htmlText), true, conversion.getConversions());

				// use a full URL
				List<String> values = htmlEditControl.getValues();
				if( values.size() == 0 )
				{
					values.add(htmlText);
				}
				else
				{
					values.set(0, htmlText);
				}
			}
			else
			{
				htmlEditControl.setValues();
			}
		}

		// FIXME: oh dear, we can't actually do this as we cannot ensure that
		// all HtmlEditMceWebControl
		// on multiple pages will contribute their html text.
		// Which goes back to what I was saying a while ago where ALL wizard
		// controls should
		// execute on each page submission...
		//
		// ensureCleanupOperation(htmlText);

	}

	// private void ensureCleanupOperation(String html)
	// {
	// WizardState state = ((WebRepository) getRepository()).getState();
	// UnusedContentCleanup cleanup = (UnusedContentCleanup) state
	// .getWizardSaveOperation(UnusedContentCleanup.ID);
	// if( cleanup == null )
	// {
	// cleanup = new UnusedContentCleanup();
	// }
	// cleanup.put(getSectionId(), html);
	// state.setWizardSaveOperation(UnusedContentCleanup.ID, cleanup);
	// }

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		editor = htmlEditorService.getControl();
		tree.registerSubInnerSection(editor, id);

		// ajax.getAjaxUpdateDomFunction(tree, id, "content",
		// events.getEventHandler("toggleEditing"));

		editLink.setClickHandler(events.getNamedHandler("toggleEditing"));
	}

	@Override
	public Class<HtmlEditMceWebControlModel> getModelClass()
	{
		return HtmlEditMceWebControlModel.class;
	}

	protected HtmlEditMceControl getDefinitionControl()
	{
		if( definitionControl == null )
		{
			definitionControl = new HtmlEditMceControl((CustomControl) getControlBean());
		}
		return definitionControl;
	}

	public Link getEditLink()
	{
		return editLink;
	}

	public static class HtmlEditMceWebControlModel extends WebControlModel
	{
		private SectionRenderable editor;
		@Bookmarked(name = "e")
		private boolean editing;
		private String staticHtml;
		private boolean dontSave;
		private boolean lazyLoad;

		public SectionRenderable getEditor()
		{
			return editor;
		}

		public void setEditor(SectionRenderable editor)
		{
			this.editor = editor;
		}

		public boolean isEditing()
		{
			return editing;
		}

		public void setEditing(boolean editing)
		{
			this.editing = editing;
		}

		public String getStaticHtml()
		{
			return staticHtml;
		}

		public void setStaticHtml(String staticHtml)
		{
			this.staticHtml = staticHtml;
		}

		public boolean isDontSave()
		{
			return dontSave;
		}

		public void setDontSave(boolean dontSave)
		{
			this.dontSave = dontSave;
		}

		public boolean isLazyLoad()
		{
			return lazyLoad;
		}

		public void setLazyLoad(boolean lazyLoad)
		{
			this.lazyLoad = lazyLoad;
		}
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}

	public class HtmlControlScriptContextFactory implements ScriptContextFactory
	{
		private final LERepository repoMan;

		protected HtmlControlScriptContextFactory(LERepository repoMan)
		{
			this.repoMan = repoMan;
		}

		@Override
		public ScriptContext createScriptContext()
		{
			return repoMan.getScriptContext(getWizardPage(), htmlEditControl, null);
		}
	}
}
