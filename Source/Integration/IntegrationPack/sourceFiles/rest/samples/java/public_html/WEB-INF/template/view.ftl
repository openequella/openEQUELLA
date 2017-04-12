<#include "page.ftl" />

<@page>
	<h1>Viewing Resource</h1>
	
	<div class="fields">
		<fieldset>
			<legend>Resource Details</legend>
			
			<div class="formfield">
				<label for="name">Name</label>
				<div id="name">${name}</div>
			</div>
			
			<#if description?exists>
			<div class="formfield">
				<label for="name">Description</label>
				<div id="name">${description}</div>
			</div>
			</#if>
			
			<div class="formfield">
				<label for="name">Owner</label>
				<div id="name">${owner}</div>
			</div>
			
			<#if modified?exists>
			<div class="formfield">
				<label for="name">Date Modified</label>
				<div id="name">${modified?datetime}</div>
			</div>
			</#if>
			
			<div class="formfield">
				<label for="attachments">Attachments</label>
				
				<div class="attachments">
				<#list attachments as attachment>
					<div class="attachment">
						<div>
							<a href="${attachment.viewUrl}" target="_blank"><img src="${attachment.thumbUrl}"></a>
						</div>
						<div>
							<a href="${attachment.viewUrl}" target="_blank"><em>${attachment.description}</em></a>
						</div>
					</div>
				</#list>
				</div>
			</div>
			
		</fieldset>
	</div>
</@page>