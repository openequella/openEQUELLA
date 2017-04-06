package com.tle.web.institution;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.institution.InstitutionStatus.InvalidReason;
import com.tle.core.user.CurrentInstitution;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;

@SuppressWarnings("nls")
@Bind
public class UnlicensedPage extends AbstractPrototypeSection<UnlicensedPage.Model> implements HtmlRenderer
{
	@PlugKey("unlicensed.title")
	private static Label LABEL_TITLE;
	@PlugKey("unlicensed.reason.")
	private static String KEY_REASON;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private InstitutionService institutionService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		InstitutionStatus status = institutionService.getInstitutionStatus(CurrentInstitution.get().getUniqueId());
		InvalidReason reason = status.getInvalidReason();
		getModel(context).setReason(new KeyLabel(KEY_REASON + reason.name().toLowerCase()));
		Decorations decorations = Decorations.getDecorations(context);
		decorations.setMenuMode(MenuMode.HIDDEN);
		decorations.setTitle(LABEL_TITLE);
		return viewFactory.createResult("unlicensed.ftl", this);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private Label reason;

		public Label getReason()
		{
			return reason;
		}

		public void setReason(Label reason)
		{
			this.reason = reason;
		}
	}
}
