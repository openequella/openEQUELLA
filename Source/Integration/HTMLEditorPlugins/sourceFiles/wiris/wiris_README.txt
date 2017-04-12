There are a number of steps you must undertake before you can use the Wiris plugin.  Please follow these steps carefully.
You will need to extract the plugin zip file to make modifications to the files within, and re-zip the files when steps 1 and 3 are complete.


1. ** Obtain the Wiris Tiny MCE code **

Due to licencing restrictions we cannot include the Wiris code in this plugin.
Download the zip file from http://www.wiris.com/en/plugins3/tinymce/download then extract the zip file and copy the entire *contents* (not the actual directory) of the tiny_mce_wiris directory into the 'plugin' directory of this plugin.



2. ** Host the Wiris server code **

The Wiris Tiny MCE plugin requires a server component that you must install.  Hosting applications within the EQUELLA Tomcat server is *not supported*, however you can host the Wiris Java WAR file on any server you like.  In fact you are not restricted to the Java version either: you could install the PHP version instead for instance.  

IMPORTANT: 
Several configuration modifications are necessary when installing the server component, refer to the Wiris documentation at http://www.wiris.com/en/plugins/docs/tinymce (step 3)

IMPORTANT:
There is one restriction when setting up the server component:  the hostname of the EQUELLA institution and the hostname of the Wiris server component must match (to avoid cross site scripting restrictions).  You can setup a rule in Apache web server (or IIS, or any other web server of your choice) to redirect requests from YOUR_HOSTNAME/pluginwiris_engine/* to YOUR_WIRIS_COMPONENT_HOSTNAME/pluginwiris_engine/*

If using Apache and mod_proxy you could configure this:

<Proxy balancer://wiris/>
	BalancerMember ajp://YOUR_WIRIS_COMPONENT_HOSTNAME:8009 route=wiris
</Proxy> 
ProxyPreserveHost On
ProxyPass /pluginwiris_engine balancer://wiris/pluginwiris_engine nocanon 


The actual editor itself is hosted by wiris.net, so you may experience speed issues when loading the formula editor.  To host the editor yourself you must obtain a licence to use the Wiris editor server software for commercial uses http://www.wiris.com/en/store



3. ** Modify editor_plugin_src.js **

Delete the 1st 10 lines, of "var _wrs_baseURL..." and the following if...else statement. (The EQUELLA Wiris plugin automatically configures the javascript variable _wrs_baseURL. Note that this configured value will end with '/' - path separator). 

Change all settings beginning with '/pluginwiris_engine' to the absolute URL of your hosted Wiris deployment in step 2.

	I.e. originals: 
		var _wrs_conf_editorPath = '/pluginwiris_engine/app/editor';
		var _wrs_conf_CASPath = '/pluginwiris_engine/app/cas';
		var _wrs_conf_createimagePath = '/pluginwiris_engine/app/createimage';
		var _wrs_conf_createcasimagePath = '/pluginwiris_engine/app/createcasimage';
		var _wrs_conf_getmathmlPath = '/pluginwiris_engine/app/getmathml';
		var _wrs_conf_servicePath = '/pluginwiris_engine/app/service';
		var _wrs_conf_getconfigPath = '/pluginwiris_engine/app/getconfig';

	Change to:
		var _wrs_conf_editorPath = 'http://[your_hosted_wiris]/pluginwiris_engine/app/editor';
		var _wrs_conf_CASPath = 'http://[your_hosted_wiris]/pluginwiris_engine/app/cas';
		var _wrs_conf_createimagePath = 'http://[your_hosted_wiris]/pluginwiris_engine/app/createimage';
		var _wrs_conf_createcasimagePath = 'http://[your_hosted_wiris]/pluginwiris_engine/app/createcasimage';
		var _wrs_conf_getmathmlPath = 'http://[your_hosted_wiris]/pluginwiris_engine/app/getmathml';
		var _wrs_conf_servicePath = 'http://[your_hosted_wiris]/pluginwiris_engine/app/service';
		var _wrs_conf_getconfigPath = 'http://[your_hosted_wiris]/pluginwiris_engine/app/getconfig';

You will also need to edit the path strings that partly contain "/plugins/tiny_mce_wiris/"

	I.e. originals:
		tinymce.ScriptLoader.load(_wrs_baseURL + '/plugins/tiny_mce_wiris/core/core.js');
		while(tinymce.ScriptLoader.isDone(_wrs_baseURL + '/plugins/tiny_mce_wiris/core/core.js');

		var _wrs_conf_pluginBasePath = _wrs_baseURL + '/plugins/tiny_mce_wiris';
	    
		var _wrs_int_editorIcon = _wrs_baseURL + '/plugins/tiny_mce_wiris/core/icons/tiny_mce/formula.gif';
		var _wrs_int_CASIcon = _wrs_baseURL + '/plugins/tiny_mce_wiris/core/icons/tiny_mce/cas.gif';
	    
	Change to:
		tinymce.ScriptLoader.load(_wrs_baseURL + 'core/core.js');
		while(tinymce.ScriptLoader.isDone(_wrs_baseURL + 'core/core.js'));

		var _wrs_conf_pluginBasePath = _wrs_baseURL;

		var _wrs_int_editorIcon = _wrs_baseURL + 'core/icons/tiny_mce/formula.gif';
		var _wrs_int_CASIcon = _wrs_baseURL + 'core/icons/tiny_mce/cas.gif';


Note that if you installed the PHP Wiris server you would need to uncomment and modify the relevant PHP URLs, but leave off the _wrs_baseURL prefix.

	E.g. uncomment:
		var _wrs_conf_editorPath = _wrs_baseURL + '/plugins/tiny_mce_wiris/integration/editor.php';
	and change to:
		var _wrs_conf_editorPath = 'http://[your_hosted_wiris]/integration/editor.php';



** 4. Upload into EQUELLA **

Re-zip the files, ensuring the plugin.json file is in the root directory of the zip, and ensuring the Wiris-supplied files (including the edited editor_plugin_src.js file) are inside the plugin sub-directory.
Then upload the zip file into EQUELLA: 
Go to Settings -> HTML editor -> Plugins



** 5. Edit the editor toolbar **

Add the button (a square-root symbol with a yellow/orange background) to the HTML editor toolbar within EQUELLA: 
Go to Settings -> HTML editor -> Toolbar



** 6. (Optional) modify your EQUELLA theme customer.css file **

You may experience issues with Wiris formula images not appearing inline with text in the HTML editor.  To fix this issue you may add the following CSS rule to your customer.css file (in your EQUELLA theme)

img.Wirisformula
{
	vertical-align: middle;
}

