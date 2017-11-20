var hbTries = 0;
$(function() {
	$.timer(600000, function(timer) {
		$.ajax( {
			url : "invoke.heartbeat",
			error : function(req, status, error) {
				hbTries++;
				if (hbTries >= 3)
				{
					timer.stop();
					alert("Lost contact with server.");
				}
			}
		});
	});
});