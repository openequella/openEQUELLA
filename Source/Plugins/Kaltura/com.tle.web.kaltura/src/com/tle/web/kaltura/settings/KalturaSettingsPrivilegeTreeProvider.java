package com.tle.web.kaltura.settings;

import javax.inject.Inject;

import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class KalturaSettingsPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<KalturaServer>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(KalturaSettingsPrivilegeTreeProvider.class);

	@Inject
	protected KalturaSettingsPrivilegeTreeProvider(KalturaService kalturaService)
	{
		super(kalturaService, Node.ALL_KALTURAS, resources.key("securitytree.kalturaservers"), Node.KALTURA, resources
			.key("securitytree.targetallkalturaservers"));
	}

	@Override
	protected KalturaServer createEntity()
	{
		return new KalturaServer();
	}
}
