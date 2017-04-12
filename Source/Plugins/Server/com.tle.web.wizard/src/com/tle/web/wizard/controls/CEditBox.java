package com.tle.web.wizard.controls;

import com.dytech.edge.common.Constants;
import com.dytech.edge.wizard.beans.control.EditBox;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides a data model for edit box controls.
 * 
 * @author Nicholas Read
 */
public class CEditBox extends EditableCtrl
{
	private static final long serialVersionUID = 1L;

	private String defaultValue = Constants.BLANK;
	private final boolean checkDuplication;
	private final boolean forceUnique;
	private final boolean forceNumber;

	public CEditBox(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		WizardControlItem item = controlBean.getItem(0);
		if( item != null )
		{
			defaultValue = item.getValue();
		}
		EditBox edbox = (EditBox) controlBean;
		checkDuplication = edbox.isCheckDuplication();
		forceUnique = edbox.isForceUnique();
		forceNumber = edbox.isNumber();
	}

	@Override
	public void resetToDefaults()
	{
		if( Check.isEmpty(defaultValue) )
		{
			setValues();
		}
		else
		{
			setValues(evalString(defaultValue));
		}
	}

	@Override
	public void validate()
	{
		if( checkDuplication || forceUnique )
		{
			// We need to inform the wizard to check for uniqueness every time,
			// no matter what
			final boolean isUnique = getRepository().checkDataUniqueness(getFirstTarget().getXoqlPath(), getValues(),
				!forceUnique);

			setInvalid(forceUnique && !isUnique && !isInvalid(),
				LangUtils.createTempLangugageBundle("wizard.controls.editbox.uniqueerror")); //$NON-NLS-1$
		}

		if( forceNumber && !isInvalid() )
		{
			String val = getValue();
			boolean invalid = false;
			try
			{
				if( !Check.isEmpty(val) )
				{
					Integer.parseInt(val);
				}
			}
			catch( NumberFormatException e )
			{
				invalid = true;
			}
			setInvalid(invalid, LangUtils.createTempLangugageBundle("wizard.controls.editbox.integererror")); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isEmpty()
	{
		return getValue().length() == 0;
	}
}
