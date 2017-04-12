var dlerrored = false;

function showProgress(progressUrl) 
{
	setupProgression(progressUrl, $('.progressbar'));
}

function setupProgression(progressUrl, progElem) 
{
	$.timer(1000, function(timer) 
	{
		/* for webkit (safari/chrome) */
		/*
		if ($.browser.safari) 
		{
			frames['progressFrame'].updateProgression(progressUrl, qelem,
					qFileElem, timer);
		} else*/ 
		{
			updateProgression(progressUrl, progElem, timer);
		}
	});
}

function updateProgression(progressUrl, progElem, timer) 
{
	$.getJSON(progressUrl, function(data, textStatus) 
	{
		if (textStatus != "success" || data.errorMessage) 
		{
			timer.stop();
			//stop 1 million alert boxes when there is a million failures queued up
			if (!dlerrored)
			{
				dlerrored = true;
				alert("Error downloading files: " + data.errorMessage);
			}
		} 
		else 
		{
			progElem.progression( {
				Current : data.percent,
				AnimateTimeOut : 100
			});
			
			if (data.finished) 
			{
				timer.stop();
				if (data.forwardUrl != "") 
				{
					window.location = data.forwardUrl;
				}
			}
		}
	});
}

