
function GSIV(viewer, options) {

	// listeners that are notified on a move (pan) event
	this.viewerMovedListeners = [];
	// listeners that are notified on a zoom event
	this.viewerZoomedListeners = [];
	// listeners that are notified on a rotate event
	this.viewerRotatedListeners = [];

	if (typeof viewer == 'string') {
		this.viewer = document.getElementById(viewer);
	}
	else {
		this.viewer = viewer;
	}

	if (typeof options == 'undefined') {
		options = {};
	}

	this.initialPan = (options.initialPan ? options.initialPan : GSIV.INITIAL_PAN);

	this.rotation = 0;  // north = 0, west = 1, south = 2, east = 3
	this.zoomLevel = -1;
	this.tileBaseUri = options.tileBaseUri;
	this.geometry = options.geometry;
	this.initialized = false;
	this.surface = null;
	this.well = null;
	this.width = 0;
	this.height = 0;
	this.top = 0;
	this.left = 0;
	this.x = 0;
	this.y = 0;
	this.mark = { 'x' : 0, 'y' : 0 };
	this.pressed = false;
	this.tiles = [];

	this.cache = {};
	this.cache['blank'] = new Image();
	this.cache['blank'].src = options.blankTile;
	
	this.grabMouseCursor = options.grabMouseCursor;
	
	GSIV.GRAB_MOUSE_CURSOR = (navigator.userAgent.search(/KHTML|Opera/i) >= 0 ? 'pointer' : (document.attachEvent ? options.grabMouseCursor : '-moz-grab'));
	GSIV.GRABBING_MOUSE_CURSOR = (navigator.userAgent.search(/KHTML|Opera/i) >= 0 ? 'move' : (document.attachEvent ? options.grabbingMouseCursor : '-moz-grabbing'));

	// employed to throttle the number of redraws that
	// happen while the mouse is moving
	this.moveCount = 0;
	this.slideMonitor = 0;
	this.slideAcceleration = 0;

	// add to viewer registry
	GSIV.VIEWERS[GSIV.VIEWERS.length] = this;
}

// CSS definition settings
GSIV.TILE_STYLE_CLASS = 'tile';

// defaults if not provided as constructor options
GSIV.TILE_SIZE = 256;
GSIV.INITIAL_PAN = { 'x' : .5, 'y' : .5 };
GSIV.USE_LOADER_IMAGE = true;
GSIV.USE_SLIDE = true;
GSIV.USE_KEYBOARD = true;

// performance tuning variables
GSIV.MOVE_THROTTLE = 3;
GSIV.SLIDE_DELAY = 40;
GSIV.SLIDE_ACCELERATION_FACTOR = 5;

// the following are calculated settings
GSIV.DOM_ONLOAD = (navigator.userAgent.indexOf('KHTML') >= 0 ? false : true);

// registry of all known viewers
GSIV.VIEWERS = [];

// utility functions
GSIV.isInstance = function(object, clazz) {
	while (object != null) {
		if (object == clazz.prototype) {
			return true;
		}

		object = object.__proto__;
	}

	return false;
}

