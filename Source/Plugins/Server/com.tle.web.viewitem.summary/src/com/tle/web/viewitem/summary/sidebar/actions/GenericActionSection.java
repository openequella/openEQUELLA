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

package com.tle.web.viewitem.summary.sidebar.actions;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.AbstractEventOnlyComponent;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class GenericActionSection<T extends AbstractEventOnlyComponent<? extends HtmlComponentState>>
	extends
		AbstractParentViewItemSection<Object>
{
	@ResourceHelper(fixed = false)
	protected PluginResourceHelper RESOURCES;
	@ViewFactory
	private FreemarkerFactory viewItemFactory;

	@EventFactory
	protected EventGenerator events;

	@Inject
	private ReceiptService receiptService;

	private boolean showForPreview = false;

	protected abstract boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status);

	protected abstract void execute(SectionInfo info) throws Exception;

	protected abstract void setupComponent(T component);

	public abstract T getComponent();

	protected void setupHandler(JSHandler handler)
	{
		// Nothing by default
	}

	protected void setReceipt(Label receipt)
	{
		receiptService.setReceipt(receipt);
	}

	public void setShowForPreview(boolean showForPreview)
	{
		this.showForPreview = showForPreview;
	}

	public boolean isShowForPreview()
	{
		return showForPreview;
	}

	@EventHandlerMethod
	public final void clicked(SectionInfo info) throws Exception
	{
		execute(info);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		T c = getComponent();
		setupComponent(c);

		JSHandler handler = events.getNamedHandler("clicked");
		setupHandler(handler);
		addClickHandler(c, handler);
	}

	protected void addClickHandler(T c, JSHandler handler)
	{
		c.setClickHandler(handler);
	}

	@Override
	public final boolean canView(SectionInfo info)
	{
		final ItemSectionInfo itemInfo = getItemInfo(info);
		final WorkflowStatus status = itemInfo.getWorkflowStatus();

		return canView(info, itemInfo, status);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) || (isForPreview(context) && !isShowForPreview()) )
		{
			return null;
		}
		return viewItemFactory.createResult("viewitem/summary/sidebar/genericactionlink.ftl", context);
	}

	@Override
	public final Class<Object> getModelClass()
	{
		return Object.class;
	}
}
