/*
 *
 *	jQuery Timer plugin v0.1
 *		Matt Schmidt [http://www.mattptr.net]
 *
 *	Licensed under the BSD License:
 *		http://mattptr.net/license/license.txt
 *
 */
 
var _timerBindings = [];

 jQuery.timer = function (interval, callback, binding)
 {
 /**
  *
  * timer() provides a cleaner way to handle intervals  
  *
  *	@usage
  * $.timer(interval, callback, binding);
  *
  *
  * @example
  * $.timer(1000, function (timer) {
  * 	alert("hello");
  * 	timer.stop();
  * }, $someElem);
  * @desc Show an alert box after 1 second and stop
  * 
  * @example
  * var second = false;
  *	$.timer(1000, function (timer) {
  *		if (!second) {
  *			alert('First time!');
  *			second = true;
  *			timer.reset(3000);
  *		}
  *		else {
  *			alert('Second time');
  *			timer.stop();
  *		}
  *	}, $someElem);
  * @desc Show an alert box after 1 second and show another after 3 seconds
  *
  * 
  */

	var interval = interval || 100;

	if (!callback)
	{
		return false;
	}
	
	_timer = function (interval, callback, binding) 
	{
		this.stop = function () 
		{
			clearInterval(self.id);
		};
		
		this.internalCallback = function () 
		{
			callback(self);
		};
		
		this.reset = function (val) 
		{
			if (self.id)
			{
				clearInterval(self.id);
			}
			
			var val = val || 100;
			this.id = setInterval(this.internalCallback, val);
		};
		
		var self = this;
		this.interval = interval;
		this.id = setInterval(this.internalCallback, this.interval);
		this.boundId = ( typeof(binding) == 'undefined' ? null : binding.attr('id') );
	};
	
	if (typeof(binding) != 'undefined')
	{
		for ( var i = _timerBindings.length -1; i >= 0; i-- )
		{
			var boundTimer = _timerBindings[i];
			if ( boundTimer.boundId == binding.attr('id') )
			{
				boundTimer.stop();
				_timerBindings.splice(i, 1);
			}
		}
	}
	
	var tmr = new _timer(interval, callback, binding);
	_timerBindings.push(tmr);
	return tmr;
 };