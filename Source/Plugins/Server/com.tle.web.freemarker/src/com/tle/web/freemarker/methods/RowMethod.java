package com.tle.web.freemarker.methods;

import java.util.List;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class RowMethod implements TemplateMethodModelEx
{
	private int getInt(Object val)
	{
		return ((SimpleNumber) val).getAsNumber().intValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object exec(List list) throws TemplateModelException
	{
		try
		{
			int cols = getInt(list.get(0));
			int current = getInt(list.get(1));
			int total = getInt(list.get(2));
			String nested = ((SimpleScalar) list.get(3)).getAsString();
			boolean last = (current == total - 1);
			int colspan = 1; // implement this if you need it...

			StringBuilder results = new StringBuilder();
			if( current % cols == 0 )
			{
				results.append("<tr>"); //$NON-NLS-1$
			}
			results.append(nested);
			int columnsDone = (current + colspan) % cols;
			if( last && columnsDone != 0 )
			{
				results.append("<td colspan=\""); //$NON-NLS-1$
				results.append(cols - columnsDone);
				results.append("\">&nbsp;</td>"); //$NON-NLS-1$
			}
			if( last || (current + colspan) % cols == 0 )
			{
				results.append("</tr>"); //$NON-NLS-1$
			}
			return results.toString();
		}
		catch( Exception e )
		{
			throw new TemplateModelException(e);
		}
	}
}
