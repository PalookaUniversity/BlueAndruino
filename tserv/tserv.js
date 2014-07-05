//
//
//
var http=require('http'),
    fs=require('fs'),
    os=require('os'),
    ifaces=os.networkInterfaces(),
    express=require('express'),
    app = express(),


    base = {
      dRoot:'/data/tserv/dev/',
      scripts:'scripts/',
      logs:'logs/',
      port:8000
     },


    context = {
      dRoot:'/data/tserv/dev/',
      scripts:'scripts/',
      logs:'logs/',
      port:8000
     },
     billBoard = { version:'0.1' },
    files = fs.readdirSync(base.dRoot + base.scripts);

//app.configure(function(){});

for (var dev in ifaces) {
  var alias=0;
  ifaces[dev].forEach(function(details){
    if (details.family=='IPv4') {
      if (details.address !== '127.0.0.1'){
	context.ipAddress=details.address;
      }
      ++alias;
    }
  });
}

context.url = 'http://'+context.ipAddress+":"+base.port;

///////////////////////////////////////////////////
//
// Top
//
///////////////////////////////////////////////////

billBoard.scripts = context.url + '/scripts';
billBoard.logs    = context.url + '/logs';


app.get('/', function(req, res){
  res.send(JSON.stringify(billBoard));
})

///////////////////////////////////////////////////
//
// Services 
//
//////////////////////////////////////////////////

//app.get('/scripts/:doc', function(req, res){
//  var foo = fs.readFileSync(context.dRoot + context.scripts + '/' + req.params.doc);
  //res.send('deliver script:' + req.params.doc);
//  res.send(foo);
//});

app.get('/scripts', function(req, res){
  res.send(files);
});



app.listen(context.port);

console.log("Server running at "+context.url);
