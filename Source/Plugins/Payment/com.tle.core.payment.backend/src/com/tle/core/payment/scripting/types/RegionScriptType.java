package com.tle.core.payment.scripting.types;

import java.util.List;

import com.tle.common.scripting.types.BaseEntityScriptType;

/**
 * @author Aaron
 */
public interface RegionScriptType extends BaseEntityScriptType
{
	List<String> listCountries();

	String[] getCountries();
}
