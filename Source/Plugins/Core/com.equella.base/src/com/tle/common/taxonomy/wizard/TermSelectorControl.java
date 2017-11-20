/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.taxonomy.wizard;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.taxonomy.SelectionRestriction;

public class TermSelectorControl extends CustomControl
{
	private static final long serialVersionUID = 1L;

	private static final String KEY_SELECTED_TAXONOMY = "KEY_SELECTED_TAXONOMY"; //$NON-NLS-1$
	private static final String KEY_ALLOW_MULTIPLE = "KEY_ALLOW_MULTIPLE"; //$NON-NLS-1$
	private static final String KEY_ALLOW_ADD_TERMS = "KEY_ALLOW_ADD_TERMS"; //$NON-NLS-1$
	private static final String KEY_TERM_STORAGE_FORMAT = "KEY_TERM_STORAGE_FORMAT"; //$NON-NLS-1$
	private static final String KEY_SELECTION_RESTRICTION = "KEY_SELECTION_RESTRICTION"; //$NON-NLS-1$
	private static final String KEY_DISPLAY_TYPE = "KEY_DISPLAY_TYPE"; //$NON-NLS-1$

	public enum TermStorageFormat
	{
		FULL_PATH, LEAF_ONLY;
	}

	public TermSelectorControl()
	{
		setClassType("termselector"); //$NON-NLS-1$
	}

	public TermSelectorControl(CustomControl cloned)
	{
		if( cloned != null )
		{
			cloned.cloneTo(this);
		}
	}

	public String getSelectedTaxonomy()
	{
		return (String) getAttributes().get(KEY_SELECTED_TAXONOMY);
	}

	public void setSelectedTaxonomy(String taxUuid)
	{
		getAttributes().put(KEY_SELECTED_TAXONOMY, taxUuid);
	}

	public boolean isAllowMultiple()
	{
		return getBooleanAttribute(KEY_ALLOW_MULTIPLE);
	}

	public void setAllowMultiple(boolean b)
	{
		getAttributes().put(KEY_ALLOW_MULTIPLE, b);
	}

	public boolean isAllowAddTerms()
	{
		return getBooleanAttribute(KEY_ALLOW_ADD_TERMS);
	}

	public void setAllowAddTerms(boolean b)
	{
		getAttributes().put(KEY_ALLOW_ADD_TERMS, b);
	}

	public TermStorageFormat getTermStorageFormat()
	{
		return get(KEY_TERM_STORAGE_FORMAT, TermStorageFormat.class, TermStorageFormat.LEAF_ONLY);
	}

	public void setTermStorageFormat(TermStorageFormat tsf)
	{
		getAttributes().put(KEY_TERM_STORAGE_FORMAT, tsf.toString());
	}

	public SelectionRestriction getSelectionRestriction()
	{
		return get(KEY_SELECTION_RESTRICTION, SelectionRestriction.class, SelectionRestriction.UNRESTRICTED);
	}

	public void setSelectionRestriction(SelectionRestriction sr)
	{
		getAttributes().put(KEY_SELECTION_RESTRICTION, sr.toString());
	}

	public String getDisplayType()
	{
		return (String) getAttributes().get(KEY_DISPLAY_TYPE);
	}

	public void setDisplayType(String displayType)
	{
		getAttributes().put(KEY_DISPLAY_TYPE, displayType);
	}
}
