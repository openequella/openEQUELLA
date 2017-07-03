package com.tle.core.i18n;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.NameValue;
import com.tle.common.i18n.BundleReference;

public class BundleNameValue extends NameValue
{
	private static final long serialVersionUID = 1L;
	private BundleCache cache;
	private Object name;

	public BundleNameValue(String name, String value)
	{
		this(name, value, null);
	}

	public BundleNameValue(Object name, String value, BundleCache cache)
	{
		super("", value); //$NON-NLS-1$
		this.name = name;
		this.cache = cache;
		if( cache == null && !(name instanceof String) )
		{
			throw new RuntimeException("Must be a key value when cache is null"); //$NON-NLS-1$
		}
		if( cache != null )
		{
			if( name instanceof LanguageBundle )
			{
				cache.addBundle(name);
			}
			else if( name instanceof Long )
			{
				cache.addBundleId((Long) name);
			}
		}
	}

	@Override
	public String getName()
	{
		return TextBundle.getLocalString(name, cache, null, ""); //$NON-NLS-1$
	}

	@Override
	public String getValue()
	{
		return getSecond();
	}

	@Override
	public String getLabel()
	{
		return TextBundle.getLocalString(name, cache, null, ""); //$NON-NLS-1$
	}

	public static List<BundleNameValue> convertList(List<? extends BundleReference> refList, BundleCache cache)
	{
		List<BundleNameValue> namesValues = new ArrayList<BundleNameValue>(refList.size());
		for( BundleReference reference : refList )
		{
			BundleNameValue nv = new BundleNameValue(reference.getBundleId(), reference.getValue(), cache);
			namesValues.add(nv);
		}
		return namesValues;
	}

	public static List<BundleNameValue> convertListUuid(List<BaseEntityLabel> refList, BundleCache cache)
	{
		List<BundleNameValue> namesValues = new ArrayList<BundleNameValue>(refList.size());
		for( BaseEntityLabel reference : refList )
		{
			BundleNameValue nv = new BundleNameValue(reference.getBundleId(), reference.getUuid(), cache);
			namesValues.add(nv);
		}
		return namesValues;
	}
}
