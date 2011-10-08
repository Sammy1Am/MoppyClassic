boolean firstRun = true; // Used for one-run-only stuffs;

//First pin being used for floppies, and the last pin.  Used for looping over all pins.
const byte FIRST_PIN = 2;
const byte PIN_MAX = 9;


/*NOTE: Many of the arrays below contain unused indexes.  This is 
to prevent the Arduino from having to convert a pin input to an alternate
array index and save as many cycles as possible.  In other words information 
for pin 2 will be stored in index 2, and information for pin 4 will be 
stored in index 4.*/


/*An array of maximum track positions for each step-control pin.  Even pins
are used for control, so only even numbers need a value here.  3.5" Floppies have
80 tracks, 5.25" have 50 (use 79 and 49).
*/
byte MAX_POSITION[] = {
  0,0,79,0,79,0,79,0,79,0};
  
//Array to track the current position of each floppy head.  (Only even indexes (i.e. 2,4,6...) are used)
byte currentPosition[] = {
  0,0,0,0,0,0,0,0,0,0};

//Array to keep track of the direction for each floppy head.  (Only even indexes are used)
boolean stepDirection[] = {
  false,false,false,false,false,false,false,false,false,false}; // false = forward, true=reverse (i.e. true=HIGH)
  
//Current period assigned to each pin.  0 = off.
unsigned int currentPeriod[] = {
  0,0,0,0,0,0,0,0,0,0 
};

//Setup pins (Even-odd pairs for step control and direction
void setup(){
  pinMode(13, OUTPUT);// Pin 13 has an LED connected on most Arduino boards
  pinMode(2, OUTPUT); // Step control 1
  pinMode(3, OUTPUT); // Direction 1
  pinMode(4, OUTPUT); // Step control 2
  pinMode(5, OUTPUT); // Direction 2
  pinMode(6, OUTPUT); // Step control 3
  pinMode(7, OUTPUT); // Direction 3
  pinMode(8, OUTPUT); // Step control 4
  pinMode(9, OUTPUT); // Direction 5

  Serial.begin(9600);
}


void loop(){
  
  //The first loop, reset all the drives, and wait 2 seconds...
  if (firstRun)
  {
    firstRun = false;
    resetAll();
    delay(2000);
  }

  //Only read if we have 
  if (Serial.available() > 2){
    //Watch for special 100-message to reset the drives
    if (Serial.peek() == 100) {
      resetAll();
      Serial.flush();
    } 
    else{
      currentPeriod[Serial.read()] = (Serial.read() << 8) | Serial.read();
    }
  }

  voice();
}

/*
The voice() method controls WHEN each pin will be stepped by mod'ing the Arduino system-clock
against the currentPeriod for each pin.  A period of 0 will skip the pin (i.e. pin is off)
*/
void voice()
{
  if (currentPeriod[2] > 0 && micros()%currentPeriod[2] < 100){
    stepPin(2,3,4); 
  }
  if (currentPeriod[4] > 0 && micros()%currentPeriod[4] < 100){
    stepPin(4,5,4); 
  }
  if (currentPeriod[6] > 0 && micros()%currentPeriod[6] < 100){
    stepPin(6,7,4); 
  }
  if (currentPeriod[8] > 0 && micros()%currentPeriod[8] < 100){
    stepPin(8,9,4); 
  }
}

/*
The stepPin() method controls the actual stepping of each pin.  It uses the
stepDireciton array to determine the state of the direction_pin, and will automatically
reverse the direction when needed.
*/
void stepPin(byte pin, byte direction_pin, byte wait) {
  
  //Switch directions if end has been reached
  if (currentPosition[pin] >= MAX_POSITION[pin]) {
    stepDirection[pin] = true;
  } 
  else if (currentPosition[pin] <= 0) {
    stepDirection[pin] = false;
  }
  
  //Set direction_pin state, and update currentPosition
  if (stepDirection[pin]){
    digitalWrite(direction_pin,HIGH);
    currentPosition[pin]--;
  } 
  else {
    digitalWrite(direction_pin,LOW);
    currentPosition[pin]++;
  }
  
  //Pulse the control pin
  digitalWrite(pin,HIGH);
  delayMicroseconds(wait);
  digitalWrite(pin,LOW);
}

//Not used now, but good for debugging...
void blinkLED(){
  digitalWrite(13, HIGH); // set the LED on
  delay(250);              // wait for a second
  digitalWrite(13, LOW); 
}

//For a given controller pin, runs the read-head all the way back to 0
void reset(byte pin)
{
  digitalWrite(pin+1,HIGH); // Go in reverse
  for (byte s=0;s<MAX_POSITION[pin];s++){
    digitalWrite(pin,HIGH);
    digitalWrite(pin,LOW);
    delay(5);
  }
  currentPosition[pin] = 0; // We're reset.
  stepDirection[pin] = false; // Ready to go forward.
}

//Resets all the pins
void resetAll(){
  for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
    reset(p);
  } 
}



