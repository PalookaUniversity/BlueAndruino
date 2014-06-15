//
//
//
var http=require('http'),
    fs=require('fs'),
    os=require('os'),
    express=require('express'),
    app = express(),
    context = {
      dRoot:'/data/tserv/dev/',
      scripts:'scripts/',
      logs:'logs/'
     },
    files = fs.readdirSync(context.dRoot + context.scripts);

//app.configure(function(){});

app.get('/scripts/:doc', function(req, res){
  var foo = fs.readFileSync(context.dRoot + context.scripts + '/' + req.params.doc);
  //res.send('deliver script:' + req.params.doc);
  res.send(foo);
});

app.get('/scripts', function(req, res){
  res.send(files);
});

app.get('/', function(req, res){
  res.send('hello world');
});

app.listen(8000);
console.log("Server running at http://127.0.0.1:8000/");
