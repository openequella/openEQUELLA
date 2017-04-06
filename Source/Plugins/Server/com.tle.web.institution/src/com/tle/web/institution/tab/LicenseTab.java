package com.tle.web.institution.tab;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.License;
import com.dytech.edge.exceptions.LicenseException;
import com.tle.common.Check;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.system.LicenseService;
import com.tle.core.system.SystemConfigService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class LicenseTab extends AbstractPrototypeSection<LicenseTab.LicenseModel> implements HtmlRenderer
{
	@Inject
	private LicenseService licenseService;
	@Inject
	private SystemConfigService systemConfigService;
	@Inject
	private ReceiptService receiptService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component
	private TextField licenseField;
	@Component
	@PlugKey(value = "institutions.license.save", global = true)
	private Button changeButton;

	public static class LicenseModel
	{
		private License license;
		private Label message;
		private Label reason;
		private Label error;

		public License getLicense()
		{
			return license;
		}

		public void setLicense(License license)
		{
			this.license = license;
		}

		public Label getMessage()
		{
			return message;
		}

		public void setMessage(Label message)
		{
			this.message = message;
		}

		public Label getReason()
		{
			return reason;
		}

		public void setReason(Label reason)
		{
			this.reason = reason;
		}

		public Label getError()
		{
			return error;
		}

		public void setError(Label error)
		{
			this.error = error;
		}

	}

	@Override
	public String getDefaultPropertyName()
	{
		return "license";
	}

	@Override
	public Class<LicenseModel> getModelClass()
	{
		return LicenseModel.class;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( systemConfigService.adminPasswordNotSet() )
		{
			return null;
		}

		LicenseModel model = getModel(context);
		License license = licenseService.getLicense();
		model.setLicense(license);
		if( model.getError() == null && license != null )
		{
			if( license.isExpired() )
			{
				model.setError(new KeyLabel("institutions.license.error"));
				model.setReason(new KeyLabel("institutions.license.expired"));
			}
			else if( ApplicationVersion.get().greaterVersionThan(license.getVersion()) )
			{
				model.setError(new KeyLabel("institutions.license.error"));
				model.setReason(new KeyLabel("institutions.license.wrongequellaversion"));
			}
		}
		return viewFactory.createResult("tab/license.ftl", context);
	}

	@EventHandlerMethod
	public void changeLicense(SectionInfo info)
	{
		LicenseModel model = getModel(info);
		String base64 = licenseField.getValue(info);
		if( !Check.isEmpty(base64) )
		{
			try
			{
				License license = new License(base64);
				licenseService.setLicense(license);
				receiptService.setReceipt(new KeyLabel("institutions.license.message"));
			}
			catch( LicenseException le )
			{
				model.setError(new KeyLabel("institutions.license.error"));
				model.setReason(new KeyLabel("institutions.license.incorrect"));
				info.preventGET();
			}
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		changeButton.setClickHandler(events.getNamedHandler("changeLicense"));
	}

	public TextField getLicenseField()
	{
		return licenseField;
	}

	public Button getChangeButton()
	{
		return changeButton;
	}
}
