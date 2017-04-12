package com.tle.web.contribute;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.wizard.WizardInfo;
import com.tle.web.wizard.WizardService;

@Bind
@SuppressWarnings("nls")
public class DiscardSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@PlugKey("discard.confirm")
	private static Confirm DISCARD_CONFIRM;

	@EventFactory
	private EventGenerator events;

	@Inject
	private WizardService wizardService;

	@Inject
	private ItemService itemService;

	@Component
	@PlugKey("discard")
	private Button discard;

	private SubmitValuesFunction discardHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		discardHandler = events.getSubmitValuesFunction("discard");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		Item item = itemInfo.getItem();
		List<WizardInfo> resumableWizards = wizardService.listWizardsInSession();
		if( resumableWizards.size() > 0 )
		{
			for( WizardInfo wizInfo : resumableWizards )
			{
				if( wizInfo.getItemUuid().equals(item.getUuid()) && wizInfo.getItemVersion() == item.getVersion() )
				{
					discard.setClickHandler(context,
						new OverrideHandler(discardHandler, wizInfo.getUuid()).addValidator(DISCARD_CONFIRM));
					return renderSection(context, discard);
				}
			}
		}
		return null;
	}

	@EventHandlerMethod
	public void discard(SectionInfo info, String wizardUuid)
	{
		wizardService.removeFromSession(info, wizardUuid, true);
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		itemService.forceUnlock(itemInfo.getItem());
		itemInfo.refreshItem(true);
	}

}
