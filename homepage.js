var api = require(__dirname + '/api.js'),
	Img = require(__dirname + '/images.js'),
	fs = require('fs');

var homepage = "";
fs.readFile(__dirname + '/pages/index.html', function(err, data) {
	homepage = data+"";
});

var Homepage = [], _Homepage = [];

var RefreshHomepage = function(cb) {	
	api.images(function(img) {
		_Homepage.push(img);
	}, function() {
		Homepage = _Homepage;
		_Homepage = [];
		cb(Homepage);
	});	
};

var GetHomepage = function(cb) {
	if( Homepage.length ) {
		cb(Homepage);
	} else {
		RefreshHomepage(cb);	
	}
};

var RenderHomepage = function(cb) {
	GetHomepage(function(imgs) {
		var renders = [], rendered = 0;
		imgs.map(function(img, index) {
			var imglink = img.link;				
			Img(imglink, img.id, function(text) {
				renders[index] = text;
				rendered += 1;
				if( rendered >= imgs.length ) {
					cb(homepage.replace(/\{\{CONTENT\}\}/, renders.join('')));
				}
			});
		});		
	});
};

module.exports = {
	refresh: RefreshHomepage,
	render: RenderHomepage
};