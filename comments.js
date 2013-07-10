var api = require(__dirname + '/api.js'),
	fs = require('fs');

var view = "";
fs.readFile(__dirname + '/pages/view.html', function(err, data) {
	view = data+"";
});

module.exports = function(id, cb) {
	var comments = [];
	api.comments(id, function(comment) {
		comments.push(comment.comment);
	}, function() {
		var commentView = comments.map(function(comment) {
			return "<div class='comment'>"+comment+"</div>";
		}).join('');
		cb(view.replace("{{IMAGE}}", "http://i.imgur.com/"+id+".jpg").replace("{{COMMENTS}}", commentView));
	});
};