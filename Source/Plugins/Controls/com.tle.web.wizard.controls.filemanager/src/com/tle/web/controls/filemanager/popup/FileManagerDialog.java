package com.tle.web.controls.filemanager.popup;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.web.appletcommon.AppletWebCommon;
import com.tle.web.controls.filemanager.FileManagerWebControl;
import com.tle.web.filemanager.FileManagerConstants;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class FileManagerDialog extends EquellaDialog<DialogModel>
{
	@PlugKey("title.untitled")
	private static Label LABEL_FILE_MANAGER;

	@Inject
	private UrlService urlService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	private Div filemanagerDiv;

	private FileManagerWebControl fileManagerControl;

	public FileManagerDialog()
	{
		setAjax(true);
		// the filemanager sits in a dynamically generated tree so the
		// RuntimeStatements is not really needed, but this may not always be
		// the case.
		setDialogOpenedCallback(new FileManagerFunction());
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		final String title = fileManagerControl.getTitle();
		return Check.isEmpty(title) ? LABEL_FILE_MANAGER : new TextLabel(title);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("filemanager/filemanpopup.ftl", this);
	}

	public class FileManagerFunction extends RuntimeFunction
	{
		@Override
		protected JSCallable createFunction(RenderContext info)
		{
			final String jarUrl = urlService.institutionalise(FileManagerConstants.FILEMANAGER_APPLET_JAR_URL);

			final ObjectExpression options = new ObjectExpression();
			options.put(AppletWebCommon.PARAMETER_PREFIX + "WIZARD", fileManagerControl.getRepository().getWizid());
			options.put(AppletWebCommon.PARAMETER_PREFIX + "AUTOMARK", fileManagerControl.isAutoMarkAsResource());
			options.put(AppletWebCommon.PARAMETER_PREFIX + "BACKEND",
				"com.tle.web.filemanager.applet.backend.ServerBackendConnector");

			return CallAndReferenceFunction.get(Js.function(Js.call_s(AppletWebCommon.WRITE_APPLET, Jq
				.$(filemanagerDiv), jarUrl, "com.tle.web.filemanager.applet.AppletLauncher", CurrentLocale.getLocale()
				.toString(), CurrentLocale.isRightToLeft(), urlService.getInstitutionUrl().toString(), "534px", "100%",
				options, "fileManager")), FileManagerDialog.this);
		}
	}

	@Override
	public String getWidth()
	{
		return "75%";
	}

	@Override
	public String getHeight()
	{
		return "600px";
	}

	@Override
	public DialogModel instantiateDialogModel(SectionInfo info)
	{
		return new DialogModel();
	}

	public void setFileManagerControl(FileManagerWebControl fileManagerControl)
	{
		this.fileManagerControl = fileManagerControl;
	}

	public Div getFilemanagerDiv()
	{
		return filemanagerDiv;
	}
}
