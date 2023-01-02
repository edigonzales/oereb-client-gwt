package ch.so.agi.oereb;

import static org.jboss.elemento.Elements.*;
import static elemental2.dom.DomGlobal.console;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.AbstractSuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;
import org.jboss.elemento.IsElement;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsString;
import elemental2.core.JsNumber;
import elemental2.dom.AbortController;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.Location;
import elemental2.dom.RequestInit;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class SearchBox implements IsElement<HTMLElement> {

    private final HTMLElement root;

    private Location location;
    private String pathname;
    
    private AbortController abortController = null;

    @SuppressWarnings("unchecked")
    public SearchBox(UrlComponents urlComponents, MyMessages messages, String searchServiceUrl) {
        root = div().element();

        location = urlComponents.getLocation();
        pathname = urlComponents.getPathname();

        HTMLElement searchCard = div().id("search-card").element();
//        body().add(searchDiv);

        HTMLElement logoDiv = div().id("logo-div").element();
        HTMLElement logoOerebSpan = span().id("logo-oereb-span").element();
        HTMLElement logoOereb = div()
                .add(img().attr("src", location.protocol + "//" + location.host + location.pathname + "logo_oereb.png")
                        .attr("alt", "Logo ÖREB-Kataster").attr("width", "70%"))
                .element();
        logoOerebSpan.appendChild(logoOereb);
        logoDiv.appendChild(logoOerebSpan);
        searchCard.appendChild(logoDiv);

        SuggestBoxStore dynamicStore = new SuggestBoxStore() {
            @Override
            public void filter(String value, SuggestionsHandler suggestionsHandler) {
                if (value.trim().length() == 0) {
                    return;
                }
                
                if (abortController != null) {
                    abortController.abort();
                }

                abortController = new AbortController();
                final RequestInit requestInit = RequestInit.create();
                requestInit.setSignal(abortController.signal);

//                RequestInit requestInit = RequestInit.create();
//                Headers headers = new Headers();
//                //headers.append("Content-Type", "application/x-www-form-urlencoded");
//                requestInit.setHeaders(headers);

                DomGlobal.fetch(searchServiceUrl + value.trim().toLowerCase(), requestInit).then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                }).then(json -> {
                    List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                    JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                    JsArray<?> results = Js.cast(parsed.get("results"));
                    for (int i = 0; i < results.length; i++) {
                        JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
                        if (resultObj.has("attrs")) {
                            JsPropertyMap attrs = (JsPropertyMap) resultObj.get("attrs");
                            String label = ((JsString) attrs.get("label")).normalize();
                            label = label.replace("<b>", "").replace("</b>", "");
                            String origin = ((JsString) attrs.get("origin")).normalize();
                            double easting = ((JsNumber) attrs.get("x")).valueOf();
                            double northing = ((JsNumber) attrs.get("y")).valueOf();
                            
                            Icon icon;
                            if (origin.equalsIgnoreCase("parcel")) {
                                icon = Icons.ALL.home();
                            } else {
                                icon = Icons.ALL.mail();
                            }

                            SearchResult searchResult = new SearchResult();
                            searchResult.setLabel(label);
                            searchResult.setOrigin(origin);
                            searchResult.setCoordinate(new ol.Coordinate(easting, northing));
                            searchResult.setSearchText(value.trim());

                            SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);
                            suggestItems.add(suggestItem);
                        }
                    }
                    abortController = null;
                    suggestionsHandler.onSuggestionsReady(suggestItems);
                    return null;
                }).catch_(error -> {
                    console.log(error);
                    return null;
                });
            }

            @Override
            public void find(Object searchValue, Consumer handler) {
                if (searchValue == null) {
                    return;
                }
                SearchResult searchResult = (SearchResult) searchValue;
                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getSearchText());
                handler.accept(suggestItem);
            }
        };

        SuggestBox suggestBox = SuggestBox.create(messages.search_placeholder(), dynamicStore);
        suggestBox.addLeftAddOn(Icons.ALL.search());
        
        HTMLElement resetIcon = Icons.ALL.close().style().setCursor("pointer").get().element();
        resetIcon.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                console.log("fubar");
                HTMLInputElement el =(HTMLInputElement) suggestBox.getInputElement().element();
                el.value = "";
                suggestBox.unfocus();                
            }
        });

        suggestBox.addRightAddOn(resetIcon);
        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");
        suggestBox.setFocusOnClose(false);
        suggestBox.setFocusColor(Color.BLUE);
        DropDownMenu suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());

        suggestBox.addSelectionHandler(new SelectionHandler() {
            @Override
            public void onSelection(Object value) {
//                loader.stop();
//                resetGui();

//                RequestInit requestInit = RequestInit.create();
//                Headers headers = new Headers();
//                headers.append("Content-Type", "application/x-www-form-urlencoded"); 
//                requestInit.setHeaders(headers);

                SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                SearchResult result = (SearchResult) item.getValue();
                console.log(result);
                
                
                // TODO egrid / coordinate?
                // bubbling?

                // Kanton hier abfragen?
                
            }
        });

        
        
        HTMLElement suggestBoxDiv = div().id("suggestbox-div").add(suggestBox).element();
        searchCard.appendChild(suggestBoxDiv);

        
        
        
        
        root.appendChild(searchCard);

    }

    @Override
    public HTMLElement element() {
        return root;
    }

}
