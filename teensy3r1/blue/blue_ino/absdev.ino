/*
  Serial Event example
 
 When new serial data arrives, this sketch adds it to a String.
 When a newline is received, the loop prints the string and
 clears it.
 

 
 */
char cmdBuffer[200];

int cmdPtr = 0;
String commandString = "";         // a string to hold incoming data
boolean commandComplete = false;  // whether the string is complete




void setup() {

  Serial.begin(9600);

  commandString.reserve(200)
  cmdBuffer[0]=0;
}

void loop() {

  if(Serial1.available()) {
    procData();
  }
}


void procData() {

  while (Serial.available()) {

    char c = (char)Serial.read();
    cmdBuff[cmdPtr++] = c;
    cmdBuff[cmdPtr]=0;

    commandString += c;

    switch(c){
      case '\n': // Single string command
      case '}':  // Group command 
        procCmd(); 
        break;
      case '{': 
        resetCmd(); 
        break;
      default  : cmdBuff[cmdPtr++] = c; 
    }
  }
}

void procCmd(){

  cmdBuff[cmdPtr]=0 
  //String commandString = String str(cmdBuffer); // Check this!
  Serial.println(commandString);
  resetCmd()
}

void resetCmd(){
  cmdPtr=0;
  cmdBuff[cmdPtr]=0;
}
