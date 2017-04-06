<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#if m.prompt??><@render m.prompt /></#if>
<ul class="choiceInteraction">
<#list m.choices as choice>
<li><@render choice /></li>
</#list>
</ul>