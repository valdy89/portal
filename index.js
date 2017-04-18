var express = require('express')
var app = express()

app.get('/', function (req, res) {
  res.send('Hello World!')
})

app.get('/getDate', function(req, res){
  var test = 'text';
  res.send('<html><body><a href="/">aaaa</a></body></html>');
})

app.listen(3000, function () {
  console.log('Example app listening on port 3000!')
})
