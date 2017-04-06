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
