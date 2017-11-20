function notificationParse(reason) {
	var stringMap = [{ bad : "rejected", good : "was rejected"}, { bad : "wentlive", good : "went live"}, { bad : "badurl", good : "contains a bad url" },
	                 { bad : "wentliv2", good : "went live" }, { bad : "overdue", good : "is overdue" }, { bad : "moderate", good : "must be moderated by you" },
	                 { bad : "itemsold", good : "was sold" }, { bad : "piupdate", good : "was updated" }, { bad : undefined, good : "" } ];
	var toReturn = null;
	
	$.each(stringMap, function(i, val) {
		if(reason == val.bad) {
			toReturn = val.good;
			return false;
		}
	});

	if(toReturn != null)return toReturn;
	else return reason;
}

//Dirty hack, must be a better way of doing this
function parseData(data) {
	$.each(data, function(i, val) {
		if ( data.results[i].attachments != undefined && data.results[i].attachments[0] != undefined && data.results[i].attachments[0].links != undefined && data.results[i].attachments[0].links.thumbnail != undefined ) {
        	data.results[i].image = data.results[i].attachments[0].links.thumbnail;
		}
    	data.results[i].pageType = "item";
	});
	return data;
}

function parseForHistory(data) {
	var itemJSON = { results : [] };
	var temp = { results : [] };
	
	$.each(data, function(i, val) {
		var stuff; //Hate switches
		switch(data[i].type) {
		
			case "statechange":
				stuff = "Moved to " + data[i].state;
				break;
			case "contributed":
				stuff = "Contributed";
				break;
			case "approved":
				stuff = "Approved at step: " + data[i].stepName;
				break;
			case "rejected":
				stuff = "Rejected at step: " + data[i].stepName;
				break;
			default:
				break;
		}
		if( stuff != undefined ) {
			var d = new Date(data[i].date);
			stuff += ": " + d.getDate() + "/" + (d.getMonth() + 1) + "/" + d.getFullYear();
			temp.results[temp.results.length] = {"description" : stuff};
		}
	});
	$.each(temp.results, function(i,val) {
		itemJSON.results[temp.results.length - i - 1] = val;
	});
	return itemJSON;
}

function addGet(url, get) {
	// Think this should work in every situation??
	if(url.indexOf("?") > 0)return url + "&" + get;
	else return url + "?" + get;
}

function getUrlVarsNoHash() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}
