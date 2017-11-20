
function setupUpdate(callback, id)
{
	$.timer(1000, function(timer) {

		timer.stop();
		callback(function(response) {

			updateIncludes(response, function() {

				$.each(response.updates, function(key, html) {
					var ajaxDiv = $("#" + key);
					ajaxDiv.html(html.html);
					$.globalEval(html.script);
					$(".progressbar", ajaxDiv).progression( {
						Current : $(".progressbar", ajaxDiv).text(),
						AnimateTimeOut : 0
					});
				});

				if(!response.finished)
				{
					timer.reset(1000);
				}
			});
		});
	});
}