# Cordova-Plugin-Bluetooth-Printer
A cordova android platform plugin for bluetooth printer.

##Support
- Text
- isOnline test
- Image Printing

##Install
Using the Cordova CLI and NPM, run:

```
cordova plugin add https://github.com/joss-fze/Cordova-Plugin-Bluetooth-Printer
```



##Usage
Get list of paired bluetooth printers

```
BTPrinter.list(function(data){
        console.log("Success");
        console.log(data); \\list of printer in data array
    },function(err){
        console.log("Error");
        console.log(err);
    })
```


Connect printer

```
BTPrinter.connect(function(data){
	console.log("Success");
	console.log(data)
},function(err){
	console.log("Error");
	console.log(err)
}, "PrinterName")
```


Disconnect printer

```
BTPrinter.disconnect(function(data){
	console.log("Success");
	console.log(data)
},function(err){
	console.log("Error");
	console.log(err)
}, "PrinterName")
```


isOnline printer

```
BTPrinter.isOnline(function(data){
	console.log("Success");
	console.log(data)
},function(err){
	console.log("Error");
	console.log(err)
})
```


Print bitmap picture

```
BTPrinter.printBmp(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "Bitmap base64 encoded string to Print")
```


