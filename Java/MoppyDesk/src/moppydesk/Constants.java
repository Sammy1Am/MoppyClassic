package moppydesk;

/**
 *
 * @author Sammy1Am
 */
public class Constants {
    //Sequencer preferences    
    public static final String PREF_LOADED_SEQ = "loadedSequencePath";                
    public static final String PREF_DELAY_RESET = "delayResetValue";
    public static final String PREF_RESET_DRIVES = "resetDrivesValue";
    public static final String PREF_REPEAT_SEQ = "repeatSequenceValue";
    public static final String PREF_OUTPUT_SETTINGS = "outputSettingsArray";
        
    //Playlist preferences
    public static final String PREF_LOADED_LIST = "loadedPlaylistPath";
    public static final String PREF_LOADED_MPL = "lastLoadedPlaylistFile";
    public static final String PREF_LOAD_MPL_ON_START = "loadMPLValue";
    
    //Pooling preferences
    public static final String PREF_POOL_ENABLE = "poolingEnabled";
    public static final String PREF_POOL_FROM_START = "poolingFromStart";
    public static final String PREF_POOL_FROM_END = "poolingFromEnd";
    public static final String PREF_POOL_TO_START = "poolingToStart";
    public static final String PREF_POOL_TO_END = "poolingToEnd";
    public static final String PREF_POOL_STRATEGY = "poolingStrategy";

    //Filter preferences
    public static final String PREF_FILTER_CONSTRAIN = "filterAutoConstrain";
    public static final String PREF_FILTER_IGNORETEN = "ignore10";

    public static final int NUM_MIDI_CHANNELS = 16;
}
