package com.tle.core.settings.convert.extension;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.core.settings.service.ConfigurationService;

/**
 * @author Aaron
 *
 */
public abstract class ConfigurationConverterExtension<T extends ConfigurationProperties>
{
	@Inject
	protected ConfigurationService configurationService;

	public abstract T construct();

	public void run(Map<Long, Long> old2new)
	{
		T t = construct();
		t = configurationService.getProperties(t);
		clone(t, old2new);
		configurationService.setProperties(t);
	}

	public abstract void clone(T t, Map<Long, Long> old2new);
}
