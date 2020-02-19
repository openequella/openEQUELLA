<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<#macro receipt r="">
	<#if r?? && r?length &gt; 0>
		<@a.div id="receipt-message" class="alert alert-success" role="alert">
			<button type="button" class="close" data-dismiss="alert"><i class="icon-remove"></i></button>
			<span>${r}</span>
		</@a.div>
	</#if>
</#macro>
