package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.expression.NotEqualsExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlTextFieldState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;

/**
 * A text field component.
 * <p>
 * It provides methods to get and set a String value. <br>
 * The default renderer is usually {@link TextFieldRenderer}.
 * 
 * @author jmaginnis
 */
public class TextField extends AbstractValueStateComponent<HtmlTextFieldState, JSValueComponent>
{
	private boolean dontBookmarkBlank = true;
	private boolean autocompleteDisabled;
	private boolean password;
	private JSCallable callback;

	public TextField()
	{
		super(RendererConstants.TEXTFIELD);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return setupState(info, new HtmlTextFieldState());
	}

	public String getValue(SectionInfo info)
	{
		return getStringValue(info);
	}

	public void setValue(SectionInfo info, String value)
	{
		HtmlValueState state = getState(info);
		state.setValue(value);
	}

	@Override
	protected HtmlTextFieldState setupState(SectionInfo info, HtmlTextFieldState state)
	{
		super.setupState(info, state);
		state.setPassword(isPassword());
		state.setAutocompleteDisabled(autocompleteDisabled);
		state.setAutoCompleteCallback(callback);
		return state;
	}

	@Override
	protected String getBookmarkStringValue(HtmlTextFieldState state)
	{
		String value = state.getValue();
		if( value != null && dontBookmarkBlank && value.isEmpty() )
		{
			return null;
		}
		return value;
	}

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		addDocumentedParam(event, getParameterId(), String.class.getName());
	}

	public void setDontBookmarkBlank(boolean b)
	{
		this.dontBookmarkBlank = b;
	}

	public JSValidator createNotBlankValidator()
	{
		return new SimpleValidator(new NotEqualsExpression(createGetExpression(), new StringExpression(""))); //$NON-NLS-1$
	}

	public boolean isAutocompleteDisabled()
	{
		return autocompleteDisabled;
	}

	public void setAutocompleteDisabled(boolean autocompleteDisabled)
	{
		this.autocompleteDisabled = autocompleteDisabled;
	}

	public boolean isPassword()
	{
		return password;
	}

	public void setPassword(boolean password)
	{
		this.password = password;
	}

	/***
	 * Used to provide the call back to obtain the Auto complete source. The
	 * call back should return either Json or a String array
	 * 
	 * @param JSCallable function that returns a Json array or String Array of
	 *            Json objects that take the form of {{label, value}}
	 */
	@SuppressWarnings("nls")
	public void setAutoCompleteCallback(JSCallable callback)
	{
		this.callback = callback;
		setDefaultRenderer("autocompletetextfield");
	}
}
