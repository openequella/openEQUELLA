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

package com.tle.core.taxonomy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.LockedException;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.common.taxonomy.terms.Term;
import com.tle.core.entity.service.EntityLockingService;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.institution.convert.XmlHelper;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.taxonomy.TermDao;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.TermService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
@Bind(TermService.class)
@Singleton
public class TermServiceImpl implements TermService
{
	private static final Logger LOGGER = Logger.getLogger(TermService.class);

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(TermServiceImpl.class);

	@Inject
	private TermDao termDao;
	@Inject
	private EntityLockingService lockingService;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private XmlHelper xmlHelper;

	@Override
	public List<String> listTerms(Taxonomy taxonomy, String parentFullPath)
	{
		return Check.isEmpty(parentFullPath) ? termDao.getRootTermValues(taxonomy)
			: termDao.getChildTermValues(getTerm(taxonomy, parentFullPath));
	}

	@Override
	public TermResult getTermResult(Taxonomy taxonomy, String fullTermPath)
	{
		final Term term = termDao.getTerm(taxonomy, fullTermPath);

		if( term == null )
		{
			return null;
		}
		return new TermResult(term.getValue(), term.getFullValue(), term.isLeaf(), term.getUuid());
	}

	@Override
	public List<TermResult> listTermResults(Taxonomy taxonomy, String parentFullPath)
	{
		return Check.isEmpty(parentFullPath) ? termDao.getRootTermResults(taxonomy)
			: termDao.getChildTermResults(getTerm(taxonomy, parentFullPath));
	}

	@Override
	public Pair<Long, List<TermResult>> searchTerms(Taxonomy taxonomy, String query, SelectionRestriction restriction,
		int limit, boolean searchFullTerms)
	{
		final String searchField = (searchFullTerms ? "fullValue" : "value");
		final Criterion c1 = Restrictions.eq("taxonomy", taxonomy);
		final Criterion c2 = Restrictions.ilike(searchField, query.replace('*', '%'));
		final Criterion c3 = getSearchRestrictionCriterion(restriction);
		final long totalCount = termDao.countByCriteria(c1, c2, c3);

		List<TermResult> results = Collections.emptyList();
		if( totalCount > 0 )
		{
			List<Term> terms = termDao.findAllByCriteria(Order.asc(searchField), -1, limit, c1, c2, c3);
			results = Lists.transform(terms, new Function<Term, TermResult>()
			{
				@Override
				public TermResult apply(Term term)
				{
					return new TermResult(term.getValue(), term.getFullValue(), term.isLeaf());
				}
			});
		}

		return new Pair<Long, List<TermResult>>(totalCount, results);
	}

	private Criterion getSearchRestrictionCriterion(SelectionRestriction restriction)
	{
		switch( restriction )
		{
			case TOP_LEVEL_ONLY:
				return Restrictions.isNull("parent");

			case LEAF_ONLY:
				return Restrictions.sqlRestriction("lft + 1 = rht");

			default:
				return null;
		}
	}

	@Override
	public String getData(Taxonomy taxonomy, String termFullPath, String dataKey)
	{
		return termDao.getDataForTerm(termDao.getTerm(taxonomy, termFullPath), dataKey);
	}

