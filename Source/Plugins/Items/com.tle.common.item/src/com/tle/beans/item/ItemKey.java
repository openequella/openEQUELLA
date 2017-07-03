package com.tle.beans.item;

import java.io.Serializable;

import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public interface ItemKey extends Serializable
{
	String getUuid();

	int getVersion();

	@Override
	String toString();

	String toString(int version);

}
