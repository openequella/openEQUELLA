<#macro filedrop section style="" id="">
<div>
	<#local renderer=_choose(section, "filedrop")>
	<#if renderer?has_content>
		<div id="filedrop-progress" class="filedrop-progress-container">
		<div class="clear"></div>
		</div>
		<@_render section=renderer style=style class="filedrop"  draggable="true" id=id>
			<#nested>
			<div class="dndicon"></div>
			<p><@bundlekey "dnd.dropfiles" /></p>
		</@_render>
	</#if>
</div>
</#macro>