	// METHOD THAT MODIFY TERMS OR STRUCTURE /////////////////////////////////

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public TermResult addTerm(Taxonomy taxonomy, String parentFullPath, String termValue, boolean createHierarchy)
	{
		String path = insertTermImpl(taxonomy, parentFullPath, termValue, -1, createHierarchy);
		return new TermResult(termValue, path, true);
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public String insertTerm(Taxonomy taxonomy, String parentFullPath, String term, int index)
	{
		return insertTermImpl(taxonomy, parentFullPath, term, index, false);
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public TermResult insertTerm(Taxonomy taxonomy, TermResult parentTermResult, String termValue, int index)
	{
		Term parentTerm = null;
		if( parentTermResult != null )
		{
			parentTerm = getTermByUuid(taxonomy, parentTermResult.getUuid());
		}
		String parentFullpath = null;
		if( parentTerm != null )
		{
			parentFullpath = parentTerm.getFullValue();
		}

		String termFullValue = insertTermImpl(taxonomy, parentFullpath, termValue, index, false);

		return this.getTermResult(taxonomy, termFullValue);
	}

	private String insertTermImpl(Taxonomy taxonomy, String parentFullPath, String term, int index,
		boolean createHierarchy)
	{
		ensureLocked(taxonomy);

		boolean root = Strings.isNullOrEmpty(parentFullPath);
		Term parent = (root ? null : termDao.getTerm(taxonomy, parentFullPath));
		validateTerm(taxonomy, parent, parentFullPath, term, !root && !createHierarchy);

		if( parent != null || root )
		{
			Term newTerm = termDao.insertNewTerm(taxonomy, parent, term, index);
			return newTerm.getFullValue();
		}
		else if( createHierarchy )
		{
			final String fullPathNoRoot = (parentFullPath.startsWith(TaxonomyConstants.TERM_SEPARATOR)
				? parentFullPath.substring(TaxonomyConstants.TERM_SEPARATOR.length()) : parentFullPath)
				+ TaxonomyConstants.TERM_SEPARATOR + term;
			final String[] parts = fullPathNoRoot.split(TaxonomyConstants.TERM_SEPARATOR_REGEX);

			StringBuilder path = new StringBuilder();
			Term pterm = null;
			for( int i = 0; i < parts.length; i++ )
			{
				if( i > 0 )
				{
					path.append(TaxonomyConstants.TERM_SEPARATOR);
				}
				path.append(parts[i]);
				pterm = termDao.getTerm(taxonomy, path.toString());
				if( pterm == null )
				{
					pterm = termDao.insertNewTerm(taxonomy, parent, parts[i], -1);
				}
				parent = pterm;
			}
			// Cannot be null, despite Eclipse warning
			if( pterm == null )
			{
				throw new Error("Impressive");
			}
			return pterm.getFullValue();
		}
		else
		{
			throw new RuntimeException("Parent term " + parentFullPath + " does not exist");
		}
	}

	@Override
	public void validateTerm(Taxonomy taxonomy, String parentFullPath, String term, boolean requireParent)
		throws InvalidDataException
	{
		validateTerm(taxonomy, termDao.getTerm(taxonomy, parentFullPath), parentFullPath, term, requireParent);
	}

	private void validateTerm(Taxonomy taxonomy, Term parent, String parentFullPath, String term, boolean requireParent)
		throws InvalidDataException
	{
		final List<ValidationError> errors = Lists.newArrayList();

		if( requireParent && parent == null )
		{
			errors.add(new ValidationError("parent", resources.getString("validation.noparent")));
			throw new InvalidDataException(errors);
		}

		final String fullPathNoRoot;
		if( Strings.isNullOrEmpty(parentFullPath) )
		{
			fullPathNoRoot = term;
		}
		else
		{
			fullPathNoRoot = (parentFullPath.startsWith(TaxonomyConstants.TERM_SEPARATOR)
				? parentFullPath.substring(TaxonomyConstants.TERM_SEPARATOR.length()) : parentFullPath)
				+ TaxonomyConstants.TERM_SEPARATOR + term;
		}

		// check if existing, then no validation required!
		final Term existing = termDao.getTerm(taxonomy, fullPathNoRoot);
		if( existing == null )
		{

			final String[] parts = fullPathNoRoot.split(TaxonomyConstants.TERM_SEPARATOR_REGEX);

			// check empty parts
			for( String part : parts )
			{
				if( part.trim().length() == 0 )
				{
					errors.add(new ValidationError("parent", resources.getString("validation.emptyterms")));
					break;
				}
			}

			try
			{
				termDao.validateTerm(taxonomy, parent, term);
			}
			catch( InvalidDataException d )
			{
				errors.addAll(d.getErrors());
			}

			if( errors.size() > 0 )
			{
				throw new InvalidDataException(errors);
			}
		}
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public void move(Taxonomy taxonomy, String termToMove, String moveToParent, int index)
	{
		ensureLocked(taxonomy);
		termDao.move(getTerm(taxonomy, termToMove), getParentTermOrRoot(taxonomy, moveToParent), index);
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public void move(Taxonomy taxonomy, TermResult termToMove, TermResult parentTerm, int index)
	{
		ensureLocked(taxonomy);
		termDao.move(getTermByUuid(taxonomy, termToMove.getUuid()), getTermByUuid(taxonomy, parentTerm.getUuid()),
			index);
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateTerm(Taxonomy taxonomy, String termUuid, String moveToParent, int index, String newValue)
	{
		ensureLocked(taxonomy);

	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public void renameTermValue(Taxonomy taxonomy, String termToRename, String newValue)
	{
		ensureLocked(taxonomy);
		termDao.renameTermValue(getTerm(taxonomy, termToRename), newValue);
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteTerm(Taxonomy taxonomy, String termFullPath)
	{
		ensureLocked(taxonomy);
		termDao.delete(getTerm(taxonomy, termFullPath));
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public void setData(Taxonomy taxonomy, String termFullPath, String dataKey, String dataValue)
	{
		ensureLocked(taxonomy);

		Term term = getTerm(taxonomy, termFullPath);
		if( Check.isEmpty(dataValue) )
		{
			term.removeAttribute(dataKey);
		}
		else
		{
			term.setAttribute(dataKey, dataValue);
		}
		termDao.update(term);
	}

	@Override
	public Map<String, String> getAllData(Taxonomy taxonomy, String fullTermPath)
	{
		return getTerm(taxonomy, fullTermPath).getAttributes();
	}

	@Override
	@SecureOnCall(priv = "EDIT_TAXONOMY")
	@Transactional(propagation = Propagation.REQUIRED)
	public void setAllData(Taxonomy taxonomy, String termFullPath, Map<String, String> data)
	{
		ensureLocked(taxonomy);
		Term term = getTerm(taxonomy, termFullPath);
		term.setAttributes(data);
		termDao.update(term);
	}

	@Override
	public Map<String, String> getAllDataByTermUuid(Taxonomy taxonomy, String termUuid)
	{
		Term term = getTermByUuid(taxonomy, termUuid);
		if( term == null )
		{
			throw new IllegalArgumentException("TermUuid " + termUuid + " is not valid");
		}
		return term.getAttributes();

	}

	@Override
	public void setDataByTermUuid(Taxonomy taxonomy, String termUuid, String dataKey, String dataValue)
	{
		ensureLocked(taxonomy);
		Term term = termDao.getTermByUuid(taxonomy, termUuid);
		if( Check.isEmpty(dataValue) )
		{
			term.removeAttribute(dataKey);
		}
		else
		{
			term.setAttribute(dataKey, dataValue);
		}
		termDao.update(term);
	}

	@Override
	public void setAllDataByTermUuid(Taxonomy taxonomy, String termUuid, Map<String, String> data)
	{
		ensureLocked(taxonomy);
		Term term = getTermByUuid(taxonomy, termUuid);
		term.setAttributes(data);
		termDao.update(term);
	}

	private Term getParentTermOrRoot(Taxonomy taxonomy, String parentFullPath)
	{
		if( Check.isEmpty(parentFullPath) )
		{
			return null;
		}
		else
		{
			return getTerm(taxonomy, parentFullPath);
		}
	}

	private Term getTerm(Taxonomy taxonomy, String termFullPath)
	{
		Term term = termDao.getTerm(taxonomy, termFullPath);
		if( term == null )
		{
			throw new NotFoundException("Term " + termFullPath + " does not exist");
		}
		if( Check.isEmpty(term.getUuid()) )
		{
			term.setUuid(UUID.randomUUID().toString());
		}
		return term;
	}

	private Term getTermByUuid(Taxonomy taxonomy, String uuid)
	{
		Term term = termDao.getTermByUuid(taxonomy, uuid);
		if( term == null )
		{
			throw new NotFoundException("Term UUID " + uuid + " does not exist");
		}
		return term;
	}

	private void ensureLocked(Taxonomy taxonomy)
	{
		try
		{
			lockingService.getLock(taxonomy);
		}
		catch( LockedException ex )
		{
			throw new RuntimeException("Taxonomy is locked by another user: " + ex.getUserID());
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void doInTransaction(Runnable runnable)
	{
		runnable.run();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteForTaxonomy(Taxonomy taxonomy)
	{
		termDao.deleteForTaxonomy(taxonomy);
	}

	@Override
	public void doExport(Taxonomy taxonomy, SubTemporaryFile termFolder, Institution institution,
		ConverterParams params)
	{
		List<Term> terms = termDao.getAllTermsInOrder(taxonomy);

		DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg");
		params.setMessageCallback(message);
		message.setTotal(terms.size());
		message.setType(CurrentLocale.get("com.tle.core.taxonomy.type.term"));
		message.setCurrent(0);

		xmlHelper.writeExportFormatXmlFile(termFolder, true);

		for( Term term : terms )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("TERM EXPORT " + message.getCurrent() + ": " + termFolder.getAbsolutePath() + ": "
					+ term.getFullValue());
			}

			initialiserService.initialise(term);

			final int current = message.getCurrent();
			final BucketFile termBucketFolder = new BucketFile(termFolder, current);
			xmlHelper.writeXmlFile(termBucketFolder, current + ".xml", term);
			message.incrementCurrent();
		}
	}

	@Override
	public void doImport(final Taxonomy taxonomy, final SubTemporaryFile termFolder, Institution institution,
		ConverterParams params)
	{
		final List<String> termFiles = xmlHelper.getXmlFileListOrdered(termFolder);

		final DefaultMessageCallback part1message = new DefaultMessageCallback(
			"com.tle.core.taxonomy.converter.readingterms.progressmessage");
		params.setMessageCallback(part1message);
		part1message.setTotal(termFiles.size());
		part1message.setType(CurrentLocale.get("com.tle.core.taxonomy.type.terms"));
		part1message.setCurrent(0);

		// build a GIANT hash map
		final Multimap<WrappedTerm, WrappedTerm> childTermMap = ArrayListMultimap.create();
		final Map<String, WrappedTerm> valueTermMap = new HashMap<String, WrappedTerm>();
		final Map<Long, WrappedTerm> idTermMap = new HashMap<Long, WrappedTerm>();
		final WrappedTerm nullTerm = new WrappedTerm(null, null);

		for( String termFile : termFiles )
		{
			final Term term = xmlHelper.readXmlFile(termFolder, termFile);

			// We first check to see if we've already imported a node with the
			// same full path. This can happen with old data where root-level
			// nodes could be duplicated. idTermMap contains all the duplicate
			// ids, but maps them to the same object representing the first
			// duplicate term that was imported. All child terms of all
			// duplicates will then get the same parent.
			WrappedTerm wrappedTerm = valueTermMap.get(term.getFullValue());
			if( wrappedTerm == null )
			{
				term.setTaxonomy(taxonomy);

				// This ensures the value hash is set correctly, especially if
				// we're updating from 5.1 or earlier where the hash didn't
				// exist.
				term.setValue(term.getValue());

				// Term object has a (persisted) UUID property
				if( Check.isEmpty(term.getUuid()) )
				{
					term.setUuid(UUID.randomUUID().toString());
				}
				// Note that pt only has the id field during institution imports
				final Term pt = term.getParent();
				final WrappedTerm parentWT = pt == null ? nullTerm : idTermMap.get(pt.getId());

				wrappedTerm = new WrappedTerm(term, parentWT);
				addToChildTermMap(childTermMap, parentWT, wrappedTerm);

				valueTermMap.put(wrappedTerm.getTerm().getFullValue(), wrappedTerm);
			}
			idTermMap.put(term.getId(), wrappedTerm);

			part1message.incrementCurrent();
		}

		final DefaultMessageCallback part2message = new DefaultMessageCallback(
			"com.tle.core.taxonomy.converter.savingterms.progressmessage");
		params.setMessageCallback(part2message);
		part2message.setTotal(termFiles.size());
		part2message.setType(CurrentLocale.get("com.tle.core.taxonomy.type.terms"));
		part2message.setCurrent(0);

		// save the terms
		doInTransaction(new Runnable()
		{
			@Override
			public void run()
			{
				processTermChildren(nullTerm, childTermMap, part2message);
			}
		});
	}

	protected void addToChildTermMap(Multimap<WrappedTerm, WrappedTerm> childTermMap, WrappedTerm parentWrappedTerm,
		WrappedTerm wrappedTerm)
	{
		parentWrappedTerm.incTotalChildCount();
		// trickle up the tree
		for( WrappedTerm p = parentWrappedTerm.getParentWrappedTerm(); p != null; p = p.getParentWrappedTerm() )
		{
			p.incTotalChildCount();
		}

		childTermMap.get(parentWrappedTerm).add(wrappedTerm);
	}

	protected void processTermChildren(final WrappedTerm parentWrappedTerm,
		final Multimap<WrappedTerm, WrappedTerm> childTermMap, final DefaultMessageCallback message)
	{
		final Term parentTerm = parentWrappedTerm.getTerm();
		int left = (parentTerm == null ? 0 : parentTerm.getLeft() + 1);

		final Collection<WrappedTerm> terms = childTermMap.get(parentWrappedTerm);
		for( WrappedTerm wrapped : terms )
		{
			final int right = left + (wrapped.getAllChildCount() * 2) + 1;

			final Term term = wrapped.getTerm();
			term.setId(0);
			term.setLeft(left);
			term.setRight(right);
			term.setParent(parentTerm);

			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug(
					"IMPORT TERM " + message.getCurrent() + " (L:" + left + ",R:" + right + ") " + term.getFullValue());
			}

			termDao.save(term);
			termDao.flush();
			termDao.clear();

			// Move the left count along
			left = right + 1;

			message.incrementCurrent();

			// Recurse
			processTermChildren(wrapped, childTermMap, message);
		}
	}

	/**
	 * For storing in the HashMap. When the id of the term changes upon save,
	 * don't want the hash code to bugger up.
	 */
	protected static class WrappedTerm
	{
		private final Long oldId;
		private final Term term;
		private final WrappedTerm parentWrappedTerm;
		private int allChildCount;

		public WrappedTerm(Term term, WrappedTerm parentWrappedTerm)
		{
			oldId = (term != null ? term.getId() : null);
			this.term = term;
			this.parentWrappedTerm = parentWrappedTerm;
		}

		public Term getTerm()
		{
			return term;
		}

		public Long getId()
		{
			return oldId;
		}

		@Override
		public int hashCode()
		{
			return (oldId != null ? oldId.hashCode() : 0);
		}

		@Override
		public boolean equals(Object obj)
		{
			if( obj instanceof WrappedTerm )
			{
				WrappedTerm other = ((WrappedTerm) obj);
				if( Objects.equals(this.oldId, other.oldId) )
				{
					return true;
				}
			}
			return false;
		}

		public WrappedTerm getParentWrappedTerm()
		{
			return parentWrappedTerm;
		}

		public int getAllChildCount()
		{
			return allChildCount;
		}

		public void incTotalChildCount()
		{
			allChildCount++;
		}
	}

	@Override
	public String getDataByTermUuid(Taxonomy taxonomy, String termUuid, String dataKey)
	{
		Term term = termDao.getTermByUuid(taxonomy, termUuid);

		if( term == null )
		{
			throw new IllegalArgumentException("TermUuid " + termUuid + " is not valid");
		}
		return termDao.getDataForTerm(termDao.getTermByUuid(taxonomy, termUuid), dataKey);
	}

	@Override
	public TermResult getTermResultByUuid(Taxonomy taxonomy, String termUuid)
	{
		final Term term = termDao.getTermByUuid(taxonomy, termUuid);
		if( term == null )
		{
			return null;
		}
		return new TermResult(term.getValue(), term.getFullValue(), term.isLeaf(), term.getUuid());
	}
}
