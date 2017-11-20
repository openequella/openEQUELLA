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

package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;

public class SummarySectionsConfig implements Serializable
{
	private static final long serialVersionUID = 1;

	private String uuid;
	private final String value;
	private LanguageBundle bundleTitle;
	private String configuration;

	@XStreamOmitField
	@SuppressWarnings("unused")
	private String title;

	@XStreamOmitField
	@SuppressWarnings("unused")
	private String name;

	public SummarySectionsConfig(String value)
	{
		this.value = value;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getValue()
	{
		return value;
	}

	public void setConfiguration(String configuration)
	{
		this.configuration = configuration;
	}

	public String getConfiguration()
	{
		return configuration;
	}

	@SuppressWarnings("nls")
	public static List<SummarySectionsConfig> createDefaultConfigs()
	{
		List<SummarySectionsConfig> rv = new ArrayList<SummarySectionsConfig>();

		// English strings are OK here - titles are not I18Nable
		// add bundleTitle with default values.

		SummarySectionsConfig basic = new SummarySectionsConfig("basicSection");
		basic.setTitle("Basic Information");
		LanguageBundle basicTitle = new LanguageBundle();
		LangUtils.setString(basicTitle, CurrentLocale.getLocale(), "Basic Information");
		basic.setBundleTitle(basicTitle);
		basic.setUuid(UUID.randomUUID().toString());
		rv.add(basic);

		SummarySectionsConfig attachments = new SummarySectionsConfig("attachmentsSection");
		attachments.setTitle("Attachments");
		LanguageBundle attachmentTitle = new LanguageBundle();
		LangUtils.setString(attachmentTitle, CurrentLocale.getLocale(), "Links to resources");
		attachments.setBundleTitle(attachmentTitle);
		attachments.setUuid(UUID.randomUUID().toString());

		rv.add(attachments);

		SummarySectionsConfig comments = new SummarySectionsConfig("commentsSection");
		comments.setTitle("Comments");
		LanguageBundle commentTitle = new LanguageBundle();
		LangUtils.setString(commentTitle, CurrentLocale.getLocale(), "Add a comment");
		comments.setBundleTitle(commentTitle);
		comments.setUuid(UUID.randomUUID().toString());
		rv.add(comments);

		return rv;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public LanguageBundle getBundleTitle()
	{
		return bundleTitle;
	}

	public void setBundleTitle(LanguageBundle bundleTitle)
	{
		this.bundleTitle = bundleTitle;
	}
}
