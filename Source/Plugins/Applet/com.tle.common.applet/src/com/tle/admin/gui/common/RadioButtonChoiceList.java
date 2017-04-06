package com.tle.admin.gui.common;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.dytech.common.collections.Iter8;
import com.google.common.collect.Lists;

@SuppressWarnings("nls")
public abstract class RadioButtonChoiceList<STATE_TYPE, CHOICE_TYPE>
	extends
		AbstractChoiceList<STATE_TYPE, CHOICE_TYPE, AbstractButton>
{
	private ButtonGroup buttonGroup;

	public RadioButtonChoiceList()
	{
		setLayout(new MigLayout("insets 0, wrap, hidemode 3", "[grow]"));

		buttonGroup = new ButtonGroup();

		updateChoicePanels();

		changeDetector.watch(buttonGroup);
	}

	@Override
	protected void removeAndIgnoreOldComponents()
	{
		changeDetector.ignore(buttonGroup);
		for( AbstractButton b : Lists.newArrayList(new Iter8<AbstractButton>(buttonGroup.getElements()).iterator()) )
		{
			buttonGroup.remove(b);
		}
		removeAll();
	}

	@Override
	protected void _loadChoices(Iterable<CHOICE_TYPE> choiceList)
	{
		for( CHOICE_TYPE choice : choiceList )
		{
			final JRadioButton button = new JRadioButton(getChoiceTitle(choice));
			button.addItemListener(buttonListener);
			buttonGroup.add(button);

			final DynamicChoicePanel<STATE_TYPE> choicePanel = getChoicePanel(choice);
			choicePanel.setId(getChoiceId(choice));

			add(button);
			add(choicePanel, "gap indent, grow");

			choices.put(button, choicePanel);
		}
	}

	@Override
	protected void updateChoicePanels()
	{
		for( Entry<AbstractButton, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet() )
		{
			choice.getValue().setVisible(isEnabledAndSelected(choice.getKey()));
		}
	}

	@Override
	public boolean isSelectionEmpty()
	{
		for( AbstractButton button : choices.keySet() )
		{
			if( button.isSelected() )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	protected void setChoicesEnabled(boolean enabled)
	{
		for( AbstractButton button : choices.keySet() )
		{
			button.setEnabled(enabled);
		}
	}

	@Override
	protected boolean isEnabledAndSelected(AbstractButton button)
	{
		return button.isEnabled() && button.isSelected();
	}

	@Override
	protected void setSelectedChoiceComponent(AbstractButton button)
	{
		button.setSelected(true);
	}

	private final ItemListener buttonListener = new ItemListener()
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			AbstractButton button = (AbstractButton) e.getSource();
			if( button.isSelected() )
			{
				choiceSelected(button);
			}
			else
			{
				choiceDeselected(button);
			}
		}
	};
}
