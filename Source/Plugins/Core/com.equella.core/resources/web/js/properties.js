/**
 * Need to be dynamic
 * No trailing slashes on any of these addresses please
 */
var splits = window.location.href.split("/p/r"); //There's probably a smarter way to do this
var address = "";
for(var i = 0; i < splits.length - 2; i++) {
	address += splits[i] + "/p/r";
};
address += splits[splits.length - 2];
var equellaurl = address;

var splits = window.location.href.split("/");
var address = "";
for(var i = 0; i < splits.length - 2; i++) {
	address += splits[i] + "/";
};
address += splits[splits.length - 2];
var thisurl = address;