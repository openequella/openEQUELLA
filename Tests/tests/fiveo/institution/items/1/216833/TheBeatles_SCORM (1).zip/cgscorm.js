/*******************************************************************************
**
** FileName: APIWrapper.js
**
*******************************************************************************/

/*******************************************************************************
**
** Concurrent Technologies Corporation (CTC) grants you ("Licensee") a non-
** exclusive, royalty free, license to use, modify and redistribute this
** software in source and binary code form, provided that i) this copyright
** notice and license appear on all copies of the software; and ii) Licensee does
** not utilize the software in a manner which is disparaging to CTC.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
** IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-
** INFRINGEMENT, ARE HEREBY EXCLUDED.  CTC AND ITS LICENSORS SHALL NOT BE LIABLE
** FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
** DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO EVENT WILL CTC  OR ITS
** LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
** INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
** CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
** OR INABILITY TO USE SOFTWARE, EVEN IF CTC  HAS BEEN ADVISED OF THE POSSIBILITY
** OF SUCH DAMAGES.
**
*******************************************************************************/


var _Debug = false;      // set this to false to turn debugging off
var _OutputFirstError = true; // and get rid of those annoying alert boxes.

// Define exception/error codes
var _NoError = 0;
var _GeneralException = 101;
var _ServerBusy = 102;
var _InvalidArgumentError = 201;
var _ElementCannotHaveChildren = 202;
var _ElementIsNotAnArray = 203;
var _NotInitialized = 301;
var _NotImplementedError = 401;
var _InvalidSetValue = 402;
var _ElementIsReadOnly = 403;
var _ElementIsWriteOnly = 404;
var _IncorrectDataType = 405;


// local variable definitions
var apiHandle = null;
var API = null;
var findAPITries = 0;
var FinishAttempt = true; // Start with true, LMSInitialise will set to false when the initialisation is complete, preventing calls to LMSFinish before the page has completely loaded.

/*******************************************************************************
**
** Function: doLMSInitialize()
** Inputs:  None
** Return:  CMIBoolean true if the initialization was successful, or
**          CMIBoolean false if the initialization failed.
**
** Description:
** Initialize communication with LMS by calling the LMSInitialize
** function which will be implemented by the LMS.
**
*******************************************************************************/
function doLMSInitialize()
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nLMSInitialize was not successful.");
		_OutputFirstError = false;
	  }
      return "false";
   }

   var result = api.LMSInitialize("");

   if (result.toString() != "true")
   {
      var err = ErrorHandler();
   }

   return result.toString();
}

/*******************************************************************************
**
** Function doLMSFinish()
** Inputs:  None
** Return:  CMIBoolean true if successful
**          CMIBoolean false if failed.
**
** Description:
** Close communication with LMS by calling the LMSFinish
** function which will be implemented by the LMS
**
*******************************************************************************/
function doLMSFinish()
{

   var api = getAPIHandle();
   if (api == null)
   {
		if ((_Debug == true) || (_OutputFirstError == true))
		{
	      alert("Unable to locate the LMS's API Implementation.\nLMSFinish was not successful.");
		  _OutputFirstError = false;
		}
	  return "false";
   }
   else
   {
      // call the LMSFinish function that should be implemented by the API

      var result = api.LMSFinish("");
      if (result.toString() != "true")
      {
         var err = ErrorHandler();
      }

   }

   return result.toString();
}

/*******************************************************************************
**
** Function doLMSGetValue(name)
** Inputs:  name - string representing the cmi data model defined category or
**             element (e.g. cmi.core.student_id)
** Return:  The value presently assigned by the LMS to the cmi data model
**       element defined by the element or category identified by the name
**       input value.
**
** Description:
** Wraps the call to the LMS LMSGetValue method
**
*******************************************************************************/
function doLMSGetValue(name)
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
	      alert("Unable to locate the LMS's API Implementation.\nLMSGetValue was not successful.");
		  _OutputFirstError = false;
	  }
      return "";
   }
   else
   {
      var value = api.LMSGetValue(name);
      var errCode = api.LMSGetLastError().toString();
      if (errCode != _NoError)
      {
         // an error was encountered so display the error description
         var errDescription = api.LMSGetErrorString(errCode);
		  if ((_Debug == true) || (_OutputFirstError == true))
		  {
			alert("LMSGetValue("+name+") failed. \n"+ errDescription);
			_OutputFirstError = false;
		  }
         return "";
      }
      else
      {
         
         return value.toString();
      }
   }
}

