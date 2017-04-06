package com.tle.web.payment.section.gateway;

import javax.inject.Inject;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.PaymentGatewayConstants;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@SuppressWarnings("nls")
public class ShowGatewaysSection
	extends
		AbstractShowEntitiesSection<PaymentGateway, AbstractShowEntitiesSection.AbstractShowEntitiesModel>
{
	@PlugKey("gateway.showlist.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("gateway.showlist.page.title")
	private static Label LABEL_GATEWAYS;
	@PlugKey("gateway.showlist.column.gateway")
	private static Label LABEL_COLUMN_GATEWAY;
	@PlugKey("gateway.showlist.column.type")
	private static Label LABEL_COLUMN_TYPE;
	@PlugKey("gateway.showlist.sandbox")
	private static Label LABEL_SANDBOX;
	@PlugKey("gateway.showlist.production")
	private static Label LABEL_PRODUCTION;
	@PlugKey("gateway.showlist.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("gateway.showlist.delete.confirm")
	private static Label LABEL_DELETE_CONFIRM;

	@Inject
	private PaymentGatewayService gatewayService;
	@Inject
	private TLEAclManager aclService;

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PaymentConstants.PRIV_CREATE_PAYMENT_GATEWAY).isEmpty();
	}

	@Override
	protected AbstractEntityService<PaymentGatewayEditingBean, PaymentGateway> getEntityService()
	{
		return gatewayService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_GATEWAYS;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_LINK_ADD;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_COLUMN_GATEWAY;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

	@Override
	protected void addDynamicColumnHeadings(SectionInfo info, TableHeaderRow header)
	{
		final TableHeaderCell headerCell = header.addCell(LABEL_COLUMN_TYPE);
		headerCell.setSort(Sort.SORTABLE_ASC);
	}

	@Override
	protected void addDynamicColumnData(SectionInfo info, PaymentGateway ent, SelectionsTableSelection row)
	{
		boolean sandbox = ent.getAttribute(PaymentGatewayConstants.SANDBOX_KEY, false);
		if( sandbox )
		{
			row.addColumn(LABEL_SANDBOX);
		}
		else
		{
			row.addColumn(LABEL_PRODUCTION);
		}
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, PaymentGateway connector)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected SectionRenderable createToggleEnabledLink(SectionInfo info, PaymentGateway ent)
	{
		boolean enabled = !ent.isDisabled();
		boolean canEnable = gatewayService.canEnable(ent.getGatewayType(), ent.getId());

		final HtmlLinkState actionLink = new HtmlLinkState(new OverrideHandler(toggleEnabledFunction, ent.getUuid()));
		actionLink.setDisabled(!canEnable && !enabled);
		final LinkRenderer link = new LinkRenderer(actionLink);
		link.setLabel(enabled ? getDisableLink() : getEnableLink());
		return link;
	}

	@Override
	protected boolean isInUse(SectionInfo info, PaymentGateway entity)
	{
		return false;
	}
}
