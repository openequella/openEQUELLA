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

package com.tle.core.wizard.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.queries.FreeTextQuery;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.ScriptException;
import com.tle.core.freetext.queries.BaseCompoundQuery;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.WizardPageException;

/*
 * Provides a base class for all wizard pages to implement.
 */
public abstract class WizardPage
{
	protected static final String XML_ACTION_SCRIPT = "actionscript"; //$NON-NLS-1$

	private final List<HTMLControl> htmlControls = new ArrayList<HTMLControl>();
	private final List<HTMLControl> allControls = new ArrayList<HTMLControl>();

	private int pageNumber;
	private int upToControl = 0;
	private DefaultWizardPage wizardPage;
	private String pageTitle = "This is the page title"; //$NON-NLS-1$
	protected String script;
	private Map<String, Boolean> nodesCleared;

	private boolean enabled = true;
	private boolean submitted;
	private boolean viewable;
	private boolean valid;
	private boolean loaded;
	private boolean showMandatory;

	private String additionalCss;

	public WizardPage()
	{
		// nothing
	}

	/**
	 * wizardXml must have it's root set at the relevant page.
	 */
	public void init()
	{
		pageTitle = CurrentLocale.get(wizardPage.getTitle());
		script = wizardPage.getScript();
		additionalCss = wizardPage.getAdditionalCssClass();
		setViewable(true);
		setValid(true);
	}

	public abstract HTMLControl createControl(WizardControl controlBean, int controlNumber, int nestingLevel)
		throws WizardPageException;

	public void setViewable(boolean b)
	{
		viewable = b;
	}

	public void checkScript() throws ScriptException
	{
		setViewable(execScript(script, null));
	}

	public boolean execScript(String script, HTMLControl control) throws ScriptException
	{
		return execScriptWithContext(script, getScriptContext(control));
	}

	protected abstract boolean execScriptWithContext(String script, ScriptContext context) throws ScriptException;

	public boolean isSubmitted()
	{
		return submitted;
	}

	public void setSubmitted(boolean b)
	{
		submitted = b;
	}

	public boolean isViewable()
	{
		return viewable;
	}

	public String getPageTitle()
	{
		return pageTitle;
	}

