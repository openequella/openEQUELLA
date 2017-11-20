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

package com.tle.web.search.actions;

import java.util.Date;

import javax.inject.Inject;

import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.service.FavouriteSearchService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public abstract class AbstractFavouriteSearchSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@PlugKey("actions.favourite.dialog.add")
	private static Label LABEL_ADD;
	@PlugKey("actions.favourite.dialog.validate")
	private static Label LABEL_VALIDATE;
	@PlugKey("actions.favourite.confirm")
	private static Label LABEL_RECEIPT;

	@Inject
	private FavouriteSearchService favouriteSearchService;
	@Inject
	private ReceiptService receiptService;

	@TreeLookup
	private AbstractQuerySection<?, ?> querySection;
	@TreeLookup
	private AbstractRootSearchSection<?> rootSearchSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component(name = "nf")
	private TextField nameField;
	@Component
	@PlugKey("actions.favourite.button.name")
	private Button addButton;
	private JSHandler addHandler;

	@Nullable
	private AbstractFavouriteSearchDialog containerDialog;

	protected abstract String getWithin(SectionInfo info);

	protected abstract String getCriteria(SectionInfo info);

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("actions/dialog/favouritesearch.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		JSStatements alert = Js.alert_s(LABEL_VALIDATE);
		addHandler = events.getNamedHandler("addToFavourites")
			.addValidator(nameField.createNotBlankValidator().setFailureStatements(alert));
		addButton.setClickHandler(addHandler);
		tree.setLayout(id, AbstractSearchActionsSection.AREA_SAVE);
	}

	public void setContainerDialog(AbstractFavouriteSearchDialog containerDialog)
	{
		this.containerDialog = containerDialog;
	}

	@EventHandlerMethod
	public void addToFavourites(SectionInfo info)
	{
		if( CurrentUser.isGuest() || CurrentUser.wasAutoLoggedIn() )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.web.search.savenotavailable"));
		}

		final String name = nameField.getValue(info);
		if( !Check.isEmpty(name) )
		{
			final FavouriteSearch fs = new FavouriteSearch();
			fs.setName(name);
			fs.setDateModified(new Date());
			fs.setOwner(CurrentUser.getUserID());
			final InfoBookmark bookmark = rootSearchSection.getPermanentUrl(info);
			final String url = String.format("%s?%s", info.getAttribute(SectionInfo.KEY_PATH), bookmark.getQuery());
			fs.setUrl(url);
			fs.setWithin(getWithin(info));
			fs.setInstitution(CurrentInstitution.get());
			fs.setCriteria(getCriteria(info));
			fs.setQuery(querySection.getQueryField().getValue(info));

			favouriteSearchService.save(fs);

			receiptService.setReceipt(LABEL_RECEIPT);

			if( containerDialog != null )
			{
				containerDialog.close(info);
			}
		}
	}

	public JSHandler getAddHandler()
	{
		return addHandler;
	}

	public TextField getNameField()
	{
		return nameField;
	}

	public Button getAddButton()
	{
		if( containerDialog != null )
		{
			return null;
		}
		return addButton;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "afss";
	}
}
