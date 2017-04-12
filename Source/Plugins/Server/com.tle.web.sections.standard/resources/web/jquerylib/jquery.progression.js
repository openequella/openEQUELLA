/*
 * Progression - jQuery plugin for Progress Bar 1.2
 *
 *	http://www.anthor.net/fr/jquery-progression.html
 *
 * Copyright (c) 2008 FOURNET Lo�c
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *
 */

(function($) {
	$.fn.progression = function(options) {
		// R�cup�ration des options par d�faut
		var opts = $.extend({
			Current: 50,
			Maximum: 100,
			//
			//EDIT
			//
			/** commented out old code:
			Background: '#FFFFFF',
			TextColor: '#000000',
			aBackground: '#0000FF',
			aTextColor: '#FFFFFF',
			BorderColor: '#000000',
			*/
			//
			//END
			//
			Animate: true,
			AnimateTimeOut: 3000,
			Easing: 'linear'
		}, $.fn.progression.defaults, options);
		
		if(options)
		{
			var newCurrent = options.Current;
			if (newCurrent < 0 ) {
				newCurrent = 0;
			}
		}
		
		// Boucle sur les �l�ments appel�s
		return this.each(function() {
			$this = $(this); // El�ment en cours
			$innerdiv=$this.find(".progress"); // On recherche si l'�l�ment a d�j� �t� trait�
			
			// Options Sp�cifiques + M�tadata ?
			var o = $.metadata ? $.extend({}, opts, $this.metadata()) : opts;

			// Premier traitement de l'�l�ment, pour la mise en place
			if($innerdiv.length!=1)
			{
				BuildBarre($this, o);
			}
			else
			{
				// Si c'est une nouvelle valeur, la fonction doit avoir la priorit� sur les m�tadata
				if(newCurrent)
					o.Current = newCurrent;
				o.Maximum = parseInt($this.attr('pmax'));
			}

			// Valeur sup�rieur au maximum ?
			if( o.Current > o.Maximum )
			{
				return false;
			}

			// Calcul du pourcentage actuel
			var aWidth = Math.round(parseInt($this.attr('pcur'))/o.Maximum*100);
			// Calcul du nouveaux pourcentage
			var Width = Math.round(parseInt(o.Current)/o.Maximum*100);			
			
			if(o.Animate)
			{
				var oldCurrent = parseInt($this.attr('pcur'));
				var Steps = Math.abs(oldCurrent - o.Current);
				var StepsTimeOut = Math.floor(o.AnimateTimeOut/o.Maximum);

				$innerdiv.queue("fx", []);
				$innerdiv.stop();
				$innerdiv.animate({ width: Width+"%" }, { duration: Math.round(StepsTimeOut*(Steps+1)), queue: false, easing: o.Easing });
				
				for (i=0; i<=Steps; i++) {
					$innerdiv.animate({opacity: 1},{
							duration: Math.round(StepsTimeOut*i), 
							queue: false, 
							complete: function(){
								if(oldCurrent<=o.Current)
									$(this).progressionSetTextTo(oldCurrent++);
								else
									$(this).progressionSetTextTo(oldCurrent--);
							}
					});
      			}
			}
			else
			{
				$innerdiv.css({ width: Width+'%' });
				$innerdiv.progressionSetTextTo(o.Current);
			}
		});
	};

	// Fonction de cr�ation de la barre
	function BuildBarre($this, o) {
		// On vide la barre
		$this.html('');


		//
		//EDIT
		//
		/**Commented out:
		$this.css({
			textAlign: 'left',
			position: 'relative',
			overflow: 'hidden',
			backgroundColor: o.Background,
			borderColor: o.BorderColor,
			color: o.TextColor
		});
		*/
		//new code:
		$this.css({
			textAlign: 'left',
			position: 'relative',
			overflow: 'hidden'
		})
		$this.addClass('progressbarOuter');
		//end new code.
		
		//
		//END
		//
		
    	// Si largeur Sp�cifique
		if(o.Width)
			$this.css('width', o.Width);
		// Si hauteur Sp�cifique
		if(o.Height)
			$this.css({ height: o.Height, lineHeight: o.Height	});
		// Si image de fond
		if(o.BackgroundImg)
			$this.css({ backgroundImage: 'url(' + o.BackgroundImg + ')' });
		
		$innerdiv=$("<div class='progress'></div>");					

		//
		//EDIT
		//
		//commented out:
		//$("<div class='text'>&nbsp;</div>").css({
		//new code:
		$("<div class='text progresstextOuter'>&nbsp;</div>").css({
		//end new code.
		//
		//END
		//
			position: 'absolute',
			width: '100%',
			height: '100%',
			textAlign: 'center'
		}).appendTo($this);
		
		//
		//EDIT
		//
		
		//commented out:
		//$("<span class='text'>&nbsp;</span>")
		//new code: 
		$("<span class='text progresstextInner'>&nbsp;</span>")
		//end new code
		
		//
		//END
		//
			.css({
				position: 'absolute',
				width: $this.width(),
				textAlign: 'center'
			})
			.appendTo($innerdiv);
		
		$this.append($innerdiv);
		
		
		//
		//EDIT
		//
		/** commented out!
		// On applique le CSS de $innerdiv
		$innerdiv.css({
			position: 'absolute',
			width: 0,
			height: '100%',
			overflow: 'hidden',
			backgroundColor: o.aBackground,
			color: o.aTextColor
		});
		// Si image de fond active
		if(o.aBackgroundImg)
			$innerdiv.css({ backgroundImage: 'url(' + o.aBackgroundImg + ')' });
		*/
		
		//new code...
		$innerdiv.css({
			position: 'absolute',
			width: 0,
			height: '100%',
			overflow: 'hidden'
		});
		$innerdiv.addClass('progressbarInner');
		//finish new code...
		
		//
		//END
		//
		
		$this.attr('pmax', o.Maximum);
		$this.attr('pcur', 0);
  	};

	// Fonction pour aller � une valeur pr�cise
	$.fn.progressionSetTextTo = function(i) {		
		return this.each(function() {
			$this = $(this).parent();
			if($this.attr('pmax')!=100)	
				$this.find(".text").html(i+"/"+$this.attr('pmax'));
			else
				$this.find(".text").html(i+" %");
				
			$this.attr('pcur', i);
		});
	};
  	

	$.fn.progression.defaults = {};

})(jQuery);