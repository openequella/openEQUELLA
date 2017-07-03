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

package com.tle.web.remoterepo.section;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.Check;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.remoterepo.RemoteRepoListItemViewHandlerCreator;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class RemoteRepoViewResultSection<M extends RemoteRepoViewResultSection.RemoteRepoViewResultModel<R>, E extends RemoteRepoListEntry<?>, R extends GenericRecord>
	extends
		AbstractPrototypeSection<M> implements RemoteRepoListItemViewHandlerCreator<E>, HtmlRenderer
{
	@Inject
	private RemoteRepoWebService repoWebService;
	@Inject
	private BundleCache bundleCache;

	@EventFactory
	protected EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@TreeLookup
	private AbstractRootRemoteRepoSection rootRemoteRepoSection;

	@Component
	private Button importButton;

	/**
	 * You should add your record to the model so you can fetch it will for
	 * getTitle and getContents
	 * 
	 * @param info
	 * @return
	 */
	protected abstract void setupModel(SectionInfo info, M model, FederatedSearch search);

	protected abstract Label getTitle(SectionInfo info, M model);

	protected abstract void getContents(SectionInfo info, M model, List<SectionRenderable> contents);

	protected abstract PropBagEx getImportXml(SectionInfo info, FederatedSearch search);

	protected abstract void clearResult(SectionInfo info);

	protected AbstractRootRemoteRepoSection getRootRemoteRepoSection()
	{
		return rootRemoteRepoSection;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		importButton.setClickHandler(events.getNamedHandler("doImport"));
	}

	@EventHandlerMethod
	public void doImport(SectionInfo info)
	{
		final FederatedSearch search = repoWebService.getRemoteRepository(info);
		repoWebService.forwardToWizard(info, getImportXml(info, search), search);
	}

	@EventHandlerMethod
	public void forwardToSearch(SectionInfo info)
	{
		final FederatedSearch search = repoWebService.getRemoteRepository(info);
		clearResult(info);
		repoWebService.forwardToSearch(info, search, false);
	}

	protected abstract boolean isShowing(SectionInfo info);

	@Override
	@SuppressWarnings("unused")
	public SectionResult renderHtml(RenderEventContext context)
	{
		final M model = getModel(context);
		final FederatedSearch search = repoWebService.getRemoteRepository(context);

		setupModel(context, model, search);

		String resourceDescriptor = model.getResult().getUrl();
		if( !Check.isEmpty(resourceDescriptor) )
		{
			try
			{
				new URL(resourceDescriptor); // NOSONAR
				model.setHasUrl(true);
			}
			catch( MalformedURLException malf )
			{
				// calling new URL for basic validation of descriptor. Having
				// failed to set the hasUrl field, ignore exception
			}
			model.setResourceUrl(new LabelRenderer(new TextLabel(model.getResult().getUrl())));
		}

		Label title = getTitle(context, model);
		Decorations.getDecorations(context).setTitle(title);

		model.setTitle(new LabelRenderer(title));

		final List<SectionRenderable> contents = new ArrayList<SectionRenderable>();
		final PropBagEx bagXml = model.getResult().getXml();
		String xml = null;
		if( bagXml != null )
		{
			xml = repoWebService.getDisplayText(search, bagXml);
		}

		if( !Check.isEmpty(xml) )
		{
			contents.add(new SimpleSectionResult(xml));
		}
		else
		{
			getContents(context, model, contents);
		}
		model.setContent(contents);

		return view.createResult("remoterepoviewresult.ftl", this);
	}

	/**
	 * To be used inside getContents.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<SectionRenderable> contents, String key, SectionRenderable renderable)
	{
		contents.add(getStandardRow(new KeyLabel(getKeyPrefix() + key), renderable));
	}

	/**
	 * To be used inside getContents.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<SectionRenderable> contents, String key, Object... values)
	{
		if( !Check.isEmpty(values) )
		{
			contents.add(getStandardRow(new KeyLabel(getKeyPrefix() + key), null, values));
		}
	}

	/**
	 * To be used inside getContents.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<SectionRenderable> contents, String key, Collection<?> values)
	{
		if( !Check.isEmpty(values) )
		{
			contents.add(getStandardRow(new KeyLabel(getKeyPrefix() + key), null, values.toArray()));
		}
	}

	/**
	 * To be used inside getContents.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<SectionRenderable> contents, String key, String value)
	{
		if( !Check.isEmpty(value) )
		{
			contents.add(getStandardRow(new KeyLabel(getKeyPrefix() + key), null, value));
		}
	}

	/**
	 * To be used inside getContents.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<SectionRenderable> contents, String key, int value)
	{
		contents.add(getStandardRow(new KeyLabel(getKeyPrefix() + key), null, value));
	}

	protected SectionRenderable getStandardRow(Label label, SectionRenderable renderable, Object... values)
	{
		return view.createResultWithModel("viewresultrow.ftl", new RowModel(label, values, renderable));
	}

	protected abstract String getKeyPrefix();

	public Button getImportButton()
	{
		return importButton;
	}

	public static class RowModel
	{
		private final LabelRenderer label;
		private final Object[] text;
		private final SectionRenderable renderable;

		public RowModel(Label label, Object[] text, SectionRenderable renderable)
		{
			this.label = new LabelRenderer(label);
			this.text = text;
			this.renderable = renderable;
		}

		public LabelRenderer getLabel()
		{
			return label;
		}

		public Object[] getText()
		{
			return text;
		}

		public SectionRenderable getRenderable()
		{
			return renderable;
		}
	}

	public abstract static class RemoteRepoViewResultModel<R>
	{
		private LabelRenderer title;
		private List<SectionRenderable> content;
		private R result;
		private boolean hasUrl;
		private LabelRenderer resourceUrl;

		public LabelRenderer getTitle()
		{
			return title;
		}

		public void setTitle(LabelRenderer title)
		{
			this.title = title;
		}

		public List<SectionRenderable> getContent()
		{
			return content;
		}

		public void setContent(List<SectionRenderable> content)
		{
			this.content = content;
		}

		public R getResult()
		{
			return result;
		}

		public void setResult(R result)
		{
			this.result = result;
		}

		public void setHasUrl(boolean hasUrl)
		{
			this.hasUrl = hasUrl;
		}

		public boolean getHasUrl()
		{
			return hasUrl;
		}

		public void setResourceUrl(LabelRenderer resourceUrl)
		{
			this.resourceUrl = resourceUrl;
		}

		public LabelRenderer getResourceUrl()
		{
			return resourceUrl;
		}
	}

	public void addCrumbs(SectionInfo info, Breadcrumbs crumbs)
	{
		HtmlLinkState repolink = new HtmlLinkState();
		final FederatedSearch search = repoWebService.getRemoteRepository(info);
		repolink.setLabel(new BundleLabel(search.getName(), bundleCache));
		repolink.setClickHandler(events.getNamedHandler("forwardToSearch"));
		crumbs.add(repolink);

		crumbs.setForcedLastCrumb(new WrappedLabel(getTitle(info, getModel(info)), -1, true));
	}
}
