package com.tle.web.controls.advancedscript.scripting;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.web.controls.advancedscript.scripting.objects.impl.RequestMapScriptWrapper;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.wizard.scripting.WizardScriptContextCreationParams;
import com.tle.web.wizard.scripting.WizardScriptObjectContributor;

/**
 * @author aholland
 */
@Bind
@Singleton
public class AdvancedScriptControlObjectContributor implements WizardScriptObjectContributor
{
	@Inject
	private UrlService urlService;
	@Inject
	private ViewableItemFactory viewableItemFactory;

	@SuppressWarnings("unchecked")
	@Override
	public void addWizardScriptObjects(Map<String, Object> objects, WizardScriptContextCreationParams params)
	{
		final Map<String, Object> attributes = params.getAttributes();
		final String prefix = (String) attributes.get(AdvancedScriptWebControlConstants.PREFIX);
		if( prefix != null )
		{
			objects.put(AdvancedScriptWebControlConstants.PREFIX, prefix);
			objects.put(AdvancedScriptWebControlConstants.SUBMIT_JS,
				attributes.get(AdvancedScriptWebControlConstants.SUBMIT_JS));
			objects.put(AdvancedScriptWebControlConstants.ATTRIBUTES,
				attributes.get(AdvancedScriptWebControlConstants.ATTRIBUTES));

			final String wizId = (String) attributes.get(AdvancedScriptWebControlConstants.WIZARD_ID);
			if( wizId != null )
			{
				objects.put(AdvancedScriptWebControlConstants.PREVIEW_URL_BASE,
					urlService.institutionalise(viewableItemFactory.getItemdirForPreview(wizId)));
			}

			final Map<Object, Object> requestMap = (Map<Object, Object>) attributes
				.get(AdvancedScriptWebControlConstants.REQUEST_MAP);
			if( requestMap != null )
			{
				objects.put(AdvancedScriptWebControlConstants.REQUEST_MAP, new RequestMapScriptWrapper(prefix,
					requestMap));
			}
		}
	}
}
