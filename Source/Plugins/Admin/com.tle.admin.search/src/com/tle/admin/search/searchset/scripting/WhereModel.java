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

package com.tle.admin.search.searchset.scripting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.Row;
import com.dytech.edge.admin.script.ScriptModel;
import com.dytech.edge.admin.script.TargetValueMap;
import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.Node;
import com.dytech.edge.admin.script.model.OpTerm;
import com.dytech.edge.admin.script.model.Operator;
import com.dytech.edge.admin.script.model.Term;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.TargetListener;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;

/**
 * This controls the data model of an "Where" statement using in the Learning
 * Edge's hierarchy. This should be used along with the <code>ScriptView</code>
 * class in order to display and edit a statement. Given a Clause data
 * structure, the model will display on Clause and OpTerm elements in the
 * ScriptView. Each Clause and OpTerm will display their first Term in the same
 * row as themselves. <b>Warning:</b> The code is ugly, and I'm not proud of
 * it!! It needs some serious refactoring, and some bits need a scraping. I'll
 * look in to it more when I have time :)
 * 
 * @author Nicholas Read
 */
@SuppressWarnings({"nls", "rawtypes"})
public class WhereModel extends DefaultListModel implements ScriptModel
{
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(WhereModel.class);

	protected static final List<String> TYPES = Collections.unmodifiableList(Arrays.asList("where", "where (", "and",
		"and (", "or", "or ("));

	protected static final List<String> OPERATORS = Collections.unmodifiableList(Arrays.asList("equals", "is not",
		"is like"));

	private static final boolean[][] SET_TABLE = {
	/* NONE, BLOCK, OP_TERM, BRACKET_END */
	/* WHERE */{false, true, false, false},
	/* WHEREB */{false, true, false, false},
	/* AND */{false, false, true, false},
	/* ANDB */{false, false, true, false},
	/* OR */{false, false, true, false},
	/* ORB */{false, false, true, false},};

	private static final boolean[][] ADD_TABLE = {
	/* NONE, BLOCK, OP_TERM, BRACKET_END */
	/* WHERE */{true, false, false, false},
	/* WHEREB */{true, false, false, false},
	/* AND */{false, true, true, true},
	/* ANDB */{false, true, true, true},
	/* OR */{false, true, true, true},
	/* ORB */{false, true, true, true},};

	protected static final int EQUALS = 0;
	protected static final int NOT_EQUALS = 1;
	protected static final int LIKE = 2;

	protected static final int WHERE = 0;
	protected static final int WHEREB = 1;
	protected static final int AND = 2;
	protected static final int ANDB = 3;
	protected static final int OR = 4;
	protected static final int ORB = 5;

	protected static final int NONE = 0;
	protected static final int BLOCK = 1;
	protected static final int OP_TERM = 2;
	protected static final int BRACKET_END = 3;

	protected static final String tab = "&nbsp;&nbsp;&nbsp;"; //$NON-NLS-1$

	protected Statement statement;
	protected int currentIndex;
	protected int currentType;
	protected int currentRow = NONE;
	protected boolean loading = true;
	protected TargetValueMap targetMap;

	protected JPanel editor;
	protected JButton setButton;
	protected JButton addButton;
	protected JComboBox typeCombo;
	protected JComboBox operatorCombo;
	protected JComboBox valueSelection;
	protected WhereTargetChooser xpathField;
	protected Component parent;

	/**
	 * Constructor for <code>WhereModel</code>.
	 */
	public WhereModel(ClientService clientService, Component parent, ItemDefinition itemDefinition)
	{
		this.parent = parent;

		statement = new Statement();
		setup();
		loadItemDef(clientService, itemDefinition);
	}

	public WhereModel(ClientService clientService, Component parent, Schema schema)
	{
		this.parent = parent;

		statement = new Statement();
		setup();
		loadSchema(clientService, schema);
	}

	private void loadItemDef(ClientService clientService, ItemDefinition itemDefinition)
	{
		try
		{
			if( itemDefinition.getSchema() == null )
			{
				itemDefinition = clientService.getService(RemoteItemDefinitionService.class)
					.get(itemDefinition.getId());
			}

			loadSchema(clientService, itemDefinition.getSchema());

			targetMap = new TargetValueMap();
			targetMap.addPages(itemDefinition.getWizard().getPages());
		}
		catch( Exception ex )
		{
			LOGGER.error("Error loading collection " + itemDefinition.getId(), ex); //$NON-NLS-1$
			Driver.displayError(parent, "itemEditor/loading", ex); //$NON-NLS-1$
		}
	}

