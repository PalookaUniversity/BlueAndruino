#include <stdlib.h>
#include <string.h>



int led = 13;

//int RXLED = 13;//17;  // The RX LED has a defined Arduino pin

int SLOW = 2000;
int NORMAL=1000;
int FAST = 100;

int delay_time_on = NORMAL;
int delay_time_off = NORMAL;

String commandString = "";         // a string to hold incoming data

void setup()
{
 pinMode(led, OUTPUT);  // Set RX LED as an output
 // TX LED is set as an output behind the scenes

 Serial.begin(9600); //This pipes to the serial monitor
 Serial1.begin(9600); //This is the UART, pipes to sensors attached to board
 commandString.reserve(200);
}
int linecount = 0;
int cycle = 0;

unsigned char cmdBuff[200];

int cmdPtr = 0;

boolean commandComplete = false;  // whether the string is complete

void loop() {
  
    Serial.print(cycle++);
    Serial.print(": tick delayTime=");
    Serial.print(delay_time_off);
    Serial.print(",");
    Serial.print(delay_time_on);
    Serial.print(":");
    Serial.println(cmdPtr);

  while (Serial1.available()) {
    procData();
  }
  
  digitalWrite(led, LOW);
  delay(delay_time_off);
  digitalWrite(led, HIGH);
  delay(delay_time_on);
}

void procData()
{ 
  if(Serial1.available()) {
    linecount++;
    char c = Serial1.read();
     
    Serial1.flush();
    Serial1.print("");
    Serial1.print(linecount);
    Serial1.print(" Received: ");
    Serial1.println(c);
        
    switch(c){
//      case '1': 
//        delay_time_on = delay_time_off = SLOW; 
//        break;
//      case '2': 
//        delay_time_on = delay_time_off = NORMAL; 
//        break;
//      case '3': 
//        delay_time_on = delay_time_off = FAST; 
//        break;
//      case '4': 
//        delay_time_on = FAST;
//        delay_time_off = SLOW; 
//        break;
        
      case '\n': // Single string command
      case '!': // Single string command
      case '}':  // Group command 
        exec(); 
        break;
        
      case '{': 
        resetCmd(); 
        break;
        
       default  : if (cmdPtr < 200){ 
         
         cmdBuff[cmdPtr++] = c; 
         //commandString += c;
       } 
    }
  }
}

void exec(){


   int val;
   char argstr[20];
   
    
    Serial1.flush();
    Serial1.println("");
    Serial1.print(linecount);
    Serial1.print(":");
    Serial1.print(cmdPtr);
    Serial1.print(":");
    Serial1.print("Exec:");
    int i = 0;
    while (cmdBuff[i]) {
      char c = cmdBuff[i];    
      Serial1.print(c);
      i++;
    }
    //commandString.getBytes(cmdBuff, cmdPtr);
    Serial1.print(commandString);
    Serial1.print(".");
    Serial1.println(cmdPtr);
    if (cmdPtr > 2){
      char key = cmdBuff[0];
      char delim = cmdBuff[1];
      strcpy(argstr, "42");
      val = atoi(argstr);
      Serial1.print(key);
      Serial1.print("=");
      Serial1.println(val); 
      switch(key){
        case '1': 
          delay_time_on = val;
          break;
        case '2': 
          delay_time_off = val;
        break;
      }  
    }

    
  resetCmd();
}

void resetCmd(){
    Serial.flush();
    Serial.println("");
    Serial.print(linecount);
    Serial.println("Reset:");
    cmdPtr=0;
    cmdBuff[cmdPtr]=0;
    commandString = "";
}

