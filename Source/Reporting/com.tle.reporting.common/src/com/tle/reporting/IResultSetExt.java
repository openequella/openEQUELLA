package com.tle.reporting;

import org.eclipse.datatools.connectivity.oda.IResultSet;

public interface IResultSetExt extends IResultSet
{
	Object getObject(int col);
}
