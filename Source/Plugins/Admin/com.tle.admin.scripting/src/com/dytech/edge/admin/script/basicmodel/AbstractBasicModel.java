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

package com.dytech.edge.admin.script.basicmodel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.basicmodel.ScriptTargetDialog.SelectionType;
import com.dytech.edge.admin.script.ifmodel.Brackets;
import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.Equals;
import com.dytech.edge.admin.script.ifmodel.GreaterThan;
import com.dytech.edge.admin.script.ifmodel.GreaterThanOrEqualTo;
import com.dytech.edge.admin.script.ifmodel.IfModel;
import com.dytech.edge.admin.script.ifmodel.LessThan;
import com.dytech.edge.admin.script.ifmodel.LessThanOrEqualTo;
import com.dytech.edge.admin.script.ifmodel.NotEquals;
import com.dytech.edge.admin.script.model.Term;
import com.dytech.edge.admin.script.options.DefaultScriptOptions;
import com.dytech.edge.admin.script.options.ScriptOptions;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.TargetListener;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.core.remoting.RemoteUserService;

@SuppressWarnings("nls")
public abstract class AbstractBasicModel extends IfModel implements ActionListener
{
	private static final boolean[][] SET_TABLE = {
	/* NONE, BLOCK, OP_TERM, BLOCK_END, BRACKET_END */
	/* IF */{false, true, false, false, false},
	/* IFB */{false, true, false, false, false},
	/* AND */{false, false, true, false, false},
	/* ANDB */{false, false, true, false, false},
	/* OR */{false, false, true, false, false},
	/* ORB */{false, false, true, false, false},};

	private static final boolean[][] ADD_TABLE = {
	/* NONE, BLOCK, OP_TERM, BLOCK_END, BRACKET_END */
	/* IF */{true, true, true, true, true},
	/* IFB */{true, true, true, true, true},
	/* AND */{false, true, true, false, true},
	/* ANDB */{false, true, true, false, true},
	/* OR */{false, true, true, false, true},
	/* ORB */{false, true, true, false, true},};

	protected int currentType;
	protected int currentRow = NONE;

	protected final SchemaModel schema;
	protected final ScriptOptions options;

	protected JPanel editor;
	protected JButton setButton;
	protected JButton addButton;
	protected JComboBox<String> typeCombo;
	protected JComboBox<String> operatorCombo;
	protected JComboBox<NameValue> valueSelection;

	protected Collection<RoleBean> roles;
	protected ScriptTargetChooser xpathField;

	public AbstractBasicModel(SchemaModel schema, ScriptOptions options)
	{
		this.schema = schema;
		this.options = options != null ? options : new DefaultScriptOptions();

		setup();
	}

	public AbstractBasicModel(SchemaModel schema, ScriptOptions options, String script) throws InvalidScriptException
	{
		this(schema, options);
		importScript(new StringReader(script));
	}

	@Override
	public JPanel getStatementEditor()
	{
		return editor;
	}

	protected void updateButtons()
	{
		int index = typeCombo.getSelectedIndex();

		setButton.setEnabled(SET_TABLE[index][currentRow]);
		addButton.setEnabled(ADD_TABLE[index][currentRow]);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.script.ScriptModel#importScript(java.io.Reader)
	 */
	@Override
	public void importScript(Reader r) throws InvalidScriptException
	{
		BasicParser p = new BasicParser(options, new BufferedReader(r));
		importStatement(p.importScript());
	}

	protected void setup()
	{
		editor = new JPanel(new BorderLayout(5, 0));
		editor.setAlignmentX(Component.LEFT_ALIGNMENT);
		editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
			BorderFactory.createEmptyBorder(2, 5, 2, 5)));

