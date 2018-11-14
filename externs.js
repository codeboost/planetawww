var window = {};
var kolbasulPlanetar;

var Oscilloscope = function () {};
Oscilloscope.prototype = {
  "animate": function () {},
  "draw": function () {},
  "stop": function () {},
  "setDrawfn": function() {}
};

var screenfull = function() {};

screenfull.prototype = {
	"request" : function() {},
	"exit" : function() {},
	"onchange": function() {},
	"onerror" : function(){},
	"toggle" : function() {}

}