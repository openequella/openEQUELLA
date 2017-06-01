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

package com.tle.web.connectors.dialog;

import javax.inject.Inject;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ParentFrameFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.DialogModel;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
@Bind
public class LMSAuthDialog extends AbstractOkayableDialog<LMSAuthDialog.Model>
{
	@PlugKey("dialog.lmsauth.title")
	private static Label LABEL_TITLE;

	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorRepositoryService repositoryService;

	@ViewFactory
	private FreemarkerFactory view;

	private ParentFrameFunction parentCallback;
	@Nullable
	private LMSAuthUrlCallable authUrlCallable;

	public LMSAuthDialog()
	{
		setAjax(true);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		final Model model = getModel(context);

		final String forwardUrl = new BookmarkAndModify(context, events.getNamedModifier("finishedAuth")).getHref();
		try
		{
			final String authUrl;
			if( authUrlCallable != null )
			{
				authUrl = authUrlCallable.getAuthorisationUrl(context, forwardUrl);
			}
			else
			{
				final String connectorUuid = model.getConnectorUuid();
				if( connectorUuid == null )
				{
					throw new RuntimeException("No connector UUID supplied to LMSAuthDialog");
				}
				final Connector connector = connectorService.getByUuid(connectorUuid);
				authUrl = repositoryService.getAuthorisationUrl(connector, forwardUrl, null);
			}
			model.setAuthUrl(authUrl);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}

		return view.createResult("dialog/lmsauth.ftl", this);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		parentCallback = new ParentFrameFunction(CallAndReferenceFunction.get(getOkCallback(), this));
	}

	@EventHandlerMethod
	public void finishedAuth(SectionInfo info)
	{
		//		final Model model = getModel(info);
		//		final String connectorUuid = model.getConnectorUuid();
		//		if( connectorUuid == null )
		//		{
		//			throw new RuntimeException("No connector UUID supplied to LMSAuthDialog");
		//		}
		//		final Connector connector = connectorService.getByUuid(connectorUuid);
		//		repositoryService.finishedAuthorisation(connector);

		//Close the dialog
		closeDialog(info, parentCallback, (Object) null);
	}

	@Override
	public String getWidth()
	{
		return "1024px";
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public Model instantiateDialogModel(SectionInfo info)
	{
		return new Model();
	}

	public void setConnectorUuid(SectionInfo info, String connectorUuid)
	{
		getModel(info).setConnectorUuid(connectorUuid);
	}

	public void setAuthUrlCallable(LMSAuthUrlCallable authUrlCallable)
	{
		this.authUrlCallable = authUrlCallable;
	}

	public interface LMSAuthUrlCallable
	{
		String getAuthorisationUrl(SectionInfo info, String forwardUrl);
	}

	@NonNullByDefault(false)
	public static class Model extends DialogModel
	{
		@Bookmarked(name = "c")
		private String connectorUuid;
		private String authUrl;

		public String getConnectorUuid()
		{
			return connectorUuid;
		}

		public void setConnectorUuid(String connectorUuid)
		{
			this.connectorUuid = connectorUuid;
		}

		public String getAuthUrl()
		{
			return authUrl;
		}

		public void setAuthUrl(String authUrl)
		{
			this.authUrl = authUrl;
		}
	}
}
