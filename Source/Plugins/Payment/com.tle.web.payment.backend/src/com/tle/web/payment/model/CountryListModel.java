package com.tle.web.payment.model;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;

public class CountryListModel extends DynamicHtmlListModel<Locale>
{
	private final List<Locale> countries;

	@SuppressWarnings("nls")
	public CountryListModel()
	{
		countries = new ArrayList<Locale>();
		String[] isoCountries = Locale.getISOCountries();
		for( String country : isoCountries )
		{
			countries.add(new Locale("", country));
		}
		setSort(true);
		setComparator(new CountryComparator());
	}

	@Override
	protected Iterable<Locale> populateModel(SectionInfo info)
	{
		return countries;
	}

	@Override
	protected Option<Locale> convertToOption(SectionInfo info, Locale locale)
	{
		return new SimpleOption<Locale>(locale.getDisplayCountry(CurrentLocale.getLocale()), locale.getCountry(),
			locale);
	}

	@SuppressWarnings("nls")
	@Override
	protected Option<Locale> getTopOption()
	{
		return new KeyOption<Locale>("com.tle.web.payment.backend.countrylist.pleaseselect", "", null);
	}

	private static class CountryComparator implements Comparator<Option<Locale>>, Serializable
	{
		@Override
		public int compare(Option<Locale> o1, Option<Locale> o2)
		{
			return Collator.getInstance(CurrentLocale.getLocale()).compare(o1.getName(), o2.getName());
		}
	}
}
