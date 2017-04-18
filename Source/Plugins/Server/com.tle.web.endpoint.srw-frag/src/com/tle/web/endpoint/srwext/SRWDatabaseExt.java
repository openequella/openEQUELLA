package com.tle.web.endpoint.srwext;

import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.z3950.zing.cql.CQLTermNode;

import ORG.oclc.os.SRW.QueryResult;
import ORG.oclc.os.SRW.Record;
import ORG.oclc.os.SRW.SRWDiagnostic;
import ORG.oclc.os.SRW.TermList;
import gov.loc.www.zing.srw.ScanRequestType;
import gov.loc.www.zing.srw.ScanResponseType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;

public class SRWDatabaseExt extends ORG.oclc.os.SRW.SRWDatabase
{

	public static ISRWDatabase impl;

	@Override
	public String getSchemaInfo()
	{
		return impl.getSchemaInfo();
	}

	@Override
	public void addRenderer(String schemaName, String schemaID, Properties props) throws InstantiationException
	{
		impl.addRenderer(schemaName, schemaID, props);
	}

	@Override
	public ScanResponseType doRequest(ScanRequestType type) throws ServletException
	{
		return impl.doRequest(type);
	}

	@Override
	public boolean hasaConfigurationFile()
	{
		return impl.hasaConfigurationFile();
	}

	@Override
	public void setExplainRecord(String ex)
	{
		impl.setExplainRecord(ex);
	}

	@Override
	public String getExplainRecord(HttpServletRequest request)
	{
		return impl.getExplainRecord(request, new ISRWExplainer()
		{

			@Override
			public void makeExplainRecord(HttpServletRequest request)
			{
				SRWDatabaseExt.this.makeExplainRecord(request);
			}
		});
	}

	@Override
	public Record transform(Record rec, String schemaID) throws SRWDiagnostic
	{
		return impl.transform(rec, schemaID);
	}

	@Override
	public String getSchemaID(String schemaName)
	{
		return impl.getSchemaID(schemaName);
	}

	@Override
	public String getExtraResponseData(QueryResult arg0, SearchRetrieveRequestType arg1)
	{
		return impl.getExtraResponseData(arg0, arg1);
	}

	@Override
	public String getIndexInfo()
	{
		return impl.getIndexInfo();
	}

	@Override
	public QueryResult getQueryResult(String arg0, SearchRetrieveRequestType arg1) throws InstantiationException
	{
		return impl.getQueryResult(arg0, arg1);
	}

	@Override
	public TermList getTermList(CQLTermNode arg0, int arg1, int arg2, ScanRequestType arg3)
	{
		return impl.getTermList(arg0, arg1, arg2, arg3);
	}

	@Override
	public void init(String arg0, String arg1, String arg2, String arg3, Properties arg4) throws Exception
	{
		impl.init(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public boolean supportsSort()
	{
		return impl.supportsSort();
	}

}
