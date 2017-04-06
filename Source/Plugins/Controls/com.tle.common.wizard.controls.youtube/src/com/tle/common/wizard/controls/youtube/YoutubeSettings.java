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
