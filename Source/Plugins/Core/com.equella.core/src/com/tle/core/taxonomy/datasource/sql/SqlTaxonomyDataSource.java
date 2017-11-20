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

package com.tle.core.taxonomy.datasource.sql;

import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_RESULT_NAME_DATAKEY;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_RESULT_NAME_DATAVALUE;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_RESULT_NAME_FULLTERM;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_RESULT_NAME_ISLEAF;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_RESULT_NAME_TERM;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_RESULT_NAME_UUID;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.COUNT_TERMS_ANY;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.COUNT_TERMS_LEAVES;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.COUNT_TERMS_TOPLEVEL;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.GET_ALL_DATA_FOR_TERM;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.GET_CHILD_TERMS;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.GET_DATA_FOR_TERM;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.GET_TERM;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.SEARCH_TERMS_ANY;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.SEARCH_TERMS_LEAVES;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query.SEARCH_TERMS_TOPLEVEL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.datasource.TaxonomyDataSource;

@SuppressWarnings("nls")
public class SqlTaxonomyDataSource implements TaxonomyDataSource
{
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final DataSource dataSource;

	private final String queryGetTerm;
	private final String queryGetChildTerms;
	private final String queryGetDataForTerm;
	private final String querySearchTermsLeaves;
	private final String queryCountTermsLeaves;
	private final String querySearchTermsTopLevel;
	private final String queryCountTermsTopLevel;
	private final String querySearchTermsAny;
	private final String queryCountTermsAny;
	private final String queryGetAllDataForTerm;

