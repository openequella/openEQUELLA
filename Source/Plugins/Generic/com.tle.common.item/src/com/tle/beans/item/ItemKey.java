package com.tle.beans.item;

import java.io.Serializable;

public interface ItemKey extends Serializable
{
	String getUuid();

	int getVersion();

	@Override
	String toString();

	String toString(int version);

}
