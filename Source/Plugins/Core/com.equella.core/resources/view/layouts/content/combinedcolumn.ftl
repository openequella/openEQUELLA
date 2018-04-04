<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/receipt.ftl">

<#if m.receiptSpanBothColumns>
	<@receipt m.receipt />
</#if>
<div id="topwide">
	<#if !m.receiptSpanBothColumns>
		<@receipt m.receipt />
	</#if>
	<@render m.template['top']/>
</div>
<div class="mainCols">
    <div id="col1">
        <@render m.template['left']/>
    </div>
    <div id="col2">
        <@render m.template['right']/>
    </div>
</div>