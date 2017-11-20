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

package com.tle.web.wizard.controls;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dytech.edge.common.Constants;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.EditBox;
import com.dytech.edge.wizard.beans.control.ShuffleList;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.tle.common.NameValue;
import com.tle.common.i18n.LangUtils;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.WizardPageException;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.sections.result.util.KeyLabel;

/**
 * Provides a data model for shuffle list controls.
 * 
 * @author Nicholas Read
 */
public class CShuffleList extends CMultiCtrl
{
	private static final long serialVersionUID = 1L;

	private final boolean checkDuplication;
	private final boolean forceUnique;

	public CShuffleList(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
		throws WizardPageException
	{
		super(page, controlNumber, nestingLevel, controlBean);

		ShuffleList sl = (ShuffleList) controlBean;
		checkDuplication = sl.isCheckDuplication();
		forceUnique = sl.isForceUnique();

		setSeparator(" - "); //$NON-NLS-1$

		EditBox editbox = new EditBox();
		editbox.getTargetnodes().add(new TargetNode(Constants.BLANK, Constants.BLANK));

		wizardPage.createCtrls(Collections.singletonList((WizardControl) editbox), -1, controls);
	}

	@Override
	public void validate()
	{
		if( checkDuplication || forceUnique )
		{
			final Set<String> values = new HashSet<String>();
			for( NameValue nv : namesValues )
			{
				// nv.value is URL encoded, but the name is ok to use
				values.add(nv.getName());
			}
			final ImmutableCollection<String> valuesReadonly = ImmutableSet.copyOf(values);

			// We need to inform the wizard to check for uniqueness every time,
			// no matter what
			final boolean isUnique = getRepository().checkDataUniqueness(getFirstTarget().getXoqlPath(),
				valuesReadonly, !forceUnique);

			setInvalid(forceUnique && !isUnique && !isInvalid(),
				new KeyLabel("wizard.controls.editbox.uniqueerror")); //$NON-NLS-1$
		}
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		final boolean tokenise = ((ShuffleList) getControlBean()).isTokenise();
		final Collection<String> vals = Lists.newArrayList(Lists.transform(namesValues,
			new Function<NameValue, String>()
			{
				@Override
				public String apply(NameValue nv)
				{
					try
					{
						return URLDecoder.decode(nv.getValue(), Constants.UTF8);
					}
					catch( UnsupportedEncodingException e )
					{
						throw new RuntimeException(e);
					}
				}
			}));
		return getDefaultPowerSearchQuery(vals, tokenise);
	}
}
