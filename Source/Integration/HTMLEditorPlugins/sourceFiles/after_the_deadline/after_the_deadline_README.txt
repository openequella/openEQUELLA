There are a number of steps you must undertake before you can use the After the Deadline plugin.  
You will need to extract the zip file to make modifications to the files within, and re-zip the files when step 2 is complete.


** 1. Install the After the Deadline server **

After The Deadline run a free, public server that is for personal use only.  You can freely download and deploy your own instance of the After The Deadline server for your institution from http://open.afterthedeadline.com/  



** 2. Configure required options **

You will need to change the atd_rpc_url configuration property in config.json to the URL of your installed AtD server in step 1.  Leave the ${institutionUrl}p/geturl?url= part as it is: the plugin makes AJAX calls to the AtD server and needs to go via the inbuilt EQUELLA proxy URL to avoid cross site scripting issues.

Change the atd_show_types in config.json parameter to include the options you require.



** 3. Upload into EQUELLA **

Upload the zip file: Settings -> HTML editor -> Plugins


** 4. Edit the editor toolbar **

Add the button ('ABC' with a green tick below it) to the HTML editor toolbar: Settings -> HTML editor -> Toolbar

