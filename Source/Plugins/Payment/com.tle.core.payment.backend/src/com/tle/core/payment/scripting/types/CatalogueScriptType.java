package com.tle.core.payment.scripting.types;

import java.util.List;

import com.tle.common.scripting.types.BaseEntityScriptType;

/**
 * @author larry
 */
public interface CatalogueScriptType extends BaseEntityScriptType
{
	/**
	 * @return Is the catalogue enabled?
	 */
	boolean isEnabled();

	/**
	 * @return Is the catalogue restricted to certain regions?
	 */
	boolean isRegionRestricted();

	/**
	 * @return Get all the regions this catalogue is restricted to
	 */
	List<RegionScriptType> listRegions();

	/**
	 * @return Get all the regions this catalogue is restricted to
	 */
	RegionScriptType[] getRegions();
}
