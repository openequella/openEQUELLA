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

package com.tle.web.scripting.advanced.contributors;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserService;
import com.tle.web.scripting.advanced.objects.DrmScriptObject;
import com.tle.web.scripting.advanced.objects.ImagesScriptObject;
import com.tle.web.scripting.advanced.objects.MimeScriptObject;
import com.tle.web.scripting.advanced.objects.NavigationScriptObject;
import com.tle.web.scripting.advanced.objects.RegionalScriptObject;
import com.tle.web.scripting.advanced.objects.impl.DrmScriptWrapper;
import com.tle.web.scripting.advanced.objects.impl.ImagesScriptWrapper;
import com.tle.web.scripting.advanced.objects.impl.MimeScriptWrapper;
import com.tle.web.scripting.advanced.objects.impl.NavigationScriptWrapper;
import com.tle.web.scripting.advanced.objects.impl.RegionalScriptWrapper;
import com.tle.web.viewurl.attachments.ItemNavigationService;

/**
 * @author aholland
 */
@Bind
@Singleton
public class AdvancedScriptObjectContributor implements ScriptObjectContributor
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ImageMagickService imageService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ItemNavigationService itemNavigationService;
	@Inject
	private UserService userService;

	@Override
	public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{
		// Really need to think about this one... I only included it to
		// demonstrate the awesomeness of the script control
		objects.put(ImagesScriptObject.DEFAULT_VARIABLE,
			new ImagesScriptWrapper(imageService, fileSystemService, params.getFileHandle()));

		// TODO: perhaps contributed by the mime plugin itself? Yes, I think so
		objects.put(MimeScriptObject.DEFAULT_VARIABLE, new MimeScriptWrapper(mimeService));
		objects.put(RegionalScriptObject.DEFAULT_VARIABLE, new RegionalScriptWrapper());

		final ItemPack<Item> pack = params.getItemPack();
		if( pack != null )
		{
			final Item item = pack.getItem();
			if( item != null )
			{
				objects.put(NavigationScriptObject.DEFAULT_VARIABLE,
					new NavigationScriptWrapper(itemNavigationService, item));

				// Script functionality expanded to provide for setting as well
				// as
				// getting DrmSetting values, so a DrmSettings object must not
				// be null
				DrmSettings drmSettings = item.getDrmSettings();
				if( drmSettings == null )
				{
					drmSettings = new DrmSettings();
				}
				objects.put(DrmScriptObject.DEFAULT_VARIABLE, new DrmScriptWrapper(userService, item, drmSettings));
			}
		}
	}
}
