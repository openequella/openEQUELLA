<div class="edit single">
	<h3>${b.key('common.username')}</h3>
	<div class="input text">
		${s.username?html}
	</div>
</div>

<div class="edit double<#if m.errors['lastName']?? || m.errors['firstName']??> ctrlinvalid</#if>">
	<#if m.errors['lastName']??><p class="ctrlinvalidmessage">${m.errors['lastName']}</p><#elseif m.errors['firstName']??><p class="ctrlinvalidmessage">${m.errors['firstName']}</p></#if>
	<h3>${b.key('common.firstname')}</h3>
	<h3>${b.key('common.surname')}</h3>
	<div class="input text">
		<@textfield section=s.firstName />
		<@textfield section=s.familyName />
	</div>
</div>

<div class="edit single<#if m.errors['email']??> ctrlinvalid</#if>">
	<#if m.errors['email']??><p class="ctrlinvalidmessage">${m.errors['email']}</p></#if>
	<h3>${b.key('common.email')}</h3>
	<div class="input text">
		<@textfield section=s.email />
	</div>
</div>



