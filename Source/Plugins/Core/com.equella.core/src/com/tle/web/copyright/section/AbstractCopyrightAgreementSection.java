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

package com.tle.web.copyright.section;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.copyright.service.AgreementStatus;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.security.TLEAclManager;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.section.ViewAttachmentSection;
import com.tle.web.viewurl.UseViewer;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;

/**
 * If you extend this you must provide your own agreement.ftl and
 * dialog/agreement.ftl
 * 
 * @author Aaron
 */
@NonNullByDefault
@TreeIndexed
@SuppressWarnings("nls")
public abstract class AbstractCopyrightAgreementSection
	extends
		AbstractPrototypeSection<AbstractCopyrightAgreementSection.Model> implements ViewItemViewer, ViewItemFilter

{
	private CopyrightService<? extends Holding, ? extends Portion, ? extends Section> copyrightService;
	private CopyrightWebService<? extends Holding> copyrightWebService;

	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;
	@Component
	@PlugKey("agreement.accept")
	private Button okButton;
	@Component
	@PlugKey("agreement.reject")
	private Button cancelButton;
	@TreeLookup
	private RootItemFileSection rootFileSection;
	@TreeLookup
	private ViewAttachmentSection viewAttachmentSection;

	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	private TLEAclManager aclService;

	@PostConstruct
	void setupServices()
	{
		copyrightService = getCopyrightServiceImpl();
		copyrightWebService = getCopyrightWebServiceImpl();
	}

	protected abstract CopyrightWebService<? extends Holding> getCopyrightWebServiceImpl();

	protected abstract CopyrightService<? extends Holding, ? extends Portion, ? extends Section> getCopyrightServiceImpl();

	@Override
	public void registered(final String id, SectionTree tree)
	{
		super.registered(id, tree);

		okButton.setStyleClass("focus");
		okButton.setClickHandler(events.getNamedHandler("accept"));
		okButton.setComponentAttribute(ButtonType.class, ButtonType.ACCEPT);

		cancelButton.setClickHandler(new OverrideHandler(new ScriptStatement("history.back();")));
		cancelButton.setComponentAttribute(ButtonType.class, ButtonType.REJECT);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootFileSection.addFilterMapping(Type.ALWAYS, this);
	}

	public JSCallable getShowAgreementFunction()
	{
		return getDialog().getOpenFunction();
	}

	protected abstract AbstractCopyrightAgreementDialog getDialog();

	public boolean canView(SectionInfo context)
	{
		return true;
	}

	@Override
	public int getOrder()
	{
		return 0;
	}

	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		ViewableItem<Item> viewableItem = resource.getViewableItem();
		if( viewableItem.isItemForReal() && copyrightService.isCopyrightedItem(viewableItem.getItem()) )
		{
			boolean showAgreement = false;
			final IAttachment attachment = getAttachment(info, resource);
			if( attachment != null )
			{
				AgreementStatus status = copyrightService.getAgreementStatus(viewableItem.getItem(), attachment);
				showAgreement = status != null && status.isNeedsAgreement();
			}

			if( showAgreement )
			{
				return new UseViewer(resource, this);
			}
		}
		return resource;
	}

	@Override
	@Nullable
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		final ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		if( viewableResource != null )
		{
			final IAttachment attachment = viewableResource.getAttachment();
			return attachment;
		}
		return null;
	}

	/**
	 * This must run before the agreement filters decide they still need to display
	 */
	@EventHandlerMethod
	public void accept(SectionInfo info)
	{
		// Cannot invoke rootFileSection.getViewItemResource since it runs the filters again and shows
		// a second agreement when already accepted.  Basically, this is pretty dirty.
		ViewableItem<Item> viewableItem = rootFileSection.getViewableItem(info);
		copyrightService.acceptAgreement(viewableItem.getItem(),
			viewAttachmentSection.getAttachment(info, rootFileSection.getBaseViewItemResource(info)));
	}

	public Collection<Button> getButtons(SectionInfo info, SectionId id)
	{
		return Lists.newArrayList(okButton, cancelButton);
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		// this is rubbish
		Collection<Button> buttons = getButtons(info, this);
		SectionRenderable sr = null;
		for( Button b : buttons )
		{
			ButtonRenderer br = (ButtonRenderer) SectionUtils.renderSection(info, b);
			if( br != null )
			{
				ButtonType type = b.getComponentAttribute(ButtonType.class);
				if( type != null )
				{
					br.showAs(type);
				}
				sr = CombinedRenderer.combineResults(sr, br);
			}
		}
		return renderAgreement(info, resource, sr);
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	public SectionRenderable renderAgreement(RenderContext info, ViewItemResource resource, SectionRenderable buttons)
	{
		Model model = getModel(info);
		Item item = (Item) resource.getViewableItem().getItem();
		Decorations.getDecorations(info).setTitle(new ItemNameLabel(item, bundleCache));
		ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		AgreementStatus status = copyrightService.getAgreementStatus(item, viewableResource.getAttachment());
		if( status.isInactive()
			&& aclService.filterNonGrantedPrivileges(ActivationConstants.VIEW_INACTIVE_PORTIONS).isEmpty() )
		{
			model.setException(copyrightService.createViolation(item));
			return view.createResult("violation.ftl", this);
		}

		SectionRenderable agreement;
		String agreementText = copyrightWebService.getAgreement(status.getAgreementFile());
		if( agreementText == null )
		{
			agreement = getStandardAgreement(info);
			model.setStandardAgreement(true);
		}
		else
		{
			agreement = new SimpleSectionResult(agreementText);
			model.setStandardAgreement(false);
		}
		model.setButtons(buttons);
		model.setAgreement(agreement);
		model.setInIntegration(rootFileSection.isInIntegration(info));
		return view.createResult("agreement.ftl", this);
	}

	protected abstract SectionRenderable getStandardAgreement(RenderContext info);

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	public static class Model
	{
		private SectionRenderable agreement;
		private SectionRenderable buttons;
		private CopyrightViolationException exception;
		private boolean standardAgreement;
		private boolean inIntegration;

		public CopyrightViolationException getException()
		{
			return exception;
		}

		public void setException(CopyrightViolationException exception)
		{
			this.exception = exception;
		}

		public SectionRenderable getAgreement()
		{
			return agreement;
		}

		public void setAgreement(SectionRenderable agreement)
		{
			this.agreement = agreement;
		}

		public SectionRenderable getButtons()
		{
			return buttons;
		}

		public void setButtons(SectionRenderable buttons)
		{
			this.buttons = buttons;
		}

		public boolean isStandardAgreement()
		{
			return standardAgreement;
		}

		public void setStandardAgreement(boolean standardAgreement)
		{
			this.standardAgreement = standardAgreement;
		}

		public boolean isInIntegration()
		{
			return inIntegration;
		}

		public void setInIntegration(boolean inIntegration)
		{
			this.inIntegration = inIntegration;
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public SectionRenderable renderAgreement(RenderContext context, SectionRenderable buttons)
	{
		ViewItemResource resource = rootFileSection.getViewItemResource(context);
		return renderAgreement(context, resource, buttons);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "cagr";
	}

	public String getAttachmentUuid(SectionInfo info)
	{
		ViewableResource viewableResource = rootFileSection.getViewItemResource(info).getAttribute(
			ViewableResource.class);
		return viewableResource.getAttachment().getUuid();
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}
}
