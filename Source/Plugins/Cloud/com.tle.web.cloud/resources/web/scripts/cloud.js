var Cloud = 
{
		onSearch: function(cb, $span, searchingText)
		{
			//TODO: spinner
			$span.text(searchingText);
			
			var updateCount = function(data)
			{
				$span.text(data.text);
			}
			cb.call(null, updateCount);
		}
};