package com.tle.common.taxonomy;

@SuppressWarnings("nls")
public final class TaxonomyConstants
{
	public static final String TERM_SEPARATOR = "\\";
	public static final String TERM_SEPARATOR_REGEX = "\\\\";

	public static final String TERM_ALLOW_ADDITION = "TERM_ALLOW_ADDITION";

	public static final String INTERNAL_DATASOURCE = "internalTaxonomyDataSource";

	public static final String PRE41_TAXONOMY_EXPORT_FOLDER = "taxonomy";
	public static final String TAXONOMY_EXPORT_FOLDER = "taxonomy2";
	public static final String SINGLE_TAXONOMIES_FILE = "taxonomies.xml";

	public static final String TERMS_EXPORT_FOLDER = "terms";

	private TaxonomyConstants()
	{
		throw new Error();
	}
}
