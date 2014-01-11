#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <pthread.h>
#include <time.h>
#include <pigpio.h>
#include <string.h>

#define RESOLUTION 40 //Microsecond resolution for notes
#define LOW 0
#define HIGH 1
#define PORT 32000

typedef unsigned char byte;

//we can use 16 pins
const byte FIRST_PIN = 2;
const byte PIN_MAX = 17;

//but we need to remap them
byte pinResolver[] = {
  0,0,17,18,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
//max motor positions get from arduino code
byte MAX_POSITION[] = {
  0,0,158,0,158,0,158,0,158,0,158,0,158,0,158,0,158,0};

//Array to track the current position of each floppy head. (Only even indexes (i.e. 2,4,6...) are used)
byte currentPosition[] = {
  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};



/*Array to keep track of state of each pin. Even indexes track the control-pins for toggle purposes. Odd indexes
track direction-pins. LOW = forward, HIGH=reverse
*/
int currentState[] = {
  0,0,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW,LOW
};

//Current period assigned to each pin. 0 = off. Each period is of the length specified by the RESOLUTION
//variable above. i.e. A period of 10 is (RESOLUTION x 10) microseconds long.
unsigned int currentPeriod[] = {
  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
};

//Current tick
unsigned int currentTick[] = {
  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
};

//For a given controller pin, runs the read-head all the way back to 0
void reset(byte pin)
{
	struct timespec tim, tim2;
	tim.tv_sec = 0;
	tim.tv_nsec = 400000L; 

	gpioWrite(pinResolver[pin+1],HIGH); // Go in reverse
	for (byte s=0;s<MAX_POSITION[pin];s+=2){ //Half max because we're stepping directly (no toggle)
		gpioWrite(pinResolver[pin],HIGH);
		gpioWrite(pinResolver[pin],LOW);
		nanosleep(&tim , &tim2);
	}
	currentPosition[pin] = 0; // We're reset.
	gpioWrite(pinResolver[pin+1],LOW);
	currentPosition[pin+1] = 0; // Ready to go forward.*/
}

//Resets all the pins
void resetAll(){
	struct timespec tim, tim2;
	tim.tv_sec = 0;
	tim.tv_nsec = 400000L; 

	//Stop all notes (don't want to be playing during/after reset)
	for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
		currentPeriod[p] = 0; // Stop playing notes
	}

	// New all-at-once reset
	for (byte s=0;s<80;s++){ // For max drive's position
		for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
			gpioWrite(pinResolver[p+1],HIGH); // Go in reverse
			gpioWrite(pinResolver[p],HIGH);
			gpioWrite(pinResolver[p],LOW);
		}
		nanosleep(&tim , &tim2);
	}

	for (byte p=FIRST_PIN;p<=PIN_MAX;p+=2){
		currentPosition[p] = 0; // We're reset.
		gpioWrite(pinResolver[p+1],LOW);
		currentState[p+1] = 0; // Ready to go forward.
	}

}

void togglePin(byte pin, byte direction_pin) {

  //Switch directions if end has been reached
  if (currentPosition[pin] >= MAX_POSITION[pin]) {
    currentState[direction_pin] = HIGH;
    gpioWrite(pinResolver[direction_pin],HIGH);
  }
  else if (currentPosition[pin] <= 0) {
    currentState[direction_pin] = LOW;
    gpioWrite(pinResolver[direction_pin],LOW);
  }

  //Update currentPosition
  if (currentState[direction_pin] == HIGH){
    currentPosition[pin]--;
  }
  else {
    currentPosition[pin]++;
  }

  //Pulse the control pin
  gpioWrite(pinResolver[pin],currentState[pin]);
  currentState[pin] = currentState[pin]==HIGH?LOW:HIGH;
}

void tick()
{
  /*
If there is a period set for control pin 2, count the number of
ticks that pass, and toggle the pin if the current period is reached.
*/
  if (currentPeriod[2]>0){
    currentTick[2]++;
    if (currentTick[2] >= currentPeriod[2]){
      togglePin(2,3);
      currentTick[2]=0;
    }
  }
  if (currentPeriod[4]>0){
    currentTick[4]++;
    if (currentTick[4] >= currentPeriod[4]){
      togglePin(4,5);
      currentTick[4]=0;
    }
  }
  if (currentPeriod[6]>0){
    currentTick[6]++;
    if (currentTick[6] >= currentPeriod[6]){
      togglePin(6,7);
      currentTick[6]=0;
    }
  }
  if (currentPeriod[8]>0){
    currentTick[8]++;
    if (currentTick[8] >= currentPeriod[8]){
      togglePin(8,9);
      currentTick[8]=0;
    }
  }
  if (currentPeriod[10]>0){
    currentTick[10]++;
    if (currentTick[10] >= currentPeriod[10]){
      togglePin(10,11);
      currentTick[10]=0;
    }
  }
  if (currentPeriod[12]>0){
    currentTick[12]++;
    if (currentTick[12] >= currentPeriod[12]){
      togglePin(12,13);
      currentTick[12]=0;
    }
  }
  if (currentPeriod[14]>0){
    currentTick[14]++;
    if (currentTick[14] >= currentPeriod[14]){
      togglePin(14,15);
      currentTick[14]=0;
    }
  }
  if (currentPeriod[16]>0){
    currentTick[16]++;
    if (currentTick[16] >= currentPeriod[16]){
      togglePin(16,17);
      currentTick[16]=0;
    }
  }

}

//ticking loop
void * tick_loop(void * param){
	struct timespec tim, tim2;
	tim.tv_sec = 0;
	tim.tv_nsec = 40000L; 
	for(;;){
		nanosleep(&tim , &tim2);
		tick();
	}
}
int main(int argc, char**argv)
{
	int sockfd,n;
	struct sockaddr_in servaddr,cliaddr;
	socklen_t len;
	char mesg[1000];
   
	if (gpioInitialise() < 0) return 1;
	
	sockfd=socket(AF_INET,SOCK_DGRAM,0);

	bzero(&servaddr,sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr=htonl(INADDR_ANY);
	servaddr.sin_port=htons(PORT);
	bind(sockfd,(struct sockaddr *)&servaddr,sizeof(servaddr));

	resetAll();
   
	pthread_t t;
	pthread_create(&t, NULL, tick_loop, NULL);
   
   //message processor loop
	for (;;)
	{
		len = sizeof(cliaddr);
		n = recvfrom(sockfd,mesg,1000,0,(struct sockaddr *)&cliaddr,&len);
		mesg[n] = 0;
		if(mesg[0]==2)
			printf("%d %d\n",mesg[0],(mesg[1] << 8) | mesg[2]);
		if(n>2){
			if(mesg[0]<=PIN_MAX+1 && mesg[0]>=PIN_MAX)
				currentPeriod[mesg[0]] = (mesg[1] << 8) | mesg[2];
			if(mesg[0]==100){
				resetAll();
			{
		}
	}
}