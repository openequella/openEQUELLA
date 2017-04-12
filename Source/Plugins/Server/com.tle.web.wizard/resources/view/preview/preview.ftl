<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css "preview.css" />

<#-- Styles are in here because otherwise fancybox doesn't autosize in IE7 or IE8 -->
<style>
.modal-content, .modal-content-inner
{
	padding:0px;
}
</style>

<div id="preview-container" style="width: 1030px;">
	<div id="versions" style="	width: 10%; min-height: 250px; float: left;">
		<ul>
			<#list m.versionDetails as version>
				<li class="${(version_index % 2 == 0)?string("odd","even")}${(version.version == m.currentVersion.version)?string(" selected","")}"
					onclick="$('#versions li').removeClass('selected'); $(this).addClass('selected'); window.open('${version.href}', 'itemContent');">
	      				<div class="title">${b.key("displayversions.version", version.version)}</div>
	     			 	<div class="status">${version.status}</span>
				</li>
			</#list>
		</ul>
	</div>
	<iframe name="itemContent" id="itemContent" src="${m.currentVersion.href}" style="width: 90%; height: 533px; float:left;"></iframe>
	<div class="clear"></div>
</div>