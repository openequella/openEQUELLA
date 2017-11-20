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

package com.tle.web.shortcuturls;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.settings.standard.ShortcutUrls;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class AddShortcutUrlDialog extends AbstractOkayableDialog<AddShortcutUrlDialog.AddShortculUrlDialogModel>
{
	@PlugKey("shortcuts.dialog.title")
	private static Label LABEL_TITLE;

	@PlugKey("dialog.error.shortcut.overwrite")
	private static String ERROR_OVERWRITE_EXISTING_SHORTCUT;
	@PlugKey("dialog.error.url.overwrite")
	private static String ERROR_DUPLICATE_EXISTING_URL;
	@PlugKey("dialog.error.shortcut.blank")
	private static Label ERROR_SHORTCUT_BLANK;
	@PlugKey("dialog.error.url.blank")
	private static Label ERROR_URL_BLANK;
	@PlugKey("dialog.error.url.invalid")
	private static String ERROR_INVALID_URL;
	@PlugKey("shortcuts.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;

	private static String SHORTCUT_ERROR = "shortcut";
	private static String URL_ERROR = "url";

	@Component(stateful = false)
	private TextField shortcutText;

	@Component(stateful = false)
	private TextField urlText;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;

	private JSCallable reloadParent;

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "asd";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
		reloadParent = addParentCallable(new ReloadFunction(false));
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("addshortcut-dialog.ftl", this);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return events.getNamedHandler("addShortcutUrl"); //$NON-NLS-1$ 
	}

	@EventHandlerMethod
	public void addShortcutUrl(SectionInfo info)
	{
		AddShortculUrlDialogModel model = getModel(info);
		Map<String, Label> errorList = model.getErrors();
		errorList.clear();

		boolean emptyField = checkBlank(info, shortcutText, SHORTCUT_ERROR, ERROR_SHORTCUT_BLANK)
			|| checkBlank(info, urlText, URL_ERROR, ERROR_URL_BLANK);

		if( !emptyField )
		{
			ShortcutUrls configShortcutUrls = configService.getProperties(new ShortcutUrls());
			String newShortcut = shortcutText.getValue(info);
			String existingUrlForShortcut = configShortcutUrls.getShortcuts().get(newShortcut);
			if( existingUrlForShortcut != null )
			{
				model.addError(SHORTCUT_ERROR,
					new KeyLabel(ERROR_OVERWRITE_EXISTING_SHORTCUT, newShortcut, existingUrlForShortcut));
			}
			else
			{
				String newUrl = urlText.getValue(info);
				if( !isValidUrl(newUrl) )
				{
					model.addError(URL_ERROR, new KeyLabel(ERROR_INVALID_URL, newUrl));
				}
				else
				{
					boolean urlExists = false;
					String existingShortcutForUrl = null;
					for( String key : configShortcutUrls.getShortcuts().keySet() )
					{
						urlExists = configShortcutUrls.getShortcuts().get(key).equals(newUrl);
						if( urlExists )
						{
							existingShortcutForUrl = key;
							break;
						}
					}
					if( urlExists )
					{
						model.addError(URL_ERROR,
							new KeyLabel(ERROR_DUPLICATE_EXISTING_URL, newUrl, existingShortcutForUrl));
					}
					else
					{
						configShortcutUrls.getShortcuts().put(newShortcut, newUrl);
						configService.setProperties(configShortcutUrls);
						receiptService.setReceipt(SAVE_RECEIPT_LABEL);
						closeDialog(info, reloadParent);
					}
				}
			}
		}
	}

	private boolean checkBlank(SectionInfo info, TextField field, String key, Label error)
	{
		if( Check.isEmpty(field.getValue(info)) )
		{
			getModel(info).addError(key, error);
			return true;
		}
		return false;
	}

	public TextField getShortcutText()
	{
		return shortcutText;
	}

	public TextField getUrlText()
	{
		return urlText;
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public AddShortculUrlDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new AddShortculUrlDialogModel();
	}

	@Override
	public Class<AddShortculUrlDialogModel> getModelClass()
	{
		return AddShortculUrlDialogModel.class;
	}

	public class AddShortculUrlDialogModel extends DialogModel
	{
		private final Map<String, Label> errors = new HashMap<String, Label>();

		public void addError(String key, Label value)
		{
			this.errors.put(key, value);
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}
	}

	@SuppressWarnings("unused")
	private boolean isValidUrl(String targetUrl)
	{
		try
		{
			new URL(targetUrl);
			return true;
		}
		catch( MalformedURLException e )
		{
			return false;
		}
	}
}
