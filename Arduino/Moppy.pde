boolean firstRun = true; // Used for one-run-only stuffs;

const byte FIRST_PIN = 2;
const byte PIN_MAX = 9;

byte MAX_POSITION[] = {
  0,0,79,0,79,0,79,0,49,0};
byte currentPosition[] = {
  0,0,0,0,0,0,0,0,0,0};
boolean stepDirection[] = {
  false,false,false,false,false,false,false,false,false,false}; // false = forward, true=reverse (i.e. true=HIGH)
unsigned int currentPeriod[] = {
  0,0,0,0,0,0,0,0,0,0 
};

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
  if (firstRun)
  {
    firstRun = false;
    resetAll();
    delay(2000);
    //runOnce();
  }

  if (Serial.available() > 2){
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

void stepPin(byte pin, byte control_pin, byte wait) {
  if (currentPosition[pin] >= MAX_POSITION[pin]) {
    stepDirection[pin] = true;
  }
  if (currentPosition[pin] <= 0) {
    stepDirection[pin] = false;
  }
  if (stepDirection[pin]){
    digitalWrite(control_pin,HIGH);
    currentPosition[pin]--;
  } 
  else {
    digitalWrite(control_pin,LOW);
    currentPosition[pin]++;
  }
  digitalWrite(pin,HIGH);
  delayMicroseconds(wait);
  digitalWrite(pin,LOW);
}

void blinkLED(){
  digitalWrite(13, HIGH); // set the LED on
  delay(250);              // wait for a second
  digitalWrite(13, LOW); 
}

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

void resetAll(){
  for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
    reset(p);
  } 
}



