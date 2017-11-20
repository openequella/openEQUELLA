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

package com.tle.integration.standard;

import org.springframework.beans.factory.annotation.Required;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

public class GenericIntegDownloadSection
	extends
		AbstractPrototypeSection<GenericIntegDownloadSection.GenericIntegDownloadModel> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	private String integrationType;
	private String downloadFile;

	@Required
	public void setIntegrationType(String integrationType)
	{
		this.integrationType = integrationType;
	}

	@Required
	public void setDownloadFile(String downloadFile)
	{
		this.downloadFile = downloadFile;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		GenericIntegDownloadModel model = getModel(context);
		model.setType(integrationType);
		model.setDownloadFile(downloadFile);

		return viewFactory.createResult("genericdownload.ftl", context); //$NON-NLS-1$
	}

	@Override
	public Class<GenericIntegDownloadModel> getModelClass()
	{
		return GenericIntegDownloadModel.class;
	}

	public static class GenericIntegDownloadModel
	{
		private String type;
		private String downloadFile;

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getDownloadFile()
		{
			return downloadFile;
		}

		public void setDownloadFile(String downloadFile)
		{
			this.downloadFile = downloadFile;
		}
	}
}
