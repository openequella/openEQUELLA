
function GsivThumbnail(viewer, thumbnailSelector) {

	this.viewer = viewer;
	this.thumbnailSelector = thumbnailSelector;
	this.locator = $('<div class="locator"><!-- --></div>');
	this.thumb = $('<img />');
	this.lastPos = {};

	var me = this;
	this.locator.draggable({
		zIndex: 2000,
		containment: 'parent',
		drag: function(evt, ui) {
			me.locatorDragged(evt, ui);
		},
		stop: function() {
			me.locatorDropped();
		}
	});

	$(thumbnailSelector).draggable({
		zIndex: 1001,
		containment: 'parent',
		drag: function(evt, ui) {
			me.thumbnailDragged(evt, ui);
		}
	});

	this.viewer.addViewerMovedListener(this);
	this.viewer.addViewerZoomedListener(this);
	this.viewer.addViewerRotatedListener(this);

	this.loadThumbnail();
}

GsivThumbnail.prototype = {

	install : function() {
		$(this.thumbnailSelector)
			.append('<div class="grabbar"></div>')
			.append(
				$('<div class="container"></div>')
					.append(this.thumb)
					.append(this.locator)
			).show();
		this.loadThumbnail();
	},

	uninstall : function() {
		$(this.thumbnailSelector).empty().hide();
	},

	loadThumbnail : function() {
		var me = this;
		this.thumb.load(function() {
			me.thumb.unbind("load");
			me.thumbWidth = me.thumb.get(0).width;
			me.thumbHeight = me.thumb.get(0).height;
			me.locatorWidth = 0;
			me.locatorHeight = 0;
			me.lastPos = {x: 0, y: 0};

			var grabbar = $('.grabbar', me.thumbnailSelector);
			if( grabbar.length )
				grabbar.width(me.thumbWidth);

			me.viewerZoomed({x: me.viewer.x, y: me.viewer.y});
		}).attr('src', this.viewer.tileBaseUri + '/thumbnail.jpg?liv.method=tile&liv.rotation=' + this.viewer.rotation);
	},

	viewerMoved : function(e) {
		var left = e.x > 0 ? 0 : -e.x / (this.tileDim.width / this.thumbWidth);
		var top = e.y > 0 ? 0 : -e.y / (this.tileDim.height / this.thumbHeight);

		if( left + this.locatorWidth > this.thumbWidth ) {
			left = this.thumbWidth - this.locatorWidth;
		}

		if( top + this.locatorHeight > this.thumbHeight ) {
			top = this.thumbHeight - this.locatorHeight;
		}

		this.locator.css({
			left: left + 'px', 
			top: top + 'px', 
			position: 'absolute'});
	},

	viewerZoomed : function(e) {
		this.tileDim = this.viewer.getTileDimensions();

		this.locatorWidth = e.x > 0 ? this.tileDim.width : this.viewer.width;
		this.locatorHeight = e.y > 0 ? this.tileDim.height : this.viewer.height;

		this.locatorWidth /= (this.tileDim.width / this.thumbWidth);
		this.locatorHeight /= (this.tileDim.height / this.thumbHeight);

		this.locator.css({
			width: this.locatorWidth + 'px',
			height: this.locatorHeight + 'px'});

		this.viewerMoved(e);
	},

	viewerRotated : function(e) {
		this.loadThumbnail();
	},

	thumbnailDragged : function(evt, ui)
	{
	},
	
	locatorDragged : function(evt, ui) 
	{
		var x = ui.position.left;
		var y =  ui.position.top;
		this.lastPos.x = x >= 0 ? x : 0;
		this.lastPos.y = y >= 0 ? y : 0;
		this.locator.css('background-color', '#FF9999');
	},

	locatorDropped : function(evt, ui) 
	{
		var x = this.viewer.x + this.lastPos.x * (this.tileDim.width / this.thumbWidth);
		var y = this.viewer.y + this.lastPos.y * (this.tileDim.height / this.thumbHeight);
		this.locator.css('background-color', '#FF0000');
		this.viewer.positionTiles({x: -x, y: -y});
	}
};

function setupGsivThumbnail(viewer, thumbnailSelector, thumbnailControl, hideText, showText) {

	var thumb = new GsivThumbnail(viewer, thumbnailSelector);
	var control = $(thumbnailControl);

	function hideThumbnail() {
		thumb.uninstall();
		control.text(showText).unbind().bind("click", function() {
			thumb.install();
			control.text(hideText).unbind().bind("click", hideThumbnail);
		});
	}

	hideThumbnail();
}
