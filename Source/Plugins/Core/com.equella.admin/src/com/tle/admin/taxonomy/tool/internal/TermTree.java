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

package com.tle.admin.taxonomy.tool.internal;

import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.dytech.gui.JValidatingTextField;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.admin.common.gui.tree.AbstractTreeEditorTree;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.common.taxonomy.terms.RemoteTermService;
import com.tle.common.taxonomy.terms.Term;

@SuppressWarnings("nls")
public class TermTree extends AbstractTreeEditorTree<TermTreeNode>
{
	private final RemoteTermService termService;
	private final Taxonomy taxonomy;

	TermTree(Taxonomy taxonomy, boolean canAddRootTerms, RemoteTermService termService)
	{
		super(canAddRootTerms);

		this.taxonomy = taxonomy;
		this.termService = termService;

		setup();
	}

	@Override
	public boolean canEdit(TermTreeNode node)
	{
		return true;
	}

	@Override
	protected TermTreeNode createNode()
	{
		return new TermTreeNode();
	}

	@Override
	protected void doAddNewNode(TermTreeNode parent, TermTreeNode newNode, Map<Object, Object> params)
	{
		termService.insertTerm(taxonomy, parent.getFullPath(), newNode.getName(), -1);
		newNode.updateFullPath(parent);
	}

	@Override
	protected void doDelete(TermTreeNode node)
	{
		termService.deleteTerm(taxonomy, node.getFullPath());
	}

	@Override
	protected List<TermTreeNode> doListNodes(final TermTreeNode parent)
	{
		return Lists.newArrayList(Lists.transform(
			termService.listTerms(taxonomy, parent == null ? null : parent.getFullPath()),
			new Function<String, TermTreeNode>()
			{
				@Override
				public TermTreeNode apply(String term)
				{
					TermTreeNode ttn = new TermTreeNode();
					ttn.setName(term);
					ttn.updateFullPath(parent);
					return ttn;
				}
			}));
	}

	@Override
	protected void doMove(TermTreeNode node, TermTreeNode parent, int position)
	{
		termService.move(taxonomy, node.getFullPath(), parent.getFullPath(), position);
	}

	@Override
	protected String promptForName()
	{
		final JValidatingTextField field = new JValidatingTextField(new JValidatingTextField.MaxLength(
			Term.MAX_TERM_VALUE_LENGTH), new JValidatingTextField.DisallowStr(TaxonomyConstants.TERM_SEPARATOR));
		String name = null;
		do
		{
			final int result = JOptionPane.showOptionDialog(this, field,
				CurrentLocale.get("com.tle.admin.gui.common.tree.editor.entername"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, null, null);
			if( result == JOptionPane.CANCEL_OPTION )
			{
				return null;
			}
			name = field.getText();
		}
		while( Check.isEmpty(name) || !isProposedNewNameValid(name) );

		return name.trim();
	}

	@Override
	protected boolean preAddNewNode(TermTreeNode parent, TermTreeNode newNode, Map<Object, Object> params)
	{
		final String newNodeName = newNode.getName();
		for( int i = 0, count = parent.getChildCount(); i < count; i++ )
		{
			TermTreeNode sib = (TermTreeNode) parent.getChildAt(i);
			if( newNodeName.equals(sib.getName()) )
			{
				JOptionPane.showMessageDialog(this, InternalDataSourceTab.s("siblingwithsamename.message"),
					InternalDataSourceTab.s("siblingwithsamename.title"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
}