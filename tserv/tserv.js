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

// Configure our HTTP server to respond with Hello World to all requests.
//var server = http.createServer(function (request, response) {
//  console.log(request.url);
//  response.writeHead(200, {"Content-Type": "text/plain"});
//  response.end("Hello World\n");
//});

app.configure(function(){
});



app.get('/scripts/:doc', function(req, res){
  res.send('deliver script:' + req.params.doc);
});


app.get('/scripts', function(req, res){
  res.send(files);
});


app.get('/', function(req, res){
  res.send('hello world');
});

// Listen on port 8000, IP defaults to 127.0.0.1
app.listen(8000);

// Put a friendly message on the terminal
console.log("Server running at http://127.0.0.1:8000/");


