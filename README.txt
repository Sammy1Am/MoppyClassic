   __  ___                   
  /  |/  /__  ___  ___  __ __
 / /|_/ / _ \/ _ \/ _ \/ // /
/_/  /_/\___/ .__/ .__/\_, / 
           /_/  /_/   /___/  

by Sammy1Am

Moppy is a M_usical Fl_oppy controller program built for the Ardiuno UNO.

--INSTALLATION--
The Arduino code requires the TimeOne library available here: http://www.arduino.cc/playground/Code/Timer1

You will need to follow the directions in the appropriate Java/SerialDrivers folder for your system to install the serial drivers prior to running Moppy.

Upload the included Arduino code to the Arduino of your choice, and open up the included Java code in your favorite IDE.  This code includes a NetBeans project for your convenience, so you should be able to open the project directly in NetBeans.

--HARDWARE--

I built Moppy using an Arduino UNO, though it should work just fine on most Arduinos.  The pins are connected in pairs to floppy drives as follows: Even pins (2,4,6...) are connected to the drive's STEP pin, the matching odd pins (3,5,7...) are connected to the drive's DIRECTION control pin.

Some pinout information can be found here: http://pinouts.ru/Storage/InternalDisk_pinout.shtml

Make sure you short the correct drive-select pin, or the drive won't respond to any input.  Also, it's VERY IMPORTANT that your Arduino is grounded with the drives, or the drives will not register the pulses correctly.


--CONFIGURAITON / USE--

Right now, there is no configuration for MoppyDesk (the Moppy Desktop application for controlling) and any variables need to be changed in the code itself.  This will be improved in the future.

Edit the code to select the appropriate COM port, and point to the correct MIDI file.  Don't forget to edit the tempo in the code as well.

MIDI files should have one MIDI track for each controller pin on the Arduino.  Track 1 will be sent to pin2, track 2 to pin4, &c.

Run the java code, cross your fingers, and enjoy!

--HELP/CONTRIBUTIONS--

https://github.com/SammyIAm/Moppy