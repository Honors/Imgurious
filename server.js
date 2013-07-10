var http = require('http'),
	fs = require('fs'),
	api = require(__dirname + '/api.js'),
	app = require(__dirname + '/route.js'),
	homepage = require(__dirname + '/homepage.js'),
	comments = require(__dirname + '/comments.js');

app.get({
	path: /^/,
	cb: function(req, res) {
		res.writeHead(404);
		res.end();
	}
}).get({
	path: /^\/dl\//,
	cb: function(req, res) {
		var fname = req.url.split('/').pop();
		res.writeHead(200, {'Content-Type': 'image/jpeg'});
		fs.createReadStream(__dirname + '/dl/' + fname).pipe(res);
	}
}).get({
	path: /^\/$/,
	cb: function(req, res) {
		res.writeHead(200, { 'Content-Type': 'text/html' });
		homepage.render(function(content) {			
			res.write(content);
			res.end();
		});		
	}
}).get({
	path: /^\/image\//,
	cb: function(req, res) {
		var id = req.url.substr(1).split('/')[1];
		res.writeHead(200, { 'Content-Type': 'text/html' });
		comments(id, function(content) {
			res.write(content);
			res.end();
		});	
	}
})

http.createServer(app).listen(8080);