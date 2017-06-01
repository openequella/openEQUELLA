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

package com.tle.web.wizard;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.wizard.beans.control.Button;
import com.dytech.edge.wizard.beans.control.Calendar;
import com.dytech.edge.wizard.beans.control.CheckBoxGroup;
import com.dytech.edge.wizard.beans.control.Date;
import com.dytech.edge.wizard.beans.control.EditBox;
import com.dytech.edge.wizard.beans.control.Group;
import com.dytech.edge.wizard.beans.control.Hidden;
import com.dytech.edge.wizard.beans.control.Html;
import com.dytech.edge.wizard.beans.control.ListBox;
import com.dytech.edge.wizard.beans.control.Multi;
import com.dytech.edge.wizard.beans.control.MultiEditBox;
import com.dytech.edge.wizard.beans.control.RadioGroup;
import com.dytech.edge.wizard.beans.control.Repeater;
import com.dytech.edge.wizard.beans.control.ShuffleBox;
import com.dytech.edge.wizard.beans.control.ShuffleList;
import com.dytech.edge.wizard.beans.control.TreeNav;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.google.common.collect.ImmutableMap;
import com.tle.common.Check;
import com.tle.common.scripting.ScriptException;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.WizardPageException;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxTagRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.DefaultSectionTree;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.wizard.controls.CButton;
import com.tle.web.wizard.controls.CCalendar;
import com.tle.web.wizard.controls.CCheckBoxGroup;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.controls.CEditBox;
import com.tle.web.wizard.controls.CGroupCtrl;
import com.tle.web.wizard.controls.CHidden;
import com.tle.web.wizard.controls.CListBox;
import com.tle.web.wizard.controls.CMultiCtrl;
import com.tle.web.wizard.controls.CMultiEditBox;
import com.tle.web.wizard.controls.CRepeater;
import com.tle.web.wizard.controls.CShuffleBox;
import com.tle.web.wizard.controls.CShuffleList;
import com.tle.web.wizard.controls.CStaticHTML;
import com.tle.web.wizard.controls.CTreeNav;
import com.tle.web.wizard.controls.WebControl;
import com.tle.web.wizard.page.AjaxUpdateData;
import com.tle.web.wizard.page.ControlResult;
import com.tle.web.wizard.page.WebWizardPageState;

