import express = require('express');
var app = express();


app.get('/is-alive', (req, res) => {
 res.sendStatus(200);
});

app.listen(3000, function () {
  console.log('Example app listening on port 3000! haha ')
})
