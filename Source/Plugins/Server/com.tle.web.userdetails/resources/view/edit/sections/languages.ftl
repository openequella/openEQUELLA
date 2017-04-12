<div class="edit">
	<h3>${b.key('regional.selectlanguage')}</h3>
	<p>${b.key('regional.languagenote', [m.defaultLanguage])}</p>
	<div class="input select">
		<@dropdown section=s.languageList />
	</div>
</div>

<div class="edit">
	<h3>${b.key('regional.timezone')}</h3>
	<p>${b.key('regional.selecttimezone')}</p>
	<div class="input select">
		<@dropdown section=s.timeZones />
	</div>
</div>

<div class="edit">
	<h3>${b.key('dateformat.title')}</h3>
	<p>${b.key('dateformat.description')}</p>
	<div class="input select">
		<@dropdown section=s.dateFormats />
	</div>
</div>
