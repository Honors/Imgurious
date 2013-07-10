var https = require('https');
	
var ImagesAPI = function(cb, end) {
	https.get({
		path: '/3/gallery/hot/viral/0.json',
		host: 'api.imgur.com',
		headers: { 'Authorization': 'Client-ID b7c16c31dd48791' }
	}, function(res) {
		var buffer = [];
		res.on('data', function(chunk) {
			buffer.push(chunk);
		});
		res.on('end', function() {
			var imgs = JSON.parse(buffer.join('')).data;
			imgs.map(cb);
			end();
		});
	});
};

var CommentsAPI = function(id, cb, end) {
	https.get({
		path: '/3/gallery/image/'+id+'/comments',
		host: 'api.imgur.com',
		headers: { 'Authorization': 'Client-ID b7c16c31dd48791' }
	}, function(res) {
		var buffer = [];
		res.on('data', function(chunk) {
			buffer.push(chunk);
		});
		res.on('end', function() {
			var comments = JSON.parse(buffer.join('')).data;
			comments.map(cb);
			end();
		});
	});
};


module.exports = {
	images: ImagesAPI,
	comments: CommentsAPI
};