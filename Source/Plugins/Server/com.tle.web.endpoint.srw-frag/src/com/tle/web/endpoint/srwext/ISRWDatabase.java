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

public interface ISRWDatabase {

	String getExtraResponseData(QueryResult arg0, SearchRetrieveRequestType arg1);

	String getIndexInfo();

	QueryResult getQueryResult(String arg0, SearchRetrieveRequestType arg1) throws InstantiationException;

	TermList getTermList(CQLTermNode arg0, int arg1, int arg2, ScanRequestType arg3);

	void init(String arg0, String arg1, String arg2, String arg3, Properties arg4) throws Exception;

	boolean supportsSort();
	
	String getSchemaInfo();

	void addRenderer(String schemaName, String schemaID, Properties props) throws InstantiationException;

	ScanResponseType doRequest(ScanRequestType type) throws ServletException;

	boolean hasaConfigurationFile();

	void setExplainRecord(String ex);

	String getExplainRecord(HttpServletRequest request, ISRWExplainer explainer);

	Record transform(Record rec, String schemaID) throws SRWDiagnostic;

	String getSchemaID(String schemaName);

}
