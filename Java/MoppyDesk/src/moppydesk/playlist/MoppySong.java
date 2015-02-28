package moppydesk.playlist;

import java.io.File;

/**
 *
 * @author AJ (MrSolidSnake745)
 * Stupid simple class to represent a song...
 */
public class MoppySong {
    private String title;    
    private Boolean played = false;
    private String filePath = null;   
    
    public MoppySong(String titleIn, String pathIn) { title = titleIn; filePath = pathIn; }
    
    public MoppySong(File fileIn) {
        title = fileIn.getName().substring(0, fileIn.getName().lastIndexOf("."));        
        filePath = fileIn.getAbsolutePath();
    }
    
    public String getName() {return title;}
    public Boolean getPlayed() {return played;}
    public String getFilePath() {return filePath;}
    
    public void setName(String titleIn) {title = titleIn;}
    public void setPlayed(Boolean playedIn) {played = playedIn;}
    public void setFilePath(String filePathIn) {filePath = filePathIn;}
    
    public File getFile() {
        if(filePath != null) return new File(filePath);
        return null;
    }    
}