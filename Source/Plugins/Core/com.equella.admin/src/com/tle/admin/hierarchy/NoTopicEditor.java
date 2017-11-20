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

package com.tle.admin.hierarchy;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.AbstractPluginService;

/**
 * A mostly blank panel used to inidicate when no topic has been selected in the
 * hierarchy tool.
 * 
 * @author Nicholas Read
 * @created 4 June 2003
 */
@SuppressWarnings("nls")
public class NoTopicEditor extends JPanel
{
	private static final long serialVersionUID = 1L;

	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(getKey(key));
	}

	protected String getKey(String key)
	{
		return KEY_PFX+key;
	}

	public static NoTopicEditor noTopicSelected()
	{
		return new NoTopicEditor("noneselected");
	}

	public static NoTopicEditor notEditable()
	{
		return new NoTopicEditor("noteditable");
	}

	public NoTopicEditor(String message)
	{
		JLabel messageLabel = new JLabel(CurrentLocale.get(getKey("notopic.") + message));
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

		setLayout(new GridLayout(1, 1));
		add(messageLabel);
	}
}
