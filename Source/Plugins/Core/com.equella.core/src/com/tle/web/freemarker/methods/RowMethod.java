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
