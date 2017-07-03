package com.tle.core.institution.convert.extension;

import com.tle.core.institution.convert.InstitutionImport;
import com.tle.core.institution.convert.InstitutionInfo;

/**
 * @author Aaron
 *
 */
public interface InstitutionInfoInitialiser
{
	void initialiseInstitutionInfo(InstitutionInfo institutionInfo, InstitutionImport imp);
}
