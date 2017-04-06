Redmine #7890, emerged after a jquery upgrade.
The original isLocal function read as:

function isLocal( anchor ) {
	return anchor.hash.length > 1 &&
		decodeURIComponent( anchor.href.replace( rhash, "" ) ) ===
			decodeURIComponent( location.href.replace( rhash, "" ) );
}

with the consequence that a path anchor with href
"http://server/trunk/branch/#leaf"
and location.href
"http://server/trunk/branch/access/fudge.do?anddidthosefeet=inancienttime&walkupon=Englandsmountainsgreeen"
would determine that the anchor was NOT local, by simply comparing the former URL before the '#', with the entirety of the latter.

Solution:
method replaced with

function isLocal( anchor ) {
	if( anchor.hash.length > 1)
	{
		var anchorhref = decodeURIComponent( anchor.href.replace( rhash, "" ) );
		var locationhref = decodeURIComponent( location.href.replace( rhash, "" ) );
		var imLocal = locationhref.substr(0, anchorhref.length) === anchorhref;
		return imLocal;
	}
	else return false;
}
(a bit long winded, but makes for easier debugging)

Assuming that the intent of the method was to see if the anchor was on the same page,
the solution put forward is to use substr to see if the location begins with the anchor's href.
 