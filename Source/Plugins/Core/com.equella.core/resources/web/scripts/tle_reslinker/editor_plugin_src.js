(function() {
	// Load plugin specific language pack
	//tinymce.PluginManager.requireLangPack('example');

	tinymce.create('tinymce.plugins.TleResourceLinkerPlugin', 
	{
		init : function(ed, url) 
		{
			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceTleResourceLinker');
			ed.addCommand('mceTleResourceLinker', function() {
				ed.windowManager.bookmark = ed.selection.getBookmark('simple');
				
				ed.windowManager.open({
					file : baseActionUrl + 'select_link',
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
			ed.addButton('tle_reslinker', {
				title : 'Insert EQUELLA Content',
				cmd : 'mceTleResourceLinker',
				image : url + '/images/equellabutton.gif'
			});
		},
		
		createControl : function(n, cm) 
		{
			return null;
		},

		getInfo : function() 
		{
			return {
				longname : 'Resource Linker plugin',
				author : 'Apereo',
				authorurl : 'https://equella.github.io/',
				infourl : 'https://equella.github.io/',
				version : "6.1"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('tle_reslinker', tinymce.plugins.TleResourceLinkerPlugin);
})();
