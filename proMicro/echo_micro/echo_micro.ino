/* 
   Echo over Serial and Serial1
   license: Public Domain - please use this code however you'd like.
   It's provided as a learning tool.
*/
#include <SoftwareSerial.h>
void setup()
{
  Serial1.begin(9600);
}


void loop()
{
  if(Serial1.available()) {
    Serial1.write(Serial1.read());
  }
}
