/* Pro Micro Test Code
   by: Nathan Seidle
   modified by: Jim Lindblom
   SparkFun Electronics
   date: September 16, 2013
   license: Public Domain - please use this code however you'd like.
   It's provided as a learning tool.

   This code is provided to show how to control the SparkFun
   ProMicro's TX and RX LEDs within a sketch. It also serves
   to explain the difference between Serial.print() and
   Serial1.print().
*/

int led = 13;

int RXLED = 13;//17;  // The RX LED has a defined Arduino pin

int SLOW = 2000;
int NORMAL=1000;
int FAST = 100;

int delay_time = NORMAL;
// The TX LED was not so lucky, we'll need to use pre-defined
// macros (TXLED1, TXLED0) to control that.
// (We could use the same macros for the RX LED too -- RXLED1,
//  and RXLED0.)

void setup()
{
 pinMode(RXLED, OUTPUT);  // Set RX LED as an output
 // TX LED is set as an output behind the scenes

 Serial.begin(9600); //This pipes to the serial monitor
 Serial1.begin(9600); //This is the UART, pipes to sensors attached to board
}
int linecount = 0;
int cycle = 0;
char btIn = '4';
void loop()
{
  
    Serial.print(cycle++);
    Serial.print(": tick delayTime=");
    Serial.println(delay_time);
  
  if(Serial1.available()) {
    linecount++;
    char btIn = Serial1.read();
    Serial1.flush();
    Serial1.println("");
    Serial1.print(linecount);
    Serial1.print(" Received: ");
    Serial1.println(btIn);
    
    switch(btIn){
      case '1': 
        delay_time = SLOW; 
        break;
      case '2': 
        delay_time = NORMAL; 
        break;
      case '3': 
        delay_time = FAST; 
        break;
      case '4': 
        delay_time = 30; 
        break;
    }
  }
  
  digitalWrite(RXLED, LOW);   // set the LED on
  //TXLED0; //TX LED is not tied to a normally controlled pin
  delay(delay_time);              // wait for a second
  digitalWrite(RXLED, HIGH);    // set the LED off
 //TXLED1;
  delay(delay_time);              // wait for a second
}
