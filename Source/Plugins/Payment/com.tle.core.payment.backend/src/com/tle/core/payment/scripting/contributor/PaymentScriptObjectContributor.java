package com.tle.core.payment.scripting.contributor;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.core.guice.Bind;
import com.tle.core.payment.scripting.objects.CatalogueScriptObject;
import com.tle.core.payment.scripting.objects.PricingTierScriptObject;
import com.tle.core.payment.scripting.objects.impl.CatalogueScriptWrapper;
import com.tle.core.payment.scripting.objects.impl.PricingTierScriptWrapper;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.ItemService;
import com.tle.web.wizard.scripting.WizardScriptContextCreationParams;
import com.tle.web.wizard.scripting.WizardScriptObjectContributor;

// TODO: this probably adds the script object twice for wizard situations.
// It might be random which one you get depending on which one was added last...
@Bind
@Singleton
public class PaymentScriptObjectContributor implements WizardScriptObjectContributor, ScriptObjectContributor
{
	@Inject
	private ItemService itemService;
	@Inject
	private PricingTierService tierService;
	@Inject
	private CatalogueService catalogueService;
	@Inject
	private ConfigurationService configService;

	@Override
	public void addWizardScriptObjects(Map<String, Object> objects, WizardScriptContextCreationParams params)
	{
		final ItemPack<Item> itemPack = params.getItemPack();
		if( itemPack != null )
		{
			final Item item = itemPack.getItem();

			objects.put(PricingTierScriptObject.DEFAULT_VARIABLE, new PricingTierScriptWrapper(tierService,
				itemService, configService, item, params.getWizardState()));
			objects.put(CatalogueScriptObject.DEFAULT_VARIABLE, new CatalogueScriptWrapper(catalogueService, item));
		}
	}

	@Override
	public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{
		final ItemPack<Item> pack = params.getItemPack();
		final Item item;
		if( pack != null )
		{
			item = pack.getItem();
		}
		else
		{
			item = null;
		}
		PricingTierScriptWrapper tierScriptWrapper = new PricingTierScriptWrapper(tierService, itemService,
			configService, item, null);
		CatalogueScriptWrapper catalogueScriptWrapper = new CatalogueScriptWrapper(catalogueService, item);

		objects.put(PricingTierScriptObject.DEFAULT_VARIABLE, tierScriptWrapper);
		objects.put(CatalogueScriptObject.DEFAULT_VARIABLE, catalogueScriptWrapper);
	}
}
