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

package com.tle.web.institution.tab;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableRow;

@SuppressWarnings("nls")
public class ThreadDumpTab extends AbstractInstitutionTab<ThreadDumpTab.ThreadDumpModel>
{
	@PlugKey("institutions.threaddump.link.name")
	private static Label LINK_LABEL;

	@PlugKey(value = "institutions.threaddump.column.thread", global = true)
	private static Label LABEL_THREAD;
	@PlugKey(value = "institutions.threaddump.column.state", global = true)
	private static Label LABEL_STATE;
	@PlugKey(value = "institutions.threaddump.column.priority", global = true)
	private static Label LABEL_PRIORITY;
	@PlugKey(value = "institutions.threaddump.column.daemon", global = true)
	private static Label LABEL_DAEMON;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "t")
	private Table threadsTable;

	private static final Comparator<Thread> THREAD_COMP = new Comparator<Thread>()
	{
		@Override
		public int compare(Thread o1, Thread o2)
		{
			int result = o1.getName().compareToIgnoreCase(o2.getName());
			if( result == 0 )
			{
				Long id1 = o1.getId();
				Long id2 = o2.getId();
				return id1.compareTo(id2);
			}
			return result;
		}
	};

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ThreadDumpModel model = getModel(context);

		final Map<Thread, StackTraceElement[]> traces = new TreeMap<Thread, StackTraceElement[]>(THREAD_COMP);
		traces.putAll(Thread.getAllStackTraces());
		model.setTraces(traces);
		final String currentUrl = context.getPublicBookmark().getHref();

		final TableState threadsTableState = threadsTable.getState(context);
		for( Thread thread : traces.keySet() )
		{
			final HtmlLinkState viewThreadLink = new HtmlLinkState(new TextLabel(thread.getName()));
			viewThreadLink.setBookmark(new SimpleBookmark(currentUrl + "#" + thread.getId()));

			final TableRow row = threadsTableState.addRow(viewThreadLink, thread.getState().toString(),
				thread.getPriority(), thread.isDaemon());
			row.setSortData(thread.getName(), thread.getState(), thread.getPriority(), thread.isDaemon());
		}

		return viewFactory.createResult("tab/threaddump.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		threadsTable.setColumnHeadings(LABEL_THREAD, LABEL_STATE, LABEL_PRIORITY, LABEL_DAEMON);
		threadsTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC);
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public Label getName()
	{
		return LINK_LABEL;
	}

	@Override
	public Class<ThreadDumpModel> getModelClass()
	{
		return ThreadDumpModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "threaddump";
	}

	public Table getThreadsTable()
	{
		return threadsTable;
	}

	public static class ThreadDumpModel
	{
		private Map<Thread, StackTraceElement[]> traces;

		public void setTraces(Map<Thread, StackTraceElement[]> traces)
		{
			this.traces = traces;
		}

		public Map<Thread, StackTraceElement[]> getTraces()
		{
			return traces;
		}
	}
}
