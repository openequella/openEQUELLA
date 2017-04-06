package com.tle.core.remoterepo.z3950;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aholland
 */
public final class Z3950Constants
{
	private static final Map<Integer, Use> usageMap = new HashMap<Integer, Use>();
	static
	{
		for( Use use : Use.values() )
		{
			usageMap.put(use.value(), use);
		}
	}

	public static Use use(int use)
	{
		return usageMap.get(use);
	}

	public enum Operator
	{
		AND, OR, ANDNOT
	}

	public enum Relation
	{
		EQUAL(3);

		private int value;

		Relation(int value)
		{
			this.value = value;
		}

		public int value()
		{
			return value;
		}
	}

	public enum Use
	{
		ANY(1016), NAME_PERSONAL(1), NAME_CORPORATE(2), NAME_CONFERENCE(3), TITLE(4), TITLE_SERIES(5),
		TITLE_UNIFORM(6), TITLE_KEY(33), ISBN(7), ISSN(8), LCCN(9), LCCN2(16), DEWEY(13), SUBJECT(21), SUBJECT_LC(27),
		SUBJECT_PERSONAL(1009), PUBLISHED_DATE(31), PUBLISHER(51), GEOGRAPHIC_NAME(58), NOTE(63), DOCID(1032), AUTHOR(
			1003), STANDARD_IDENTIFIER(1007);

		private int value;

		Use(int value)
		{
			this.value = value;
		}

		public int value()
		{
			return value;
		}
	}

	public enum Accuracy
	{
		PRECISION("accuracy.precision", ".3.3.101.100.1"), PRECISION_RIGHT_TRUNCATION("accuracy.precision.right",
			".3.3.101.1.1"), KEYWORD("accuracy.keyword", ".3.3.2.100.1"), KEYWORD_RIGHT_TRUNCATION(
			"accuracy.keyword.right", ".3.3.2.1.1"), EXACT("accuracy.exact", ".3.1.1.100.3"), FIRST_WORD(
			"accuracy.first.word", ".3.1.1.100.1"), FIRST_CHARACTER("accuracy.first.char", ".3.1.1.1.1");

		private String langKey;
		private String attributes;

		Accuracy(String langKey, String attributes)
		{
			this.langKey = langKey;
			this.attributes = attributes;
		}

		public String getLangKey()
		{
			return langKey;
		}

		public String getAttributes()
		{
			return attributes;
		}
	}

	private Z3950Constants()
	{
		throw new Error();
	}
}
