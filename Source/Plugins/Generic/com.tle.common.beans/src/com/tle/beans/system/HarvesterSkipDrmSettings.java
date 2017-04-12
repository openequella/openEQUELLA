/**
 * 
 */
package com.tle.beans.system;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

/**
 * @author larry
 */
public class HarvesterSkipDrmSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = -6491584723123150379L;

	@Property(key = "harvester.skipdrm")
	private boolean harvestingSkipDrm;

	public boolean isHarvestingSkipDrm()
	{
		return harvestingSkipDrm;
	}

	public void setHarvestingSkipDrm(boolean harvestingSkipDrm)
	{
		this.harvestingSkipDrm = harvestingSkipDrm;
	}
}
