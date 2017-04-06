/**
 * LIFTED FROM http://james.padolsey.com/javascript/sorting-elements-with-jquery/
 * 
 * jQuery.fn.sortElements
 * --------------
 * @param Function comparator:
 *   Exactly the same behaviour as [1,2,3].sort(comparator)
 *   
 * @param Function getSortable
 *   A function that should return the element that is
 *   to be sorted. The comparator will run on the
 *   current collection, but you may want the actual
 *   resulting sort to occur on a parent or another
 *   associated element.
 *   
 *   E.g. $('td').sortElements(comparator, function(){
 *      return this.parentNode; 
 *   })
 *   
 *   The <td>'s parent (<tr>) will be sorted instead
 *   of the <td> itself.
 */
jQuery.fn.sortElements = (function(){
 
    var sort = [].sort;
 
    return function(comparator, getSortable) {
 
        getSortable = getSortable || function(){return this;};
        
        var placements = this.map(function(){

            var sortElement = getSortable.call(this),
                parentNode = sortElement.parentNode,
 
                // Since the element itself will change position, we have
                // to have some way of storing its original position in
                // the DOM. The easiest way is to have a 'flag' node:
                nextSibling = parentNode.insertBefore(
                    document.createTextNode(''),
                    sortElement.nextSibling
                );
 
            return function() {
 
                if (parentNode === this) {
                    throw new Error(
                        "You can't sort elements if any one is a descendant of another."
                    );
                }
 
                // Insert before flag:
                parentNode.insertBefore(this, nextSibling);
                // Remove flag:
                parentNode.removeChild(nextSibling);
 
            };
 
        });
 
        return sort.call(this, comparator).each(function(i){
            placements[i].call(getSortable.call(this));
        });
    };
 
})();


function swallowEnter(event)
{
	if (event.which == 13)
	{		
		event.preventDefault();
		return false;
	}
}

function filterTable($table, filterText)
{
	var timer = $table.data("timer");
	clearTimeout(timer);
	g_filteringTable = true;
	timer=setTimeout(function(){doFilter($table, filterText.toLowerCase())}, 350);
	$table.data("timer", timer);
}

function doFilter($table, filterText)
{
	var $trs = $table.find('tbody').find('tr');
	var index = 0;
	$trs.each(function()
			{
				var $tr = $(this);
				var text = $tr.text().toLowerCase();
				if (filterText == '' || ~text.indexOf(filterText))
				{
					$tr.removeClass('rowHidden').addClass('rowShown');
					if (index % 2 == 0)
					{
						$tr.removeClass('odd').addClass('even');
					}
					else
					{
						$tr.removeClass('even').addClass('odd');
					}
					index++;
				}
				else
				{
					$tr.removeClass('rowShown').addClass('rowHidden');
				}
			}
	);
	g_filteringTable = false;
}


function sortColumn($table, $thPrimary, columnIndex, initial)
{
	var $tbody = $table.find('tbody');
	var $trs = $tbody.find('tr');
	
	var doit = function(){
	
		var isCurrentlyAsc = $thPrimary.hasClass('sortedasc');
		var isCurrentlyDesc = $thPrimary.hasClass('sorteddesc');
		
		//If this is the initial sort
		var defaultDesc = $thPrimary.hasClass('sortDesc');
		
		var doDesc = (initial && defaultDesc) 
				|| isCurrentlyAsc 
				|| (defaultDesc && !isCurrentlyAsc && !isCurrentlyDesc);
				
		var $allTh = $thPrimary.parent().find('.sortable');
		
		$allTh.addClass('unsorted')
			.removeClass('sorteddesc')
			.removeClass('sortedasc');
		$thPrimary.removeClass('unsorted');		
		
		var up = 1;
		if ( !doDesc )
		{
			$thPrimary
				.removeClass('sorteddesc')
				.addClass('sortedasc');
		}
		else
		{
			$thPrimary
				.removeClass('sortedasc')
				.addClass('sorteddesc');
			up = -1;
		}
		
		var getSortData = function(tr)
		{
			var td = $(tr).find('td')[columnIndex];
			var $data = $(td).find('.sortData');
			
			var isInt = $data.hasClass('sortInteger');
			var isDate = $data.hasClass('sortDate');
			var dataText = $data.length == 0 ? $(td).text() : $data.text();
				
			return {
			 data: (isInt || isDate ? parseInt(dataText) : dataText.toUpperCase()),
			 type: (isInt ? 1 : (isDate ? 2 : 0))
			};
		};
		
		$trs.sortElements(
			function(tr1, tr2)
			{  
				var data1 = getSortData(tr1);
				var data2 = getSortData(tr2);
				//reverse the order for dates
				var u = (data1.type == 2 ? -up : up);
				if (data1.data == data2.data)
				{
					return 0;
				}
				return (data1.data > data2.data ? u : -u);
			}
		);
		
		//Re-style the rows
		$trs = $tbody.find('tr');
		var index = 0;
		$trs.each(
			function()
			{
				var $tr = $(this);
				if ($tr.hasClass('rowShown'))
				{
					if (index % 2 == 0)
					{
						$tr.removeClass('odd').addClass('even');
					}
					else
					{
						$tr.removeClass('even').addClass('odd');
					}
					index++;
				}
			}
		);
		
//		if (!initial)
//		{
//			$tbody.fadeIn(200);
//		}
	};
	
	//Animations look really bad and flickery, hence turned off.
	//if (initial)
	//{
		doit();
	//}
//	else
//	{
//		$tbody.fadeOut(200, doit);
//	}
}
