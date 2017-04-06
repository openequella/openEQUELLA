package com.tle.web.sections.equella.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.component.model.SelectionsTableState;
import com.tle.web.sections.equella.freemarker.ChooseRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.AbstractEventOnlyComponent;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;

public class SelectionsTableRenderer extends ZebraTableRenderer
{
	private final SelectionsTableState stuff;

	public SelectionsTableRenderer(SelectionsTableState stuff, RendererFactory rendererFactory)
	{
		super(stuff, rendererFactory);
		this.stuff = stuff;
		addClass("selections");

		// Convert things to rows
		boolean hasRows = false;
		for( SelectionsTableSelection thing : stuff.getSelections() )
		{
			hasRows = true;

			final TableRow row = stuff.addRow();
			if( thing.isClassesSet() )
			{
				row.addClasses(thing.getStyleClasses());
			}

			final TableCell cell1 = new TableCell();
			cell1.addClass("name");
			final SectionRenderable icon = thing.getIcon();
			if( icon != null )
			{
				cell1.addContent(icon);
			}
			final SectionRenderable viewAction = thing.getViewAction();
			if( viewAction != null )
			{
				cell1.addContent(viewAction);
			}
			else
			{
				cell1.addContent(thing.getName());
			}
			row.addCell(cell1);

			final List<TableCell> extras = thing.getCells();
			if( extras != null )
			{
				for( TableCell extra : extras )
				{
					row.addCell(extra);
				}
			}

			final TableCell actionsCell = new TableCell();
			actionsCell.addClass("actions");
			final List<SectionRenderable> actions = thing.getActions();
			if( actions != null )
			{
				boolean firstAction = true;
				for( SectionRenderable action : actions )
				{
					if( !firstAction )
					{
						actionsCell.addContent(" | ");
					}
					actionsCell.addContent(action);
					firstAction = false;
				}
			}
			row.addCell(actionsCell);
		}

		if( !hasRows )
		{
			final Label nothingSelectedText = stuff.getNothingSelectedText();
			if( nothingSelectedText != null )
			{
				ArrayList<Object> row = new ArrayList<Object>();
				row.add(nothingSelectedText);

				TableHeaderRow headerRow = stuff.getHeaderRow();
				if( headerRow != null )
				{
					for( int i = 1; i < headerRow.getCells().size(); i++ )
					{
						row.add(null);
					}
				}
				stuff.addRow(row.toArray());
				addClass("no-selections");
			}
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		// if no rows and no 'nothing selected' text then don't render the table
		if( stuff.getRows().size() != 0 )
		{
			super.realRender(writer);
		}

		final AbstractEventOnlyComponent<? extends HtmlComponentState> addAction = stuff.getAddAction();

		if( addAction != null )
		{
			addAction.getState(writer).addClass("add");
			SectionRenderable sectionRenderable = ChooseRenderer.getSectionRenderable(writer, addAction,
				addAction.getDefaultRenderer(), rendererFactory);
			if( sectionRenderable != null )
			{
				writer.preRender(sectionRenderable);
				sectionRenderable.realRender(writer);
			}
		}
	}

}
