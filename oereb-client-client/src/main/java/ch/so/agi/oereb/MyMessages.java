package ch.so.agi.oereb;

import com.google.gwt.i18n.client.Messages;

public interface MyMessages extends Messages {
    @DefaultMessage("Search: Real estates and addresses")
    String search_placeholder(); 

    @DefaultMessage("Real estates")
    String map_popup_title(); 
    
    @DefaultMessage("No.")
    String map_realestate_abbrevation(); 
    
    @DefaultMessage("Canton not supported.")
    String error_message_not_supported_canton_title();
    
    @DefaultMessage("The canton {0} is not supported due to reasons...")
    String error_message_not_supported_canton_detail(String canton);

    @DefaultMessage("Close extract")
    String result_header_button_tooltip_remove();
    
    @DefaultMessage("Expand window")
    String result_header_button_tooltip_maximize();

    @DefaultMessage("Minimize window")
    String result_header_button_tooltip_minimize();

    @DefaultMessage("Request extract as PDF")
    String result_button_request_pdf();

    @DefaultMessage("Real estate nr {0}")
    String result_header_real_estate(String number);
    
    
    @DefaultMessage("Fubar")
    String fubar();
    
    @DefaultMessage("Füü {0} bar {1}")
    String yinyang(String yin, String yang);
}
