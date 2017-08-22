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

package com.tle.web.wizard;

import java.io.InputStream;
import java.util.*;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.FileNode;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.google.common.collect.ImmutableCollection;
import com.tle.beans.Language;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.wizard.scripting.WizardScriptConstants;
import com.tle.web.wizard.scripting.objects.impl.ControlScriptWrapper;
import com.tle.web.wizard.scripting.objects.impl.PageScriptWrapper;

@Bind
public class SimpleLERepository implements LERepository
{
	@Inject
	private LanguageService languageService;
	@Inject
	private WizardService wizardService;

	private boolean expert = true;
	private ItemPack<Item> itemPack;
	private Stack<Pair<String, Integer>> pathOverrides;

	@Override
	public FileInfo uploadStream(String szFileName, InputStream inp, boolean calcMd5)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String szFileName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream read(String szFileName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void copy(String filename, String targetFilename)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public PropBagEx getItemBag()
	{
		return itemPack.getXml();
	}

	@Override
	public String getWizardName()
	{
		return "Wizard";
	}

	@Override
	public boolean isEditable()
	{
		return true;
	}

	@Override
	public boolean isExpert()
	{
		return expert;
	}

	@Override
	public String getWebUrl()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUserUUID()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public FileNode getFileTree(String path)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConvertibleToHtml(String filename)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void spawnBrowser(String link)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getWizid()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getStagingid()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkLinkAttachmentUniqueness(String[] urls)
	{
		throw new UnsupportedOperationException();
	}

	public void setExpert(boolean expert)
	{
		this.expert = expert;
	}

	public String getAttachURL(String path)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public FileInfo unzipFile(String zipfile, String packagefile, boolean ignoreZipError)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ModifiableAttachments getAttachments()
	{
		throw new UnsupportedOperationException();
	}

	public NameValue[] checkUnique(String node, String value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ScriptContext getScriptContext(WizardPage page, HTMLControl control, Map<String, Object> attributes)
	{
		ScriptContext context = wizardService.createScriptContext(itemPack, page, control, attributes);
		// set the page and control
		context.addScriptObject(WizardScriptConstants.PAGE, new PageScriptWrapper(page));
		context.addScriptObject(WizardScriptConstants.CONTROL, new ControlScriptWrapper(control, page));

		PropBagWrapper xml = context.getXml();
		xml.clearOverrides();
		for( Pair<String, Integer> override : getPathOverrides() )
		{
			xml.pushOverride(override.getFirst(), override.getSecond());
		}

		return context;
	}

	@Override
	public Item getItem()
	{
		return itemPack.getItem();
	}

	@Override
	public Locale getLocale()
	{
		return CurrentLocale.getLocale();
	}

	@Override
	public List<Language> getLanguages()
	{
		return languageService.getLanguages();
	}

	public void setDocumentXml(PropBagEx documentXml)
	{
		itemPack = new ItemPack<>();
		itemPack.setXml(documentXml);
		// itemPack.setItem(new Item());
	}

	@Override
	public boolean checkDataUniqueness(String xpath, ImmutableCollection<String> list, boolean canAccept)
	{
		return true;
	}

	@Override
	public boolean fileExists(String file)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateMetadataMapping()
	{
		// nothing
	}

	private Stack<Pair<String, Integer>> getPathOverrides()
	{
		if( pathOverrides == null )
		{
			pathOverrides = new Stack<Pair<String, Integer>>();
		}
		return pathOverrides;
	}

	@Override
	public void pushPathOverride(String path, int index)
	{
		getPathOverrides().push(new Pair<String, Integer>(path, index));
	}

	@Override
	public void popPathOverride()
	{
		getPathOverrides().pop();
	}

	@Override
	public Object getThreadLock()
	{
		return new Object();
	}

	@Override
	public boolean registerFilename(UUID id, String filename)
	{
		return true;
	}

	@Override
	public void unregisterFilename(UUID id)
	{

	}
}