	public void setPageTitle(String pageTitle)
	{
		this.pageTitle = pageTitle;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	public boolean isValid()
	{
		return valid;
	}

	public PropBagEx getDocBag()
	{
		if( getRepository() == null )
		{
			return new PropBagEx();
		}
		return getRepository().getItemBag();
	}

	public void pushPathOverride(HTMLControl control, String path, int index)
	{
		getRepository().pushPathOverride(path, index);
	}

	protected ScriptContext getScriptContext(HTMLControl control)
	{
		return getRepository().getScriptContext(this, control, null);
	}

	public void popPathOverride(HTMLControl control)
	{
		getRepository().popPathOverride();
	}

	public void createCtrls(List<WizardControl> controls, int nestingLevel, List<HTMLControl> vCtrls)
		throws WizardPageException
	{
		// The propbag root will be set to the appropriate page, so count the
		// number of controls and call the CtrlFactory Method

		for( WizardControl ctrlBag : controls )
		{
			HTMLControl ctrl = createControl(ctrlBag, ++upToControl, nestingLevel);
			allControls.add(ctrl);
			vCtrls.add(ctrl);
		}
	}

	public boolean beenCleared(TargetNode tnode)
	{
		if( nodesCleared.containsKey(tnode.getFullTarget()) )
		{
			return true;
		}
		else
		{
			nodesCleared.put(tnode.getFullTarget(), true);
			return false;
		}
	}

	public void createPage() throws WizardPageException
	{
		if( getRepository() != null )
		{
			setEnabled(getRepository().isEditable());
		}

		// Check the script for validity in the current context
		createCtrls(wizardPage.getControls(), 0, htmlControls);

		for( HTMLControl ctrl : htmlControls )
		{
			ctrl.evaluate();
			ctrl.resetToDefaults();
		}
		setValid(true);
		setLoaded(true);
	}

	public void setLoaded(boolean b)
	{
		loaded = b;

	}

	public void setEnabled(boolean b)
	{
		enabled = b;
	}

	protected void loadFromDocument(List<? extends HTMLControl> controls, GetItemsInterface getItems)
	{
		evaluate(controls);
		for( HTMLControl ctrl : controls )
		{
			if( ctrl.isViewable() && !ctrl.isInvalid() )
			{
				ctrl.resetToDefaults();
				ctrl.loadFromDocument(getDocBag());
				if( getItems != null )
				{
					getItems.getItems(ctrl);
				}
			}
		}
	}

	private void evaluate(List<? extends HTMLControl> controls)
	{
		for( HTMLControl ctrl : controls )
		{
			ctrl.evaluate();
		}
	}

	public boolean saveToDocument(List<? extends HTMLControl> controls, SetItemsInterface setitems) throws Exception
	{
		// For the appropriate control call set values with the
		// request object and property bag
		if( !isEnabled() )
		{
			return true;
		}

		boolean bInvalid = false;
		setValid(true);
		nodesCleared = new HashMap<String, Boolean>();

		final PropBagEx docBag = getDocBag();
		for( HTMLControl ctrl : controls )
		{
			if( ctrl.isVisible() )
			{
				ctrl.clearInvalid();
				if( ctrl.isEnabled() && !ctrl.isHidden() )
				{
					if( setitems != null )
					{
						setitems.setItems(ctrl);
					}
					ctrl.validate();
					ctrl.saveToDocument(docBag);
				}
			}
			else
			{
				ctrl.clearTargets(docBag);
			}
		}
		if( setitems != null )
		{
			setitems.afterAllSaved();
		}
		evaluate(controls);

		for( HTMLControl ctrl : controls )
		{
			if( ctrl.isVisible() )
			{
				ctrl.afterSaveValidate();
				if( ctrl.isInvalid() )
				{
					bInvalid = true;
					setValid(false);
				}

				if( !ctrl.isHidden() && ctrl.isEmpty() && ctrl.isMandatory() )
				{
					setValid(false);
				}
			}
		}

		return !bInvalid;
	}

	public void setShowMandatory(boolean show)
	{
		this.showMandatory = show || showMandatory;
	}

	public boolean showMandatory()
	{
		return isSubmitted() && isShowMandatory();
	}

	public boolean isShowMandatory()
	{
		return showMandatory;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public List<? extends HTMLControl> getControls()
	{
		return htmlControls;
	}

	public List<String> getCriteriaList()
	{
		List<String> criteria = new ArrayList<String>();
		BaseCompoundQuery compound = new BaseCompoundQuery();

		for( HTMLControl control : getControls() )
		{
			if( control.isViewable() && control.isIncluded() )
			{
				BaseQuery childQuery = control.getPowerSearchQuery();
				if( childQuery != null )
				{
					compound.addQuery(childQuery);
				}
			}
		}
		compound.addCriteria(criteria);
		return criteria;
	}

	public FreeTextQuery getPowerSearchQuery()
	{
		BaseCompoundQuery compound = new BaseCompoundQuery();

		for( HTMLControl control : getControls() )
		{
			if( control.isViewable() && control.isIncluded() )
			{
				BaseQuery childQuery = control.getPowerSearchQuery();
				if( childQuery != null )
				{
					compound.addQuery(childQuery);
				}
			}
		}
		return compound.getFullFreeTextQuery();
	}

	public interface SetItemsInterface
	{
		void setItems(HTMLControl wrapped) throws Exception;

		void clearTargets(HTMLControl wrapped, PropBagEx doc);

		void afterAllSaved();
	}

	public interface GetItemsInterface
	{
		void getItems(HTMLControl wrapped);
	}

	public abstract LERepository getRepository();

	public int getPageNumber()
	{
		return pageNumber;
	}

	public DefaultWizardPage getWizardPage()
	{
		return wizardPage;
	}

	public void setWizardPage(DefaultWizardPage wizardPage)
	{
		this.wizardPage = wizardPage;
	}

	public void setPageNumber(int pageNumber)
	{
		this.pageNumber = pageNumber;
	}

	public String resolveXpaths(String str)
	{
		// see Jira Defect TLE-695 :
		// http://apps.dytech.com.au/jira/browse/TLE-695
		Pattern p = Pattern.compile("^(.*?)\\{([\\p{Alnum}\\._/@]+)\\}(.*)$", Pattern.DOTALL); //$NON-NLS-1$

		StringBuilder result = new StringBuilder();
		resolveXPaths(result, str, p);
		return result.toString();
	}

	private void resolveXPaths(StringBuilder result, String input, Pattern p)
	{
		Matcher m = p.matcher(input);

		if( m.matches() )
		{
			String value = getScriptContext(null).getXml().get(m.group(2));

			result.append(m.group(1));
			result.append(value);
			resolveXPaths(result, m.group(3), p);
		}
		else
		{
			result.append(input);
		}
	}

	public String getAdditionalCss()
	{
		return additionalCss;
	}

	public void setAdditionalCss(String additionalCss)
	{
		this.additionalCss = additionalCss;
	}
}
