var DoSelection = {
	init : function()
	{
		tinyMCEPopup.resizeToInnerSize();
	},

	sm : function(cb)
	{
		if (!tinyMCEPopup.domLoaded)
		{
			tinyMCEPopup.onInit.add(cb);
		}
		else
		{
			cb.call(this);
		}
	},

	selectionsMade : function(id, urls)
	{
		var t = this;
		if (urls && urls.length > 0)
		{
			return t.insertAction(urls[0]);
		}
		else
		{
			t.close();
		}
	},

	setHref : function(ed, elm, value)
	{
		if (typeof (value) == 'undefined' || value == null)
		{
			value = '';
		}
		ed.dom.setAttrib(elm, 'href', value);
	},

	insertAction : function(resource)
	{
		var t = this, ed = tinyMCEPopup.editor;
		var isEmbeddable = (resource.template != null && resource.template != '');

		tinyMCEPopup.restoreSelection();

		if (isEmbeddable)
		{
			t.doIt(ed, confirm(ed.getLang('embeddable_msg')), resource);
		}
		else
		{
			t.doIt(ed, false, resource);
		}
	},

	transaction : function(f)
	{
		f();
		tinyMCEPopup.execCommand('mceEndUndoLevel');
	},

	doIt : function(ed, isEmbed, resource)
	{
		var t = this;
		var url = resource.href;
		var template = resource.template;
		var title = resource.title;
		var selnode = null;

		if (isEmbed) // embeddable type
		{
			t.debug('embedding');
			t.transaction(function()
			{
				ed.execCommand('mceInsertContent', false, template, {
					skip_undo : 1
				});
			});
		}
		else
		// plain old link to attachment
		{
			t.transaction(function()
			{
				var initialSel = ed.selection.getNode();
				t.debug('initial selection: ' + ed.selection.getContent());

				var elm = ed.dom.getParent(initialSel, 'A');
				// Create new anchor elements
				if (elm == null)
				{
					t.debug('creating new link');

					var TURL = "#mce_temp_url#";
					tinyMCEPopup.execCommand('mceInsertRawHTML', false, '<a href="'+ TURL +'">'+title+'</a>', {
						skip_undo : 1
					});
					
					var elementArray = tinymce.grep(ed.dom.select("a"),
							function(n)
							{
								return ed.dom.getAttrib(n, 'href') == TURL;
							});
					for (i = 0; i < elementArray.length; i++)
					{
						elm = elementArray[i];
						t.setHref(ed, elm, url);
					}
				}
				else
				{
					t.debug('updating link');
					t.setHref(ed, elm, url);
				}

				// Don't move caret if selection was image
				if (elm.childNodes.length != 1
						|| elm.firstChild.nodeName != 'IMG')
				{
					ed.focus();
					ed.selection.select(elm);
					ed.selection.collapse(0);
					tinyMCEPopup.storeSelection();
				}
			});
		}

		tinyMCEPopup.editor.execCommand('mceRepaint');
		tinyMCEPopup.editor.focus();
		t.close();
	},

	debug : function(text)
	{
		window.parent.debug(text);
	},

	error : function(text)
	{
		var msg = 'ERROR [doselection.js]  ' + text;
		alert(msg);
	},

	close : function()
	{
		// magical IE hax. Redmine #6096
		setTimeout(function()
		{
			tinyMCEPopup.close();
		}, 500);
	}
};
