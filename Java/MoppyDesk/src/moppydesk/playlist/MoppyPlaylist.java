/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk.playlist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author AJ (MrSolidSnake745)
 * Class that represents a playlist
 */
public class MoppyPlaylist extends AbstractTableModel {
    private List<MoppySong> playlist = new ArrayList<>();
    private int currentIndex = -1;
    private static final int COL_COUNT = 3;
    private static final int INDICATOR_INDEX = 0;
    private static final int TITLE_INDEX = 2;
    private static final int PLAYED_INDEX = 1;    
    
    public MoppyPlaylist() {
        
    }                
    public void addSong(File file) {playlist.add(new MoppySong(file)); fireTableDataChanged();}
    public void currentSongFinished() {if(currentIndex != -1) {playlist.get(currentIndex).setPlayed(true);} fireTableDataChanged();}
    public void currentSongReset() {if(currentIndex != -1) {playlist.get(currentIndex).setPlayed(false);} fireTableDataChanged();}
    
    public Boolean isFinished() {
        if(isEmpty()) return true;
        for (MoppySong s: playlist) {if(!(s.getPlayed())) return false;}
        return true;
    }
    
    public Boolean isEmpty() {return playlist.isEmpty();}
    public Boolean isFirstSong() {return currentIndex == 0;}
    public Boolean isLastSong() {return currentIndex == playlist.size() - 1;}
    public int getIndex() {return currentIndex;}
    
    public String getCurrentSongName() {return playlist.get(currentIndex).getName();}
    public Boolean getCurrentSongPlayed() {return playlist.get(currentIndex).getPlayed();}      
    public void randomize() {Collections.shuffle(playlist); reset();}
    
    public File getNextSong() {       
        if(!isLastSong()) {            
            currentSongFinished();            
            return playlist.get(++currentIndex).getFile();
        }
        return null;
    }
    
    public File getPreviousSong() {        
        if(!isFirstSong()) {            
            --currentIndex; currentSongReset();            
            return playlist.get(currentIndex).getFile();            
        }
        return null;
    }
    
    public void reset() {
        currentIndex = -1;
        for (MoppySong i: playlist) {i.setPlayed(false);}
        fireTableDataChanged();
    }
    
    public void clear() {
        currentIndex = -1;
        playlist.clear();
        fireTableDataChanged();
    }        
    
    public boolean savePlaylistFile(File input) {
        try {
            FileWriter fr = new FileWriter(input);
            BufferedWriter br  = new BufferedWriter(fr);
            for (MoppySong i: playlist) {
                br.write(i.getName() + ";" + i.getFile().getPath());
                br.newLine();
            }
            br.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(MoppyPlaylist.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }        
    }    
        
    public boolean loadPlaylistFile(File input) {
        try {
            FileReader fr = new FileReader(input);
            BufferedReader br  = new BufferedReader(fr);
            List<MoppySong> newpl = new ArrayList<>();
            String s = br.readLine();            
            while (s != null) {
                String[] line = s.split(";");                
                if(line.length == 2) { newpl.add(new MoppySong(line[0], line[1])); }                
                s = br.readLine();
            }
            br.close();
            playlist = newpl;
            fireTableDataChanged();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(MoppyPlaylist.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }                
    }

    
    // <editor-fold defaultstate="collapsed" desc="TableModel Implementation">      
    @Override
    public int getRowCount() { return playlist.size(); }
    @Override
    public int getColumnCount() { return COL_COUNT; }
    @Override
    public boolean isCellEditable(int row, int column) { return false; } //Setting all cells to read only

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MoppySong song = playlist.get(rowIndex);
        switch (columnIndex) {
            case INDICATOR_INDEX:
                if(rowIndex == currentIndex) return " ► ";
                return "";
            case TITLE_INDEX:
                return song.getName();
            case PLAYED_INDEX:
                return song.getPlayed();
            default:
                return new Object();
         }
    }       
    
    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case INDICATOR_INDEX:
                return String.class;
            case TITLE_INDEX:                          
                return String.class;
            case PLAYED_INDEX:
                return Boolean.class;
            default:
                return Object.class;
        }
    }
    
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case INDICATOR_INDEX:
                return "";
            case TITLE_INDEX:                          
                return "Name";
            case PLAYED_INDEX:
                return "Played";
            default:
                return "";
        }
    }
    // </editor-fold> 
}
