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

package com.tle.web.remoterepo.equella;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.TLESettings;
import com.tle.common.Check;
import com.tle.common.util.TokenGenerator;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.convert.ConvertedToNull;
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
 */
@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class EquellaRepoSessionDialog
	extends
		AbstractOkayableDialog<EquellaRepoSessionDialog.EquellaRepoDoSessionModel>
{
	private static final String WIDTH = "810px";

	@Inject
	private RemoteRepoWebService repoWebService;

	@ViewFactory
	private FreemarkerFactory view;
	private ParentFrameFunction parentCallback;

	public EquellaRepoSessionDialog()
	{
		setAjax(true);
	}

	@Override
	public String getWidth()
	{
		return WIDTH;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		parentCallback = new ParentFrameFunction(CallAndReferenceFunction.get(getOkCallback(), this));
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext info)
	{
		final EquellaRepoDoSessionModel model = getModel(info);
		final FederatedSearch search = repoWebService.getRemoteRepository(info);
		final TLESettings equellaSettings = new TLESettings();
		equellaSettings.load(search);

		final String user = (equellaSettings.isUseLoggedInUser() ? CurrentUser.getUsername() : equellaSettings
			.getUsername());

		String cancelUrl = new BookmarkAndModify(info, events.getNamedModifier("results")).getHref();
		String returnUrl = cancelUrl;

		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("method", "lms");
		params.put("action", "selectOrAdd");
		params.put("selectMultiple", false);
		params.put("useDownloadPrivilege", true);
		params.put("returnprefix", "rr_");
		params.put("itemonly", true);
		try
		{
			params.put(
				"token",
				TokenGenerator.createSecureToken(user, equellaSettings.getSharedSecretId(),
					equellaSettings.getSharedSecretValue(), null));
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
		params.put("returnurl", returnUrl);
		params.put("cancelurl", cancelUrl);

		String institutionUrl = equellaSettings.getInstitutionUrl();
		if( !institutionUrl.endsWith("/") )
		{
			institutionUrl += '/';
		}
		model.setIntegrationUrl(institutionUrl + "signon.do?"
			+ SectionUtils.getParameterString(SectionUtils.getParameterNameValues(params, false)));
		return view.createResult("session.ftl", this);
	}

	@EventHandlerMethod
	public void results(SectionInfo info)
	{
		EquellaRepoDoSessionModel model = getModel(info);
		final String uuid = model.getUuid();
		if( !Check.isEmpty(uuid) )
		{
			final ItemSelection selection = new ItemSelection();
			selection.setUuid(uuid);
			selection.setVersion(model.getVersion());
			selection.setName(model.getItemName());
			selection.setUrl(model.getUrl());
			closeDialog(info, parentCallback, selection);
		}
		else
		{
			closeDialog(info, parentCallback, (Object) null);
		}
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return null;
	}

	@Override
	protected SectionRenderable getDialogContents(RenderContext context)
	{
		return getRenderableContents(context);
	}

	@Override
	public EquellaRepoDoSessionModel instantiateDialogModel(SectionInfo info)
	{
		return new EquellaRepoDoSessionModel();
	}

	@NonNullByDefault(false)
	public static class EquellaRepoDoSessionModel extends DialogModel
	{
		@Bookmarked(parameter = "rr_uuid")
		private String uuid;
		@Bookmarked(parameter = "rr_name")
		private String itemName;
		@Bookmarked(parameter = "rr_version")
		private int version;
		@Bookmarked(parameter = "rr_url")
		private String url;

		private String integrationUrl;

		public String getIntegrationUrl()
		{
			return integrationUrl;
		}

		public void setIntegrationUrl(String integrationUrl)
		{
			this.integrationUrl = integrationUrl;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public int getVersion()
		{
			return version;
		}

		public void setVersion(int version)
		{
			this.version = version;
		}

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public String getItemName()
		{
			return itemName;
		}

		public void setItemName(String itemName)
		{
			this.itemName = itemName;
		}
	}

	public static class ItemSelection
	{
		private String uuid;
		private int version;
		private String name;
		private String url;

		public ItemSelection(String dec)
		{
			if( Check.isEmpty(dec) )
			{
				throw new ConvertedToNull();
			}

			String[] parts = dec.split("\\|");
			if( parts.length != 4 )
			{
				throw new RuntimeException("Wrong number of parts");
			}
			uuid = parts[0];
			version = Integer.parseInt(parts[1]);
			name = parts[2].replaceAll("@p#", "\\|");
			url = parts[3];
		}

		public ItemSelection()
		{
			// nothing
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public int getVersion()
		{
			return version;
		}

		public void setVersion(int version)
		{
			this.version = version;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		@Override
		public String toString()
		{
			StringBuilder bm = new StringBuilder();
			// the replaceAll here sucks
			bm.append(uuid).append("|").append(version).append("|").append(name.replaceAll("\\|", "@p#")).append("|")
				.append(url);
			return bm.toString();
			// return Base64.encode(bm.toString().getBytes());
		}
	}
}
