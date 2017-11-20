<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "shuffle.css" />
<@css path="component/zebratable.css" plugin="com.tle.web.sections.equella" hasRtl=true/>

<@render section=s.div>
		<@textfield section=s.text autoSubmitButton=s.addButton />
		<@button section=s.addButton class="ctrlbutton" showAs="add" />
		<@render section=s.list />
</@render>