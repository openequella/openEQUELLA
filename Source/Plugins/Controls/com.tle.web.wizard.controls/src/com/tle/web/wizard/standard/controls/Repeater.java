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

package com.tle.web.wizard.standard.controls;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.AjaxCaptureResult;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.FullDOMResult;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.wizard.WizardJSLibrary;
import com.tle.web.wizard.controls.CRepeater;
import com.tle.web.wizard.controls.GroupsCtrl.ControlGroup;
import com.tle.web.wizard.controls.WebControl;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.page.ControlResult;

@SuppressWarnings("nls")
@Bind
public class Repeater extends GroupWebControl<Repeater.RepeaterModel>
{
	private static final String CLASS_REMOVE = "repeater-remove";
	private static final String CLASS_MOVE = "repeater-move";
	private static final String CLASS_MOVE_UP = "move-up";
	private static final String CLASS_MOVE_DOWN = "move-down";

	static
	{
		PluginResourceHandler.init(Repeater.class);
	}

	@AjaxFactory
	private AjaxGenerator ajax;

	@PlugURL("js/repeater.js")
	private static String URL_REPEATERJS;
	private static final IncludeFile INCJS = new IncludeFile(URL_REPEATERJS, WizardJSLibrary.INCLUDE,
		JQueryUIEffects.SLIDE);
	private static ExternallyDefinedFunction REPEATER = new ExternallyDefinedFunction("repeater", INCJS);

	@Component
	private Link addButton;