/*******************************************************************************
**
** Function doLMSSetValue(name, value)
** Inputs:  name -string representing the data model defined category or element
**          value -the value that the named element or category will be assigned
** Return:  CMIBoolean true if successful
**          CMIBoolean false if failed.
**
** Description:
** Wraps the call to the LMS LMSSetValue function
**
*******************************************************************************/
function doLMSSetValue(name, value)
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nLMSSetValue was not successful.");
		_OutputFirstError = false;
	  }
      return;
   }
   else
   {
      var result = api.LMSSetValue(name, value);
      if (result.toString() != "true")
      {
         var err = ErrorHandler();
      }
   }

   return;
}

/*******************************************************************************
**
** Function doLMSCommit()
** Inputs:  None
** Return:  None
**
** Description:
** Call the LMSCommit function 
**
*******************************************************************************/
function doLMSCommit()
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nLMSCommit was not successful.");
		_OutputFirstError = false;
	  }
      return "false";
   }
   else
   {
      var result = api.LMSCommit("");
      if (result != "true")
      {
         var err = ErrorHandler();
      }
   }

   return result.toString();
}

/*******************************************************************************
**
** Function doLMSGetLastError()
** Inputs:  None
** Return:  The error code that was set by the last LMS function call
**
** Description:
** Call the LMSGetLastError function 
**
*******************************************************************************/
function doLMSGetLastError()
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nLMSGetLastError was not successful.");
		//since we can't get the error code from the LMS, return a general error
		_OutputFirstError = false;
	  }
      return _GeneralError;
   }

   return api.LMSGetLastError().toString();
}

/*******************************************************************************
**
** Function doLMSGetErrorString(errorCode)
** Inputs:  errorCode - Error Code
** Return:  The textual description that corresponds to the input error code
**
** Description:
** Call the LMSGetErrorString function 
**
********************************************************************************/
function doLMSGetErrorString(errorCode)
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nLMSGetErrorString was not successful.");
		_OutputFirstError = false;
	  }
   }

   return api.LMSGetErrorString(errorCode).toString();
}

/*******************************************************************************
**
** Function doLMSGetDiagnostic(errorCode)
** Inputs:  errorCode - Error Code(integer format), or null
** Return:  The vendor specific textual description that corresponds to the 
**          input error code
**
** Description:
** Call the LMSGetDiagnostic function
**
*******************************************************************************/
function doLMSGetDiagnostic(errorCode)
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nLMSGetDiagnostic was not successful.");
		_OutputFirstError = false;
	  }
   }

   return api.LMSGetDiagnostic(errorCode).toString();
}

/*******************************************************************************
**
** Function LMSIsInitialized()
** Inputs:  none
** Return:  true if the LMS API is currently initialized, otherwise false
**
** Description:
** Determines if the LMS API is currently initialized or not.
**
*******************************************************************************/
function LMSIsInitialized()
{
   // there is no direct method for determining if the LMS API is initialized
   // for example an LMSIsInitialized function defined on the API so we'll try
   // a simple LMSGetValue and trap for the LMS Not Initialized Error

   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nLMSIsInitialized() failed.");
	    _OutputFirstError = false;
	  }
      return false;
   }
   else
   {
      var value = api.LMSGetValue("cmi.core.student_name");
      var errCode = api.LMSGetLastError().toString();
      if (errCode == _NotInitialized)
      {
         return false;
      }
      else
      {
         return true;
      }
   }
}

/*******************************************************************************
**
** Function ErrorHandler()
** Inputs:  None
** Return:  The current value of the LMS Error Code
**
** Description:
** Determines if an error was encountered by the previous API call
** and if so, displays a message to the user.  If the error code
** has associated text it is also displayed.
**
*******************************************************************************/
function ErrorHandler()
{
   var api = getAPIHandle();
   if (api == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to locate the LMS's API Implementation.\nCannot determine LMS error code.");
		_OutputFirstError = false;
	  }
      return;
   }

   // check for errors caused by or from the LMS
   var errCode = api.LMSGetLastError().toString();
   if (errCode != _NoError)
   {
      // an error was encountered so display the error description
      var errDescription = api.LMSGetErrorString(errCode);

      if (_Debug == true)
      {
         errDescription += "\n";
         errDescription += api.LMSGetDiagnostic(null);
         // by passing null to LMSGetDiagnostic, we get any available diagnostics
         // on the previous error.
      }

	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
        alert(errDescription);
		_OutputFirstError = false;
	  }
   }

   return errCode;
}

