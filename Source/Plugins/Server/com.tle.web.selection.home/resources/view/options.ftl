<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<#macro optionRow selectable index modulus><#t/>
<#if index % 2 == modulus><#t/>
	<div class="optionrow">
		<div>
			<img src="images/gonexticon.gif" />
			<b><@render section=selectable.state>${selectable.name}</@render></b>
		</div>
		<div>${selectable.description}</div>
	</div>
	</#if><#t/>
</#macro><#t/>

<#if m.displayedSelectables??><#t/>
<#if m.displayedSelectables?size gt 0><#t/>
	<div class="colwidth2" class="float-right">
		<#list m.displayedSelectables as selectable><#t/>
			<@optionRow selectable=selectable index=selectable_index modulus=1 /><#t/>
		</#list><#t/>
	</div>
	
	<div class="colwidth2" class="float-left">
		<#list m.displayedSelectables as selectable><#t/>
			<@optionRow selectable=selectable index=selectable_index modulus=0 /><#t/>
		</#list><#t/>
	</div>
</#if><#t/>
</#if><#t/>