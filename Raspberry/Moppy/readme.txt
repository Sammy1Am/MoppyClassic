You will need the http://abyz.co.uk/rpi/pigpio/index.html library.
You coud build it with g++ moppypi.c -o moppy -lpigpio -lrt -lpthread
Pls check the pinresolver array and config it.
Renice can be your friend :) (htop the threads, and renice the 2 moppy uid to -20)