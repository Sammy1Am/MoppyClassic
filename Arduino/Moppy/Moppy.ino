#include <TimerOne.h>

boolean firstRun = true; // Used for one-run-only stuffs;

/*First pin being used for floppies, and the last pin.  Used for looping over all pins.
 Depending on your Arduino microcontroller you can adjust FIRST_PIN and PIN_MAX value.
 If you want to use more or less than 8 midi channels or if you want to start with a
 different pin than pin 2.  If you want to use different values than 2 and 17 you must
 adjust your Java code too and implement a offset functionality.
 */
const byte FIRST_PIN = 2; //Possible values: 2,4,6,...
const byte PIN_MAX = 17;  //Possible values: 3,5,7,... (MUST be higher than FIRST_PIN)
#define RESOLUTION 40 //Microsecond resolution for notes


/*NOTE: Many of the arrays below contain unused indexes.  This is 
 to prevent the Arduino from having to convert a pin input to an alternate
 array index and save as many cycles as possible.  In other words information 
 for pin 2 will be stored in index 2, and information for pin 4 will be 
 stored in index 4.*/

/*An array of maximum track positions for each step-control pin.  Even pins
 are used for control, so only even numbers need a value here.  3.5" Floppies have
 80 tracks, 5.25" have 50.  These should be doubled, because each tick is now
 half a position (use 158 and 98).  If you want to use just 3.5" Floppies, set
 FDD_TYPE to 158 and if you want to use just 5.25" Floppies, set it to 98.  In the
 special case of a mixed environment with 3.5" and 5.25" Floppies, some additional
 adjustments are needed.  Then you need to remove 'byte MAX_POSITION[PIN_MAX + 1];',
 'const byte FDD_TYPE = 158;' and the whole MAX_POSITION if-else block in the setup()
 method.  Plus you need to add 'byte MAX_POSITION[] = {0,0,158,0,...,98,0};' right
 here with MAX_PIN + 1 entries where all numbers smaller than FIRST_PIN must be 0,
 all even numbers must be filled with 158 (3.5" Floppy) or 98 (5.25" Floppy).
 */
byte MAX_POSITION[PIN_MAX + 1];
const byte FDD_TYPE = 158; //Possible values: 158 (3.5" Floppies) and 98 (5.25" Floppies)

//Array to track the current position of each floppy head.  (Only even indexes (i.e. 2,4,6...) are used)
byte currentPosition[PIN_MAX + 1];

/*Array to keep track of state of each pin.  Even indexes track the control-pins for toggle purposes.  Odd indexes
 track direction-pins.  LOW = forward, HIGH=reverse
 */
int currentState[PIN_MAX + 1];

//Current period assigned to each pin.  0 = off.  Each period is of the length specified by the RESOLUTION
//variable above.  i.e. A period of 10 is (RESOLUTION x 10) microseconds long.
unsigned int currentPeriod[PIN_MAX + 1];

//Current tick
unsigned int currentTick[PIN_MAX + 1];


void setup(){

  // Initialize all arrays dynamically based on FIRST_PIN and PIN_MAX values.
  for (int i = 0; i <= PIN_MAX; i++){

    // Initialize every pin with 0
    currentPosition[i] = 0;
    currentPeriod[i] = 0;
    currentTick[i] = 0;

    // Initialize every pin used for floppies with LOW and all others with 0.
    if(i < FIRST_PIN){
      currentState[i] = 0;
    }
    else {
      currentState[i] = LOW;
    }

    // Initialize every step control pin with a value and all others with 0.
    if((i >= FIRST_PIN) && (i % 2 == 0)){
      MAX_POSITION[i] = FDD_TYPE;
    }
    else {
      MAX_POSITION[i] = 0;
    }

  }
  
  // Setup pins (Even-odd pairs for step control and direction)
  for (int i = FIRST_PIN; i <= PIN_MAX; i++){
    pinMode(i, OUTPUT);
  }

  Timer1.initialize(RESOLUTION); // Set up a timer at the defined resolution
  Timer1.attachInterrupt(tick); // Attach the tick function

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
      //Flush any remaining messages.
      while(Serial.available() > 0){
        Serial.read();
      }
    } 
    else{
      currentPeriod[Serial.read()] = (Serial.read() << 8) | Serial.read();
    }
  }
}


/*
Called by the timer interrupt at the specified resolution.
 */
void tick()
{
  /* 
   If there is a period set for control pin 2 (4,6,8...), count the number of
   ticks that pass, and toggle the pin if the current period is reached.
   */
  for(int i = FIRST_PIN; i < PIN_MAX; i+=2){
    if (currentPeriod[i]>0){
      currentTick[i]++;
      if (currentTick[i] >= currentPeriod[i]){
        togglePin(i,i+1);
        currentTick[i]=0;
      }
    }
  }

}

void togglePin(byte pin, byte direction_pin) {

  //Switch directions if end has been reached
  if (currentPosition[pin] >= MAX_POSITION[pin]) {
    currentState[direction_pin] = HIGH;
    digitalWrite(direction_pin,HIGH);
  } 
  else if (currentPosition[pin] <= 0) {
    currentState[direction_pin] = LOW;
    digitalWrite(direction_pin,LOW);
  }

  //Update currentPosition
  if (currentState[direction_pin] == HIGH){
    currentPosition[pin]--;
  } 
  else {
    currentPosition[pin]++;
  }

  //Pulse the control pin
  digitalWrite(pin,currentState[pin]);
  currentState[pin] = ~currentState[pin];
}


//
//// UTILITY FUNCTIONS
//

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
  for (byte s=0;s<MAX_POSITION[pin];s+=2){ //Half max because we're stepping directly (no toggle)
    digitalWrite(pin,HIGH);
    digitalWrite(pin,LOW);
    delay(5);
  }
  currentPosition[pin] = 0; // We're reset.
  digitalWrite(pin+1,LOW);
  currentPosition[pin+1] = 0; // Ready to go forward.
}

//Resets all the pins
void resetAll(){

  // Old one-at-a-time reset
  //for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
  //  reset(p);
  //}

  //Stop all notes (don't want to be playing during/after reset)
  for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
    currentPeriod[p] = 0; // Stop playing notes
  }

  // New all-at-once reset
  for (byte s=0;s<80;s++){ // For max drive's position
    for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
      digitalWrite(p+1,HIGH); // Go in reverse
      digitalWrite(p,HIGH);
      digitalWrite(p,LOW);
    }
    delay(5);
  }

  for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
    currentPosition[p] = 0; // We're reset.
    digitalWrite(p+1,LOW);
    currentState[p+1] = 0; // Ready to go forward.
  }

}
