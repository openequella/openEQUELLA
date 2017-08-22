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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.scripting.ScriptException;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.freetext.queries.NodeInQuery;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;

/**
 * Represents the common elements of all controls.
 */
@SuppressWarnings("nls")
public abstract class AbstractHTMLControl implements Serializable, HTMLControl
{
	private static final long serialVersionUID = 1L;

	protected static String[] blanks = {Constants.BLANK, Constants.BLANK};

	// Attributes
	private boolean invalid;
	private boolean hidden;
	private boolean visible = true;
	private boolean expertSearch = false;
	protected boolean uniquified = false;
	private boolean enabled;
	private String title;
	private String description;
	private String formName = null;
	private int size1;
	private int size2;
	private boolean dontShowEmpty;
	private HTMLControl topLevel;
	private HTMLControl parent;

	protected final List<TargetNode> targets = new ArrayList<TargetNode>();
	protected WizardPage wizardPage;
	protected WizardControl controlBean;
	private final int controlNumber;
	private final int nestingLevel;
	private Label invalidMessage;

	private static final String FORM_KEY = "key";
	private static final String FORM_VALUE = "value";

	public AbstractHTMLControl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		wizardPage = page;
		if( page.getRepository() != null )
		{
			setExpertSearch(page.getRepository().isExpert());
		}
		this.controlBean = controlBean;
		this.controlNumber = controlNumber;
		this.nestingLevel = nestingLevel;
		invalid = false;
		enabled = true;
		targets.addAll(controlBean.getTargetnodes());
		formName = "c" + controlNumber;
		size1 = controlBean.getSize1();
		size2 = controlBean.getSize2();
		topLevel = this;
	}

	@Override
	public abstract void loadFromDocument(PropBagEx itemxml);

	@Override
	public abstract void saveToDocument(PropBagEx itemxml) throws Exception;

	@Nullable
	@Override
	public abstract BaseQuery getPowerSearchQuery();

	@Override
	public abstract void resetToDefaults();

	@Override
	public abstract boolean isEmpty();

	@Override
	public void afterSaveValidate()
	{
		String script = controlBean.getAfterSaveScript();
		if( script != null )
		{
			execScript(script);
		}
	}

	/**
	 * Return a string value after resolving xpaths.
	 * 
	 * @dytech.jira see Jira Defect TLE-695 :
	 *              http://apps.dytech.com.au/jira/browse/TLE-695
	 */
	public String evalString(LanguageBundle bundle)
	{
		String value = Constants.BLANK;
		if( bundle != null )
		{
			value = LangUtils.getString(bundle, CurrentLocale.getLocale(), null);
		}
		return evalString(value);
	}

	public String evalString(String str)
	{
		if( str == null )
		{
			str = Constants.BLANK;
		}

		if( wizardPage == null )
		{
			return str;
		}

		return wizardPage.resolveXpaths(str);
	}

	public void defaultEvaluate()
	{
		title = evalString(controlBean.getTitle());
		description = evalString(controlBean.getDescription());

		if( wizardPage != null )
		{
			enabled = wizardPage.isEnabled();
			setVisible(true);
			if( !execScript(controlBean.getScript()) )
			{
				setVisible(false);
			}
		}
	}

	@Override
	public void evaluate()
	{
		if( topLevel != this && topLevel != null )
		{
			topLevel.evaluate();
		}
		else
		{
			doEvaluate();
		}
	}

	public void doEvaluate()
	{
		defaultEvaluate();
	}

	@Override
	public void setVisible(boolean b)
	{
		visible = b;
	}

	public boolean execScript(String script)
	{
		if( Check.isEmpty(script) || getRepository() == null )
		{
			return true;
		}

		try
		{
			return wizardPage.execScript(script, this);
		}
		catch( ScriptException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isVisible()
	{
		// Data that is extracted during the CreateCtrl phase
		return visible;
	}

	@Override
	public boolean isViewable()
	{
		return visible && !hidden;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public boolean isMandatory()
	{
		// Data that is extracted during the CreateCtrl phase
		return controlBean.isMandatory();
	}

	@Override
	public boolean isIncluded()
	{
		return controlBean.isInclude();
	}

	@Override
	public Label getMessage()
	{
		if( wizardPage != null )
		{
			if( isInvalid() )
			{
				return getInvalidMessage();
			}
			if( wizardPage.showMandatory() && topLevel.isEnabled() && !isDontShowEmpty() && topLevel.isEmpty() )
			{
				return getEmptyMessage();
			}
		}
		return null;
	}

	@Override
	public void clearTargets(PropBagEx itemxml)
	{
		for( int j = 0; j < targets.size(); j++ )
		{
			TargetNode tnode = targets.get(j);
			if( !wizardPage.beenCleared(tnode) )
			{
				tnode.clear(itemxml);
			}
		}
	}

	@Override
	public String getFormName()
	{
		return formName;
	}

	public Label getInvalidMessage()
	{
		return invalidMessage;
	}

	public Label getEmptyMessage()
	{
		return new KeyLabel("wizard.controls.empty");
	}

	public static String ent(String szStr)
	{
		return Utils.ent(szStr);
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public int getSize1()
	{
		return size1;
	}

	@Override
	public int getSize2()
	{
		return size2;
	}

	@Override
	public boolean isExpertSearch()
	{
		return expertSearch;
	}

	@Override
	public boolean isUniquified()
	{
		return uniquified;
	}

	public boolean isReload()
	{
		return controlBean.isReload();
	}

	@Override
	public boolean isInvalid()
	{
		return invalid;
	}

	@Override
	public void setInvalid(boolean invalid, Label message)
	{
		this.invalid = invalid;
		invalidMessage = message;
	}

	@Override
	public void clearInvalid()
	{
		invalid = false;
	}

	public void setExpertSearch(boolean expertSearch)
	{
		this.expertSearch = expertSearch;
	}

	@Override
	public void setUniquified(boolean uniquified)
	{
		this.uniquified = uniquified;
	}

	@Nullable
	@Override
	public LERepository getRepository()
	{
		return wizardPage.getRepository();
	}

	@Override
	public WizardPage getWizardPage()
	{
		return wizardPage;
	}

	@Override
	public List<TargetNode> getTargets()
	{
		return targets;
	}

	@Override
	public TargetNode getFirstTarget()
	{
		return targets.get(0);
	}

	protected void addValueToTargets(String value, List<TargetNode> targets, PropBagEx itemxml) throws Exception
	{
		for( TargetNode tnode : targets )
		{
			tnode.addNode(itemxml, value);
			if( isUniquified() )
			{
				return;
			}
		}
	}

	@Override
	public NameValue getNameValue()
	{
		throw new RuntimeException("Not supported by default");
	}

	@Override
	public void setSize1(int size1)
	{
		this.size1 = size1;
	}

	@Override
	public void setSize2(int size2)
	{
		this.size2 = size2;
	}

	public String getWizid()
	{
		return getRepository().getWizid();
	}

	public boolean isDontShowEmpty()
	{
		return dontShowEmpty;
	}

	@Override
	public void setDontShowEmpty(boolean dontShowEmpty)
	{
		this.dontShowEmpty = dontShowEmpty;
	}

	@Override
	public void validate()
	{
		String validateScript = controlBean.getValidateScript();
		if( validateScript != null )
		{
			execScript(validateScript);
		}
	}

	@Override
	public WizardControl getControlBean()
	{
		return controlBean;
	}

	@Override
	public int getControlNumber()
	{
		return controlNumber;
	}

	@Override
	public int getNestingLevel()
	{
		return nestingLevel;
	}

	@Override
	public boolean isHidden()
	{
		return hidden;
	}

	@Override
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public BaseQuery getDefaultPowerSearchQuery(Collection<String> values, boolean tokenise)
	{
		if( values.size() > 0 )
		{
			final Collection<String> freetextFields = new ArrayList<String>();
			for( TargetNode node : getTargets() )
			{
				freetextFields.add(node.getFreetextField());
			}

			final Collection<String> valuesCopy = new HashSet<String>(values);
			for( Iterator<String> it = valuesCopy.iterator(); it.hasNext(); )
			{
				String v = it.next();
				if( v.equals("*") || Check.isEmpty(v) )
				{
					it.remove();
				}
			}

			final NodeInQuery newQuery = new NodeInQuery(valuesCopy, false, freetextFields, CurrentLocale.get(
				controlBean.getPowerSearchFriendlyName(), null));
			newQuery.setTokenise(tokenise);
			newQuery.setBooleanType(false);
			return newQuery;
		}
		return null;
	}

	public String getFormKey()
	{
		return FORM_KEY;
	}

	public String getFormValue()
	{
		return FORM_VALUE;
	}

	public static String urlEncode(String val)
	{
		try
		{
			String newVal = URLEncoder.encode(val, Constants.UTF8);
			return newVal.replace("+", "%20");
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	public static String urlDecode(String val)
	{
		try
		{
			String newVal = val.replace("+", "%2B");
			return URLDecoder.decode(newVal, Constants.UTF8);
		}
		catch( UnsupportedEncodingException uee )
		{
			throw new RuntimeException(uee);
		}
	}

	@Override
	public void setTopLevel(HTMLControl topLevel)
	{
		this.topLevel = topLevel;
	}

	public HTMLControl getTopLevel()
	{
		return topLevel;
	}

	@Override
	public HTMLControl getParent()
	{
		return parent;
	}

	@Override
	public void setParent(HTMLControl parent)
	{
		this.parent = parent;
	}
}
