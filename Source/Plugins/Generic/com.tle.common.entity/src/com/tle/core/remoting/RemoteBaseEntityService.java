/*
 * Created on 18/04/2006
 */
package com.tle.core.remoting;

import com.tle.beans.entity.LanguageBundle;

public interface RemoteBaseEntityService
{
	LanguageBundle getNameForId(long id);
}
