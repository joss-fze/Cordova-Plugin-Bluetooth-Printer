var exec = require('cordova/exec');

var BTPrinter = {
   list: function(fnSuccess, fnError){
      exec(fnSuccess, fnError, "BluetoothPrinter", "list", []);
   },
   connect: function(fnSuccess, fnError, name){
      exec(fnSuccess, fnError, "BluetoothPrinter", "connect", [name]);
   },
   disconnect: function(fnSuccess, fnError){
      exec(fnSuccess, fnError, "BluetoothPrinter", "disconnect", []);
   },
   print: function(fnSuccess, fnError, str){
      exec(fnSuccess, fnError, "BluetoothPrinter", "print", [str]);
   },
   printBMP: function(fnSuccess, fnError, printernName, str){
      console.info("Sending bitmap to printer: "+JSON.stringify({ "name" : printernName, "args": str}));
      exec(fnSuccess, fnError, "BluetoothPrinter", "printBMP", [{ "name" : printernName, "args": str}]);
   },
   printText: function(fnSuccess, fnError, str){
      exec(fnSuccess, fnError, "BluetoothPrinter", "printText", [str]);
   },
   printPOSCommand: function(fnSuccess, fnError, str){
      exec(fnSuccess, fnError, "BluetoothPrinter", "printPOSCommand", [str]);
   }
};

module.exports = BTPrinter;
