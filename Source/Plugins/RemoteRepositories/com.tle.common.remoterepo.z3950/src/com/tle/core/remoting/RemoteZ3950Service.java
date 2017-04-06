package com.tle.core.remoting;

import java.util.List;

import com.tle.beans.search.Z3950Settings.AttributeProfile;
import com.tle.common.NameValue;

public interface RemoteZ3950Service
{
	List<NameValue> listDefaultFields(AttributeProfile profile);
}
