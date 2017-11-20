<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/dropdown.ftl"/>

<@css "shuffle.css" />
<@css path="component/zebratable.css" plugin="com.tle.web.sections.equella" hasRtl=true/>

<@render section=s.controlDialog />

<@render section=s.div>
	<div class="addlink"><@render section=s.addLink class="add" /></div>
	<@render section=s.list />
</@render>