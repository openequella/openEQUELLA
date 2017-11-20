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

package com.tle.common.wizard.controls.youtube;

import java.util.List;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.Pair;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author Peng
 */
@SuppressWarnings("nls")
public class YoutubeSettings extends UniversalSettings
{
	private static final String KEY_ALLOW_CHANNEL = "AllowChannel";
	private static final String KEY_OPTION_ALLOW_CHANNEL = "OptionAllowChannel";
	private static final String KEY_OPTION_RESTRICT_CHANNEL = "OptionRestrictChannel";
	public static final String KEY_CHANNELS = "YoutubeChannels";

	public YoutubeSettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public YoutubeSettings(UniversalSettings settings)
	{
		super(settings.getWrapped());
	}

	public boolean isAllowChannelSelection()
	{
		return wrapped.getBooleanAttribute(KEY_ALLOW_CHANNEL);
	}

	public void setAllowChannelSelection(boolean allowChannel)
	{
		wrapped.getAttributes().put(KEY_ALLOW_CHANNEL, allowChannel);
	}

	public boolean isOptionAllowChannelSelection()
	{
		return wrapped.getBooleanAttribute(KEY_OPTION_ALLOW_CHANNEL);
	}

	public void setOptionAllowChannelSelection(boolean optionAllowChannel)
	{
		wrapped.getAttributes().put(KEY_OPTION_ALLOW_CHANNEL, optionAllowChannel);
	}

	public boolean isOptionRestrictChannelSelection()
	{
		return wrapped.getBooleanAttribute(KEY_OPTION_RESTRICT_CHANNEL);
	}

	public void setOptionRestrictChannelSelection(boolean restrictChannel)
	{
		wrapped.getAttributes().put(KEY_OPTION_RESTRICT_CHANNEL, restrictChannel);
	}

	public void setChannels(List<Pair<String, String>> channels)
	{
		wrapped.getAttributes().put(KEY_CHANNELS, channels);
	}

	@SuppressWarnings("unchecked")
	public List<Pair<String, String>> getChannels()
	{
		return (List<Pair<String, String>>) wrapped.getAttributes().get(KEY_CHANNELS);
	}

}
