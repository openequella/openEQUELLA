package com.dytech.edge.admin.script.ifmodel;

import com.dytech.edge.admin.script.model.Term;

public interface Comparison extends Term
{
	@Override
	String toScript();

	String toEasyRead();
}
