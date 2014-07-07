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
      devices:'devices/',
      config:'config.json',
      port:8000
     },

    context = {},
     billboard = { version:'0.1' };
//    files = fs.readdirSync(base.dRoot + base.scripts);

//app.configure(function(){});

for (var dev in ifaces) {
  ifaces[dev].forEach(function(details){
    if (details.family=='IPv4') {
      if (details.address !== '127.0.0.1'){
	context.ipAddress=details.address;
      }
    }
  });
}

context.url = 'http://'+context.ipAddress+":"+base.port;
context.scripts = fs.readdirSync(base.dRoot + base.scripts);

///////////////////////////////////////////////////
//
// Top
//
///////////////////////////////////////////////////

billboard.scripts = context.url + '/scripts';
billboard.logs    = context.url + '/logs';
billboard.devices    = context.url + '/devices';


app.get('/', function(req, res){
  res.send(JSON.stringify(billboard));
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
  var scriptRoot,
      scriptUrls,
      scriptFiles;
  scriptRoot = billboard.scripts;
  scriptFiles = fs.readdirSync(base.dRoot + base.scripts);
  scriptUrls = scriptFiles.map(function(fileName){return scriptRoot + "/" +  fileName })
  //res.send(fs.readdirSync(base.dRoot + base.scripts));
  res.send(scriptUrls);
});

app.get('/scripts/:id', function(req, res){
  res.send(fs.readFileSync(base.dRoot + base.scripts + req.params.id));
});

app.get('/logs', function(req, res){
  res.send(fs.readdirSync(base.dRoot + base.logs));
});

app.get('/devices', function(req, res){
  res.send(fs.readdirSync(base.dRoot + base.devices));
});

app.get('/devices/:id', function(req, res){
  res.send(fs.readFileSync(base.dRoot + base.scripts + req.params.id));
});

app.get('/config', function(req, res){
  res.send(fs.readFileSync(base.dRoot + base.config));
});


app.listen(base.port);

console.log("Server running at "+context.url);
