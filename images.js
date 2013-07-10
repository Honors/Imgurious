var fs = require('fs'),
	http = require('http');
	
var Cache = [];	
var GetCache = function(cb) {
	if( Cache.length ) {
		cb(Cache);
	} else {
		fs.readdir(__dirname + '/dl/', function(err, files) {
			Cache = files;
			cb(files);
		});
	}
};	
	
var UpdateCache = function(file) {
	GetCache(function() {
		Cache.push(file);
	});
};	
	
var CacheImage = function(link, cb) {
	var fname = link.split('/').pop();
	GetCache(function(files) {
		if( files.indexOf(fname) != -1 ) {
			cb(true);
			// cached...
			UpdateCache(fname);
		} else {
			cb(false);
			http.get({
				path: '/'+link.substr(7).split('/').slice(1).join('/'),
				host: link.substr(7).split('/')[0],
				headers: { 'Authorization': 'Client-ID b7c16c31dd48791' }
			}, function(res) {
				var dlPath = [__dirname, 'dl', fname].join('/');
				var write = fs.createWriteStream(dlPath);
				res.on("data", function(chunk) {
					write.write(chunk);
				});
				res.on("end", function() {
					write.end();
					UpdateCache(fname);					
				});				
			});	
		}	
	});	
};	
	
module.exports = function(link, id, cb) {
	if( !link.match(/\.jpg$/) ) {
		cb("");
		return;
	}
	CacheImage(link, function(ready) {
		if( ready ) {
			cb(['<div class="img"><a href="/image/', id ,'"><img src="/dl/', link.split('/').pop() ,'"></a></div>'].join(''))
		} else {
			cb('<div class="img"><a href="/">refresh to see this image.</a></div>');
		}
	});
};