		editor.add(createButtons(), BorderLayout.EAST);
		editor.add(createCentre(), BorderLayout.CENTER);
	}

	private JPanel createCentre()
	{
		typeCombo = new JComboBox<>(TYPES.toArray(new String[0]));
		xpathField = new ScriptTargetChooser(schema, options);
		operatorCombo = new JComboBox<>(BASIC_OPERATORS.toArray(new String[0]));
		valueSelection = new JComboBox<>();

		valueSelection.setEditable(true);

		typeCombo.addActionListener(this);
		operatorCombo.addActionListener(this);

		xpathField.addTargetListener(new TargetListener()
		{
			@Override
			public void targetAdded(String target)
			{
				populateValues();
				updateButtons();
			}

			@Override
			public void targetRemoved(String target)
			{
				// We don't care about this event.
			}
		});

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

	private JPanel createButtons()
	{
		addButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.add"));
		setButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.set"));

		addButton.addActionListener(this);
		setButton.addActionListener(this);

		setButton.setEnabled(false);

		JPanel all = new JPanel(new BorderLayout(5, 5));
		all.add(addButton, BorderLayout.NORTH);
		all.add(setButton, BorderLayout.SOUTH);

		return all;
	}

	@Override
	public void dealWithTerm(Term term)
	{
		while( term instanceof Brackets )
		{
			term = ((Brackets) term).getClause().getFirst();
		}

		if( term instanceof ModerationComparison )
		{
			ModerationComparison com = (ModerationComparison) term;
			xpathField.setType(SelectionType.MODERATION);
			dealWithEquality(com.getOperation());
			setValue(String.valueOf(com.getValue()));
		}
		else if( term instanceof StatusComparison )
		{
			StatusComparison com = (StatusComparison) term;
			xpathField.setType(SelectionType.ITEM_STATUS);
			dealWithEquality(com.getOperation());
			setValue(com.getValue());
		}
		else if( term instanceof WorkflowStepComparison )
		{
			WorkflowStepComparison com = (WorkflowStepComparison) term;
			xpathField.setType(SelectionType.WORKFLOW_STEP);
			dealWithEquality(com.getOperation());
			setValue(com.getValue());
		}
		else if( term instanceof TypeComparison )
		{
			TypeComparison com = (TypeComparison) term;
			xpathField.setType(SelectionType.USER_TYPE);
			dealWithEquality(com.getOperation().getOp());
			setValue(com.getValue());
		}
		else if( term instanceof XpathComparison )
		{
			XpathComparison com = (XpathComparison) term;
			xpathField.setType(SelectionType.SCHEMA_ITEM, new Object[]{com.getLHS()});
			dealWithEquality(com.getOperator());
			setValue(com.getRHS());
		}
		else
		{
			ContainsComparison com = (ContainsComparison) term;
			xpathField.setType(SelectionType.SCHEMA_ITEM, new Object[]{com.getXpath()});
			operatorIsContains();
			setValue(com.getValue());
		}
	}

	@Override
	protected void rowIsIf()
	{
		typeCombo.setSelectedIndex(IF);
		currentType = IF;
		currentRow = BLOCK;
		updateButtons();
	}

	@Override
	protected void rowIsIfBracket()
	{
		typeCombo.setSelectedIndex(IFB);
		currentType = IFB;
		currentRow = BLOCK;
		updateButtons();
	}

	@Override
	protected void rowIsAnd()
	{
		typeCombo.setSelectedIndex(AND);
		currentType = AND;
		currentRow = OP_TERM;
		updateButtons();
	}

	@Override
	protected void rowIsAndBracket()
	{
		typeCombo.setSelectedIndex(ANDB);
		currentType = ANDB;
		currentRow = OP_TERM;
		updateButtons();
	}

	@Override
	protected void rowIsOr()
	{
		typeCombo.setSelectedIndex(OR);
		currentType = OR;
		currentRow = OP_TERM;
		updateButtons();
	}

	@Override
	protected void rowIsOrBracket()
	{
		typeCombo.setSelectedIndex(ORB);
		currentType = ORB;
		currentRow = OP_TERM;
		updateButtons();
	}

	@Override
	protected void rowIsBracketEnd()
	{
		typeCombo.setSelectedIndex(IF);
		currentRow = BRACKET_END;
		updateButtons();
	}

	@Override
	protected void rowIsBlockEnd()
	{
		typeCombo.setSelectedIndex(IF);
		currentRow = BLOCK_END;
		updateButtons();
	}

	@Override
	protected void noRowSelected()
	{
		typeCombo.setSelectedIndex(IF);
		currentRow = NONE;
		updateButtons();
	}

	@Override
	protected void operatorIsNotEquals()
	{
		operatorCombo.setSelectedIndex(NOT_EQUALS);
	}

	@Override
	protected void operatorIsEquals()
	{
		operatorCombo.setSelectedIndex(EQUALS);
	}

	@Override
	protected void operatorIsLessThan()
	{
		operatorCombo.setSelectedIndex(LESS_THAN);
	}

	@Override
	protected void operatorIsLessThanOrEqualTo()
	{
		operatorCombo.setSelectedIndex(LESS_OR_EQUAL_TO);
	}

	@Override
	protected void operatorIsGreaterThan()
	{
		operatorCombo.setSelectedIndex(GREATER_THAN);
	}

	@Override
	protected void operatorIsGreaterThanOrEqualTo()
	{
		operatorCombo.setSelectedIndex(GREATER_OR_EQUAL_TO);
	}

	protected void operatorIsContains()
	{
		operatorCombo.setSelectedIndex(CONTAINS);
	}

	protected void setValue(String value)
	{
		populateValues();

		int count = valueSelection.getItemCount();
		for( int i = 0; i < count; i++ )
		{
			NameValue nv = valueSelection.getItemAt(i);
			if( nv.getValue().equals(value) )
			{
				valueSelection.setSelectedIndex(i);
				return;
			}
		}

		// Set the value directly if we get to here
		valueSelection.getEditor().setItem(value);
	}

	// // STATEMENT CREATION ///////////////////////////////////////////////////

	@Override
	protected Comparison constructComparison()
	{
		NameValue value = getValue();
		switch( xpathField.getType() )
		{
			case ITEM_STATUS:
				return new StatusComparison(getEquality(), value.getValue());

			case USER_TYPE:
				return new TypeComparison(getEquality(), value.getValue());

			case WORKFLOW_STEP:
				return new WorkflowStepComparison(getEquality(), value.getValue(), value.getName());

			case MODERATION:
				return new ModerationComparison(getEquality(), value.getValue().equalsIgnoreCase("true"));

			case SCHEMA_ITEM:
				int op = operatorCombo.getSelectedIndex();
				if( op == NOT_EQUALS )
				{
					return new XpathComparison(new NotEquals(), xpathField.getSchemaXpath(), value.getValue());
				}
				else if( op == CONTAINS )
				{
					return new ContainsComparison(xpathField.getSchemaXpath(), value.getValue());
				}
		}
		return null;
	}

	protected NameValue getValue()
	{
		if( valueSelection.isEditable() )
		{
			Object obj = valueSelection.getEditor().getItem();
			if( obj instanceof String )
			{
				String value = (String) obj;
				return new NameValue(value, value);
			}
		}
		return (NameValue) valueSelection.getSelectedItem();
	}

	protected Equality getEquality()
	{
		int type = operatorCombo.getSelectedIndex();
		switch( type )
		{
			case CONTAINS: // same as EQUALS:
				return new Equals();
			case NOT_EQUALS:
				return new NotEquals();
			case LESS_THAN:
				return new LessThan();
			case LESS_OR_EQUAL_TO:
				return new LessThanOrEqualTo();
			case GREATER_THAN:
				return new GreaterThan();
			case GREATER_OR_EQUAL_TO:
				return new GreaterThanOrEqualTo();
			default:
				return null;
		}
	}

	protected void populateValues()
	{
		valueSelection.setEditable(false);
		valueSelection.removeAllItems();
		valueSelection.getEditor().setItem("");
		defaultPopulateValues();
	}

	protected void defaultPopulateValues()
	{
		switch( xpathField.getType() )
		{
			case MODERATION: {
				valueSelection.addItem(new NameValue("true", "true"));
				valueSelection.addItem(new NameValue("false", "false"));
				break;
			}

			case ITEM_STATUS:
				addStatuses();
				break;

			case USER_TYPE:
				addRoles();
				break;

			case WORKFLOW_STEP:
				for( NameValue nv : options.getWorkflowSteps() )
				{
					valueSelection.addItem(nv);
				}
				break;
			default:
				valueSelection.setEditable(true);
		}
	}

	protected void addRoles()
	{
		if( roles == null )
		{
			roles = Driver.instance().getClientService().getService(RemoteUserService.class).searchRoles("*");
			TreeSet<RoleBean> set = new TreeSet<RoleBean>(new NumberStringComparator<RoleBean>());
			set.addAll(roles);
			roles = set;
		}

		for( RoleBean role : roles )
		{
			valueSelection.addItem(new NameValue(role.getName(), role.getUniqueID()));
		}
	}

	protected void addStatuses()
	{
		List<String> statuses = new ArrayList<String>();
		if( !options.restrictItemStatusForModeration() )
		{
			statuses.add("draft");
			statuses.add("live");
		}
		statuses.add("moderating");
		statuses.add("review");
		if( !options.restrictItemStatusForModeration() )
		{
			statuses.add("rejected");
			statuses.add("archived");
			statuses.add("suspended");
		}

		for( String status : statuses )
		{
			valueSelection.addItem(new NameValue(CurrentLocale.get("com.dytech.edge.admin.script.status." + status),
				status));
		}
	}

	// // EVENT HANDLERS
	// ////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == setButton )
		{
			setNode(currentType, typeCombo.getSelectedIndex());
		}
		else if( e.getSource() == addButton )
		{
			addNode(typeCombo.getSelectedIndex());
		}
		else if( e.getSource() == typeCombo || e.getSource() == operatorCombo )
		{
			updateButtons();
		}
	}
}
