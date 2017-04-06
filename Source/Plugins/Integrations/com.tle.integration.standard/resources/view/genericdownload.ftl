<li style="background-image: url(${p.url(m.type + '.jpg')})">
	<h3>${b.key(m.type + '.title')}</h3>
	<p>${b.key(m.type + '.description')}</p>
	<p><a href="${p.url('downloads/' + m.downloadFile)}">${b.key(m.type + '.download')}</a></p>
</li>
