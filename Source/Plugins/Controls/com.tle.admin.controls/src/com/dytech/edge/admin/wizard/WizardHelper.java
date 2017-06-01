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

package com.dytech.edge.admin.wizard;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.thoughtworks.xstream.XStream;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.admin.schema.TargetChooser;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public final class WizardHelper
{
	/**
	 * Indicates that the current wizard is a contribution wizard.
	 */
	public static final int WIZARD_TYPE_CONTRIBUTION = 0;

	/**
	 * Indicates that the current wizard is an advanced search.
	 */
	public static final int WIZARD_TYPE_POWERSEARCH = 1;

	/**
	 * The array indice relates to the WIZARD_TYPE_* constants.
	 */
	public static final String[] XML_NODES = {"contribution", "powersearch"}; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The array indice relates to the WIZARD_TYPE_* constants.
	 */
	public static final String[] XML_ROOT = {"pages", "search"}; //$NON-NLS-1$ //$NON-NLS-2$ 

	/**
	 * Any array of class types that all editor constructors must take. To keep
	 * Sonar happy we provide a covering call to keep the originals immutable.
	 */
	private static final Class<?>[] EDITOR_PARAM_TYPES = {Control.class, int.class, SchemaModel.class};

	public static Class<?>[] getEditorParamTypes()
	{
		return Arrays.copyOf(EDITOR_PARAM_TYPES, EDITOR_PARAM_TYPES.length);
	}

	/**
	 * Checks if the control is a page.
	 */
	public static boolean isPage(Control control)
	{
		return isStandardPage(control) || isDrmPage(control) || isCalPage(control) || isNavPage(control);
	}

	/**
	 * Checks if the control is a standard page (eg, not DRM);
	 */
	public static boolean isStandardPage(Control control)
	{
		return isStandardPage(control.getControlClass());
	}

	/**
	 * Checks if the control is a page.
	 */
	public static boolean isStandardPage(String id)
	{
		return id.equals("page"); //$NON-NLS-1$
	}

	/**
	 * Checks if this is a DRM page.
	 */
	public static boolean isDrmPage(Control control)
	{
		return isDrmPage(control.getControlClass());
	}

	/**
	 * Checks if the control is a page.
	 */
	public static boolean isDrmPage(String id)
	{
		return id.equals("drm"); //$NON-NLS-1$
	}

	/**
	 * Checks if the control is a page.
	 */
	public static boolean isCalPage(Control control)
	{
		return isCalPage(control.getControlClass());
	}

	/**
	 * Checks if the control is a page.
	 */
	public static boolean isCalPage(String id)
	{
		return id.equals("cal"); //$NON-NLS-1$
	}

	public static boolean isNavPage(Control control)
	{
		return isNavPage(control.getControlClass());
	}

	public static boolean isNavPage(String id)
	{
		return id.equals("nav"); //$NON-NLS-1$
	}

	/**
	 * Checks if this is a group control.
	 */
	public static boolean isGroup(Control control)
	{
		return control.getControlClass().equals("group"); //$NON-NLS-1$
	}

	/**
	 * Checks if this is a group item control.
	 */
	public static boolean isGroupItem(Control control)
	{
		return control.getControlClass().equals("groupitem"); //$NON-NLS-1$
	}

	/**
	 * Checks if this is a metadata control.
	 */
	public static boolean isMetadata(Control control)
	{
		return isMetadata(control.getControlClass());
	}

	/**
	 * Checks if this is a metadata control.
	 */
	public static boolean isMetadata(String id)
	{
		return id.equals("metadata"); //$NON-NLS-1$
	}

	/**
	 * Checks if this is a multi control.
	 */
	public static boolean isMultiControl(Control control)
	{
		return control.getControlClass().equals("multi"); //$NON-NLS-1$
	}

	/**
	 * Checks if this is an editbox control.
	 */
	public static boolean isEditBox(Control control)
	{
		return control.getControlClass().equals("editbox"); //$NON-NLS-1$
	}

	/**
	 * Checks if this is an attachment control.
	 */
	public static boolean isAttachment(Control control)
	{
		return control.getControlClass().equals("attachment"); //$NON-NLS-1$
	}

	public static MultiTargetChooser createMultiTargetChooser(Editor editor)
	{
		String targetBase = editor.getControl().getParent().getTargetBase();
		MultiTargetChooser chooser = new MultiTargetChooser(editor.getSchema(), targetBase);
		commonTargetChooserSetup(chooser, editor);
		return chooser;
	}

	public static SingleTargetChooser createSingleTargetChooser(Editor editor)
	{
		String targetBase = editor.getControl().getParent().getTargetBase();
		SingleTargetChooser chooser = new SingleTargetChooser(editor.getSchema(), targetBase);
		commonTargetChooserSetup(chooser, editor);
		return chooser;
	}

	private static void commonTargetChooserSetup(TargetChooser chooser, Editor editor)
	{
		chooser.addTargetListener(new CheckDuplicateTargetsHandler(editor, editor.getControl()));

		if( editor.getWizardType() == WizardHelper.WIZARD_TYPE_POWERSEARCH )
		{
			chooser.setWarnAboutNonFields(true);
			chooser.addTargetListener(new CheckTargetIsFieldHandler(editor.getSchema()));
		}
	}

	public static void loadSchemaChooser(MultiTargetChooser picker, WizardControl control)
	{
		picker.setTargets(convertTargetNodes(control.getTargetnodes()));
	}

	private static String convertTargetNode(TargetNode node)
	{
		String target = node.getTarget();
		if( Check.isEmpty(target) )
		{
			target = "/"; //$NON-NLS-1$
		}
		String attribute = node.getAttribute();
		if( Check.isEmpty(attribute) )
		{
			return target;
		}
		return target + '@' + attribute;
	}

	public static List<String> convertTargetNodes(Collection<TargetNode> targetnodes)
	{
		List<String> snodes = new ArrayList<String>();
		for( TargetNode node : targetnodes )
		{
			snodes.add(convertTargetNode(node));
		}
		return snodes;
	}

	public static void loadSchemaChooser(SingleTargetChooser picker, WizardControl control)
	{
		if( !control.getTargetnodes().isEmpty() )
		{
			String target = convertTargetNode(control.getTargetnodes().get(0));
			picker.setTarget(target);
		}
	}

	public static void saveSchemaChooser(MultiTargetChooser picker, WizardControl control)
	{
		control.getTargetnodes().clear();
		control.getTargetnodes().addAll(convertToTargetNodes(picker.getTargets()));
	}

	private static TargetNode convertToTargetNode(String target)
	{
		int ind = target.indexOf('@');
		if( ind >= 0 )
		{
			return new TargetNode(target.substring(0, ind), target.substring(ind + 1));
		}
		return new TargetNode(target, ""); //$NON-NLS-1$
	}

	private static List<TargetNode> convertToTargetNodes(List<String> targets)
	{
		List<TargetNode> tnodes = new ArrayList<TargetNode>();
		for( String target : targets )
		{
			tnodes.add(convertToTargetNode(target));
		}
		return tnodes;
	}

	public static void saveSchemaChooser(SingleTargetChooser picker, WizardControl control)
	{
		control.getTargetnodes().clear();
		if( picker.hasTarget() )
		{
			control.getTargetnodes().add(convertToTargetNode(picker.getTarget()));
		}
	}

	public static String getXmlForComparison(Object wizard)
	{
		return new XStream().toXML(wizard);
	}

	// ////// OLD STUFF //////////////////////////////////////////

	public static Control getPage(Control control)
	{
		Control page = control;
		while( page != null && !WizardHelper.isPage(page) )
		{
			page = page.getParent();
		}
		return page;
	}

	public static Control getRoot(Control control)
	{
		Control root = control;
		while( root.getParent() != null )
		{
			root = root.getParent();
		}
		return root;
	}

	public static JComponent createItems(JComponent chooser, String text)
	{
		JLabel heading = new JLabel(text);

		JPanel all = new JPanel(new BorderLayout(5, 5));

		all.add(heading, BorderLayout.NORTH);
		all.add(chooser, BorderLayout.CENTER);

		return all;
	}

	public static JComponent createMetaData(TargetChooser chooser)
	{
		JLabel heading = new JLabel(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardhelper.select")); //$NON-NLS-1$

		JPanel all = new JPanel(new BorderLayout(5, 5));

		all.add(heading, BorderLayout.NORTH);
		all.add(chooser, BorderLayout.CENTER);

		return all;
	}

	private WizardHelper()
	{
		throw new Error();
	}
}
