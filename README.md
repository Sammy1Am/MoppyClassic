<pre>
    __  ___                           __                  __  ___                    ____
   /  |/  /___  ____  ____  __  __   / /_  ____ ______   /  |/  /___ _   _____  ____/ / /
  / /|_/ / __ \/ __ \/ __ \/ / / /  / __ \/ __ `/ ___/  / /|_/ / __ \ | / / _ \/ __  / / 
 / /  / / /_/ / /_/ / /_/ / /_/ /  / / / / /_/ (__  )  / /  / / /_/ / |/ /  __/ /_/ /_/  
/_/  /_/\____/ .___/ .___/\__, /  /_/ /_/\__,_/____/  /_/  /_/\____/|___/\___/\__,_(_)   
            /_/   /_/    /____/                                                          
</pre>

***

![#FFD700](https://placehold.it/15/FFD700/000000?text=+) **This project has been replaced with [Moppy 2.0](https://github.com/SammyIAm/Moppy2)** and will not see any updates in the future.  Moppy 2.0 implements nearly all the features that this version had, and adds many additional features, so there's no reason not to head on over there to check it out.  This repo will be preserved for historical purposes and just incase someone *needs* something that this older version offers.

***

FAQ
------------
#### Why Moppy 2.0?
The original Moppy was good at doing its thing, but not very suitable for modifications and features added by the community (at least without making the initial setup experience more complicated for everyone).  Moppy 2.0 has been redesigned with the goal of keeping the out-of-the-box experience and easy as possible while allowing for advanced users to build and contribute new features and support new devices (like scanners, stepper motors, and relays!).

#### Why a new repository?
Moppy 2.0 was built from the ground up, so any new "branch" would have started its life by having all its contents deleted and no future merge would ever be possible.

#### What if I still need *this* version of Moppy (for some reason)?
You can still checkout the last-available version of this repository, but there will be no future development or bug fixes.

## Original README
------------

This version attempts to improve upon the original Moppy by adding additional functionality including:

- MIDI-IN support
- Per-channel output control
- Support for multiple Arduinos/MIDI devices
- Drive pooling


This document is meant to be a sort of quick-start guide.  You can find an FAQ and troubleshooting guide on the [Wiki](https://github.com/SammyIAm/Moppy/wiki).

Installation
------------
The Arduino code requires the TimerOne library available here: http://www.arduino.cc/playground/Code/Timer1


The latest build is using NRJavaSerial, which should include suitable native drives for most systems.  If you've previously run an older version of Moppy, you'll need to make sure that the RXTX jar file(s) are **not** being loaded (i.e. not on the classpath, &c.), since the two libraries will conflict if both present.


Upload the included Arduino code to the Arduino of your choice (requires [Arduino IDE](http://arduino.cc/en/Main/Software)), and open up the included Java code in your favorite IDE.  This code includes a NetBeans project for your convenience, so you should be able to open the project directly in NetBeans.

Hardware
--------
I built Moppy using an Arduino Uno, though it should work just fine on most Arduinos.  The pins are connected in pairs to floppy drives as follows: Even pins (2,4,6...) are connected to each drive's STEP pin, the matching odd pins (3,5,7...) are connected to the each drive's DIRECTION control pin.  So the first floppy would be connected to pin 2 & 3, the second floppy to 4 & 5, and so on.


Some pinout information can be found here: http://pinouts.ru/Storage/InternalDisk_pinout.shtml


Make sure you ground the correct drive-select pin, or the drive won't respond to any input (just connect the drive-select pin on the floppy to the pin directly below it).  You can tell when you have the right drive selected, because the light on the front of the drive will come on.  


Also, it's VERY IMPORTANT that your Arduino is grounded with the drives, or the drives will not register the pulses correctly.  To do this, make sure that the GND pin on the Arduino is connected to the odd-numbered pin below the STEP pin on the floppy (i.e. if the STEP pin is 20, connect the Audnio's GND pin to Floppy-pin 19).  You might need to do this for the DIRECTION pin as well (I did it for both, but I don't know if it's required).

Configuration and use
---------------------
- Open up the code in [NetBeans](http://netbeans.org) (or your favorite IDE) and run it.  Alternatively, you can build the MoppyDesk.jar file and run that directly.
- On the right half of the screen, check the channel output boxes for the number of drives you have connected, select "Moppy", and choose the COM port that you have your Arduino connected to.
- Click the "Load Sequence" button and select a suitable MIDI file (see below).
- Click the "Connect" button to connect the program to the devices specified in the output area.
- Click "Start" to start playback (if all goes well).  
- The Stop/Reset button will stop playback and reset the drives.

MIDI file information and guidelines
------------------------------------
- MIDI files should have one MIDI channel for each controller pin on the Arduino.  Channel 1 will be sent to pin2, channel 2 to pin4, &c.
- Each drive can only play a single note at a time.
- The software will only attempt to play notes between C1 and B4.  Floppy drives don't seem to respond well to notes outside of this range (especially higher).
- Generally shorter notes tend to sound better, as longer notes are marred by the read-heads changing directions repeatedly.

Cross your fingers, and enjoy!

Help / contributions
--------------------
https://github.com/SammyIAm/Moppy
