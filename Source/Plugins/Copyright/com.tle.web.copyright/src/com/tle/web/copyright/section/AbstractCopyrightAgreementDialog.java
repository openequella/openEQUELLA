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

import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.EventAuthoriser;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.dialog.renderer.DialogRenderer;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewurl.ItemUrlExtender;
import com.tle.web.viewurl.ViewAttachmentUrl;
import com.tle.web.viewurl.ViewItemUrl;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractCopyrightAgreementDialog extends EquellaDialog<AbstractCopyrightAgreementDialog.Model>
{

	@PlugURL("css/agreedialog.css")
	private static String URL_DIALOGCSS;
	@PlugURL("js/agreedialog.js")
	private static String URL_DIALOGJS;
	@PlugKey("agreement.title")
	private static Label LABEL_TITLE;
	private static ExternallyDefinedFunction FUNC_AGREE;

	static
	{
		PluginResourceHandler.init(AbstractCopyrightAgreementDialog.class);
		FUNC_AGREE = new ExternallyDefinedFunction("acceptAgreement", new IncludeFile(URL_DIALOGJS));
	}

	// Cannot be tree looked-up. CALAgreementSection section is inserted into
	// the tree at the same place as CLA version, therefore we don't know which
	// one we'll get.
	// @TreeLookup
	private AbstractCopyrightAgreementSection agreementSection;

	@Component
	@PlugKey("agreement.accept")
	private Button okButton;
	@Component
	@PlugKey("agreement.reject")
	private Button cancelButton;

	private JSCallable acceptCall;

	public AbstractCopyrightAgreementDialog()
	{
		setAjax(true);
	}

	protected abstract Class<? extends AbstractCopyrightAgreementSection> getAgreementSectionClass();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		okButton.setComponentAttribute(ButtonType.class, ButtonType.ACCEPT);
		cancelButton.setComponentAttribute(ButtonType.class, ButtonType.REJECT);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		agreementSection = tree.lookupSection(getAgreementSectionClass(), this);

		cancelButton.setClickHandler(getCloseFunction());
		acceptCall = ajaxEvents.getAjaxFunction("accept");
	}

	@AjaxMethod
	public boolean accept(SectionInfo info)
	{
		agreementSection.accept(info);
		return true;
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		okButton.setClickHandler(context, FUNC_AGREE, acceptCall, getCloseFunction(), getModel(context).getLinkId(),
			agreementSection.getAttachmentUuid(context));

		return agreementSection.renderAgreement(context, null);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return Lists.newArrayList(okButton, cancelButton);
	}

	@Override
	public Model instantiateDialogModel(SectionInfo info)
	{
		return new Model();
	}

	public Button getOkButton()
	{
		return okButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	private ItemUrlExtender getLinkUrl(final String linkId)
	{
		return new ItemUrlExtender()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void execute(SectionInfo info)
			{
				getModel(info).setLinkId(linkId);
			}
		};
	}

	public static class Model extends DialogModel
	{
		@Bookmarked(name = "li")
		private String linkId;

		public String getLinkId()
		{
			return linkId;
		}

		public void setLinkId(String linkId)
		{
			this.linkId = linkId;
		}

	}

	public LinkTagRenderer createAgreementDialog(SectionInfo info, SectionInfo from, ViewItemUrl vurl,
		LinkTagRenderer viewerTag, IAttachment attach)
	{
		viewerTag.setRel(null);
		viewerTag.registerUse();
		vurl.add(getLinkUrl(viewerTag.getElementId(from)));
		vurl.add((BookmarkModifier) info.getAttribute(EventAuthoriser.class));
		vurl.add(new ViewAttachmentUrl(attach.getUuid()));
		vurl.setFilepath(UrlEncodedString.BLANK);
		HtmlLinkState state = new HtmlLinkState(vurl);
		getState(info).setContentsUrl(vurl);
		DialogRenderer dialog = getSelectedRenderer(info.getRootRenderContext());
		state.setClickHandler(new OverrideHandler(dialog.createOpenFunction()));
		String attachUuid = attach.getUuid();
		String linkClass = "copylink" + attachUuid; //$NON-NLS-1$
		viewerTag.addClass("copylink_hidden " + linkClass); //$NON-NLS-1$
		CopyrightLinkRenderer link = new CopyrightLinkRenderer(state, viewerTag);
		link.addClass(linkClass);
		link.setNestedRenderable(viewerTag.getNestedRenderable());
		return link;
	}

	public static class CopyrightLinkRenderer extends LinkRenderer
	{
		private final LinkTagRenderer viewerTag;

		public CopyrightLinkRenderer(HtmlLinkState state, LinkTagRenderer viewerTag)
		{
			super(state);
			this.viewerTag = viewerTag;
		}

		@Override
		public TagRenderer setNestedRenderable(SectionRenderable nested)
		{
			super.setNestedRenderable(nested);
			viewerTag.setNestedRenderable(nested);
			return this;
		}

		@Override
		protected void writeEnd(SectionWriter writer) throws IOException
		{
			super.writeEnd(writer);
			viewerTag.realRender(writer);
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			super.preRender(info);
			viewerTag.ensureClickable();
			info.addCss(URL_DIALOGCSS);
			info.preRender(viewerTag);
		}
	}
}
