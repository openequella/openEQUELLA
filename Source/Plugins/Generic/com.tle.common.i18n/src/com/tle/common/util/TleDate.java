package com.tle.common.util;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author aholland
 */
@NonNullByDefault
public interface TleDate extends Serializable, Comparable<TleDate>
{
	Date toDate();

	long toLong();

	String format(Dates dateFormat);

	@Nullable
	String formatOrNull(Dates dateFormat);

	/**
	 * I'd argue strongly that there is no such thing. 'Conceptual' means that
	 * this date is just storing a day/month/year value and shouldn't be used as
	 * an actionable date (who's date is it??)
	 * 
	 * @return
	 */
	boolean isConceptual();

	// TODO: remove
	@Nullable
	String getConceptualValue();

	boolean before(TleDate otherDate);

	boolean after(TleDate otherDate);

	TleDate toMidnight();

	TimeZone getTimeZone();

	// TODO: change to ConceptualDate
	UtcDate conceptualDate();

	TleDate addDays(int days);
}