	SqlTaxonomyDataSource(DataSource dataSource, Map<String, String> as)
	{
		this.dataSource = dataSource;
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

		queryGetTerm = as.get(GET_TERM.toString());
		queryGetChildTerms = as.get(GET_CHILD_TERMS.toString());
		queryGetDataForTerm = as.get(GET_DATA_FOR_TERM.toString());
		querySearchTermsLeaves = as.get(SEARCH_TERMS_LEAVES.toString());
		queryCountTermsLeaves = as.get(COUNT_TERMS_LEAVES.toString());
		querySearchTermsTopLevel = as.get(SEARCH_TERMS_TOPLEVEL.toString());
		queryCountTermsTopLevel = as.get(COUNT_TERMS_TOPLEVEL.toString());
		querySearchTermsAny = as.get(SEARCH_TERMS_ANY.toString());
		queryCountTermsAny = as.get(COUNT_TERMS_ANY.toString());
		queryGetAllDataForTerm = as.get(GET_ALL_DATA_FOR_TERM.toString());
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	@Override
	public boolean supportsTermAddition()
	{
		return false;
	}

	@Override
	public boolean supportsTermBrowsing()
	{
		return !Check.isEmpty(queryGetChildTerms);
	}

	@Override
	public boolean supportsTermSearching()
	{
		// Term searching available if at least one of the search queries has
		// been defined. An error will be thrown if a restriction is chosen in a
		// Term Selector, but a query is not defined for it.
		return !(Check.isEmpty(querySearchTermsLeaves) && Check.isEmpty(querySearchTermsTopLevel) && Check
			.isEmpty(querySearchTermsAny));
	}

	@Override
	public TermResult getTerm(String fullTermPath)
	{
		List<TermResult> trs = executeListQuery(queryGetTerm, Collections.singletonMap("term", fullTermPath));
		if( Check.isEmpty(trs) )
		{
			return null;
		}
		else if( trs.size() == 1 )
		{
			return trs.get(0);
		}
		else
		{
			// Sanity check - ensure only a single result
			throw new IncorrectResultSizeDataAccessException(1);
		}
	}

	@Override
	public List<TermResult> getChildTerms(String parentTerm)
	{
		return executeListQuery(queryGetChildTerms,
			Collections.singletonMap("parentTerm", Check.nullToEmpty(parentTerm)));
	}

	/**
	 * NOTE: is not using the searchFullTerm parameter!
	 */
	@Override
	public Pair<Long, List<TermResult>> searchTerms(String searchQuery, SelectionRestriction restriction, int limit,
		boolean searchFullTerm)
	{
		String countSql;
		String searchSql;
		switch( restriction )
		{
			case LEAF_ONLY:
				countSql = queryCountTermsLeaves;
				searchSql = querySearchTermsLeaves;
				break;
			case TOP_LEVEL_ONLY:
				countSql = queryCountTermsTopLevel;
				searchSql = querySearchTermsTopLevel;
				break;
			case UNRESTRICTED:
				countSql = queryCountTermsAny;
				searchSql = querySearchTermsAny;
				break;
			default:
				throw new UnsupportedOperationException(restriction.name());
		}

		searchQuery = searchQuery.replace('*', '%');

		if( Check.isEmpty(searchSql) )
		{
			throw new RuntimeException("SQL query for restriction type '" + restriction.name()
				+ "' has not been defined");
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("searchQuery", searchQuery);
		params.put("limit", limit);
		List<TermResult> searchResults = executeListQuery(searchSql, params);

		long totalCount = -1;
		if( !Check.isEmpty(countSql) )
		{
			totalCount = ((Number) executeSingleResultQuery(countSql, params)).longValue();
		}

		return new Pair<Long, List<TermResult>>(totalCount, searchResults);
	}

	@Override
	public String getDataForTerm(String term, String key)
	{
		if( Check.isEmpty(queryGetDataForTerm) )
		{
			return null;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("term", term);
		params.put(SQL_RESULT_NAME_DATAKEY, key);
		return (String) executeSingleResultQuery(queryGetDataForTerm, params);
	}

	@Override
	public TermResult addTerm(String parentFullPath, String termValue, boolean createHierarchy)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void validateTerm(String parentFullTermPath, String termValue, boolean requireParent)
		throws InvalidDataException
	{
		// It's all good
	}

	@SuppressWarnings("unchecked")
	private List<TermResult> executeListQuery(String query, Map<String, ?> params)
	{
		if( Check.isEmpty(query) )
		{
			return Collections.emptyList();
		}

		return jdbcTemplate.query(query, params, new RowMapper()
		{
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				TermResult tr = new TermResult();
				String uuid;
				try
				{
					uuid = rs.getString(SQL_RESULT_NAME_UUID);
					if( Strings.isNullOrEmpty(uuid) )
					{
						uuid = rs.getString(SQL_RESULT_NAME_TERM);
					}
				}
				catch( SQLException sql )
				{
					uuid = rs.getString(SQL_RESULT_NAME_TERM);
				}

				tr.setUuid(uuid);
				tr.setTerm(rs.getString(SQL_RESULT_NAME_TERM));
				tr.setFullTerm(rs.getString(SQL_RESULT_NAME_FULLTERM));
				tr.setLeaf(rs.getBoolean(SQL_RESULT_NAME_ISLEAF));
				return tr;
			}
		});
	}

	private Object executeSingleResultQuery(String query, Map<?, ?> params)
	{
		return jdbcTemplate.query(query, params, new ResultSetExtractor()
		{
			@Override
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException
			{
				Object data = null;
				if( rs.next() )
				{
					data = rs.getObject(1);

					// Sanity check - ensure only a single result
					if( rs.next() )
					{
						throw new IncorrectResultSizeDataAccessException(1);
					}
				}
				return data;
			}
		});
	}

	@Override
	public boolean isReadonly()
	{
		return true;
	}

	@Override
	public TermResult getTermByUuid(String termUuid)
	{
		List<TermResult> trs = executeListQuery(queryGetTerm, Collections.singletonMap("uuid", termUuid));
		if( Check.isEmpty(trs) )
		{
			return null;
		}
		else if( trs.size() == 1 )
		{
			return trs.get(0);
		}
		else
		{
			// Sanity check - ensure only a single result
			throw new IncorrectResultSizeDataAccessException(1);
		}
	}

	@Override
	public String getDataByTermUuid(String termUuid, String dataKey)
	{
		if( Check.isEmpty(queryGetDataForTerm) )
		{
			return null;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("uuid", termUuid);
		params.put("dataKey", dataKey);
		return (String) executeSingleResultQuery(queryGetDataForTerm, params);
	}

	@Override
	public Map<String, String> getAllDataByTermUuid(String termUuid)
	{
		Map<String, String> data = Maps.newHashMap();
		Map<String, String> params = Collections.singletonMap("uuid", Check.nullToEmpty(termUuid));
		if( Check.isEmpty(queryGetAllDataForTerm) )
		{
			return data;
		}

		@SuppressWarnings("unchecked")
		List<Map<String, String>> records = jdbcTemplate.query(queryGetAllDataForTerm, params, new RowMapper()
		{
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				Map<String, String> data = Maps.newHashMap();
				data.put("key", rs.getString(SQL_RESULT_NAME_DATAKEY));
				data.put("value", rs.getString(SQL_RESULT_NAME_DATAVALUE));
				return data;
			}
		});

		for( Map<String, String> one : records )
		{
			data.put(one.get("key"), one.get("value"));
		}

		return data;
	}
}
