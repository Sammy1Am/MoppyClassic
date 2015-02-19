package moppysim.comm;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Pan;
import gnu.io.NRSerialPort;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import moppysim.components.SimDrive;

/**
 *
 * @author Sammy1Am
 */
public class SimController implements Runnable{
    
    NRSerialPort serial;
    static int SERIAL_RATE = 9600;
    private boolean running = false;
    private SimDrive[] simDrives;
    static int ARDUINO_RESOLUTION = 40; // Used to convert incoming data into actual microseconds
    private int numberOfDrives;
    
    Synthesizer synth = JSyn.createSynthesizer();
    LineOut lout = new LineOut();
    
    public SimController(int numOfDrives){
        numberOfDrives = numOfDrives;
        simDrives = new SimDrive[numberOfDrives];
        
        // Set up the synthesizer
        synth.add(lout);
        Pan pan = new Pan();
        pan.pan.set(0.0);
        pan.output.connect(0,lout.input,0);
        pan.output.connect(1,lout.input,1);
        
        for (int d=0;d<numberOfDrives;d++){
            SimDrive sd = new SimDrive();
            simDrives[d] = sd;
            synth.add(sd.so);
            sd.so.output.connect(pan.input);
        }
    }
    
    public SimDrive[] getDrives(){
        return simDrives;
    }
    
    public static String[] getComPorts(){
        Set<String> ports = NRSerialPort.getAvailableSerialPorts();
        return ports.toArray(new String[ports.size()]);
    }
    
    public void connect(String comPort){
        serial = new NRSerialPort(comPort, SERIAL_RATE);
        serial.connect();
    }
    
    public void disconnect(){
        stop();
        resetAll();
        if (serial != null){
            serial.disconnect();
        }
    }
    
    public void stop(){
        this.running = false;
        synth.stop();
    }

    @Override
    public void run() {
        running = true;
        synth.start();
        lout.start();
        if (serial.isConnected()){
            
            DataInputStream in = new DataInputStream(serial.getInputStream());
            
            byte messageCommand;
            int messageData;
            
            while (running && !Thread.currentThread().isInterrupted()){
                try {
                    if (in.available() > 2){
                        messageCommand = in.readByte();
                        messageData = in.readUnsignedShort();
                        
                        if (messageCommand == 100){
                            resetAll();
                        } else {
                            setNote(messageCommand,messageData);
                        }
                        
                    } else {
                        Thread.sleep(5);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SimController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SimController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private void resetAll(){
        for (SimDrive s : simDrives){
            s.setNote(0);
            s.resetDrive();
        }
    }
    
    private void setNote(int driveNumber, int periodData){
        if ((driveNumber/2)-1 < numberOfDrives){
            simDrives[(driveNumber/2)-1].setNote(periodData * (ARDUINO_RESOLUTION*2));
        }
        
        //System.out.println("Set drive "+((driveNumber/2)-1)+" to "+periodData);
    }
}
