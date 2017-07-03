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

package com.tle.core.taxonomy.institution.migration;

import static com.tle.common.taxonomy.TaxonomyConstants.PRE41_TAXONOMY_EXPORT_FOLDER;
import static com.tle.common.taxonomy.TaxonomyConstants.TAXONOMY_EXPORT_FOLDER;
import static com.tle.common.taxonomy.TaxonomyConstants.TERMS_EXPORT_FOLDER;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.common.taxonomy.terms.Term;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.impl.BaseEntityXmlConverter;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.TreeNodeCreator;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.institution.convert.importhandler.ImportHandler;
import com.tle.core.institution.convert.importhandler.MultiFileImportHandler;
import com.tle.core.institution.convert.importhandler.SingleTreeNodeFileImportHandler;
import com.tle.core.taxonomy.schema.TaxonomyNode;

/**
 * Changes com.tle.beans.tree.TaxonomyNode to
 * com.tle.core.taxonomy.schema.TaxonomyNode within the taxonomy XML file(s),
 * then it converts TaxonomyNodes into Terms and writes the XML out into the
 * taxonomy2/terms/TAXUUID/ folder where they will be expected to be found in
 * the future.
 * 
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class TaxonomyNodeToTermXmlMigrator extends XmlMigrator implements TreeNodeCreator<TaxonomyNode>
{
	private static final String OLD_TAXONOMY_NODE_CLASS = "com.tle.beans.tree.TaxonomyNode"; //$NON-NLS-1$

	@Inject
	private EntityRegistry registry;
	private XStream xstream;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, final ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile sourceFolder = new SubTemporaryFile(staging, PRE41_TAXONOMY_EXPORT_FOLDER);
		final SubTemporaryFile taxonomiesFolder = new SubTemporaryFile(staging, TAXONOMY_EXPORT_FOLDER);

		final ImportHandler<TaxonomyNode> importHandler = (fileSystemService.fileExists(sourceFolder,
			TaxonomyConstants.SINGLE_TAXONOMIES_FILE)
				? new SingleTreeNodeFileImportHandler<TaxonomyNode>(sourceFolder,
					TaxonomyConstants.SINGLE_TAXONOMIES_FILE, xmlHelper, this, getXStream())
				: new MultiFileImportHandler<TaxonomyNode>(sourceFolder, xmlHelper, getXStream()));

		DefaultMessageCallback message = new DefaultMessageCallback(
			"com.tle.core.taxonomy.converter.nodetoterm.progressmessage");
		params.setMessageCallback(message);
		message.setType(CurrentLocale.get("com.tle.core.taxonomy.type.taxonomynode"));
		message.setTotal(importHandler.getNodeCount());

		// write export format
		xmlHelper.writeExportFormatXmlFile(taxonomiesFolder, true);

		final Map<Long, Term> nodeId2Term = new HashMap<Long, Term>();
		final Map<Long, Taxonomy> nodeId2Taxonomy = new HashMap<Long, Taxonomy>();
		final Progress p = new Progress();
		for( final Iterator<TaxonomyNode> iter = importHandler.iterateNodes(); iter.hasNext(); )
		{
			final TaxonomyNode node = iter.next();

			// Taxonomy is based on the root nodes
			// (all old style taxonomies are rooted at a single node)
			if( node.getParent() == null )
			{
				writeTaxonomyXmlFileFromRootNode(taxonomiesFolder, instInfo.getInstitution(), node, nodeId2Taxonomy);
			}
			else
			{
				writeTermXmlFileFromNode(taxonomiesFolder, node, instInfo.getInstitution(), params, nodeId2Term,
					nodeId2Taxonomy, p);
			}

			message.incrementCurrent();
		}
	}

	protected void writeTaxonomyXmlFileFromRootNode(final SubTemporaryFile taxonomiesFolder,
		final Institution institution, final TaxonomyNode node, Map<Long, Taxonomy> nodeId2Taxonomy)
	{
		final Taxonomy taxonomy = new Taxonomy();
		taxonomy.setUuid(node.getUuid());
		final LanguageBundle nameBundle = new LanguageBundle();
		LangUtils.setString(nameBundle, CurrentLocale.getLocale(), node.getName());
		taxonomy.setName(nameBundle);
		taxonomy.setDataSourcePluginId(TaxonomyConstants.INTERNAL_DATASOURCE);
		taxonomy.setInstitution(institution);
		final Date now = new Date();
		taxonomy.setDateCreated(now);
		taxonomy.setDateModified(now);

		final String uuid = taxonomy.getUuid();
		final BucketFile taxonomyBucketFolder = new BucketFile(taxonomiesFolder, uuid);
		final SubTemporaryFile actualTaxonomyFolder = new SubTemporaryFile(taxonomyBucketFolder, uuid);
		xmlHelper.writeXmlFile(taxonomyBucketFolder, uuid + ".xml", taxonomy, getXStream());

		// make the terms folder
		final SubTemporaryFile taxonomyTermsFolder = new SubTemporaryFile(actualTaxonomyFolder, TERMS_EXPORT_FOLDER);
		xmlHelper.writeExportFormatXmlFile(taxonomyTermsFolder, true);
		nodeId2Taxonomy.put(node.getId(), taxonomy);
	}

	protected void writeTermXmlFileFromNode(final SubTemporaryFile taxonomiesFolder, final TaxonomyNode node,
		Institution institution, ConverterParams params, final Map<Long, Term> nodeId2Term,
		Map<Long, Taxonomy> nodeId2Taxonomy, Progress progress)
	{
		final Term parentTerm = getParentTerm(node, nodeId2Term);

		Term newTerm = new Term();
		newTerm.setParent(parentTerm);
		Taxonomy taxonomy;
		if( parentTerm == null )
		{
			taxonomy = nodeId2Taxonomy.get(node.getParent().getId());
		}
		else
		{
			taxonomy = parentTerm.getTaxonomy();
		}
		final String uuid = taxonomy.getUuid();
		final BucketFile taxonomyBucketFolder = new BucketFile(taxonomiesFolder, uuid);
		final SubTemporaryFile actualTaxonomyFolder = new SubTemporaryFile(taxonomyBucketFolder, uuid);
		final SubTemporaryFile taxonomyTermsFolder = new SubTemporaryFile(actualTaxonomyFolder, TERMS_EXPORT_FOLDER);
		newTerm.setTaxonomy(taxonomy);
		newTerm.setValue(node.getName());
		newTerm.setFullValue(node.getFullpath());
		// fake it
		newTerm.setId(node.getId());

		nodeId2Term.put(node.getId(), newTerm);

		// now xstream it
		final BucketFile termBucketFolder = new BucketFile(taxonomyTermsFolder, progress.counter);
		xmlHelper.writeXmlFile(termBucketFolder, progress.counter + ".xml", newTerm, getXStream());
		++progress.counter;
	}

	private Term getParentTerm(TaxonomyNode node, Map<Long, Term> nodeIdToTerm)
	{
		return node.getParent() == null ? null : nodeIdToTerm.get(node.getParent().getId());
	}

	@Override
	public TaxonomyNode createNode()
	{
		return new TaxonomyNode();
	}

	private XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = xmlHelper.createXStream(getClass().getClassLoader());
			final Set<Class<? extends BaseEntity>> classes = new HashSet<Class<? extends BaseEntity>>();
			classes.add(Taxonomy.class);
			xstream.registerConverter(new BaseEntityXmlConverter(classes, registry));
			xstream.alias(OLD_TAXONOMY_NODE_CLASS, TaxonomyNode.class);
		}
		return xstream;
	}

	protected static class Progress
	{
		long counter;
	}
}
