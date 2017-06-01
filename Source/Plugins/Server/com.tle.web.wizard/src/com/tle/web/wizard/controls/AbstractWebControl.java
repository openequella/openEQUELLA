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

package com.tle.web.wizard.controls;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.HTMLCtrlWrapper;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.AbstractCallable;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.impl.CombinedDisableable;
import com.tle.web.sections.standard.renderers.FieldsetTagRenderer;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;
import com.tle.web.wizard.WebWizardPage;
import com.tle.web.wizard.impl.WebRepository;

/**
 * @author jmaginnis
 */
@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractWebControl<M extends WebControlModel> extends HTMLCtrlWrapper
	implements
		WebControl,
		ElementId
{
	private WebWizardPage webWizardPage;
	private boolean inColumn;
	private String sectionId;
	private boolean nested;
	private boolean groupLabelNeeded = false;

	private boolean used;
	private SectionTree tree;

	private FieldsetTagRenderer fsRenderer;

	@Override
	public boolean isTreeIndexed()
	{
		return false;
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		control.setTopLevel(this);
	}

	public AbstractWebControl()
	{
		// spring
	}

	public JSCallable getReloadFunction()
	{
		return getReloadFunction(false, null);
	}

	public JSCallable getReloadFunction(boolean reloadSelf)
	{
		return getReloadFunction(reloadSelf, null);
	}

	public JSCallable getReloadFunction(final boolean reloadSelf, @Nullable final ParameterizedEvent event,
		final String... ajaxIds)
	{
		final JSCallable reloadFunction = getWebWizardPage().getReloadFunction();
		return new AbstractCallable()
		{
			@Override
			public void preRender(PreRenderContext info)
			{
				info.preRender(reloadFunction);
			}

			@Override
			public int getNumberOfParams(RenderContext context)
			{
				if( event != null )
				{
					return event.getParameterCount();
				}
				return 0;
			}

			@Override
			protected String getCallExpression(RenderContext info, JSExpression[] params)
			{
				ArrayExpression eventArray = new ArrayExpression();
				if( event != null )
				{
					eventArray.add(event.getEventId());
					eventArray.addAll(params);
				}
				return new FunctionCallExpression(reloadFunction, reloadSelf ? null : sectionId, eventArray,
					new ArrayExpression((Object[]) ajaxIds)).getExpression(info);
			}
		};
	}

	@Override
	public void deletedFromParent(SectionInfo info)
	{
		// nothing by default
	}

	@Override
	public M instantiateModel(@Nullable SectionInfo info)
	{
		try
		{
			return getModelClass().newInstance();
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setWebWizardPage(WebWizardPage webWizardPage)
	{
		this.webWizardPage = webWizardPage;
	}

	@Override
	public void setInColumn(boolean inColumn)
	{
		this.inColumn = inColumn;
	}

	@Override
	public boolean isInColumn()
	{
		return inColumn;
	}

	@Override
	public WebWizardPage getWebWizardPage()
	{
		return webWizardPage;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return getFormName();
	}

	public abstract Class<M> getModelClass();

	@Override
	public void evaluate()
	{
		((AbstractHTMLControl) control).doEvaluate();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		this.sectionId = id;
		this.tree = tree;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		// nothing
	}

	@Override
	public String getSectionId()
	{
		return sectionId;
	}

	@Override
	public String getElementId(SectionInfo info)
	{
		return getSectionId();
	}

	@Override
	public void registerUse()
	{
		used = true;
	}

	@Override
	public boolean isStaticId()
	{
		return true;
	}

	@Override
	public boolean isElementUsed()
	{
		return used;
	}

	public M getModel(SectionInfo info)
	{
		return info.<M> getModelForId(getSectionId());
	}

	public M getModel(SectionContext context)
	{
		return context.<M> getModelForId(context.getSectionId());
	}

	@Override
	public CombinedDisableable getDisabler(SectionInfo info)
	{
		final M model = getModel(info);
		CombinedDisableable disabler = model.getDisabler();
		if( disabler == null )
		{
			disabler = new CombinedDisableable(this);
			model.setDisabler(disabler);
		}
		return disabler;
	}

	public void addDisabler(SectionInfo info, JSDisableable disabler)
	{
		getDisabler(info).addDisabler(disabler);
	}

	public void addDisablers(SectionInfo info, JSDisableable... disablers)
	{
		getDisabler(info).addDisablers(disablers);
	}

	@Override
	public void clearTargets(SectionInfo info, PropBagEx itemxml)
	{
		clearTargets(itemxml);
	}

	@Override
	public void doEditsIfRequired(SectionInfo info)
	{
		if( !webWizardPage.isNewlyAdded(info, tree) )
		{
			doEdits(info);
		}

	}

	public void doEdits(SectionInfo info)
	{
		// nothing by default
	}

	@Override
	public void doReads(SectionInfo info)
	{
		// nothing by default
	}

	@Override
	public boolean isNested()
	{
		return nested;
	}

	@Override
	public void setNested(boolean nested)
	{
		this.nested = nested;
	}

	public WebRepository getWebRepository()
	{
		return (WebRepository) getRepository();
	}

	@Override
	public SectionTree getTree()
	{
		return tree;
	}

	@Override
	public Section getSectionObject()
	{
		return null;
	}

	@Override
	public boolean canHaveChildren()
	{
		return false;
	}

	public LabelTagRenderer getLabelTag()
	{
		if( groupLabelNeeded )
		{
			fsRenderer = new FieldsetTagRenderer(null, null);
			AppendedElementId elementId = new AppendedElementId(getIdForLabel(), "_fs");
			elementId.registerUse();
			fsRenderer.setElementId(elementId);
			return new LabelTagRenderer(fsRenderer, null, null);
		}
		else
		{
			return new LabelTagRenderer(getIdForLabel(), null, null);
		}
	}

	protected abstract ElementId getIdForLabel();

	public FieldsetTagRenderer getFieldsetTag()
	{
		return fsRenderer;
	}

	public boolean isGroupLabelNeeded()
	{
		return groupLabelNeeded;
	}

	public void setGroupLabellNeeded(boolean groupLabellNeeded)
	{
		this.groupLabelNeeded = groupLabellNeeded;
	}
}
