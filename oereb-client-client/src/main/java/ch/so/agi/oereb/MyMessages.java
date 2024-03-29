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
    
    @DefaultMessage("Municipality")
    String result_municipality();
    
    @DefaultMessage("Land register area")
    String result_land_register_area();
    
    @DefaultMessage("Land register type")
    String result_land_register_type();
    
    @DefaultMessage("Land register")
    String result_subunit_of_land_register_designation();

    @DefaultMessage("Real estate nr {0}")
    String result_header_real_estate(String number);
    
    @DefaultMessage("Concerned themes")
    String result_theme_concerned_themes();
    
    @DefaultMessage("Not concerned themes")
    String result_theme_not_concerned_themes();

    @DefaultMessage("Themes without data")
    String result_theme_theme_without_data();

    @DefaultMessage("Opacity")
    String result_wms_opacity();

    @DefaultMessage("Type")
    String result_type_name();

    @DefaultMessage("Share")
    String result_share_name();

    @DefaultMessage("Share %")
    String result_share_in_percent_name();
    
    @DefaultMessage("Point(s)")
    String result_nr_of_points();

    @DefaultMessage("Legal provisions)")
    String result_documents_legal_provisions();

    @DefaultMessage("Laws)")
    String result_documents_laws();

    @DefaultMessage("Hints)")
    String result_documents_hints();
    
    @DefaultMessage("Responsible offices)")
    String result_responsible_offices();
    
    @DefaultMessage("Fubar")
    String fubar();
    
    @DefaultMessage("Füü {0} bar {1}")
    String yinyang(String yin, String yang);
}
