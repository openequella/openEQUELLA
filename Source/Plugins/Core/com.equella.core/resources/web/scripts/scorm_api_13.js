/***************************************************************************
**
** This file has been modified by Dytech Solutions for use in The Learning
** Edge.  All prior notices are shown below.
**
***************************************************************************/
/***************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) grants you
** ("Licensee") a non-exclusive, royalty free, license to use, and
** redistribute this software, provided that i) this copyright notice and
** license appear on all copies of the software; and ii) Licensee does not
** utilize the software in a manner which is disparaging to ADL Co-Lab.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab AND ITS LICENSORS
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO
** EVENT WILL ADL Co-Lab OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE
** SOFTWARE, EVEN IF ADL Co-Lab HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGES.
**
***************************************************************************/
/*******************************************************************************
**
** This file is being presented to Content Developers, Content Programmers and
** Instructional Designers to demonstrate one way to abstract API calls from the
** actual content to allow for uniformity and reuse of content fragments.
**
** The purpose in wrapping the calls to the API is to (1) provide a
** consistent means of finding the LMS API adapter within the window
** hierarchy, (2) to ensure that the method calls are called correctly by the
** SCO and (3) to make possible changes to the actual API Specifications and
** Standards easier to implement/change quickly.
**
** This is just one possible example for implementing the API guidelines for
** runtime communication between an LMS and executable content components.
** There are many other possible implementations.
**
*******************************************************************************/

// 1.3 API
function APIObject2004(url)
{
	this.ajaxUrl = url;
	this.Initialize = Initialize;
	this.Terminate = Terminate;
	this.Finish = Terminate;
	this.GetValue = GetValue;
	this.SetValue = SetValue;
	this.Commit = Commit;
	this.GetLastError = GetLastError;
	this.GetErrorString = GetErrorString;
	this.GetDiagnostic = GetDiagnostic;
	this.LMSInitialize = Initialize;
	this.LMSTerminate = Terminate;
	this.LMSFinish = Terminate;
	this.LMSGetValue = GetValue;
	this.LMSSetValue = SetValue;
	this.LMSCommit = Commit;
	this.LMSGetLastError = GetLastError;
	this.LMSGetErrorString = GetErrorString;
	this.LMSGetDiagnostic = GetDiagnostic;
}

/**************************************************************************
**
** Function: Initialize()
** Inputs:  None
** Return:  CMIBoolean true if the initialization was successful, or
**          CMIBoolean false if the initialization failed.
**
** Description:
** Initialize communication with LMS by calling the Initialize
** function which will be implemented by the LMS.
**
//**************************************************************************/
function Initialize(iInParameter)
{
	return doJson(this.ajaxUrl + '?method=initialize&param1='+iInParameter);
}

/**************************************************************************
**
** Function: Terminate()
** Inputs:  None
** Return:
**
** Description:
**
**
**************************************************************************/
function Terminate(iInParameter)
{
	return doJson(this.ajaxUrl + '?method=terminate&param1='+iInParameter);
}

/**************************************************************************
**
** Function: GetValue()
** Inputs:  Name of Element to retrieve
** Return:  Result of the getValue call
**
** Description:
** This funciton takes in the name of the element and returns the
** appropriate return value.
**
**************************************************************************/
function GetValue( name )
{
	return doJson(this.ajaxUrl + '?method=getvalue&param1='+name);
}

/**************************************************************************
**
** Function: SetValue()
** Inputs:  Element name and value to set it to
** Return:  Status of the call
**
** Description:
** This setValue call excepts the value and checks
**
***************************************************************************/
function SetValue( name, value )
{
	return doJson(this.ajaxUrl + '?method=setvalue&param1='+name+'&param2='+value);
}

/**************************************************************************
**
** Function: Commit()
** Inputs:  None
** Return:
**
** Description:
**
**
***************************************************************************/
function Commit(iInParameter)
{
	return doJson(this.ajaxUrl + '?method=commit&param1='+iInParameter);
}

/**************************************************************************
**
** Function: GetLastError()
** Inputs:  None
** Return:
**
** Description:
**
**
**************************************************************************/
function GetLastError()
{
	return doJson(this.ajaxUrl + '?method=getlasterror');
}

/**************************************************************************
**
** Function: GetErrorString()
** Inputs:  None
** Return:
**
** Description:
**
**
**************************************************************************/
function GetErrorString( errCode )
{
	return doJson(this.ajaxUrl + '?method=geterrorstring&param1='+errCode);
}

/**************************************************************************
**
** Function: GetDiagnostic()
** Inputs:  None
** Return:
**
** Description:
**
**************************************************************************/
function GetDiagnostic( error )
{
	return doJson(this.ajaxUrl + '?method=getdiagnostic&param1'+error);
}

function doJson(url)
{
	var rv = null;
	jQuery.ajax({
		url: url, 
		dataType: "text",
		success: function(data, status) {
			rv = data;
		},
		async: false
	});
	return rv;
}

