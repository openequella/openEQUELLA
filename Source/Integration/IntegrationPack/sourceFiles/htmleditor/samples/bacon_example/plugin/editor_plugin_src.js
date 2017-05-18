(function() {
	// Load plugin specific language pack
	//tinymce.PluginManager.requireLangPack('example');

	tinymce.create('tinymce.plugins.BaconExample', 
	{
		init : function(ed, url) 
		{
			var self = this;
			tinymce.extend(self, ed.getParam('bacon_example'));
			
			var insertPic = function(picUrl, altText, times)
			{
				var r = ed.selection.getRng();
				
				// First delete the contents seems to work better on WebKit when the selection spans multiple list items or multiple table cells.
				if (!ed.selection.isCollapsed() && r.startContainer != r.endContainer)
				{
					ed.getDoc().execCommand('Delete', false, null);
				}
				
				for (var i = 0; i < times; i++)
				{
					ed.execCommand('mceInsertContent', false, '<img src="' + picUrl + '" alt="' + altText + '" title="' + altText + '">', {skip_undo : false});
				}
			};
			
			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceReceiveBacon');
			ed.addCommand('mceReceiveBacon', function() {
				
				//checks for configuration property (defaults to 1)
				var number = (self.numberOfBaconStrips ? parseInt(self.numberOfBaconStrips) : 1);
				insertPic(self.image1, self.keywords, number);
			});
						
			// Register example buttons
			ed.addButton('bacon_example_receivebacon', {
				title : 'Receive bacon',
				cmd : 'mceReceiveBacon',
				image : url + '/images/receivebacon.png'
			});
		},
		
		createControl : function(n, cm) 
		{
			return null;
		},

		getInfo : function() 
		{
			return {
				longname : 'Bacon Example Plugin',
				author : 'Apereo',
				authorurl : 'https://equella.github.io/',
				infourl : 'https://equella.github.io/',
				version : "6.1"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('bacon_example', tinymce.plugins.BaconExample);
})();
