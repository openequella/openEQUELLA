package com.tle.web.payment.section.tax;

import javax.inject.Inject;

import com.tle.common.payment.entity.TaxType;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.TaxService;
import com.tle.core.payment.service.session.TaxTypeEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.section.tax.TaxEditorSection.TaxEditorModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.NumberField;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author Aaron
 */
@Bind
public class TaxEditorSection extends AbstractEntityEditor<TaxTypeEditingBean, TaxType, TaxEditorModel>
{
	@Inject
	private TaxService taxService;

	@Component(name = "cd", stateful = false)
	private TextField code;
	@Component(name = "pc", stateful = false)
	private NumberField percent;

	@ViewFactory
	private FreemarkerFactory view;

	@SuppressWarnings("nls")
	@Override
	public SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<TaxTypeEditingBean, TaxType> session)
	{
		return view.createResult("tax/edittax.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		percent.setMin(0.0);
		percent.setMax(1000.0);
		percent.setDefaultNumber(0.0);
		percent.setAnyStep(true);
		percent.setIntegersOnly(false);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	protected AbstractEntityService<TaxTypeEditingBean, TaxType> getEntityService()
	{
		return taxService;
	}

	@Override
	protected TaxType createNewEntity(SectionInfo info)
	{
		return new TaxType();
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<TaxTypeEditingBean, TaxType> session)
	{
		final TaxTypeEditingBean tax = session.getBean();
		code.setValue(info, tax.getCode());
		percent.setStringValue(info, tax.getPercent());
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<TaxTypeEditingBean, TaxType> session,
		boolean validate)
	{
		final TaxTypeEditingBean tax = session.getBean();
		tax.setCode(code.getStringValue(info));
		tax.setPercent(percent.getStringValue(info));
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new TaxEditorModel();
	}

	public TextField getCode()
	{
		return code;
	}

	public NumberField getPercent()
	{
		return percent;
	}

	public class TaxEditorModel
		extends
			AbstractEntityEditor<TaxTypeEditingBean, TaxType, TaxEditorModel>.AbstractEntityEditorModel
	{
		// Nothing
	}
}
