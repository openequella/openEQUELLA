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

package com.dytech.edge.admin.script.ifmodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;

import com.dytech.edge.admin.script.Row;
import com.dytech.edge.admin.script.ScriptModel;
import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.Node;
import com.dytech.edge.admin.script.model.OpTerm;
import com.dytech.edge.admin.script.model.Operator;
import com.dytech.edge.admin.script.model.Term;

/**
 * This controls the data model of an "If" statement. This should be used along
 * with the <code>ScriptView</code> class in order to display and edit a
 * statement. Given a Statement data structure, the model will display on Block
 * and OpTerm elements in the ScriptView. Each Block and OpTerm will display
 * their first Term in the same row as themselves. <b>Warning:</b> The code is
 * ugly, and I'm not proud of it!! It needs some serious refactoring, and some
 * bits need a scraping. I'll look in to it more when I have time :)
 * 
 * @author Nicholas Read
 */
@SuppressWarnings({"nls", "rawtypes"})
public abstract class IfModel extends DefaultListModel implements ScriptModel
{
	protected static final List<String> TYPES = Collections.unmodifiableList(Arrays.asList("if", "if (", "and",
		"and (", "or", "or ("));

	protected static final List<String> BASIC_OPERATORS = Collections.unmodifiableList(Arrays.asList("=", "!="));

	protected static final String[] EXTENDED_OPERATORS = new String[]{"=", "!=", "<", ">", "<=", ">="};

	protected static final int CONTAINS = 0;
	protected static final int EQUALS = 0;
	protected static final int NOT_EQUALS = 1;
	protected static final int LESS_THAN = 2;
	protected static final int GREATER_THAN = 3;
	protected static final int LESS_OR_EQUAL_TO = 4;
	protected static final int GREATER_OR_EQUAL_TO = 5;

	protected static final int IF = 0;
	protected static final int IFB = 1;
	protected static final int AND = 2;
	protected static final int ANDB = 3;
	protected static final int OR = 4;
	protected static final int ORB = 5;

	protected static final int NONE = 0;
	protected static final int BLOCK = 1;
	protected static final int OP_TERM = 2;
	protected static final int BLOCK_END = 3;
	protected static final int BRACKET_END = 4;

	protected static final String tab = "&nbsp;&nbsp;&nbsp;"; //$NON-NLS-1$

	protected int currentIndex;
	protected Statement statement;

	/**
	 * Constructor for <code>IfModel</code>.
	 */
	public IfModel()
	{
		statement = new Statement();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.script.ScriptModel#toScript()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.script.ScriptModel#allowRemoval(com.dytech.edge
	 * .admin.script.Row)
	 */
	@Override
	public boolean allowRemoval(Row r)
	{
		Node n = (Node) r;
		return n instanceof Block || n instanceof OpTerm;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.script.ScriptModel#clearScript()
	 */
	@Override
	public void clearScript()
	{
		statement.removeAll();
		this.clear();
	}

	@Override
	public void clear()
	{
		rowSelected(null, -1);
		super.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.script.ScriptModel#remove(com.dytech.edge.admin
	 * .script.Row)
	 */
	@Override
	public void remove(Row r)
	{
		Node n = (Node) r;
		if( n instanceof Block )
		{
			Statement s = (Statement) n.getParent();
			s.removeBlock((Block) n);
			importStatement(statement);
		}
		else if( n instanceof OpTerm )
		{
			Clause c = (Clause) n.getParent();
			c.remove((OpTerm) n);
			importStatement(statement);
		}

		// if( instanceof BracketEnd or BlockEnd )
		// We don't do anything if it is a fake row. We actually should
		// never get to this case since it should be blocked at the user
		// interface.
	}

	// // ROWS SELECTION AND NOTIFICATIONS ////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.script.ScriptModel#rowSelected(com.dytech.edge.
	 * admin.script.Row, int)
	 */
	@Override
	public void rowSelected(Row r, int index)
	{
		currentIndex = index;
		if( r == null )
		{
			noRowSelected();
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
			fakeSelected((Fake) r);
		}
	}

	protected void dealWithEquality(Equality equality)
	{
		if( equality instanceof NotEquals )
		{
			operatorIsNotEquals();
		}
		else if( equality instanceof Equals )
		{
			operatorIsEquals();
		}
		else if( equality instanceof LessThan )
		{
			operatorIsLessThan();
		}
		else if( equality instanceof LessThanOrEqualTo )
		{
			operatorIsLessThanOrEqualTo();
		}
		else if( equality instanceof GreaterThan )
		{
			operatorIsGreaterThan();
		}
		else if( equality instanceof GreaterThanOrEqualTo )
		{
			operatorIsGreaterThanOrEqualTo();
		}
	}

