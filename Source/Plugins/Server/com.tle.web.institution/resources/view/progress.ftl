<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">
<#assign data = m.data>

<@css "institutions.css"/>

<#assign TEMP_body>
<div class="area">
	<h2>${b.gkey(data.action)} '${data.institutionName?html}'...</h2>
	<ol id="progressMessages">
		<#list data.tasks as task>
			<li class="taskIncomplete">${task?html}</li>
		</#list>
	</ol>

	<p id="message"></p>
	<p id="error-div" style="display: none">
		${b.gkey("institutions.progress.issues")}
		<ul id="error-list">
		</ul>
	</p>
	<p id="downloadDiv" style="display: none;">
		<a id="downloadLink" style="text-decoration: underline">${b.gkey("institutions.progress.downloadlink")}</a>
	</p>
	<@button section=s.returnButton style="display: none" id="returnLink" showAs="prev">${b.gkey("institutions.progress.return")}</@button>
</div>
</#assign>


<#assign PART_FUNCTION_DEFINITIONS>
function refreshMessages()
{
	$.getJSON('${data.ajaxUrl?js_string}', function(data){
		updateMessages(data);
	});
}

function updateMessages(json)
{
	var list = document.getElementById('progressMessages').getElementsByTagName('li');

	var current = json.current;
	if( current > list.length || json.finished)
	{
		current = list.length;
	}

	for( var i = 0; current > i; i++ )
	{
		if( list[i] )
		{
			list[i].className = 'taskComplete';
		}
	}

	if( list.length > current )
	{
		if( list[current] )
		{
			list[current].className = 'taskCurrent';
		}
	}
	var msg = json.message;
	if (msg == null)
		msg = "";
	document.getElementById('message').innerHTML = msg;

	if( json.finished )
	{
		clearInterval(interval);
		document.getElementById('returnLink').style.display = '';
		document.getElementById('message').style.display = 'none';

		if (json.forwardUrl != null && json.forwardUrl != "")
		{
			document.getElementById('downloadDiv').style.display = 'block';
			document.getElementById('downloadLink').href = json.forwardUrl;
		}

		if( json.errors != null && json.errors.length > 0 )
		{
			document.getElementById('error-div').style.display = 'block';
			var list = document.getElementById('error-list');

			for( var i = 0; json.errors.length > i; i++ )
			{
				var trace = json.traces[i];
				var text = json.errors[i];
				var element = document.createElement('li');
				var html = text;
				if (trace != null)
				{
					html += '<br><pre class="progress-error">'+trace+'</pre>';
				}
				element.innerHTML = html;
				list.appendChild(element);
			}
		}
	}
}
</#assign>

<#assign PART_READY>
interval = window.setInterval('refreshMessages()', 500);
</#assign>