GSIV.prototype = {
	/**
	 * Resize the viewer to fit snug inside the browser window (or frame).
	 * This method should be called prior to init()
	 */
	fitToWindow : function() {
	  	
		var calcWidth = 0;
		var calcHeight = 0;
		if (window.innerWidth) {
			calcWidth = window.innerWidth;
			calcHeight = window.innerHeight;
		} else {
			var w = $(window);
			calcWidth = w.width();
			calcHeight = w.height();
		}
		
		if (calcWidth == 0)
		{
			calcWidth = (document.compatMode == 'CSS1Compat'
							? document.documentElement.clientWidth
							: document.body.clientWidth);
		}
		
		if (calcHeight == 0)
		{
			calcHeight = (document.compatMode == 'CSS1Compat'
							? document.documentElement.clientHeight
							: document.body.clientHeight);
		}
		
		/* Avoid scrollbars */
		calcWidth -= 1;
		calcHeight -= 1;

		/* Remove the height of the controls */
		calcHeight -= $('.controls').outerHeight();
		
		if (calcWidth % 2) {
			calcWidth--;
		}

		if (calcHeight % 2) {
			calcHeight--;
		}
		
		if( calcHeight < 100 ) {
			calcHeight = 100;
		}

		if( calcWidth < 100 ) {
			calcWidth = 100;
		}

		this.width = calcWidth;
		this.height = calcHeight;
		this.viewer.style.width = this.width + 'px';
		this.viewer.style.height = this.height + 'px';
		this.viewer.style.top = '0';
		this.viewer.style.left = '0';
	},

	init : function() {
		if (document.attachEvent) {
			document.body.ondragstart = function() { return false; }
		}

		if (this.width == 0 && this.height == 0) {
			this.width = this.viewer.offsetWidth;
			this.height = this.viewer.offsetHeight;
		}

		// calculate the zoom level based on what fits best in window
		var td;
		var maxZoom = this.getZoomLevelCount() - 1;
		this.zoomLevel = -1;
		do {
			this.zoomLevel++;
			td = this.getTileDimensions();
		} while (this.zoomLevel < maxZoom && (td.width > this.width || td.height > this.height));

		// move top level up and to the left so that the image is centered
		this.x = Math.floor((td.width - this.width) * -this.initialPan.x);
		this.y = Math.floor((td.height - this.height) * -this.initialPan.y);

		// offset of viewer in the window
		for (var node = this.viewer; node; node = node.offsetParent) {
			this.top += node.offsetTop;
			this.left += node.offsetLeft;
		}

		this.viewer.backingBean = this;
		var jviewer = $(this.viewer);

		this.surface = $('<div class="surface"></div>').get(0);
		this.surface.backingBean = this;
		this.surface.style.cursor = GSIV.GRAB_MOUSE_CURSOR;
		jviewer.append(this.surface);

		this.well = $('<div class="well"></div>').get(0);
		this.well.backingBean = this;
		jviewer.append(this.well);

		this.prepareTiles();
		this.initialized = true;
	},

	prepareTiles : function() {
		var rows = Math.ceil(this.height / GSIV.TILE_SIZE) + 1;
		var cols = Math.ceil(this.width / GSIV.TILE_SIZE) + 1;

		for (var c = 0; c < cols; c++) {
			var tileCol = [];

			for (var r = 0; r < rows; r++) {
				/**
				 * element is the DOM element associated with this tile
				 * posx/posy are the pixel offsets of the tile
				 * xIndex/yIndex are the index numbers of the tile segment
				 * qx/qy represents the quadrant location of the tile
				 */
				var tile = {
					'element' : null,
					'posx' : 0,
					'posy' : 0,
					'xIndex' : c,
					'yIndex' : r,
					'qx' : c,
					'qy' : r
				};

				tileCol.push(tile);
			}

			this.tiles.push(tileCol);
		}

		this.surface.onmousedown = GSIV.mousePressedHandler;
		this.surface.onmouseup = this.surface.onmouseout = GSIV.mouseReleasedHandler;
		this.surface.ondblclick = GSIV.doubleClickHandler;

		window.onkeypress = GSIV.keyboardMoveHandler;
		window.onkeydown = GSIV.keyboardZoomHandler;

		$(this.surface).mousewheel(function(event, delta) {
			// HACK: Events appear to be being raised more than once.
			//       Unsure whose fault this is at the moment.
			if( (typeof event.timeStamp == 'undefined') || (this.timeStamp != event.timeStamp) )
			{
				this.timeStamp = event.timeStamp;
				this.backingBean.zoom(delta > 0 ? 1 : -1);
			}
			return false;
		});

		this.positionTiles();
	},

	/**
	 * Ensure the motion will not move the image to place we do not want it to go
	 */
	sanitiseMotion : function(motion) {
		var targetX = this.x + motion.x;
		var targetY = this.y + motion.y;
		var td = this.getTileDimensions();

		if (td.width <= this.width)
		{
			motion.x = 0;
		}
		else if (targetX > 0)
		{
			motion.x = -this.x;
		}
		else if (targetX + td.width < this.width)
		{
			motion.x = -this.x + this.width - td.width;
		}

		if (td.height <= this.height)
		{
			motion.y = 0;
		}
		else if (targetY > 0)
		{
			motion.y = -this.y;
		}
		else if (targetY + td.height < this.height)
		{
			motion.y = -this.y + this.height - td.height;
		}
	},

	/**
	 * Position the tiles based on the x, y coordinates of the
	 * viewer, taking into account the motion offsets, which
	 * are calculated by a motion event handler.
	 */
	positionTiles : function(motion, reset) {
		// default to no motion, just setup tiles
		if (typeof motion == 'undefined') {
			motion = { 'x' : 0, 'y' : 0 };
		}
		else
		{
			this.sanitiseMotion(motion);
		}

		var dbg = 0;
		for (var c = 0; c < this.tiles.length; c++) {
			for (var r = 0; r < this.tiles[c].length; r++) {
				dbg += 1;
				var tile = this.tiles[c][r];

				tile.posx = (tile.xIndex * GSIV.TILE_SIZE) + this.x + motion.x;
				tile.posy = (tile.yIndex * GSIV.TILE_SIZE) + this.y + motion.y;

				var visible = true;

				if (tile.posx > this.width) {
					// tile moved out of view to the right
					// consider the tile coming into view from the left
					do {
						tile.xIndex -= this.tiles.length;
						tile.posx = (tile.xIndex * GSIV.TILE_SIZE) + this.x + motion.x;
					} while (tile.posx > this.width);

					if (tile.posx + GSIV.TILE_SIZE < 0) {
						visible = false;
					}

				} else {
					// tile may have moved out of view from the left
					// if so, consider the tile coming into view from the right
					while (tile.posx < -GSIV.TILE_SIZE) {
						tile.xIndex += this.tiles.length;
						tile.posx = (tile.xIndex * GSIV.TILE_SIZE) + this.x + motion.x;
					}

					if (tile.posx > this.width) {
						visible = false;
					}
				}

				if (tile.posy > this.height) {
					// tile moved out of view to the bottom
					// consider the tile coming into view from the top
					do {
						tile.yIndex -= this.tiles[c].length;
						tile.posy = (tile.yIndex * GSIV.TILE_SIZE) + this.y + motion.y;
					} while (tile.posy > this.height);

					if (tile.posy + GSIV.TILE_SIZE < 0) {
						visible = false;
					}

				} else {
					// tile may have moved out of view to the top
					// if so, consider the tile coming into view from the bottom
					while (tile.posy < -GSIV.TILE_SIZE) {
						tile.yIndex += this.tiles[c].length;
						tile.posy = (tile.yIndex * GSIV.TILE_SIZE) + this.y + motion.y;
					}

					if (tile.posy > this.height) {
						visible = false;
					}
				}

				// initialize the image object for this quadrant
				if (!this.initialized) {
					this.assignTileImage(tile, true);
					tile.element.style.top = tile.posy + 'px';
					tile.element.style.left = tile.posx + 'px';
				}

				// display the image if visible
				if (visible) {
					this.assignTileImage(tile);
				}

				// seems to need this no matter what
				tile.element.style.top = tile.posy + 'px';
				tile.element.style.left = tile.posx + 'px';
			}
		}

		// reset the x, y coordinates of the viewer according to motion
		if (reset) {
			this.x += motion.x;
			this.y += motion.y;
		}
		//alert(dbg + ' x,y:' + motion.x + ',' + motion.y);
	},

	/**
	 * Determine the source image of the specified tile based
	 * on the zoom level and position of the tile.  If forceBlankImage
	 * is specified, the source should be automatically set to the
	 * null tile image.  This method will also setup an onload
	 * routine, delaying the appearance of the tile until it is fully
	 * loaded, if configured to do so.
	 */
	assignTileImage : function(tile, forceBlankImage) {
		var tileImgId, src;
		var useBlankImage = (forceBlankImage ? true : false);

		// check if image has been scrolled too far in any particular direction
		// and if so, use the null tile image
		if (!useBlankImage) {
			var g = this.getGeometry();

			var left = tile.xIndex < 0;
			var high = tile.yIndex < 0;
			var right = tile.xIndex >= g.width;
			var low = tile.yIndex >= g.height;

			if (high || left || low || right) {
				useBlankImage = true;
			}
		}

		if (useBlankImage) {
			tileImgId = 'blank:' + tile.qx + ':' + tile.qy;
			src = this.cache['blank'].src;
		}
		else {
			tileImgId = src = this.getTileImagePath(tile.xIndex, tile.yIndex, this.zoomLevel);
		}

		// only remove tile if identity is changing
		if (tile.element != null &&
			tile.element.parentNode != null &&
			tile.element.relativeSrc != src) {
			this.well.removeChild(tile.element);
		}

		var tileImg = this.cache[tileImgId];
		// create cache if not exist
		if (tileImg == null) {
			tileImg = this.cache[tileImgId] = this.createPrototype(src);
		}

		if (useBlankImage || !GSIV.USE_LOADER_IMAGE || tileImg.complete || (tileImg.image && tileImg.image.complete)) {
			tileImg.onload = function() {};
			if (tileImg.image) {
				tileImg.image.onload = function() {};
			}
			tile.element = this.well.appendChild(tileImg);
		}
		else {
			var loadingImgId = 'loading:' + tile.qx + ':' + tile.qy;
			var loadingImg = this.cache[loadingImgId];
			if (loadingImg == null) {
				loadingImg = this.cache[loadingImgId] = this.createPrototype(this.cache['blank'].src);
			}

			loadingImg.targetSrc = tileImgId;

			var well = this.well;
			tile.element = well.appendChild(loadingImg);

			tileImg.onload = function() {
				// make sure our destination is still present
				if (loadingImg.parentNode && loadingImg.targetSrc == tileImgId) {
					tileImg.style.top = loadingImg.style.top;
					tileImg.style.left = loadingImg.style.left;
					tile.element = tileImg;

					try {
						well.replaceChild(tileImg, loadingImg);
						$(tileImg).hide().fadeIn('fast');
					} catch( e ) {
						// Safe to ignore
					}
				}

				tileImg.onload = function() {};
				return false;
			}

			// konqueror only recognizes the onload event on an Image
			// javascript object, so we must handle that case here
			if (!GSIV.DOM_ONLOAD) {
				tileImg.image = new Image();
				tileImg.image.onload = tileImg.onload;
				tileImg.image.src = tileImg.src;
			}
		}
	},

	createPrototype : function(src) {
		var img = document.createElement('img');
		img.src = src;
		img.relativeSrc = src;
		img.className = GSIV.TILE_STYLE_CLASS;
		img.style.width = GSIV.TILE_SIZE + 'px';
		img.style.height = GSIV.TILE_SIZE + 'px';
		return img;
	},

	addViewerMovedListener : function(listener) {
		this.viewerMovedListeners.push(listener);
	},

	addViewerZoomedListener : function(listener) {
		this.viewerZoomedListeners.push(listener);
	},

	addViewerRotatedListener : function(listener) {
		this.viewerRotatedListeners.push(listener);
	},

	/**
	 * Notify listeners of a zoom event on the viewer.
	 */
	notifyViewerZoomed : function() {
		var percentage = 100 / this.getZoomLevelCount() * (this.zoomLevel + 1);
		var me = this;
		$.each(this.viewerZoomedListeners, function(i, n) {
			n.viewerZoomed(new GSIV.ZoomEvent(me.x, me.y, me.zoomLevel, percentage));
		});
	},

	/**
	 * Notify listeners of a move event on the viewer.
	 */
	notifyViewerMoved : function(coords) {
		if (typeof coords == 'undefined') {
			coords = { 'x' : 0, 'y' : 0 };
		}

		var me = this;
		$.each(this.viewerMovedListeners, function(i, n) {
			n.viewerMoved(new GSIV.MoveEvent(
				me.x + (coords.x - me.mark.x),
				me.y + (coords.y - me.mark.y)
			));
		});
	},

	/**
	 * Notify listeners of a rotate event on the viewer.
	 */
	notifyViewerRotated : function() {
		var me = this;
		$.each(this.viewerRotatedListeners, function(i, n) {
			n.viewerRotated(new GSIV.RotateEvent(me.rotation));
		});
	},

	zoom : function(direction) {
		// ensure we are not zooming out of range
		var targetZoomLevel = this.zoomLevel - direction;
		if (targetZoomLevel < 0 || targetZoomLevel >= this.getZoomLevelCount()) {
			return;
		}

		this.blank();

		var coords = { 'x' : Math.floor(this.width / 2), 'y' : Math.floor(this.height / 2) };

		var before = {
			'x' : (coords.x - this.x),
			'y' : (coords.y - this.y)
		};

		var after = {
			'x' : Math.floor(before.x * Math.pow(2, direction)),
			'y' : Math.floor(before.y * Math.pow(2, direction))
		};

		this.x = coords.x - after.x;
		this.y = coords.y - after.y;

		this.zoomLevel -= direction;

		var td = this.getTileDimensions();

		// Ensure that image is centred if smaller than screen, or not scrolled past edges
		if (td.width <= this.width)
		{
			this.x = (this.width - td.width) / 2;
		}
		else if (this.x > 0)
		{
			this.x = 0
		}
		else if (this.x + td.width < this.width)
		{
			this.x = this.width - td.width;
		}

		// Ensure that image is centred if smaller than screen, or not scrolled past edges
		if (td.height <= this.height)
		{
			this.y = (this.height - td.height) / 2;
		}
		else if (this.y > 0)
		{
			this.y = 0;
		}
		else if (this.y + td.height < this.height)
		{
			this.y = this.height - td.height;
		}

		this.positionTiles();
		this.notifyViewerZoomed();
	},

	/**
	 * Clear all the tiles from the well for a complete reinitialization of the
	 * viewer. At this point the viewer is not considered to be initialized.
	 */
	clear : function() {
		this.blank();
		this.initialized = false;
		this.tiles = [];
	},

	/**
	 * Remove all tiles from the well, which effectively "hides"
	 * them for a repaint.
	 */
	blank : function() {
		for (imgId in this.cache) {
			var img = this.cache[imgId];
			img.onload = function() {};
			if (img.image) {
				img.image.onload = function() {};
			}

			if (img.parentNode != null) {
				this.well.removeChild(img);
			}
		}
	},

	/**
	 * Method specifically for handling a mouse move event.  A direct
	 * movement of the viewer can be achieved by calling positionTiles() directly.
	 */
	moveViewer : function(coords) {
		this.positionTiles({ 'x' : (coords.x - this.mark.x), 'y' : (coords.y - this.mark.y) });
		this.notifyViewerMoved(coords);
	},

	/**
	 * Make the specified coords the new center of the image placement.
	 * This method is typically triggered as the result of a double-click
	 * event.  The calculation considers the distance between the center
	 * of the viewable area and the specified (viewer-relative) coordinates.
	 * If absolute is specified, treat the point as relative to the entire
	 * image, rather than only the viewable portion.
	 */
	recenter : function(coords, absolute) {
		if (absolute) {
			coords.x += this.x;
			coords.y += this.y;
		}

		var motion = {
			'x' : Math.floor((this.width / 2) - coords.x),
			'y' : Math.floor((this.height / 2) - coords.y)
		};

		this.sanitiseMotion(motion);

		if (motion.x == 0 && motion.y == 0) {
			return;
		}

		if (GSIV.USE_SLIDE) {
			var target = motion;
			var x, y;
			// handle special case of vertical movement
			if (target.x == 0) {
				x = 0;
				y = this.slideAcceleration;
			}
			else {
				var slope = Math.abs(target.y / target.x);
				x = Math.round(Math.pow(Math.pow(this.slideAcceleration, 2) / (1 + Math.pow(slope, 2)), .5));
				y = Math.round(slope * x);
			}

			motion = {
				'x' : Math.min(x, Math.abs(target.x)) * (target.x < 0 ? -1 : 1),
				'y' : Math.min(y, Math.abs(target.y)) * (target.y < 0 ? -1 : 1)
			}
		}

		this.positionTiles(motion, true);
		this.notifyViewerMoved();

		if (!GSIV.USE_SLIDE) {
			return;
		}

		var newcoords = {
			'x' : coords.x + motion.x,
			'y' : coords.y + motion.y
		};

		var self = this;
		// TODO: use an exponential growth rather than linear (should also depend on how far we are going)
		// FIXME: this could be optimized by calling positionTiles directly perhaps
		this.slideAcceleration += GSIV.SLIDE_ACCELERATION_FACTOR;
		this.slideMonitor = setTimeout(function() { self.recenter(newcoords); }, GSIV.SLIDE_DELAY );
	},

	resize : function() {
		// IE fires a premature resize event
		if (!this.initialized) {
			return;
		}

		this.viewer.style.display = 'none';
		this.clear();

		var before = {
			'x' : Math.floor(this.width / 2),
			'y' : Math.floor(this.height / 2)
		};

		this.fitToWindow();

		this.prepareTiles();

		var after = {
			'x' : Math.floor(this.width / 2),
			'y' : Math.floor(this.height / 2)
		};

		this.x += (after.x - before.x);
		this.y += (after.y - before.y);
		this.positionTiles();
		this.viewer.style.display = '';
		this.initialized = true;

		this.notifyViewerZoomed();
	},

	/**
	 * Resolve the coordinates from this mouse event by subtracting the
	 * offset of the viewer in the browser window (or frame).  This does
	 * take into account the scroll offset of the page.
	 */
	resolveCoordinates : function(e) {
		return {
			'x' : (e.pageX || (e.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft))) - this.left,
			'y' : (e.pageY || (e.clientY + (document.documentElement.scrollTop || document.body.scrollTop))) - this.top
		}
	},

	press : function(coords) {
		this.activate(true);
		this.mark = coords;
	},

	release : function(coords) {
		this.activate(false);
		var motion = {
			'x' : (coords.x - this.mark.x),
			'y' : (coords.y - this.mark.y)
		};

		this.sanitiseMotion(motion);
		this.x += motion.x;
		this.y += motion.y;
		this.mark = { 'x' : 0, 'y' : 0 };

		this.notifyViewerMoved();
	},

	/**
	 * Activate the viewer into motion depending on whether the mouse is pressed or
	 * not pressed.  This method localizes the changes that must be made to the
	 * layers.
	 */
	activate : function(pressed) {
		this.pressed = pressed;
		this.surface.style.cursor = (pressed ? GSIV.GRABBING_MOUSE_CURSOR : GSIV.GRAB_MOUSE_CURSOR);
		this.surface.onmousemove = (pressed ? GSIV.mouseMovedHandler : function() {});
	},

	/**
	 * Check whether the specified point exceeds the boundaries of
	 * the viewer's primary image.
	 */
	pointExceedsBoundaries : function(coords) {
		var td = this.getTileDimensions();
		return
			coords.x < this.x ||
			coords.y < this.y ||
			coords.x > (td.width + this.x) ||
			coords.y > (td.height + this.y);
	},

	// QUESTION: where is the best place for this method to be invoked?
	resetSlideMotion : function() {
		if (this.slideMonitor != 0) {
			clearTimeout(this.slideMonitor);
			this.slideMonitor = 0;
		}

		this.slideAcceleration = 0;
	},

	getZoomLevelCount: function() {
		return this.geometry.length;
	},

	getTileDimensions : function() {
		var g = this.getGeometry();
		return {width: g.width * GSIV.TILE_SIZE, height: g.height * GSIV.TILE_SIZE};
	},

	/**
	 * Returns the number of tiles {width,height} for the current zoom level.
	 */
	getGeometryPreRotation : function() {
		if (this.getZoomLevelCount() == 0)
		{
			return { width: 1, height: 1 };
		}
		else
		{
			var g = this.geometry[this.zoomLevel];
			return {
				width: g[0],
				height: g[1]
			};
		}
	},

	/**
	 * Returns the number of tiles {width,height} for the current zoom level.
	 */
	getGeometry : function() {
		var g = this.getGeometryPreRotation();
		if( this.rotation % 2 != 0 )
		{
			var h = g.height;
			g.height = g.width;
			g.width = h;
		}
		return g;
	},

	/**
	 * Returns the path for the tile image.  Rotates tile coordinates to the required coordinates.
	 */
	getTileImagePath : function(x, y, z) {

		var xy = [x, y];

		// Simple matrix multiplication - 90 degrees clockwise each time.
		// If we are pointing 'north' (0), we need no translation.
		// If we are pointing 'west' (1), we need the tile 270 c.c.w, so 90 c.w.
		// If we are pointing 'south' (2), we need the tile 180 c.c.w, so 180 c.w.
		// If we are pointing 'east' (3), we need the tile 90 c.c.w, so 270 c.w.
		var g = this.getGeometry();
		for( var i = 0; i < this.rotation; i++ )
		{
			var cols = i % 2 ? g.width : g.height;
			xy = [-xy[1] + cols - 1, xy[0]];
		}
		
		return this.tileBaseUri + '/' + z + '_' + xy[1] + '_' + xy[0] + '.jpg?liv.method=tile&liv.rotation=' + this.rotation;
	},

	rotateLeft : function() {
		this.rotate((this.rotation + 1) % 4);
	},

	rotateRight : function() {
		// Same as: (this.rotation + 4 - 1) % 4
		// This is so 0 becomes 3 and not -1
		this.rotate((this.rotation + 3) % 4);
	},

	rotate : function(direction) {
		this.rotation = direction;
		this.blank();
		this.positionTiles();
		this.notifyViewerRotated();
	}
};

