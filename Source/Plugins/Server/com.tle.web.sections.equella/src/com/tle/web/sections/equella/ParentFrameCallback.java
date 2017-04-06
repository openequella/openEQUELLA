package com.tle.web.sections.equella;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.ParentFrameFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.result.util.CloseWindowResult;

/**
 * @author aholland
 */
public class ParentFrameCallback implements ModalSessionCallback
{
	private static final long serialVersionUID = 1L;

	protected final String parentFunction;

	public ParentFrameCallback(JSCallAndReference function)
	{
		if( !function.isStatic() )
		{
			throw new SectionsRuntimeException("Callback function must be static"); //$NON-NLS-1$
		}
		this.parentFunction = function.getExpression(null);
	}

	public JSCallable createParentFrameCall()
	{
		return new ParentFrameFunction(new ExternallyDefinedFunction(parentFunction));
	}

	@Override
	public void executeModalFinished(SectionInfo info, ModalSession session)
	{
		JSCallable call = createParentFrameCall();
		info.getRootRenderContext().setRenderedBody(new CloseWindowResult(new FunctionCallStatement(call)));
	}
}
