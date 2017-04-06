package com.tle.core.reporting.birttypes;

import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.data.DataType;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.CascadingParameterGroupHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;

public abstract class AbstractBirtType
{
	protected IScalarParameterDefn scalarDef;
	protected IParameterGroupDefn group;
	protected int paramNum;
	protected String targetNode;
	protected int index;
	protected WizardControl control;

	public AbstractBirtType(IScalarParameterDefn def, int paramNum, IParameterGroupDefn group)
	{
		this.scalarDef = def;
		this.paramNum = paramNum;
		this.targetNode = "params/p" + paramNum; //$NON-NLS-1$
		this.group = group;
	}

	public AbstractBirtType(IScalarParameterDefn def, int paramNum)
	{
		this(def, paramNum, null);
	}

	public WizardControl createWizardControl(IGetParameterDefinitionTask paramTask)
	{
		WizardControl locControl = createControl(paramTask);
		setupControl(paramTask, locControl);
		return locControl;
	}

	protected void setupControl(IGetParameterDefinitionTask paramTask, WizardControl control)
	{
		String helpText = scalarDef.getHelpText();
		String promptText = scalarDef.getPromptText();

		if( Check.isEmpty(promptText) )
		{
			promptText = scalarDef.getName() + ":"; //$NON-NLS-1$
		}
		control.setTitle(LangUtils.createTextTempLangugageBundle(promptText));
		control.setDescription(LangUtils.createTextTempLangugageBundle(helpText));
		control.setMandatory(scalarDef.isRequired());
		control.getTargetnodes().add(new TargetNode(targetNode, "")); //$NON-NLS-1$ 
		control.setReload(group != null);
		this.control = control;
	}

	protected String getParameterFormat()
	{
		switch( scalarDef.getDataType() )
		{
			case IParameterDefn.TYPE_DATE_TIME:
				return ParameterValidationUtil.DEFAULT_DATETIME_FORMAT;
			case IParameterDefn.TYPE_DATE:
				return ParameterValidationUtil.DEFAULT_DATE_FORMAT;
			case IParameterDefn.TYPE_TIME:
				return ParameterValidationUtil.DEFAULT_TIME_FORMAT;
			default:
				return null;
		}
	}

	protected String getFirstValue(Map<String, String[]> params)
	{
		String[] vals = params.get(scalarDef.getName());
		if( vals != null && vals.length > 0 )
		{
			return vals[0];
		}
		return null;
	}

	protected abstract WizardControl createControl(IGetParameterDefinitionTask paramTask);

	public abstract void convertToXml(PropBagEx docXml, Map<String, String[]> params);

	public abstract void convertToParams(PropBagEx docXml, Map<String, String[]> params);

	public void update(Map<String, String[]> parameters, List<AbstractBirtType> parameterControls,
		IGetParameterDefinitionTask task)
	{
		ScalarParameterHandle parameterHandle = (ScalarParameterHandle) scalarDef.getHandle();

		if( !(parameterHandle.getContainer() instanceof CascadingParameterGroupHandle && group != null) )
		{
			return;
		}

		int locIndex = parameterHandle.getContainerSlotHandle().findPosn(parameterHandle);
		if( locIndex == 0 )
		{
			return;
		}

		Object[] thingsBefore = new Object[locIndex];

		for( int j = 0; j < locIndex; j++ )
		{
			for( AbstractBirtType parameterControl : parameterControls )
			{
				if( parameterControl.getIndex() == j && group.getName().equals(parameterControl.getGroupname()) )
				{
					thingsBefore[j] = convertToParam(parameters.get(parameterControl.getName()), parameterControl);
					break;
				}
			}
		}

		task.getSelectionListForCascadingGroup(group.getName(), thingsBefore);
		setupControl(task, control);
	}

	private Object convertToParam(String[] input, AbstractBirtType control)
	{
		if( input == null )
		{
			return null;
		}
		String toUse = input[0];

		// There might be others, but this should do for now
		if( control.scalarDef.getDataType() == DataType.INTEGER_TYPE
			|| control.scalarDef.getDataType() == DataType.DATE_TYPE )
		// It gets confused...
		{
			try
			{
				return Integer.parseInt(toUse);
			}
			catch( NumberFormatException e )
			{
				// Shouldn't happen, ignore
				return null;
			}
		}

		return toUse;
	}

	public int getIndex()
	{
		ScalarParameterHandle parameterHandle = (ScalarParameterHandle) scalarDef.getHandle();
		return parameterHandle.getContainerSlotHandle().findPosn(parameterHandle);
	}

	public String getGroupname()
	{
		if( group != null )
		{
			return group.getName();
		}
		return null;
	}

	public String getName()
	{
		return scalarDef.getName();
	}

	public String[] getDisplayTexts(PropBagEx docXml)
	{
		return null;
	}
}