	private Term blockSelected(Block block)
	{
		Term term = block.getClause().getFirst();
		if( term instanceof Brackets )
		{
			rowIsIfBracket();
		}
		else
		{
			rowIsIf();
		}
		return term;
	}

	private Term opTermSelected(OpTerm opTerm)
	{
		Operator op = opTerm.getOperator();
		Term term = opTerm.getTerm();

		if( op instanceof AndOperator )
		{
			if( term instanceof Brackets )
			{
				rowIsAndBracket();
			}
			else
			{
				rowIsAnd();
			}
		}
		else
		{
			if( term instanceof Brackets )
			{
				rowIsOrBracket();
			}
			else
			{
				rowIsOr();
			}
		}

		return term;
	}

	private void fakeSelected(Fake fake)
	{
		if( fake instanceof BlockEnd )
		{
			rowIsBlockEnd();
		}
		else
		{
			rowIsBracketEnd();
		}
	}

	// // ABSTRACT METHODS ////////////////////////////////////////////////////

	protected abstract void dealWithTerm(Term term);

	protected abstract void noRowSelected();

	protected abstract void rowIsIf();

	protected abstract void rowIsIfBracket();

	protected abstract void rowIsAnd();

	protected abstract void rowIsAndBracket();

	protected abstract void rowIsOr();

	protected abstract void rowIsOrBracket();

	protected abstract void rowIsBlockEnd();

	protected abstract void rowIsBracketEnd();

	protected abstract void operatorIsNotEquals();

	protected abstract void operatorIsEquals();

	protected abstract void operatorIsLessThan();

	protected abstract void operatorIsLessThanOrEqualTo();

	protected abstract void operatorIsGreaterThan();

	protected abstract void operatorIsGreaterThanOrEqualTo();

	// // STATEMENT CREATION
	// ////////////////////////////////////////////////////

	protected abstract Comparison constructComparison();

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

	// // ADDING NEW ROWS
	// ///////////////////////////////////////////////////////

	protected void addNode(int type)
	{
		Term term = constructComparison();
		if( type == IFB || type == ANDB || type == ORB )
		{
			term = constructBrackets(term);
		}

		if( type == IF || type == IFB )
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
		int count = 0;
		for( int i = 0; i < index; i++ )
		{
			if( get(index) instanceof Block )
			{
				++count;
			}
		}

		if( index == -1 )
		{
			statement.addBlock(b);
		}
		else
		{
			statement.insertBlock(b, count + 1);
		}

		importStatement(statement);
	}

	protected void addOpTerm(OpTerm ot, int index)
	{
		Node n = (Node) get(index);
		if( n instanceof Block )
		{
			Clause c = ((Block) n).getClause();
			Term t = c.getFirst();
			if( t instanceof Brackets )
			{
				c = ((Brackets) t).getClause();
			}
			c.insert(ot, 1);
		}
		else if( n instanceof OpTerm )
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
		{
			// If we get to here, it must be a BracketEnd. It should never be
			// a BlockEnd since the user interface should prevent this.
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
		importStatement(statement);
	}

	// // SETTING AND MODIFYING OF ROWS
	// /////////////////////////////////////////

	protected void setNode(int currentType, int chosenType)
	{
		Comparison comparison = constructComparison();
		Node node = (Node) get(currentIndex);

		switch( currentType )
		{
			case IF: {
				Block block = (Block) node;
				switch( chosenType )
				{
					case IF:
						setInBlock(block, comparison);
						break;

					case IFB:
						setInBlock(block, constructBrackets(comparison));
						break;

					default:
						break;
				}
				break;
			}
			case IFB: {
				Block block = (Block) node;
				switch( chosenType )
				{
					case IF:
						setInBlock(block, comparison);
						break;

					case IFB:
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

	// // IMPORT METHODS
	// ////////////////////////////////////////////////////////

	protected void importStatement(Statement statement)
	{
		clear();

		// currentIndex = -1;
		this.statement = statement;
		List<Block> blocks = statement.getBlocks();

		if( !blocks.isEmpty() )
		{
			includeBlocks(blocks);
		}
	}

	protected void includeBlocks(List<Block> blocks)
	{
		boolean first = true;
		for( Block b : blocks )
		{
			b.setText(b.toEasyRead(first));
			first = false;

			includeClause(b.getClause(), b, tab);
		}

		this.addElement(new BlockEnd());
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

	public static String encode(String value)
	{
		String temp = value.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		return temp.replaceAll("'", "\\\\'"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
