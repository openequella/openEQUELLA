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

package com.tle.core.taxonomy.schema;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.hibernate.SQLQuery;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.common.taxonomy.wizard.TermSelectorControl;
import com.tle.common.taxonomy.wizard.TermSelectorControl.TermStorageFormat;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.xml.service.XmlService;

@Bind
@SuppressWarnings("nls")
public class MigrateOldTaxonomyToNew extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(MigrateOldTaxonomyToNew.class)
		+ ".migration.";

	@Inject
	private XmlService xmlService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		System.out.println("create migrate old taxonomy");
		return new MigrationInfo(KEY_PREFIX + "title", KEY_PREFIX + "description");
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{TaxonomyNode.class, FakeTerm.class, FakeTermAttribute.class, FakeTaxonomy.class,
				FakeBaseEntity.class, LanguageBundle.class, Institution.class, LanguageString.class,
				FakeItemdefBlobs.class};
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getCreationSql(new TablesOnlyFilter("taxonomy", "term", "term_attributes"));
		for( String s : sql )
		{
			System.out.println(s);

		}
		sql.add(helper.getAddNamedIndex("taxonomy_node_all_parents", "temptnap", "all_parents_id"));
		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropTableSql("taxonomy_node_all_parents", "taxonomy_node");
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM TaxonomyNode") + count(session, "FROM ItemdefBlobs");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws XPathExpressionException
	{
		final Date now = new Date();
		final Map<Long, Integer> totalChildCounts = getTotalChildCounts(session);

		// Convert TaxonomyNodes to Taxonomy & Terms
		final List<TaxonomyNode> roots = session.createQuery("FROM TaxonomyNode WHERE parent IS NULL").list();
		for( TaxonomyNode root : roots )
		{
			final FakeTaxonomy taxonomy = new FakeTaxonomy();
			taxonomy.uuid = root.getUuid();
			taxonomy.institution = root.getInstitution();
			taxonomy.dataSourcePluginId = TaxonomyConstants.INTERNAL_DATASOURCE;
			taxonomy.name = LangUtils.createTextTempLangugageBundle(root.getName());
			taxonomy.dateCreated = now;
			taxonomy.dateModified = now;

			session.save(taxonomy);
			result.incrementStatus();

			processTermChildren(totalChildCounts, result, session, taxonomy, root, null);

			session.flush();
		}

		// Convert Pick List controls to Term Selector controls
		final XStream xstream = xmlService.createDefault(getClass().getClassLoader());
		xstream.alias("com.dytech.edge.wizard.beans.control.PickList", PickList.class);
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression xpression = xpath.compile("//com.dytech.edge.wizard.beans.control.PickList");

		final List<FakeItemdefBlobs> idbs = session.createQuery("FROM ItemdefBlobs").list();
		for( FakeItemdefBlobs idb : idbs )
		{
			boolean changed = false;

			PropBagEx wizardBag = new PropBagEx(idb.getWizard());
			Document wizardDocument = wizardBag.getRootElement().getOwnerDocument();
			NodeList matched = (NodeList) xpression.evaluate(wizardBag.getRootElement(), XPathConstants.NODESET);

			for( int i = 0; i < matched.getLength(); i++ )
			{
				Node pickNode = matched.item(i);
				PickList pickList = (PickList) xstream.fromXML(new PropBagEx(pickNode).toString());
				CustomControl customControl = convertPicklist(pickList);
				PropBagEx newControl = new PropBagEx(xstream.toXML(customControl));
				Element newControlElem = newControl.getRootElement();
				pickNode.getParentNode().replaceChild(wizardDocument.importNode(newControlElem, true), pickNode);
				changed = true;
			}
			if( changed )
			{
				idb.setWizard(wizardBag.toString());
				session.update(idb);
				session.flush();
			}

			result.incrementStatus();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<Long, Integer> getTotalChildCounts(Session session)
	{
		// get total child counts in one hit
		// NOTE THAT THIS IS NOT HQL!!! IT IS PRETTY MUCH SQL!!!
		final StringBuilder sql = new StringBuilder(
			"SELECT all_parents_id, COUNT(*) AS CHILD_COUNT FROM taxonomy_node_all_parents GROUP BY all_parents_id");

		final Map<Long, Integer> totalChildCounts = new HashMap<Long, Integer>();
		final SQLQuery countQuery = session.createSQLQuery(sql.toString());
		final List<Object[]> countResults = countQuery.list();
		for( Object[] countResult : countResults )
		{
			totalChildCounts.put(((Number) countResult[0]).longValue(), ((Number) countResult[1]).intValue());
		}
		return totalChildCounts;
	}

	@SuppressWarnings("unchecked")
	private void processTermChildren(Map<Long, Integer> totalChildCounts, MigrationResult result, Session session,
		FakeTaxonomy taxonomy, TaxonomyNode parentNode, FakeTerm parentTerm)
	{
		int left = parentTerm == null ? 0 : parentTerm.left + 1;

		// Oracle bitched about ScrollableResults
		final List<TaxonomyNode> nodes = session.createQuery("FROM TaxonomyNode WHERE parent = :parent ORDER BY name")
			.setParameter("parent", parentNode).list();
		for( TaxonomyNode tn : nodes )
		{
			if( !Check.isEmpty(tn.getName()) )
			{
				Integer childCount = totalChildCounts.get(tn.getId());
				if( childCount == null )
				{
					childCount = 0;
				}
				final int right = left + (childCount * 2) + 1;

				final FakeTerm t = new FakeTerm();
				t.left = left;
				t.right = right;
				t.taxonomy = taxonomy;
				t.parent = parentTerm;
				t.value = tn.getName();
				t.fullValue = tn.getFullpath();

				session.save(t);
				session.flush();
				session.clear();
				result.incrementStatus();

				// Move the left count along
				left = right + 1;

				// Recurse
				processTermChildren(totalChildCounts, result, session, taxonomy, tn, t);
			}
			else
			{
				result.incrementStatus();
			}
		}
	}

	private CustomControl convertPicklist(PickList plc)
	{
		final TermSelectorControl tsc = new TermSelectorControl();

		tsc.setAfterSaveScript(plc.getAfterSaveScript());
		tsc.setAllowMultiple(plc.isMultiple());
		tsc.setCustomName(plc.getCustomName());
		tsc.setDescription(plc.getDescription());
		tsc.setDisplayType("popupBrowser");
		tsc.setMandatory(plc.isMandatory());
		tsc.setReload(plc.isReload());
		tsc.setScript(plc.getScript());
		tsc.setSelectedTaxonomy(plc.getTaxonomy());
		tsc.setSelectionRestriction(SelectionRestriction.UNRESTRICTED);
		tsc.setTargetnodes(plc.getTargetnodes());
		tsc.setTermStorageFormat(TermStorageFormat.FULL_PATH);
		tsc.setTitle(plc.getTitle());
		tsc.setValidateScript(plc.getValidateScript());

		// Swap the PickList for the TermSelector!
		return new CustomControl(tsc);
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		String uuid;
		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		Institution institution;
		@Column(nullable = false)
		Date dateModified;
		@Column(nullable = false)
		Date dateCreated;
		LanguageBundle name;
	}

	@AccessType("field")
	@Entity(name = "Taxonomy")
	public static class FakeTaxonomy extends FakeBaseEntity
	{
		@Column(length = 100)
		private String dataSourcePluginId;
	}

	@Entity(name = "Term")
	@AccessType("field")
	public static class FakeTerm
	{
		@Id
		long id;

		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "term_parent")
		private FakeTerm parent;

		@Column(name = "lft")
		@Index(name = "term_left_position")
		@XStreamOmitField
		private int left;

		@Column(name = "rht")
		@Index(name = "term_right_position")
		@XStreamOmitField
		private int right;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		FakeTaxonomy taxonomy;

		@Column(length = 4000)
		@Type(type = "blankable")
		@Index(name = "term_full_value")
		String fullValue;

		@Column(length = 1024, nullable = false)
		private String value;

		@Column(length = 32)
		private String valueHash;
	}

	@Entity(name = "ItemdefBlobs")
	@AccessType("field")
	public static class FakeItemdefBlobs implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Id
		public long id;

		@Lob
		private String wizard;

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public String getWizard()
		{
			return wizard;
		}

		public void setWizard(String wizard)
		{
			this.wizard = wizard;
		}
	}

	@Entity(name = "TermAttributes")
	@AccessType("field")
	public static class FakeTermAttribute
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@XStreamOmitField
		long id;

		@ManyToOne
		@JoinColumn(name = "term_id", insertable = false, nullable = false, updatable = false)
		@XStreamOmitField
		@Index(name = "termAttrIndex")
		private FakeTerm term;

		@Column(length = 64, nullable = false)
		private String key;

		@Lob
		private String value;

		public FakeTermAttribute()
		{
			// Required by Hibernate
		}

		public FakeTermAttribute(String key, String value)
		{
			this.key = key;
			this.value = value;
		}

		public String getKey()
		{
			return key;
		}

		public String getValue()
		{
			return Check.nullToEmpty(value);
		}

		public FakeTerm getTerm()
		{
			return term;
		}
	}

}
