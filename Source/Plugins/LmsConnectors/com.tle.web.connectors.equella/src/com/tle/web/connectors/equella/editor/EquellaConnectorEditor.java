package com.tle.web.connectors.equella.editor;

import static com.tle.core.connectors.equella.EquellaConnectorConstants.CONNECTOR_TYPE;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.connectors.editor.AbstractConnectorEditorSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class EquellaConnectorEditor
	extends
		AbstractConnectorEditorSection<EquellaConnectorEditor.EquellaConnectorEditorModel>
{
	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	private UrlService urlService;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<ConnectorEditingBean, Connector> session)
	{
		return view.createResult("equellaconnector.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		getUsernameDiv().setDisplayed(false);
		getExportDiv().setDisplayed(false);
	}

	@Override
	protected String getAjaxDivId()
	{
		return "equellasetup";
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return null;
	}

	@Override
	protected Connector createNewConnector()
	{
		Connector connector = new Connector(CONNECTOR_TYPE);
		connector.setServerUrl(urlService.getInstitutionUrl().toString());
		return connector;
	}

	@Override
	protected void customValidate(SectionInfo info, ConnectorEditingBean connector, Map<String, Object> errors)
	{
		if( errors.containsKey("url") )
		{
			errors.remove("url");
		}
	}

	@Override
	protected void customLoad(SectionInfo info, ConnectorEditingBean connector)
	{

	}

	@Override
	protected void customSave(SectionInfo info, ConnectorEditingBean connector)
	{
		connector.setServerUrl(urlService.getInstitutionUrl().toString());
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new EquellaConnectorEditorModel();
	}

	public class EquellaConnectorEditorModel
		extends
			AbstractConnectorEditorSection<EquellaConnectorEditorModel>.AbstractConnectorEditorModel
	{

	}
}