GSIV.mousePressedHandler = function(e) {
	e = e ? e : window.event;
	// only grab on left-click
	if (e.button < 2) {
		var self = this.backingBean;
		var coords = self.resolveCoordinates(e);
		if (self.pointExceedsBoundaries(coords)) {
			e.cancelBubble = true;
		}
		else {
			self.press(coords);
		}
	}

	// NOTE: MANDATORY! must return false so event does not propagate to well!
	return false;
};

GSIV.mouseReleasedHandler = function(e) {
	e = e ? e : window.event;
	var self = this.backingBean;
	if (self.pressed) {
		// OPTION: could decide to move viewer only on release, right here
		self.release(self.resolveCoordinates(e));
	}
};

GSIV.mouseMovedHandler = function(e) {
	e = e ? e : window.event;
	var self = this.backingBean;
	self.moveCount++;
	if (self.moveCount % GSIV.MOVE_THROTTLE == 0) {
		self.moveViewer(self.resolveCoordinates(e));
	}
};

GSIV.zoomInHandler = function(e) {
	e = e ? e : window.event;
	var self = this.parentNode.parentNode.backingBean;
	self.zoom(1);
	return false;
};

GSIV.zoomOutHandler = function(e) {
	e = e ? e : window.event;
	var self = this.parentNode.parentNode.backingBean;
	self.zoom(-1);
	return false;
};

