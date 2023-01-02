package ch.so.agi.oereb;

import com.google.gwt.i18n.client.Messages;

public interface MyMessages extends Messages {
    @DefaultMessage("Search: Real estates and addresses")
    String search_placeholder(); 
    
    @DefaultMessage("Fubar")
    String fubar();
    
    @DefaultMessage("Füü {0} bar {1}")
    String yinyang(String yin, String yang);
}
