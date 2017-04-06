package com.tle.admin.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.JShuffleBox;
import com.tle.admin.Driver;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.i18n.BundleCache;

public abstract class EntityShuffler<STATE> extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected final Class<? extends RemoteAbstractEntityService<?>> serviceClass;

	protected final JCheckBox restrict;
	protected final JShuffleBox<NameValue> shuffle;

	public EntityShuffler(String textKey, Class<? extends RemoteAbstractEntityService<?>> serviceClass)
	{
		this.serviceClass = serviceClass;

		restrict = new JCheckBox(CurrentLocale.get(textKey));
		restrict.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				shuffle.setEnabled(restrict.isSelected());
			}
		});

		shuffle = new JShuffleBox<NameValue>();
		shuffle.setEnabled(false);
		shuffle.setAllowDuplicates(false);

		setLayout(new MigLayout("wrap 1", "[grow, fill]"));

		add(restrict);
		add(shuffle);

		List<BaseEntityLabel> labels = listAll();
		List<NameValue> nameValues = BundleCache.getNameUuidValues(labels);
		Collections.sort(nameValues, Format.NAME_VALUE_COMPARATOR);
		shuffle.addToLeft(nameValues);
	}

	public void load(STATE control)
	{
		boolean restricted = isRestricted(control);
		restrict.setSelected(restricted);

		shuffle.setEnabled(restricted);
		shuffle.removeAllFromLeft();
		shuffle.removeAllFromRight();

		List<BaseEntityLabel> labels = listAll();
		List<NameValue> nameValues = BundleCache.getNameUuidValues(labels);
		Collections.sort(nameValues, Format.NAME_VALUE_COMPARATOR);

		shuffle.addToLeft(nameValues);

		if( restricted )
		{
			Collection<String> uuids = getRestrictedTo(control);
			for( NameValue nv : nameValues )
			{
				if( uuids.contains(nv.getValue()) )
				{
					shuffle.addToRight(nv);
				}
			}
		}
	}

	public void save(STATE control)
	{
		setRestricted(control, restrict.isSelected());

		Set<String> uuids = null;
		if( restrict.isSelected() )
		{
			uuids = new HashSet<String>();

			for( NameValue nv : shuffle.getRight() )
			{
				uuids.add(nv.getValue());
			}
		}
		setRestrictedTo(control, uuids);
	}

	protected List<BaseEntityLabel> listAll()
	{
		return Driver.instance().getClientService().getService(serviceClass).listAll();
	}

	protected abstract boolean isRestricted(STATE control);

	protected abstract Set<String> getRestrictedTo(STATE control);

	protected abstract void setRestricted(STATE control, boolean restricted);

	protected abstract void setRestrictedTo(STATE control, Set<String> uuids);
}