/*
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@Bind
public class WebWizardPage extends WizardPage implements com.tle.web.wizard.page.WizardPage
{
	@Deprecated
	private static final Map<String, Class<? extends HTMLControl>> CONTROLMAP =

	new ImmutableMap.Builder<String, Class<? extends HTMLControl>>().put(Button.CLASS, CButton.class)
		.put(Calendar.CLASS, CCalendar.class).put(CheckBoxGroup.CLASS, CCheckBoxGroup.class)
		.put(Date.CLASS, CCalendar.class).put(EditBox.CLASS, CEditBox.class).put(Group.CLASS, CGroupCtrl.class)
		.put(ListBox.CLASS, CListBox.class).put(Multi.CLASS, CMultiCtrl.class)
		.put(RadioGroup.CLASS, CCheckBoxGroup.class).put(Repeater.REPEATER_CLASS, CRepeater.class)
		.put(ShuffleBox.CLASS, CShuffleBox.class).put(ShuffleList.CLASS1, CShuffleList.class)
		.put(Html.CLASS, CStaticHTML.class).put(TreeNav.CLASS, CTreeNav.class)
		.put(MultiEditBox.CLASS, CMultiEditBox.class).put(Hidden.CLASS, CHidden.class).build();

	private static final Class<?>[] CONSTRUCTCLASSES = new Class<?>[]{WizardPage.class, Integer.TYPE, Integer.TYPE,
			WizardControl.class};

	private static final String PAGESREG_KEY = "WebWizardPages-registered";
	private static final String PAGESREG_NEW = "WebWizardPages-newtrees";
	private static final String PAGESREG_NEWLYADDED = "WebWizardPages-newlyadded";
	private List<WebControl> pageControls;
	private final Map<Object, DefaultSectionTree> sectionTrees = new IdentityHashMap<Object, DefaultSectionTree>();

	private LERepository webRepository;
	private WebWizardPageState state;
	@Inject
	private WizardControlFactory factory;
	@Inject
	private RegistrationController registrationController;
	@Inject
	private ScriptingService scriptingService;
	private JSCallable reloadFunction;

	private int treeCount = 0;

	public WebWizardPage()
	{
		// blah
	}

	@Override
	public void setState(WebWizardPageState state)
	{
		this.state = state;
	}

	@Override
	public void createPage() throws WizardPageException
	{
		super.createPage();
		pageControls = wrapControls(super.getControls());
		for( DefaultSectionTree tree : sectionTrees.values() )
		{
			tree.setAttribute(PAGESREG_NEWLYADDED, false);
		}
	}

	@Override
	public List<? extends HTMLControl> getControls()
	{
		return pageControls;
	}

	@Override
	public void loadFromDocument(final SectionInfo info)
	{
		super.loadFromDocument(pageControls, new GetItemsInterface()
		{
			@Override
			public void getItems(HTMLControl wrapped)
			{
				((WebControl) wrapped).doReads(info);
			}
		});
	}

	@Override
	public List<ControlResult> renderPage(RenderContext info)
	{
		return renderChildren(info, pageControls, null, new RenderData(), true, false);
	}

	@Override
	public HTMLControl createControl(WizardControl controlBean, int controlNumber, int nestingLevel)
		throws WizardPageException
	{
		String classType = controlBean.getClassType();
		Class<? extends HTMLControl> clazz = CONTROLMAP.get(classType);
		if( clazz == null )
		{
			return new CCustomControl(this, controlNumber, nestingLevel, controlBean);
		}
		try
		{
			Constructor<? extends HTMLControl> constructor = clazz.getConstructor(CONSTRUCTCLASSES);
			return constructor.newInstance(new Object[]{this, controlNumber, nestingLevel, controlBean});
		}
		catch( Exception e )
		{
			throw new WizardPageException(e);
		}
	}

	public void removeControls(List<WebControl> controls)
	{
		sectionTrees.remove(controls);
	}

	public List<WebControl> wrapControls(List<? extends HTMLControl> list)
	{
		List<WebControl> outList = new ArrayList<WebControl>();
		if( list.size() > 0 )
		{
			DefaultSectionTree sectionTree = new DefaultSectionTree(registrationController, new SectionNode("p"
				+ getPageNumber() + ((treeCount++ == 0) ? "" : "t" + treeCount)));
			for( HTMLControl control : list )
			{
				WebControl webcontrol = factory.createWebControl(control);
				webcontrol.setWebWizardPage(this);
				outList.add(webcontrol);
				sectionTree.registerSections(webcontrol, null);
			}
			sectionTree.treeFinished();
			sectionTree.setAttribute(PAGESREG_NEWLYADDED, true);
			sectionTrees.put(outList, sectionTree);
		}
		return outList;
	}

	@Override
	public LERepository getRepository()
	{
		return webRepository;
	}

	@Override
	public boolean isEnabled()
	{
		return state.isEnabled();
	}

	@Override
	public boolean isShowMandatory()
	{
		return state.isShowMandatory();
	}

	@Override
	public boolean isSubmitted()
	{
		return state.isSubmitted();
	}

	@Override
	public boolean isValid()
	{
		return state.isValid();
	}

	@Override
	public boolean isViewable()
	{
		return state.isViewable();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		state.setEnabled(enabled);
	}

	@Override
	public void setShowMandatory(boolean showMandatory)
	{
		state.setShowMandatory(showMandatory);
	}

	@Override
	public void setSubmitted(boolean submitted)
	{
		state.setSubmitted(submitted);
	}

	@Override
	public void setValid(boolean valid)
	{
		state.setValid(valid);
	}

	@Override
	public void setViewable(boolean viewable)
	{
		state.setViewable(viewable);
	}

	public void setWebRepository(LERepository webRepository)
	{
		this.webRepository = webRepository;
	}

	@Override
	public void saveToDocument(final SectionInfo info) throws Exception
	{
		saveToDocument(pageControls, new SetItemsInterface()
		{
			@Override
			public void setItems(HTMLControl wrapped) throws Exception
			{
				WebControl webControl = (WebControl) wrapped;
				webControl.doEditsIfRequired(info);
			}

			@Override
			public void clearTargets(HTMLControl wrapped, PropBagEx doc)
			{
				((WebControl) wrapped).clearTargets(info, doc);
			}

			@Override
			public void afterAllSaved()
			{
				webRepository.updateMetadataMapping();
			}
		});
	}

	@Override
	public void saveDefaults() throws Exception
	{
		saveToDocument(pageControls, null);
	}

	@Override
	public void ensureTreeAdded(SectionInfo info)
	{
		ensureTreeAdded(info, true);
	}

	@Override
	public void ensureTreeAdded(SectionInfo info, boolean processParams)
	{
		Map<SectionTree, SectionTree> registered = getRegisterTreeMap(info);
		MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
		for( DefaultSectionTree sectionTree : sectionTrees.values() )
		{
			if( !registered.containsKey(sectionTree) )
			{
				registered.put(sectionTree, sectionTree);
				if( Boolean.TRUE.equals(sectionTree.getAttribute(PAGESREG_NEWLYADDED)) )
				{
					Map<SectionTree, SectionTree> newTrees = info.getAttributeSafe(PAGESREG_NEW, IdentityHashMap.class);
					newTrees.put(sectionTree, sectionTree);
					sectionTree.setAttribute(PAGESREG_NEWLYADDED, false);
				}
				minfo.addTreeToBottom(sectionTree, processParams);
			}
		}

	}

	private Map<SectionTree, SectionTree> getRegisterTreeMap(SectionInfo info)
	{
		return info.getAttributeSafe(PAGESREG_KEY, IdentityHashMap.class);
	}

	@Override
	protected boolean execScriptWithContext(String script, ScriptContext context) throws ScriptException
	{
		if( !Check.isEmpty(script) && getRepository() != null )
		{
			return scriptingService.evaluateScript(script, getPageTitle(), context);
		}
		return true;
	}

	@Override
	public void setReloadFunction(JSCallable reloadFunction)
	{
		this.reloadFunction = reloadFunction;
	}

	@Override
	public void setRepository(LERepository repos)
	{
		setWebRepository(repos);
	}

	@Override
	public Map<String, List<ControlResult>> renderPage(RenderContext context, AjaxUpdateData updateData, String rootId)
	{
		RenderData renderData;
		if( updateData != null )
		{
			Set<String> ignoredIds = new HashSet<String>();
			String controlId = updateData.getControlId();
			if( controlId != null )
			{
				ignoredIds.add(controlId);
			}
			renderData = new RenderData(updateData.getVisibleIds(), ignoredIds);
		}
		else
		{
			renderData = new RenderData();
		}
		context.setAttribute(this, renderData);
		renderChildren(context, pageControls, rootId, renderData, true, false);
		return renderData.getParentMap();
	}

	public List<ControlResult> renderChildren(RenderContext context, List<WebControl> controls, String parentId,
		boolean forceDisable)
	{
		RenderData renderData = (RenderData) context.getAttribute(this);
		if( renderData == null )
		{
			renderData = new RenderData();
		}
		return renderChildren(context, controls, parentId, renderData, false, forceDisable);
	}

	public List<ControlResult> renderChildren(RenderContext context, List<WebControl> controls, String parentId)
	{
		return renderChildren(context, controls, parentId, false);
	}

	private List<ControlResult> renderChildren(RenderContext context, List<WebControl> controls, String parentId,
		RenderData data, boolean disable, boolean forceDisable)
	{
		Set<String> visibleIds = data.getVisibleIds();
		Set<String> ignoredIds = data.getIgnoredIds();
		List<ControlResult> results = new ArrayList<ControlResult>();
		for( WebControl ctrl : controls )
		{
			if( ctrl.isViewable() )
			{
				String sectionId = ctrl.getSectionId();
				TagRenderer renderable = null;
				TagState tagState = new TagState(sectionId);

				boolean ajax = !ctrl.canHaveChildren() || !visibleIds.contains(sectionId);
				boolean ignored = !ctrl.canHaveChildren() && ignoredIds.contains(sectionId);

				if( !ignored )
				{
					SectionRenderable rendered = SectionUtils.renderSection(context, ctrl);
					if( ajax )
					{
						renderable = new AjaxTagRenderer("div", tagState, rendered, true);
					}
					else
					{
						renderable = new TagRenderer("div", tagState, rendered);
					}
					if( forceDisable || (disable && !ctrl.isEnabled()) )
					{
						tagState.addReadyStatements(new FunctionCallStatement(ctrl.getDisabler(context)
							.createDisableFunction(), true));
					}
				}
				results.add(new ControlResult(sectionId, renderable));
			}
		}
		data.getParentMap().put(parentId, results);
		return results;
	}

	public static class RenderData
	{
		private final Set<String> visibleIds;
		private final Set<String> ignoredIds;
		private final Map<String, List<ControlResult>> parentMap = new HashMap<String, List<ControlResult>>();

		public RenderData()
		{
			visibleIds = Collections.emptySet();
			ignoredIds = Collections.emptySet();
		}

		public RenderData(Set<String> visibleIds, Set<String> ignoredIds)
		{
			this.visibleIds = visibleIds;
			this.ignoredIds = ignoredIds;
		}

		public Set<String> getVisibleIds()
		{
			return visibleIds;
		}

		public Set<String> getIgnoredIds()
		{
			return ignoredIds;
		}

		public Map<String, List<ControlResult>> getParentMap()
		{
			return parentMap;
		}
	}

	public JSCallable getReloadFunction()
	{
		return reloadFunction;
	}

	public boolean isNewlyAdded(SectionInfo info, SectionTree tree)
	{
		Map<SectionTree, SectionTree> newTrees = info.getAttributeSafe(PAGESREG_NEW, IdentityHashMap.class);
		return newTrees.containsKey(tree);
	}

	@Override
	public List<? extends SectionId> getRootIds()
	{
		return pageControls;
	}
}