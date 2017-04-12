package com.tle.web.payment.section.tax;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_TAX;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.payment.service.TaxService;
import com.tle.core.payment.service.session.TaxTypeEditingBean;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.NumberLabel;

/**
 * @author Aaron
 */
public class ShowTaxesSection
	extends
		AbstractShowEntitiesSection<TaxType, AbstractShowEntitiesSection.AbstractShowEntitiesModel>
{
	@PlugKey("tax.showlist.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("tax.showlist.title")
	private static Label LABEL_TITLE;
	@PlugKey("tax.showlist.column.title")
	private static Label LABEL_TAX;
	@PlugKey("tax.showlist.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("tax.showlist.delete.confirm")
	private static Label LABEL_DELETE_CONFIRM;
	@PlugKey("tax.showlist.inuse")
	private static Label LABEL_IN_USE;
	@PlugKey("tax.showlist.column.code")
	private static Label LABEL_COLUMN_CODE;
	@PlugKey("tax.showlist.column.percent")
	private static Label LABEL_COLUMN_PERCENT;

	@Inject
	private TaxService taxService;
	@Inject
	private TLEAclManager aclService;

	@Override
	protected AbstractEntityService<TaxTypeEditingBean, TaxType> getEntityService()
	{
		return taxService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_LINK_ADD;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_TAX;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, TaxType entity)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_CREATE_TAX).isEmpty();
	}

	@Override
	protected boolean canDisable(SectionInfo info, TaxType ent)
	{
		return false;
	}

	@Override
	protected boolean canDelete(SectionInfo info, TaxType ent)
	{
		return getEntityService().canDelete(ent);
	}

	@Override
	protected boolean isInUse(SectionInfo info, TaxType entity)
	{
		final List<Class<?>> references = taxService.getReferencingClasses(entity.getId());
		return !references.isEmpty();
	}

	@Override
	protected List<Object> getColumnHeadings()
	{
		return Lists.newArrayList((Object) getEntityColumnLabel(), (Object) LABEL_COLUMN_CODE,
			(Object) LABEL_COLUMN_PERCENT);
	}

	@Override
	protected void addDynamicColumnData(SectionInfo info, TaxType ent, SelectionsTableSelection row)
	{
		row.addColumn(ent.getCode());
		row.addColumn(new NumberLabel(ent.getPercent(), 2, 4));
	}

	@Override
	protected Label getInUseLabel(SectionInfo info, TaxType entity)
	{
		return LABEL_IN_USE;
	}
}
