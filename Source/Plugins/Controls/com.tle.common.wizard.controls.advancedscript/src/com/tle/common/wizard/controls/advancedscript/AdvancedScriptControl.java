package com.tle.common.wizard.controls.advancedscript;

import java.util.List;

import com.dytech.edge.wizard.beans.control.CustomControl;

/**
 * @author aholland
 */
public class AdvancedScriptControl extends CustomControl
{
	private static final long serialVersionUID = 1L;

	private static final String KEY_MARK_UP = "MarkUp"; //$NON-NLS-1$
	private static final String KEY_ONLOAD_JS = "OnLoadJs"; //$NON-NLS-1$
	private static final String KEY_ONSUBMIT_JS = "OnSubmitJs"; //$NON-NLS-1$
	private static final String KEY_JS_LIBS = "JsLibs"; //$NON-NLS-1$
	private static final String KEY_READ_JS = "ReadJs"; //$NON-NLS-1$
	private static final String KEY_STORE_JS = "StoreJs"; //$NON-NLS-1$
	private static final String KEY_NOTES = "Notes"; //$NON-NLS-1$

	public AdvancedScriptControl()
	{
		setClassType("advancedscript"); //$NON-NLS-1$
	}

	public AdvancedScriptControl(CustomControl cloned)
	{
		if( cloned != null )
		{
			cloned.cloneTo(this);
		}
	}

	public String getMarkUp()
	{
		return (String) getAttributes().get(KEY_MARK_UP);
	}

	public void setMarkUp(String markUp)
	{
		getAttributes().put(KEY_MARK_UP, markUp);
	}

	/**
	 * Client side onload js
	 * 
	 * @return
	 */
	public String getOnLoadJs()
	{
		return (String) getAttributes().get(KEY_ONLOAD_JS);
	}

	public void setOnLoadJs(String js)
	{
		getAttributes().put(KEY_ONLOAD_JS, js);
	}

	/**
	 * Client side onsubmit js
	 * 
	 * @return
	 */
	public String getOnSubmitJs()
	{
		return (String) getAttributes().get(KEY_ONSUBMIT_JS);
	}

	public void setOnSubmitJs(String js)
	{
		getAttributes().put(KEY_ONSUBMIT_JS, js);
	}

	/**
	 * Format is "library id"."module id"
	 * 
	 * @return jsLibs
	 */
	public List<String> getJsLibs()
	{
		return ensureListAttribute(KEY_JS_LIBS);
	}

	/**
	 * Format is "library id"."module id"
	 * 
	 * @param jsLibs
	 */
	public void setJsLibs(List<String> jsLibs)
	{
		getAttributes().put(KEY_JS_LIBS, jsLibs);
	}

	/**
	 * Server side load js
	 * 
	 * @return
	 */
	public String getReadJs()
	{
		return (String) getAttributes().get(KEY_READ_JS);
	}

	public void setReadJs(String js)
	{
		getAttributes().put(KEY_READ_JS, js);
	}

	/**
	 * Server side submit js
	 * 
	 * @return
	 */
	public String getStoreJs()
	{
		return (String) getAttributes().get(KEY_STORE_JS);
	}

	public void setStoreJs(String js)
	{
		getAttributes().put(KEY_STORE_JS, js);
	}

	/**
	 * Notes about the control. Has no functional impact
	 * 
	 * @return
	 */
	public String getNotes()
	{
		return (String) getAttributes().get(KEY_NOTES);
	}

	public void setNotes(String notes)
	{
		getAttributes().put(KEY_NOTES, notes);
	}
}
