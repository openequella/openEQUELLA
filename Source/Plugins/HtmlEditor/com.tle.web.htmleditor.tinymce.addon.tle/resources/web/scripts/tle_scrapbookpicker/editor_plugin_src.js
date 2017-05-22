(function() {
	// Load plugin specific language pack
	//tinymce.PluginManager.requireLangPack('example');

	tinymce.create('tinymce.plugins.TleScrapbookPickerPlugin', 
	{
		init : function(ed, url) 
		{	
			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceTleScrapbookPicker');
			ed.addCommand('mceTleScrapbookPicker', function() {	
				ed.windowManager.bookmark = ed.selection.getBookmark('simple');
				
				ed.windowManager.open({
					file : baseActionUrl + 'select_embed',
					width :810,
					height : 600,
					inline : 1,
					scroll : true
				}, {
					plugin_url : url,  // Plugin absolute URL
					some_custom_arg : 'custom arg' // Custom argument
				});
			});
	
			// Register example button
			ed.addButton('tle_scrapbookpicker', {
				title : 'Embed EQUELLA Scrapbook Content',
				cmd : 'mceTleScrapbookPicker',
				image : url + '/images/scrapbook.gif'
			});
		},

		createControl : function(n, cm) 
		{
			return null;
		},

		getInfo : function() 
		{
			return {
				longname : 'Scrapbook Picker plugin',
				author : 'Apereo',
				authorurl : 'https://equella.github.io',
				infourl : 'https://equella.github.io',
				version : "6.1"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('tle_scrapbookpicker', tinymce.plugins.TleScrapbookPickerPlugin);
})();