/******************************************************************************
**
** Function getAPIHandle()
** Inputs:  None
** Return:  value contained by APIHandle
**
** Description:
** Returns the handle to API object if it was previously set,
** otherwise it returns null
**
*******************************************************************************/
function getAPIHandle()
{
   if (apiHandle == null)
   {
      apiHandle = getAPI();
   }

   return apiHandle;
}


/*******************************************************************************
**
** Function findAPI(win)
** Inputs:  win - a Window Object
** Return:  If an API object is found, it's returned, otherwise null is returned
**
** Description:
** This function looks for an object named API in parent and opener windows
**
*******************************************************************************/
function findAPI(win)
{
   while ((win.API == null) && (win.parent != null) && (win.parent != win))
   {
      findAPITries++;
      // Note: 7 is an arbitrary number, but should be more than sufficient
      if (findAPITries > 7) 
      {
         // alert("Error finding API -- too deeply nested.");
         return null;
      }
      
      win = win.parent;

   }
   return win.API;
}



/*******************************************************************************
**
** Function getAPI()
** Inputs:  none
** Return:  If an API object is found, it's returned, otherwise null is returned
**
** Description:
** This function looks for an object named API, first in the current window's 
** frame hierarchy and then, if necessary, in the current window's opener window
** hierarchy (if there is an opener window).
**
*******************************************************************************/
function getAPI()
{
   var theAPI = findAPI(window);
   if ((theAPI == null) && (window.opener != null) && (typeof(window.opener) != "undefined"))
   {
      theAPI = findAPI(window.opener);
   }
   if (theAPI == null)
   {
	  if ((_Debug == true) || (_OutputFirstError == true))
	  {
		alert("Unable to find a SCORM API adapter - Progress will not be saved.");
		_OutputFirstError = false;
	  }
   }
   return theAPI;
}



/*******************************************************************************
**
** FileName: SCOFunctions.js
** Functions below are based on file SCOFunctions.js - modified by Connected Learning Ltd
**
*******************************************************************************/


var startDate;


function loadPage()
{
   var result = doLMSInitialize();

   if (result != "true")
   {
		return "false";
   }
   var status = doLMSGetValue( "cmi.core.lesson_status" );

   if (status == "not attempted")
   {
	  // the student is now attempting the lesson
	  doLMSSetValue( "cmi.core.lesson_status", "incomplete" );
   }

   initQuestions();
   result = doLMSCommit();
	
   startTimer();
   FinishAttempt = false; // API has initialised allow the unload code to function.
}


function startTimer()
{
   startDate = new Date().getTime();
}


function computeTime()
{
   if ( startDate != 0 )
   {
      var currentDate = new Date().getTime();
      var elapsedSeconds = ( (currentDate - startDate) / 1000 );
      var formattedTime = convertTotalSeconds( elapsedSeconds );
   }
   else
   {
      formattedTime = "00:00:00.0";
   }

   doLMSSetValue( "cmi.core.session_time", formattedTime );
}



