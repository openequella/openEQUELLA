<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<div class="modal<#if m.contentBodyClass??> ${m.contentBodyClass}</#if>">
	<div class="modal-title">
		<h3><@render m.pageTitle /></h3><@render m.template["head"] />
	</div>
	<div class="modal-content">
		<div class="modal-content-background">
			<div class="modal-content-inner">
				<@render m.template["body"] />
				<#-- Fix for body that contains tall floated content -->
				<div style="clear:both"></div>
			</div>
		</div>
	</div>
	<#if m.footer??>
		<@ajax.div id=m.footerId class="modal-footer">
			<div class="modal-footer-inner">
				<@render m.footer />
			</div>
		</@ajax.div>
	</#if>
</div>
