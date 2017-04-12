<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<@css path="kaltura.css" hasRtl=true />

<label for="kalt_option_fs">
	<h3>${b.key("add.option.heading")}</h3>
	<p>${b.key("choice.description", m.kalturaServer)}</p>
</label>
<div class="choice-list">
	<#assign count = 0 />
	<fieldset id="kalt_option_fs" class="focus" tabIndex="0">	
		<@l.boollist section=s.choice; opt, state>
			<div class="choice ${(count % 2 == 0)?string("odd","even")}">
				<h4><@render section=state style="display: none" /></h4>
				${opt.description}
				<#assign count = count +1 />
			</div>
		</@l.boollist>
	</fieldset>
</div>
<@render m.kalturaLogo />