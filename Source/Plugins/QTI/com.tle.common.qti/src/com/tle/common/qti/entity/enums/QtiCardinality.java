package com.tle.common.qti.entity.enums;

/**
 * @author Aaron
 */
public enum QtiCardinality
{
	SINGLE, MULTIPLE, ORDERED, RECORD;

	@Override
	public String toString()
	{
		return name().toLowerCase();
	}
}
