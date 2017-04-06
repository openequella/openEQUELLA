var baseActionUrl;
var mceOptions;
var initCalled = false;

function initialiseMce(baseUrl, actionUrl, options, addOns)
{
	baseActionUrl = actionUrl;
	if (!initCalled)
	{
		initCalled = true;
		debug('initialiseMce: base url -> ' + baseUrl);

		tinymce.baseURL = baseUrl;
		tinymce.baseURI = new tinymce.util.URI(tinymce.baseURL);
		tinymce.documentBaseURL = baseUrl;

		mceOptions = options;

		// register add ons
		for ( var i = 0; i < addOns.length; i++)
		{
			var addOn = addOns[i];
			tinymce.PluginManager.load(addOn.uid, addOn.jsUrl);
		}
		// If ajax loaded, dom ready event won't get called
		//tinymce.dom.Event._pageInit(window);

		// FIXME: these should be added dynamically, based from the properties
		// files
		tinymce.ScriptLoader
				.loadQueue(function()
				{
					tinymce
							.addI18n({
								en : {
									common : {
										clipboard_msg : 'The cut, copy, and paste toolbar buttons do not function on Mozilla-based browsers (such as Firefox) by default due to security reasons. <br /><br />'
												+ 'These measures are taken by the browser to avoid possibly sensitive information being leaked to illegitimate websites without your knowledge. <br /><br />'
												+ 'It is recommended that the keyboard shortcuts be used to access this functionality: <br><br>'
												+ '<table class="nocopypastecommands">'
												+ '<tr><th>Function</th><th>Windows and Linux</th><th>Mac OS</th></tr>'
												+ '<tr class="odd"><td>Cut</td><td>Ctrl + x</td><td>Cmd \u2318 + x</td></tr>'
												+ '<tr class="even"><td>Copy</td><td>Ctrl + c</td><td>Cmd \u2318 + c</td></tr>'
												+ '<tr class="odd"><td>Paste</td><td>Ctrl + v</td><td>Cmd \u2318 + v</td></tr></table>'
												+ '<br><br>'
												+ 'If you still wish to use the toolbar icons, it is recommended that you install the AllowClipboard Helper add-on at <a href="https://addons.mozilla.org/en-US/firefox/addon/852" target="_blank" style="top:auto; height:auto; width:auto; display:inline; position:static; font-weight:bold; color:blue">https://addons.mozilla.org/en-US/firefox/addon/852</a>. This add-on will allow you to grant cut/copy/paste privileges to EQUELLA in a straightforward manner. <br><br>'
												+ 'For more information on this issue, along with advanced instructions for enabling these privileges without the above add-on, please see <a href="http://www.mozilla.org/editor/midasdemo/securityprefs.html" target="_blank" style="top:auto; height:auto; width:auto; display:inline; position:static; font-weight:bold; color:blue">http://www.mozilla.org/editor/midasdemo/securityprefs.html</a>.',
										embeddable_msg : 'Do you wish to embed the content of the selection into the page?\nPress Cancel to link to the selection instead.'
									}
								}
							});
				});
	}
	else
	{
		debug('initialiseMce: already initialised');
	}
}


//This is a dirty hack to fix a firefox issue:
//http://www.tinymce.com/forum/viewtopic.php?pid=99397#p99397
function registerEditor($elem)
{
	setTimeout(function(){registerEditorCall($elem)}, 10);
}

function registerEditorCall($elem)
{
	var elemId = $elem.attr('id');

	if (!initCalled)
	{
		debug('registerEditor: !!You need to call initialiseMce before calling registerEditor!! ' + elemId);
	}
	else
	{
		//don't editorise something that is meant to be disabled
		var dis = $elem.data('edDisabled');
		if (dis)
		{
			debug('registerEditor: did not create editor for disabled element ' + elemId);
		}
		else
		{
			debug('registerEditor: creating editor for ' + elemId);
			var ed = new tinymce.Editor(elemId, mceOptions);
			ed.render();
			return ed;
		}
	}
}

function unregisterEditor($elem)
{
	debug('unregisterEditor');
	
	var elemId = $elem.attr('id'); 
	if (tinymce.getInstanceById(elemId))
	{
		debug('removeControl');
		
	    //tinymce.execCommand('mceFocus', false, elemId);                    
	    tinymce.execCommand('mceRemoveControl', false, elemId);
	}
}

