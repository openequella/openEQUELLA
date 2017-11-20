<div class="edit">
	<h3>${b.key('common.hidelogin.title')}</h3>
	<div class="input checkbox">
		<@render id="hideLoginNotice" section=s.hideLoginNotice />
		<label for="hideLoginNotice">${b.key('common.hidelogin')}</label>
	</div>
</div>
<hr>
<div class="edit">
	<h3>${b.key('accessibility.mode.title')}</h3>
	<p>${b.key('accessibility.mode.help')}</p>
	<div class="input checkbox">
		<@render id="accessMode" section=s.accessibilityMode />
		<label for="accessMode">${b.key('accessibility.mode.label')}</label>
		
	</div>
</div>