package com.tle.common.scripting.objects;

import com.tle.common.scripting.types.ExecutionResultScriptType;

/**
 * Referenced by the 'system' variable in script.
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
public interface SystemScriptObject
{
	String DEFAULT_VARIABLE = "system";

	/**
	 * Executes a program without waiting for it finish. There is no indication
	 * of when the program completes or if it was successful.
	 * 
	 * @param programPath The full path to an executable program or script.
	 * @param parameters An array of command line parameters to send to the
	 *            executable program at programPath. The parameters can contain
	 *            String, AttachmentScriptType and FileHandleScriptType objects.
	 */
	void executeInBackground(String programPath, Object[] parameters);

	/**
	 * Executes a program and retrieves the console output as well as any error
	 * codes returned by the program.
	 * 
	 * @param programPath The full path to an executable program or script.
	 * @param parameters An array of command line parameters to send to the
	 *            executable program at programPath. The parameters can contain
	 *            String, AttachmentScriptType and FileHandleScriptType objects.
	 * @return An ExecutionResult object with the output of the standard and
	 *         error streams
	 */
	ExecutionResultScriptType execute(String programPath, Object[] parameters);

	// /**
	// * Executes a program and outputs the results to a temporary file which is
	// returned as binary data.
	// *
	// * @param programPath The full path to an executable program or script
	// * @param parameters An array of command line parameters to send to the
	// executable program at programPath
	// * @return Binary data, useable in
	// attachments.createBinaryFileAttachment()
	// */
	// BinaryDataScriptType executeWithBinaryResult(String programPath, String[]
	// parameters);
}
