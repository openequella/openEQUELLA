<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<@css path="comments.css" />

<h3>${m.title}</h3> 
<p><#if m.mandatoryMessage><span class="mandatory">* </span></#if>${m.subTitle}</p>

<#if m.mustHaveMessage><p class="mandatory">${b.key('error.musthavemessage')}</p></#if>

<@textarea section=s.commentField/>

<#if m.mandatoryMessage>
<script type="text/javascript">
	$(document).ready(function(){
		$('#bss_bulkDialog_okButton').hide();
		setInterval(function(){
			var id = 'bwrto2_commentField';
			var message = $.trim($('#' + id).val());
			if(message === '')
			{
				$('#bss_bulkDialog_okButton').hide();
			}
			else
			{
				$('#bss_bulkDialog_okButton').show();
			}
		}, 2000);
	});
</script>
</#if>
