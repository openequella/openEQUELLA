package com.tle.web.sections.standard;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.model.HtmlFileDropState;

import static com.tle.web.sections.standard.FileUpload.isMultipartRequest;

/**
 * @author Dongsheng Cai, Doolse
 */
public class FileDrop extends AbstractFileUpload<HtmlFileDropState>
{
	public FileDrop()
	{
		super(RendererConstants.FILEDROP);
	}

	@Override
	public Class<HtmlFileDropState> getModelClass()
	{
		return HtmlFileDropState.class;
	}

}
