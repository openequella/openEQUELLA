function setupProgress(ajax, schemaIds) {
	schemaIds = JSON.parse(schemaIds);
	$.timer(100, function(timer) {
		timer.stop();
		ajax(function(json) {
			updateIncludes(json, function() {
				$.each(json.updates, function(key, html) {
					$("#" + key).html(html.html);
					$.globalEval(html.script);
				});
				schemaIds = json.updateTimes;
				if (json.rows) {
					$.each(json.rows, function(i, val) {
						var id = val.params.id;
						var newupdate = $(val.html);
						var ctx = $("#schemaRow_" + id);
						ctx.replaceWith(newupdate);
						$.globalEval(val.script);
					});
				}
				if (json.someUpdating)
				{
					timer.reset(1000);
				}
			});
		}, JSON.stringify(schemaIds));
	});
}

var showing;

function cancelPopup($other) {
	if (!$other) {
		return;
	}
	$other.hide();
	$("body").unbind("click.closepopup");
}

function handlerDispatcher($dd, handlers) {
	handlers[$dd.val()]();
}

function waitFor(ajax) {
	var tries = 0;
	$.timer(2500, function(timer) {
		timer.stop();
		ajax([function(json) {
				if (json.value) 
				{
					window.location.reload(true);
				} 
				else 
				{
					if(tries == 0)
					{
						// First time give migrations time to run
						// This is important for tomcat restart migration
						timer.reset(10000);
					}
					else
					{
						timer.reset(1000);						
					}
				}
			}, 
			function(jqXHR, textStatus, errorThrown) {
				if(tries < 30)
				{
					// Server unavailable
					if (jqXHR.status == 503 || jqXHR.status == 500)
					{
						timer.reset(1000);
						tries++;
					}
					else
					{
						// Server appears to have reloaded
						setTimeout(function(){
							window.location.reload(true);							
						}, 5000);
					}
				}
				else
				{	
					// Hopefully never see this
					alert("Could not contact EQUELLA webserver. Please manually restart EQUELLA");
				}
			}
		]);
	});
}

function switchOutput(to) {
	var $d = $('.databaseprogressdialog');
	var $to = $d.find('.link-' + to);
	if (!$to.hasClass('selected')) {
		$d.find('.progress-links .selected').removeClass('selected');
		$to.addClass('selected');

		$d.find('.textpane').hide();
		$d.find('.progress-' + to).show();
	}
}

function setupProgressDialog(ajax, taskId) {
	var offset = 0;
	$.timer(1000, function(timer) {
		timer.stop();
		ajax(function(json) {
			$('.progress-curr-migration').text(json.migration);
			$('.progress-curr-migration-name').text(json.migrationName);
			$('.progress-curr-step').text(json.step);

			offset = json.offset;
			if (!$.isEmptyObject(json.messages)) {
				var $messages = $('.progress-messages');
				$.each(json.messages, function(i, msg) {
					$messages.append(msg + "<br><br>");
				});
				$messages.scrollTop(99999);
				offset += json.messages.length;
			}

			if (!$.isEmptyObject(json.warnings)) {
				$('.warnings-link-wrapper').show();
				var $warnings = $('.progress-warnings');
				$.each(json.warnings, function(i, warning) {
					$warnings.append(warning + "<br><br>");
				});
				$warnings.scrollTop(99999);
				offset += json.warnings.length;
			}

			if (!json.finished) {
				timer.reset(1000);
			}
		}, taskId, offset);
	});
}