	private void loadSchema(ClientService clientService, Schema schema)
	{
		try
		{
			schema = clientService.getService(RemoteSchemaService.class).get(schema.getId());
			xpathField.loadSchema(schema.getDefinitionNonThreadSafe());
		}
		catch( Exception ex )
		{
			LOGGER.error("Error loading schema " + schema.getId(), ex); //$NON-NLS-1$
			Driver.displayInformation(parent, "There was an error retrieving the schema from the server"); //$NON-NLS-1$
		}
	}

	@Override
	public void importScript(Reader r) throws InvalidScriptException
	{
		WhereParser p = new WhereParser(new BufferedReader(r));
		importStatement(p.importScript());
	}

	@Override
	public JPanel getStatementEditor()
	{
		return editor;
	}

	@Override
	public String toScript()
	{
		if( statement.isEmpty() )
		{
			return null;
		}
		else
		{
			return statement.toScript();
		}
	}

	private void setup()
	{
		editor = new JPanel(new BorderLayout(5, 0));
		editor.setAlignmentX(Component.LEFT_ALIGNMENT);
		editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
			BorderFactory.createEmptyBorder(2, 5, 2, 5)));

		editor.add(createButtons(), BorderLayout.EAST);
		editor.add(createCentre(), BorderLayout.CENTER);
	}

	@SuppressWarnings("unchecked")
	protected JPanel createCentre()
	{
		typeCombo = new JComboBox(TYPES.toArray());
		xpathField = new WhereTargetChooser(new SchemaModel());
		operatorCombo = new JComboBox(OPERATORS.toArray());
		valueSelection = new JComboBox();

		UpdateHandler listener = new UpdateHandler();
		typeCombo.addActionListener(listener);
		xpathField.addTargetListener(new LHSHandler());
		operatorCombo.addActionListener(listener);

		final int height = typeCombo.getPreferredSize().height;
		final int width1 = typeCombo.getPreferredSize().width;
		final int width2 = operatorCombo.getPreferredSize().width;

		final int[] rows = {TableLayout.FILL, height, TableLayout.FILL,};
		final int[] columns = {width1, TableLayout.FILL, width2, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, columns, 0, 5));

		all.add(typeCombo, new Rectangle(0, 1, 1, 1));
		all.add(xpathField, new Rectangle(1, 1, 1, 1));
		all.add(operatorCombo, new Rectangle(2, 1, 1, 1));
		all.add(valueSelection, new Rectangle(3, 1, 1, 1));

		return all;
	}

	protected JPanel createButtons()
	{
		setButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.set")); //$NON-NLS-1$
		setButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		setButton.setEnabled(false);
		setButton.addActionListener(new SetHandler());

		addButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.add")); //$NON-NLS-1$
		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addButton.addActionListener(new AddHandler());

		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.add(Box.createVerticalGlue());
		all.add(addButton);
		all.add(Box.createRigidArea(new Dimension(0, 5)));
		all.add(setButton);
		all.add(Box.createVerticalGlue());

		return all;
	}

	/**
	 * Determines whether or not the given node is deletable.
	 * 
	 * @param r The row to check.
	 * @return True is the node can be deleted.
	 */
	@Override
	public boolean allowRemoval(Row r)
	{
		Node n = (Node) r;
		return n instanceof Block || n instanceof OpTerm;
	}

	/**
	 * Removes the entire script
	 */
	@Override
	public void clearScript()
	{
		statement.setBlock(null);
	}

	/**
	 * Removes the node from the statement.
	 * 
	 * @param r The row to remove.
	 */
	@Override
	public void remove(Row r)
	{
		Node n = (Node) r;
		if( n instanceof Block )
		{
			statement.setBlock(null);
			importStatement(statement);
		}
		else if( n instanceof OpTerm )
		{
			Clause c = (Clause) n.getParent();
			c.remove((OpTerm) n);
			importStatement(statement);
		}

		// if( instanceof BracketEnd )
		// We don't do anything if it is a fake row. We actually should
		// never get to this case since it should be blocked at the user
		// interface.
	}

	@Override
	public void rowSelected(Row r, int index)
	{
		currentIndex = index;

		if( r == null )
		{
			currentRow = NONE;
			updateButtons();
		}
		else if( r instanceof Block )
		{
			Term term = blockSelected((Block) r);
			dealWithTerm(term);
		}
		else if( r instanceof OpTerm )
		{
			Term term = opTermSelected((OpTerm) r);
			dealWithTerm(term);
		}
		else
		{
			typeCombo.setSelectedIndex(WHERE);
			currentRow = BRACKET_END;
			updateButtons();
		}
	}

	protected void dealWithTerm(Term term)
	{
		while( term instanceof Brackets )
		{
			term = ((Brackets) term).getClause().getFirst();
		}

		Comparison com = (Comparison) term;
		xpathField.setSchemaItem(com.getLHS());
		dealWithEquality(com.getOperator());
		setValue(com.getRHS());
	}

	protected void setValue(String value)
	{
		populateValues();
		if( valueSelection.isEditable() )
		{
			valueSelection.getEditor().setItem(value);
		}
		else
		{
			int count = valueSelection.getItemCount();
			for( int i = 0; i < count; i++ )
			{
				if( valueSelection.getItemAt(i).toString().equals(value) )
				{
					valueSelection.setSelectedIndex(i);
					return;
				}
			}
		}
	}

	protected void dealWithEquality(Equality equality)
	{
		if( equality instanceof Equals )
		{
			operatorCombo.setSelectedIndex(EQUALS);
		}
		else if( equality instanceof NotEquals )
		{
			operatorCombo.setSelectedIndex(NOT_EQUALS);
		}
		else if( equality instanceof Like )
		{
			operatorCombo.setSelectedIndex(LIKE);
		}
	}

	protected Term blockSelected(Block block)
	{
		Term term = block.getClause().getFirst();

		if( term instanceof Brackets )
		{
			typeCombo.setSelectedIndex(WHEREB);
			currentType = WHEREB;
		}
		else
		{
			typeCombo.setSelectedIndex(WHERE);
			currentType = WHERE;
		}

		currentRow = BLOCK;
		updateButtons();

		return term;
	}

	protected Term opTermSelected(OpTerm opTerm)
	{
		Operator op = opTerm.getOperator();
		Term term = opTerm.getTerm();

		if( op instanceof AndOperator )
		{
			if( term instanceof Brackets )
			{
				typeCombo.setSelectedIndex(ANDB);
				currentType = ANDB;
			}
			else
			{
				typeCombo.setSelectedIndex(AND);
				currentType = AND;
			}
		}
		else
		{
			if( term instanceof Brackets )
			{
				typeCombo.setSelectedIndex(ORB);
				currentType = ORB;
			}
			else
			{
				typeCombo.setSelectedIndex(OR);
				currentType = OR;
			}
		}

		currentRow = OP_TERM;
		updateButtons();

		return term;
	}

	protected void updateButtons()
	{
		int index = typeCombo.getSelectedIndex();

		setButton.setEnabled(SET_TABLE[index][currentRow]);
		addButton.setEnabled(ADD_TABLE[index][currentRow]);

		if( (index == WHERE || index == WHEREB) && !statement.isEmpty() )
		{
			addButton.setEnabled(false);
		}
	}

	// // IMPORT METHODS
	// ////////////////////////////////////////////////////////

	protected void importStatement(Statement statement)
	{
		clear();

		this.statement = statement;

		if( !statement.isEmpty() )
		{
			includeBlock(statement.getBlock());
		}
	}

	protected void includeBlock(Block block)
	{
		block.setText(block.toEasyRead());
		includeClause(block.getClause(), block, tab);
	}

	protected void includeClause(Clause c, Node n, String prefix)
	{
		includeTerm(c.getFirst(), n, prefix);

		for( OpTerm opterm : c.getOpTerms() )
		{
			opterm.setText(prefix + opterm.getOperator().toEasyRead());
			includeTerm(opterm.getTerm(), opterm, prefix);
		}
	}

	protected void includeTerm(Term t, Node n, String prefix)
	{
		if( t instanceof Comparison )
		{
			Comparison c = (Comparison) t;
			n.appendText(c.toEasyRead());
			this.addElement(n);

		}
		else if( t instanceof Brackets )
		{
			Brackets b = (Brackets) t;
			n.appendText("<b>(</b> "); //$NON-NLS-1$

			includeClause(b.getClause(), n, prefix + tab);

			this.addElement(new BracketEnd(n, prefix));
		}
	}

	// // INSERTION OF NODES/ROWS
	// ///////////////////////////////////////////////

	protected void addNode()
	{
		int type = typeCombo.getSelectedIndex();

		Term term = constructComparison();
		if( type == WHEREB || type == ANDB || type == ORB )
		{
			term = constructBrackets(term);
		}

		if( type == WHERE || type == WHEREB )
		{
			addBlock(constructBlock(term), currentIndex);
		}
		else
		{
			addOpTerm(constructOpTerm(type, term), currentIndex);
		}
	}

	protected void addBlock(Block b, int index)
	{
		statement.setBlock(b);
		importStatement(statement);
	}

	protected void addOpTerm(OpTerm ot, int index)
	{
		if( index == 0 )
		{
			statement.getBlock().getClause().insert(ot, 0);
		}
		else
		{
			Node n = (Node) get(index);

			if( n instanceof OpTerm )
			{
				OpTerm selected = (OpTerm) n;

				if( selected.getTerm() instanceof Brackets )
				{
					Clause c = ((Brackets) selected.getTerm()).getClause();
					c.insert(ot, 0);
				}
				else
				{
					Clause c = (Clause) selected.getParent();
					int i = c.indexOf(selected);
					c.insert(ot, i + 1);
				}
			}
			else
			// If we get to here, it must be a BracketEnd.
			{
				Node node = ((BracketEnd) n).getNode();

				if( node instanceof Block )
				{
					((Block) node).getClause().insert(ot, 0);
				}
				else
				{
					Clause c = (Clause) node.getParent();
					int i = c.indexOf((OpTerm) node);
					c.insert(ot, i + 1);
				}
			}
		}

		importStatement(statement);
	}

	// // SETTING AND MODIFYING ROWS ///////////////////////////////////////////

	protected void setNode()
	{
		Comparison comparison = constructComparison();
		int chosenType = typeCombo.getSelectedIndex();
		Node node = (Node) get(currentIndex);

		switch( currentType )
		{
			case WHERE: {
				Block block = (Block) node;

				switch( chosenType )
				{
					case WHERE:
						setInBlock(block, comparison);
						break;

					case WHEREB:
						setInBlock(block, constructBrackets(comparison));
						break;

					default:
						break;
				}
				break;
			}

			case WHEREB: {
				Block block = (Block) node;

				switch( chosenType )
				{
					case WHERE:
						setInBlock(block, comparison);
						break;

					case WHEREB:
						setInBrackets(block, comparison);
						break;

					default:
						break;
				}
				break;
			}

			case AND: {
				OpTerm term = (OpTerm) node;

				switch( chosenType )
				{
					case AND:
						setInOpTerm(term, comparison);
						break;

					case ANDB:
						setInOpTerm(term, constructBrackets(comparison));
						break;

					case OR:
						term.setOperator(new OrOperator());
						setInOpTerm(term, comparison);
						break;

					case ORB:
						term.setOperator(new OrOperator());
						setInOpTerm(term, constructBrackets(comparison));
						break;

					default:
						break;
				}
				break;
			}

			case ANDB: {
				OpTerm term = (OpTerm) node;

				switch( chosenType )
				{
					case AND:
						setInOpTerm(term, comparison);
						break;

					case ANDB:
						setInBrackets(term, comparison);
						break;

					case OR:
						term.setOperator(new OrOperator());
						setInOpTerm(term, comparison);
						break;

					case ORB:
						term.setOperator(new OrOperator());
						setInBrackets(term, comparison);
						break;

					default:
						break;
				}
				break;
			}

			case OR: {
				OpTerm term = (OpTerm) node;

				switch( chosenType )
				{
					case AND:
						term.setOperator(new AndOperator());
						setInOpTerm(term, comparison);
						break;

					case ANDB:
						term.setOperator(new AndOperator());
						setInOpTerm(term, constructBrackets(comparison));
						break;

					case OR:
						setInOpTerm(term, comparison);
						break;

					case ORB:
						setInOpTerm(term, constructBrackets(comparison));
						break;

					default:
						break;
				}
				break;
			}

			case ORB: {
				OpTerm term = (OpTerm) node;

				switch( chosenType )
				{
					case AND:
						term.setOperator(new AndOperator());
						setInOpTerm(term, comparison);
						break;

					case ANDB:
						term.setOperator(new AndOperator());
						setInBrackets(term, comparison);
						break;

					case OR:
						setInOpTerm(term, comparison);
						break;

					case ORB:
						setInBrackets(term, comparison);
						break;

					default:
						break;
				}
				break;
			}

			default:
				break;
		}

		importStatement(statement);
	}

	protected void setInBlock(Block block, Term term)
	{
		block.getClause().setFirst(term);
	}

	protected void setInBrackets(Block block, Term term)
	{
		Brackets brackets = (Brackets) block.getClause().getFirst();
		brackets.getClause().setFirst(term);
	}

	protected void setInOpTerm(OpTerm opTerm, Term term)
	{
		opTerm.setTerm(term);
	}

	protected void setInBrackets(OpTerm opTerm, Term term)
	{
		Brackets brackets = (Brackets) opTerm.getTerm();
		brackets.getClause().setFirst(term);
	}

	// // STATEMENT CREATION ///////////////////////////////////////////////////

	protected Block constructBlock(Term term)
	{
		Clause c = new Clause();
		c.setFirst(term);
		Block b = new Block();
		b.setClause(c);

		return b;
	}

	protected OpTerm constructOpTerm(int type, Term term)
	{
		if( type == AND || type == ANDB )
		{
			return new OpTerm(new AndOperator(), term);

		}
		else if( type == OR || type == ORB )
		{
			return new OpTerm(new OrOperator(), term);

		}
		else
		{
			return null;
		}
	}

	protected Brackets constructBrackets(Term term)
	{
		Brackets b = new Brackets();
		Clause c = new Clause();
		b.setClause(c);
		c.setFirst(term);

		return b;
	}

	protected Comparison constructComparison()
	{
		Equality e = null;

		switch( operatorCombo.getSelectedIndex() )
		{
			case EQUALS:
				e = new Equals();
				break;

			case NOT_EQUALS:
				e = new NotEquals();
				break;

			case LIKE:
				e = new Like();
				break;

			default:
				break;
		}

		return new Comparison(e, xpathField.getSchemaItem(), getValue());
	}

	protected String getValue()
	{
		Object val = valueSelection.getSelectedItem();
		if( val == null )
		{
			val = valueSelection.getEditor().getItem();
		}
		return val.toString();
	}

	protected void populateValues()
	{
		valueSelection.setEditable(false);
		valueSelection.removeAllItems();
		valueSelection.getEditor().setItem(""); //$NON-NLS-1$

		String target = xpathField.getSchemaItem();

		List<String> vs = null;
		if( targetMap != null )
		{
			vs = new ArrayList<String>(targetMap.getValuesForTarget(target));
		}

		if( vs != null )
		{
			Collections.sort(vs);
			for( String v : vs )
			{
				valueSelection.addItem(v);
			}
		}
		else
		{
			valueSelection.setEditable(true);
		}
	}

	public static String encode(String value)
	{
		return value.replaceAll("'", "''"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// // EVENT HANDLERS
	// ////////////////////////////////////////////////////////

	protected class SetHandler implements ActionListener
	{
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			setNode();
		}
	}

	protected class AddHandler implements ActionListener
	{
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				addNode();
				updateButtons();
			}
			catch( NullPointerException ex ) // NOSONAR
			{
				// Jira TLE-2396
				LOGGER.info("Invalid statement", ex); //$NON-NLS-1$
			}
		}
	}

	protected class UpdateHandler implements ActionListener
	{
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			updateButtons();
		}
	}

	protected class LHSHandler implements TargetListener
	{
		/*
		 * (non-Javadoc)
		 * @see com.dytech.edge.admin.schema.TargetListener#targetAdded(
		 * com.dytech.edge.admin.schema.SchemaNode)
		 */
		@Override
		public void targetAdded(String target)
		{
			populateValues();
			updateButtons();
		}

		/*
		 * (non-Javadoc)
		 * @see com.dytech.edge.admin.schema.TargetListener#targetRemoved(
		 * com.dytech.edge.admin.schema.SchemaNode)
		 */
		@Override
		public void targetRemoved(String target)
		{
			// We don't care about this event
		}
	}
}
