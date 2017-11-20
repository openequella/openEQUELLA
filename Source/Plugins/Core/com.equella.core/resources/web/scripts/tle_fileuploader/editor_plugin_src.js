(function() {
	tinymce.create('tinymce.plugins.TleFileUploaderPlugin', 
	{
		init : function(ed, url) 
		{
			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceTleResourceLinker');
			ed.addCommand('mceTleFileUploader', function() {
				ed.windowManager.open({
					file : baseActionUrl + 'select_upload',
					width : 810,
					height : 600,
					inline : 1,
					scroll : true
				}, {
					plugin_url : url,  // Plugin absolute URL
					some_custom_arg : 'custom arg' // Custom argument
				});
			});
			
			// Register example button
			ed.addButton('tle_fileuploader', {
				title : 'Upload a File',
				cmd : 'mceTleFileUploader',
				image : url + '/images/paperclip.gif'
			});
		},

		createControl : function(n, cm) 
		{
			return null;
		},

		getInfo : function() 
		{
			return {
				longname : 'File Uploader plugin',
				author : 'Apereo',
				authorurl : 'https://equella.github.io/',
				infourl : 'https://equella.github.io/',
				version : "6.1"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('tle_fileuploader', tinymce.plugins.TleFileUploaderPlugin);
})();
