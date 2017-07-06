
<#assign TEMP_pagetitle>View Tabs</#assign>

<#assign PART_HEAD>		
	<style>
		html, body, form, iframe, #tabs, #tabs ul, #iframe-content  {
			margin: 0;
			padding: 0;
		}

		html, body, form, iframe, #contentdiv, #iframe-content {
			border: 0;
		}

		html, body, form {
			height: 100%;
		}

		#tabs, #iframe-content {
			position: absolute;
			left: 0;
			bottom: 0;
			background:url("../images/bg-pv-content-right.png") no-repeat scroll 0 bottom #D4CFB9;
		}

		.tabs-nav {
			padding-top: 2px; 
			border-bottom: 1px solid #97a5b0;
		}
		
		/* We need to have tab panels or it all breaks, so we use them to store the iFrame
		   URL for the tab and hide them in the middle of nowhere. */
		.fake-tab-panel {
			position: absolute;
			left: -10000px;
		}
	</style>
</#assign>
		 
<#assign PART_FUNCTION_DEFINITIONS>
function resizeContent()
{
	var cd = $('#iframe-content');
	var tb = $('#tabs');
	
	var par = findParentWithAHeight(cd);
	if( par )
	{
		var cdh = par.height() - tb.outerHeight(true);
		if( cdh > 0 )
		{
			cd.height(cdh);
			tb.css('bottom', cdh + 'px');
		}
	}
}

/* This is to support IE6, which occasionally returns a height of 0 */
function findParentWithAHeight(jqElem)
{
	var par = jqElem.parent();
	while( true )
	{
		if( !par )
			return null;
			
		try	{
			if( par.height() != 0 )
				return par;
		} catch(err) {
			return null;
		} 
		
		par = par.parent();
	}
}
</#assign>

<#assign PART_READY>
$(document).ready(function() {
	    $("#tabs").tabs({
	    	create: function(e, ui) {
				$("#iframe-content").attr("src", ui.panel.text());
			},
	    	activate: function(e, ui) {
				$("#iframe-content").attr("src", ui.newPanel.text());
			}
		});
		
		$(window).resize(resizeContent);
		resizeContent();
	});
</#assign>

<#assign TEMP_body>
	<input type="hidden" id="${pfx}method" name="${pfx}method" value="unspecified">
	<div id="tabs">
		<ul class="tabs-nav">
			<#list m.tabs as tab>
				<li>
					<a href="#tab-${tab_index}"><span>${tab.name}</span></a>
				</li>
			</#list>
		</ul>
		<#list m.tabs as tab>
			<div id="tab-${tab_index}" class="fake-tab-panel">${tab.value}</div>
		</#list> 
	</div>
	<iframe id="iframe-content" name="iframe-content" style="width: 100%"></iframe>
</#assign>