function saveContents($elem)
{
	debug('saveContents');
	
	var elemId = $elem.attr('id');
	var ed = tinymce.get(elemId);
	if (ed)
	{
		$elem.val(ed.getContent());
		
		//unregister the editor???
		//unregisterEditor($elem);
	}
	else
	{
		debug('saveContents: no editor ' + elemId);
	}
}

function setTinyMceDisabled($elem, $fullscreenLinkElem, dis)
{
	var elemId = $elem.attr('id');
	var readOnlyId = 'rdonly_'+ elemId;

	if (dis)
	{
		if (!$elem.data('edDisabled'))
		{
			var ed = tinymce.get(elemId);
			var html = '';
			if (ed)
			{
				html = ed.getContent();
				tinyMCE.execCommand('mceRemoveControl', false, elemId);
			}
			else
			{
				debug('setTinyMceDisabled: no editor to disable (yet...) ' + elemId);
			}
			$elem.hide();
			$elem.data('edDisabled', true);
			var $div = $('<div id="' + readOnlyId + '" class="disablededitor" />');
			$div.width($elem.width());
			$div.height($elem.height());
			$div.html(html);
			$elem.after($div);
			
			//hide the full-screen link
			var $linkParent = $fullscreenLinkElem.parent();
			$linkParent.hide();
		}
		else
		{
			debug('setTinyMceDisabled: already disabled ' + elemId);
		}
	}
	else
	{
		if ($elem.data('edDisabled'))
		{
			var $div= $('#' + readOnlyId);
			if ($div.length == 0)
			{
				debug('setTinyMceDisabled: no readonly div, editor must already be enabled ' + elemId);
			}
			else
			{
				var html = $div.html();
				$div.remove();
				$elem.show();
				$elem.data('edDisabled', false);
				var oldEd = tinymce.get(elemId);
				if (oldEd)
				{
					debug('setTinyMceDisabled: old editor is still there!!! ' + elemId);
				}
				else
				{
					var ed = registerEditor($elem);
					//dirty... you gotta give it time (in IE anyway apparently)
					waitForEditor($elem.attr('id'), function(ed2){ ed2.setContent(html) } );
				}
				
				//show the full-screen link
				var $linkParent = $fullscreenLinkElem.parent();
				$linkParent.show();
			}
		}
		else
		{
			debug('setTinyMceDisabled: already enabled ' + elemId);
		}
	}
}


function toggleTinyMceFullscreen($elem, $link)
{
	var $linkParent = $link.parent();
	
	//move the link location when in fullscreen mode!
	if ($linkParent.data('fullscreen') == null)
	{
		var elemId = $elem.attr('id');
		waitForEditor(elemId, function(ed){
			debug('toggleTinyMceFullscreen: fs callback');
			$linkParent.addClass('fullscreened');
			
			ed.execCommand('mceFullScreen', false);
			
			$linkParent.data('$prev', $linkParent.next());
			$('#mce_fullscreen_container').append($linkParent);
			
			debug('toggleTinyMceFullscreen: setting fullscreen yes');
			$linkParent.data('fullscreen', 'yes');
		});
	}
	else
	{
		//fullscreen mode actually creates it's OWN editor called mce_fullscreen!
		var ed = tinymce.get('mce_fullscreen');
		ed.execCommand('mceFullScreen', false);
		
		if (debugEnabled)
		{
			debug('toggleTinyMceFullscreen: link parent old prev element: ' + serialise($linkParent.data('$prev')));
		}
		$linkParent.data('$prev').before($linkParent);
		
		$linkParent.removeClass('fullscreened');
		
		debug('toggleTinyMceFullscreen: setting fullscreen null');
		$linkParent.data('fullscreen', null);
	}
	
}

function waitForEditor(elemId, cb)
{
	var ed = tinymce.get(elemId);
	if (!ed || !ed.initialized)
	{
		debug('waitForEditor: no editor yet... waiting');
		var tmr = setInterval(
				function(){  
					ed = tinymce.get(elemId);
					if (ed && ed.initialized)
					{
						debug('waitForEditor: invoking callback');
						clearInterval(tmr);
						cb(ed);
					}
					else
					{
						debug('waitForEditor: still no editor yet...');
					}
				}, 
				300);
	}
	else
	{
		debug('waitForEditor: editor already there, invoking callback');
		cb(ed);
	}
}