	private CRepeater crepeater;
	private String addedDivId;

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		crepeater = (CRepeater) control;
		super.setWrappedControl(control);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		addButton.addReadyStatements(new JQueryStatement(this, new FunctionCallExpression(REPEATER,
			new ObjectExpression("addAjax", ajax.getAjaxFunction("addAjax"), "addButton", addButton, "disabler",
				addButton.createDisableFunction(), "removeAjax", ajax.getAjaxFunction("removeAjax"), "swapIndexAjax",
				ajax.getAjaxFunction("swapIndexAjax")))));
		addedDivId = getSectionId() + "_groups";
	}

	@AjaxMethod
	public JSONResponseCallback addAjax(AjaxRenderContext context)
	{
		getModel(context).setRenderMeOnly(true);
		final String ajaxId = getSectionId() + "_gajax_" + crepeater.getGroups().size();
		context.addAjaxDivs(ajaxId);
		context.setModalId(getSectionId());
		context.setFormBookmarkEvent(new BookmarkEvent(this, true, context));
		add(context);
		return new JSONResponseCallback()
		{
			@Override
			public Object getResponseObject(AjaxRenderContext context)
			{
				return new RepeaterUpdate(context, context.getFullDOMResult(), ajaxId);
			}
		};
	}

	@AjaxMethod
	public JSONResponseCallback removeAjax(AjaxRenderContext context, int index)
	{
		getModel(context).setRenderMeOnly(true);
		context.addAjaxDivs(addedDivId);
		context.setModalId(getSectionId());
		context.setFormBookmarkEvent(new BookmarkEvent(this, true, context));
		remove(context, index);
		return new JSONResponseCallback()
		{
			@Override
			public Object getResponseObject(AjaxRenderContext context)
			{
				return new RepeaterUpdate(context, context.getFullDOMResult(), addedDivId);
			}
		};
	}

	@AjaxMethod
	public JSONResponseCallback swapIndexAjax(AjaxRenderContext context, int oldIndex, int newIndex)
	{
		getModel(context).setRenderMeOnly(true);
		context.addAjaxDivs(addedDivId);
		context.setModalId(getSectionId());
		swapIndex(oldIndex, newIndex);
		doEdits(context); // helps with the path overrides
		return new JSONResponseCallback()
		{
			@Override
			public Object getResponseObject(AjaxRenderContext context)
			{
				return new RepeaterUpdate(context, context.getFullDOMResult(), addedDivId);
			}
		};
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		crepeater.setEmpty(false);

		// Check if we are removing a group.
		List<List<WebControl>> webGroups = getWebGroups();
		int size = webGroups.size();
		for( int i = size - 1; i >= 0; i-- )
		{
			// path overrides for advanced script control
			final WizardPage wizardPage = crepeater.getWizardPage();
			wizardPage.pushPathOverride(crepeater, crepeater.getFirstTarget().getTarget(), i);

			List<WebControl> vGroup = webGroups.get(i);
			processGroup(vGroup, info);
			wizardPage.popPathOverride(crepeater);
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final RepeaterModel model = getModel(context);
		final List<RepeaterRenderedGroup> renderedGroups = new ArrayList<RepeaterRenderedGroup>();
		final List<List<WebControl>> groups = getWebGroups();

		// path overrides for advanced script control (freemarker)
		// push overrides for parent repeaters if this control is the only one
		// being rendered
		final WizardPage wizardPage = crepeater.getWizardPage();
		List<Pair<CRepeater, Integer>> parentChain = (model.isRenderMeOnly() ? pushParentOverrides(wizardPage) : null);

		int index = 0;
		for( List<WebControl> controls : groups )
		{
			wizardPage.pushPathOverride(crepeater, crepeater.getFirstTarget().getTarget(), index);

			List<ControlResult> results = getWebWizardPage().renderChildren(context, controls,
				getSectionId() + "_" + index);

			HtmlLinkState removeLink = new HtmlLinkState().addClass(getSectionId() + CLASS_REMOVE).addClass(
				CLASS_REMOVE);
			removeLink.setElementId(new AppendedElementId(this, "index" + index));
			removeLink.setDisablable(true);
			removeLink.addReadyStatements(new JQueryStatement(removeLink, new FunctionCallExpression("data",
				"repeater.index", index)));

			RepeaterRenderedGroup group = new RepeaterRenderedGroup(results);
			LinkRenderer remove = new LinkRenderer(removeLink);
			addDisabler(context, remove);
			group.setDeleteButton(remove);

			HtmlLinkState moveDown = new HtmlLinkState().addClass(CLASS_MOVE)
				.addClass(getSectionId() + CLASS_MOVE_DOWN);
			moveDown.setElementId(new AppendedElementId(this, "moveDownIndex" + index));
			moveDown.setLabel(new IconLabel(Icon.DOWN, null));
			moveDown.setDisablable(true);
			moveDown.addReadyStatements(new JQueryStatement(moveDown, new FunctionCallExpression("data",
				"repeater.index", index)));

			LinkRenderer moveDownLink = new LinkRenderer(moveDown);
			addDisabler(context, moveDownLink);
			group.setMoveDownButton(moveDownLink);

			HtmlLinkState moveUp = new HtmlLinkState().addClass(CLASS_MOVE).addClass(getSectionId() + CLASS_MOVE_UP);
			moveUp.setElementId(new AppendedElementId(this, "moveUpIndex" + index));
			moveUp.setDisablable(true);
			moveUp.setLabel(new IconLabel(Icon.UP, null));
			moveUp.addReadyStatements(new JQueryStatement(moveUp, new FunctionCallExpression("data", "repeater.index",
				index)));

			LinkRenderer moveUpLink = new LinkRenderer(moveUp);
			addDisabler(context, moveUpLink);
			group.setMoveUpButton(moveUpLink);

			renderedGroups.add(group);

			wizardPage.popPathOverride(crepeater);
			index++;
		}
		popParentOverrides(wizardPage, parentChain);

		model.setRenderedGroups(renderedGroups);
		addButton.setDisabled(context, groups.size() >= crepeater.getMax());
		addDisablers(context, addButton);
		addDisableablesForControls(context);
		return viewFactory.createResult("repeater/repeater.ftl", context);
	}

	/**
	 * @param wizardPage
	 * @return A list of parents with the index of where the control sits within
	 *         the parent
	 */
	private List<Pair<CRepeater, Integer>> pushParentOverrides(WizardPage wizardPage)
	{
		final List<Pair<CRepeater, Integer>> parentChain = Lists.newArrayList();
		HTMLControl parent = crepeater.getParent();
		HTMLControl child = crepeater;
		while( parent != null )
		{
			if( parent instanceof CRepeater ) // it will be
			{
				parentChain.add(new Pair<CRepeater, Integer>((CRepeater) parent, indexOf(((CRepeater) parent), child)));
			}

			child = parent;
			parent = parent.getParent();
		}

		if( parentChain.size() > 0 )
		{
			for( int i = parentChain.size() - 1; i >= 0; i-- )
			{
				Pair<CRepeater, Integer> rep = parentChain.get(i);
				CRepeater repeater2 = rep.getFirst();
				wizardPage.pushPathOverride(repeater2, repeater2.getFirstTarget().getTarget(), rep.getSecond());
			}
		}
		return parentChain;
	}

	/**
	 * @param parent
	 * @param child
	 * @return The index of the <em>group</em> that this child is found in.
	 */
	private int indexOf(CRepeater parent, HTMLControl child)
	{
		for( ControlGroup group : parent.getGroups() )
		{
			if( group.contains(child) )
			{
				return group.getIndex();
			}
		}
		throw new Error("Child not found in parent!");
	}

	private void popParentOverrides(WizardPage wizardPage, List<Pair<CRepeater, Integer>> parentChain)
	{
		if( parentChain != null )
		{
			for( int i = 0; i < parentChain.size(); i++ )
			{
				Pair<CRepeater, Integer> rep = parentChain.get(i);
				CRepeater repeater = rep.getFirst();
				wizardPage.popPathOverride(repeater);
			}
		}
	}

	public void add(SectionInfo info)
	{
		crepeater.addAndEvaluate();
		getWebWizardPage().ensureTreeAdded(info, false);
	}

	public void remove(SectionInfo info, int index)
	{
		crepeater.removeGroup(info, index);
	}

	public void swapIndex(int oldIndex, int newIndex)
	{
		if( newIndex < crepeater.getGroups().size() && newIndex >= 0 )
		{
			crepeater.swapGroups(oldIndex, newIndex);
			// web groups
			List<List<WebControl>> webGroups = getWebGroups();
			List<WebControl> movingWeb = webGroups.get(oldIndex);
			webGroups.set(oldIndex, webGroups.get(newIndex));
			webGroups.set(newIndex, movingWeb);
		}
	}

	@Override
	public Class<RepeaterModel> getModelClass()
	{
		return RepeaterModel.class;
	}

	public static class RepeaterModel extends WebControlModel
	{
		private List<RepeaterRenderedGroup> renderedGroups;
		private boolean renderMeOnly;

		public List<RepeaterRenderedGroup> getRenderedGroups()
		{
			return renderedGroups;
		}

		public void setRenderedGroups(List<RepeaterRenderedGroup> renderedGroups)
		{
			this.renderedGroups = renderedGroups;
		}

		public boolean isRenderMeOnly()
		{
			return renderMeOnly;
		}

		public void setRenderMeOnly(boolean renderMeOnly)
		{
			this.renderMeOnly = renderMeOnly;
		}
	}

	public static class RepeaterRenderedGroup extends RenderedGroup
	{
		private LinkRenderer deleteButton;
		private LinkRenderer moveDownButton;
		private LinkRenderer moveUpButton;

		public RepeaterRenderedGroup(List<ControlResult> results)
		{
			super(results);
		}

		public void setMoveUpButton(LinkRenderer moveUp)
		{
			this.moveUpButton = moveUp;
		}

		public LinkRenderer getMoveUpButton()
		{
			return moveUpButton;
		}

		public void setMoveDownButton(LinkRenderer moveDown)
		{
			this.moveDownButton = moveDown;
		}

		public LinkRenderer getMoveDownButton()
		{
			return moveDownButton;
		}

		public LinkRenderer getDeleteButton()
		{
			return deleteButton;
		}

		public void setDeleteButton(LinkRenderer deleteButton)
		{
			this.deleteButton = deleteButton;
		}
	}

	public Link getAddButton()
	{
		return addButton;
	}

	public class RepeaterUpdate extends AbstractDOMResult
	{
		private final boolean disabled;
		private final String message;
		private AjaxCaptureResult added;

		public RepeaterUpdate(SectionInfo info, FullDOMResult result, String divId)
		{
			super(result);
			this.added = result != null ? new AjaxCaptureResult(result.getHtml().get(divId)) : null;
			Label message = Repeater.this.getMessage();
			this.message = message != null ? message.getText() : null;
			this.disabled = addButton.isDisabled(info);
		}

		public RepeaterUpdate(SectionInfo info)
		{
			Label message = Repeater.this.getMessage();
			this.message = message != null ? message.getText() : null;
			this.disabled = addButton.isDisabled(info);
		}

		public boolean isDisabled()
		{
			return disabled;
		}

		public String getMessage()
		{
			return message;
		}

		public AjaxCaptureResult getAdded()
		{
			return added;
		}
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return addButton;
	}
}