function unloadPage()
{
   // Only attempt to commit the SCO Status if we have not already done so. Also only when the SCO is initialized.
   if (FinishAttempt == false) {
	   doLMSSetValue( "cmi.core.exit", "" );
	   var api = getAPIHandle();
	   if (api == null)
	   {
		  if ((_Debug == true) || (_OutputFirstError == true))
	      {
		      alert("Unable to locate the LMS's API Implementation.\nLMSGetValue was not successful.");
			  _OutputFirstError = false;
		  }
		      return "";
	   }
	   else
	   {
	      var value = api.LMSGetValue("cmi.core.lesson_mode");
	      var errCode = api.LMSGetLastError().toString();
	      if ((errCode != _NoError) || (errCode = _NotImplementedError))
	      {
	         // an NotImplemented Error was encountered so default to Normal
		   var mode = "normal";
	      }
	      else
	      {
		   var mode = value.toString();
	      }
	   }

	   var mast = (isNaN(masteryScore) ? 0 : masteryScore); 
	
	   if ( mode != "review"  &&  mode != "browse" && mast == 0 )
	   {
	      doLMSSetValue( "cmi.core.lesson_status", "completed" );
	   }
	 
	   computeTime();
	   
	   var result;
	   result = doLMSCommit();
	   FinishAttempt = true; // If there is an error during the LMSFinish which causes the script to terminate and the API to be removed do not attempt to execute and more API calls.
	   result = doLMSFinish();
   }
   else {
   	   result = "true";
   }

}


function initQuestions()
{
   var api = getAPIHandle();
   if (api == null)
   {
      if ((_Debug == true) || (_OutputFirstError == true))
      {
          alert("Unable to locate the LMS's API Implementation.\nLMSGetValue was not successful.");
          _OutputFirstError = false;
      }
      return "";
   }
   else
   {
      // Set values directly and ignore return - elements are RTE2 and may not be implemented
      var resultMin = api.LMSSetValue("cmi.core.score.min", scoreMin);
      var resultMax = api.LMSSetValue("cmi.core.score.max", scoreMax);
   }
}



/*******************************************************************************
** this function will convert seconds into hours, minutes, and seconds in
** CMITimespan type format - HHHH:MM:SS.SS (Hours has a max of 4 digits &
** Min of 2 digits
*******************************************************************************/
function convertTotalSeconds(ts)
{
   var sec = (ts % 60);

   ts -= sec;
   var tmp = (ts % 3600);  //# of seconds in the total # of minutes
   ts -= tmp;              //# of seconds in the total # of hours

   // convert seconds to conform to CMITimespan type (e.g. SS.00)
   sec = Math.round(sec*100)/100;
   
   var strSec = new String(sec);
   var strWholeSec = strSec;
   var strFractionSec = "";

   if (strSec.indexOf(".") != -1)
   {
      strWholeSec =  strSec.substring(0, strSec.indexOf("."));
      strFractionSec = strSec.substring(strSec.indexOf(".")+1, strSec.length);
   }
   
   if (strWholeSec.length < 2)
   {
      strWholeSec = "0" + strWholeSec;
   }
   strSec = strWholeSec;
   
   if (strFractionSec.length)
   {
      strSec = strSec+ "." + strFractionSec;
   }


   if ((ts % 3600) != 0 )
      var hour = 0;
   else var hour = (ts / 3600);
   if ( (tmp % 60) != 0 )
      var min = 0;
   else var min = (tmp / 60);

   if ((new String(hour)).length < 2)
      hour = "0"+hour;
   if ((new String(min)).length < 2)
      min = "0"+min;

   var rtnVal = hour+":"+min+":"+strSec;

   return rtnVal;
}


/*******************************************************************************
** 
** Assessment functions
** Functions below are by Connected Learning Ltd
**
*******************************************************************************/

function initArray(anArray, aValue) {
	for (var i=0;i<anArray.length;i++) { 
		anArray[i] = aValue;
	}
}

function storeScore(q,s) {
	questionScores[q] = s;
	var t = sumScores(questionScores);
	// alert("storeScore: Qn" + q + ", Score " + t + ", Array " +  questionScores.join(","));
	doLMSSetValue("cmi.core.score.raw", t);
	
	// Set status to passed or failed depending on score and mastery score
	masteryScore = parseInt(masteryScore,10);
	if (!isNaN(masteryScore)) {
		var stat = (t >= masteryScore ? "passed" : "failed");
		doLMSSetValue("cmi.core.lesson_status",stat);
	}
	
   	result = doLMSCommit();
}

function sumScores(scoreArray) {
	var sum = 0;
	for (var i=0;i<scoreArray.length;i++) {
		if (!isNaN(scoreArray[i])) {
			sum = sum + scoreArray[i];
		}
	}
	return sum;
}

function resetError() {
	// Allows the next error message to be output, to enable feedback from UI Events (such as saving a question respomse)
	_OutputFirstError = true;
	return;
}

