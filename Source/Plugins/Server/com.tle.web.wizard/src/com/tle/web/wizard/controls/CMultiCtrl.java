package com.tle.web.wizard.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.Multi;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.NameValue;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.WizardPageException;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides a data model for 'multi' controls.
 * 
 * @author Nicholas Read
 */
public class CMultiCtrl extends MultipleCtrl
{
	private static final long serialVersionUID = 1L;

	protected List<NameValue> namesValues = new ArrayList<NameValue>();
	private String separator = " / "; //$NON-NLS-1$

	public CMultiCtrl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
		throws WizardPageException
	{
		super(page, controlNumber, nestingLevel, controlBean);

		if( getSize2() == 0 )
		{
			setSize2(450);
		}

		Multi multi = (Multi) controlBean;
		wizardPage.createCtrls(multi.getControls(), -1, controls);
	}

	@Override
	public void doEvaluate()
	{
		super.doEvaluate();
		for( HTMLControl control : controls )
		{
			control.setDontShowEmpty(true);
		}
	}

	@Override
	public void loadFromDocument(PropBagEx docPropBag)
	{
		TargetNode firstTarget = getFirstTarget();

		StringBuilder sbufName = new StringBuilder();
		StringBuilder sbufValue = new StringBuilder();

		namesValues.clear();
		Iterator<PropBagEx> iter = docPropBag.iterator(firstTarget.getTarget());
		while( iter.hasNext() )
		{
			sbufName.setLength(0);
			sbufValue.setLength(0);
			PropBagEx targBag = iter.next();

			boolean first = true;
			for( HTMLControl control : controls )
			{
				control.loadFromDocument(targBag);

				NameValue nameValue = control.getNameValue();
				if( !first )
				{
					sbufName.append(separator);
					sbufValue.append('|');
				}
				else
				{
					first = false;
				}
				sbufName.append(nameValue.getName());
				sbufValue.append(urlEncode(nameValue.getValue()));
				if( !iter.hasNext() )
				{
					control.resetToDefaults();
				}
			}
			String name = sbufName.toString();
			String val = sbufValue.toString();
			namesValues.add(new NameValue(name, val));
		}
	}

	@Override
	public boolean isEmpty()
	{
		return namesValues.isEmpty();
	}

	@Override
	public void setValues(String... values)
	{
		namesValues.clear();
		if( values != null )
		{
			StringBuilder sbufName = new StringBuilder();
			StringBuilder sbufValue = new StringBuilder();
			for( String element : values )
			{
				sbufName.setLength(0);
				sbufValue.setLength(0);
				boolean first = true;
				String[] colvals = element.split("\\|", -1); //$NON-NLS-1$
				for( int j = 0; j < colvals.length; j++ )
				{
					HTMLControl ctrl = controls.get(j);

					String oneValue = urlDecode(colvals[j]);
					ctrl.setValues(oneValue);
					NameValue nameValue = ctrl.getNameValue();
					if( !first )
					{
						sbufName.append(separator);
						sbufValue.append('|');
					}
					else
					{
						first = false;
					}
					sbufName.append(nameValue.getName());
					sbufValue.append(urlEncode(nameValue.getValue()));
				}
				namesValues.add(new NameValue(sbufName.toString(), sbufValue.toString()));
			}
		}
	}

	@Override
	public void saveToDocument(PropBagEx itemxml) throws Exception
	{
		clearTargets(itemxml);

		for( NameValue nameValue : namesValues )
		{
			String[] colvals = nameValue.getValue().split("\\|", -1); //$NON-NLS-1$
			for( int j = 0; j < colvals.length; j++ )
			{
				HTMLControl ctrl = controls.get(j);
				String oneValue = urlDecode(colvals[j]);
				ctrl.setValues(oneValue);
			}

			for( TargetNode tnode : getTargets() )
			{
				PropBagEx tarBag = tnode.addNode(itemxml, ""); //$NON-NLS-1$

				for( int j = 0; j < colvals.length; j++ )
				{
					HTMLControl ctrl = controls.get(j);
					ctrl.saveToDocument(tarBag);
				}
			}
		}
		for( HTMLControl control : controls )
		{
			control.resetToDefaults();
		}
	}

	@Override
	public List<HTMLControl> getControls()
	{
		return controls;
	}

	public String getSeparator()
	{
		return separator;
	}

	protected void setSeparator(String separator)
	{
		this.separator = separator;
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	public List<NameValue> getNamesValues()
	{
		return namesValues;
	}
}
