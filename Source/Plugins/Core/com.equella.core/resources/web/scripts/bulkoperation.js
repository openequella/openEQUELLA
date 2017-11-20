function setupPolling(ajaxCall, resultsTable, warningText)
{
	addCloseWarning(warningText);
	
	var offset = 0;
	$.timer(1000, function(timer)
	{
		ajaxCall(function(data)
		{
			var temps = $("#bulkresults_templates");
			var rlist = resultsTable;
			offset = data.offset;
			var len = data.newResults.length;
			for (i=0; i<len; i++)
			{
				result = data.newResults[i];
				var newrow;
				if (result.succeeded)
				{
					newrow = $(".succeedmsg", temps).clone();
				}
				else
				{
					newrow = $(".failedmsg", temps).clone();
					$(".reason", newrow).text(" - "+result.reason);
				}
				$(newrow).addClass(offset%2==0?"even":"odd");
				$(".itemname", newrow).text(result.name);
				rlist.append(newrow);
				offset++;
			}
			$("#bulkresults_container").scrollTo("max");
			if (data.finished)
			{
				timer.stop();
				removeCloseWarning();
				resultsTable.after("<p>Operations finished</p>");
			}
		}, offset);
	});
}

function addCloseWarning(warningText){
	$("#bss_bulkDialog_close").removeAttr("onClick");
	$("#bss_bulkDialog_close").bind("click.warning", function(event){
		if (!(confirm(warningText)))
		{
			return false;
		}
		cancel_bss();
		return false;
	});
}

function removeCloseWarning(){
	$("#bss_bulkDialog_close").unbind("click.warning");
	$("#bss_bulkDialog_close").attr("onClick", "cancel_bss();return false;");
	
}