GSIV.rotateLeftHandler = function(e) {
	e = e ? e : window.event;
	var self = this.parentNode.parentNode.backingBean;
	self.rotateLeft();
	return false;
};

GSIV.rotateRightHandler = function(e) {
	e = e ? e : window.event;
	var self = this.parentNode.parentNode.backingBean;
	self.rotateRight();
	return false;
};

GSIV.doubleClickHandler = function(e) {
	e = e ? e : window.event;
	var self = this.backingBean;
	coords = self.resolveCoordinates(e);
	if (!self.pointExceedsBoundaries(coords)) {
		self.resetSlideMotion();
		self.recenter(coords);
	}
};

GSIV.keyboardMoveHandler = function(e) {
	e = e ? e : window.event;
	for (var i = 0; i < GSIV.VIEWERS.length; i++) {
		var viewer = GSIV.VIEWERS[i];
		if (e.keyCode == 40)
				viewer.positionTiles({'x': 0,'y': -GSIV.MOVE_THROTTLE}, true);
		if (e.keyCode == 39)
				viewer.positionTiles({'x': -GSIV.MOVE_THROTTLE,'y': 0}, true);
		if (e.keyCode == 38)
				viewer.positionTiles({'x': 0,'y': GSIV.MOVE_THROTTLE}, true);
		if (e.keyCode == 37)
				viewer.positionTiles({'x': GSIV.MOVE_THROTTLE,'y': 0}, true);
	}
}

GSIV.keyboardZoomHandler = function(e) {
	e = e ? e : window.event;
	for (var i = 0; i < GSIV.VIEWERS.length; i++) {
		var viewer = GSIV.VIEWERS[i];
		if (e.keyCode == 109)
				viewer.zoom(-1);
		if (e.keyCode == 61)
				viewer.zoom(1);
	}
}

GSIV.MoveEvent = function(x, y) {
	this.x = x;
	this.y = y;
};

GSIV.ZoomEvent = function(x, y, level, percentage) {
	this.x = x;
	this.y = y;
	this.percentage = percentage;
	this.level = level;
};

GSIV.RotateEvent = function(rotation) {
	this.rotation = rotation;
};
