package com.tle.beans.item;

public enum VersionSelection
{
	FORCE_LATEST, FORCE_CURRENT, DEFAULT_TO_LATEST, DEFAULT_TO_CURRENT,

	// The following value is deprecated as it's not really valid. For courses
	// that currently store this in the database, they should really be using
	// "null" instead to indicate that the course doesn't have a preference.
	@Deprecated
	INSTITUTION_DEFAULT;
}
