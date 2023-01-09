package ch.so.agi.oereb;

import ol.Collection;
import ol.Coordinate;
import ol.Extent;
import ol.Map;
import ol.MapOptions;
import ol.OLFactory;
import ol.View;
import ol.ViewOptions;
import ol.control.Control;
import ol.interaction.DefaultInteractionsOptions;
import ol.interaction.Interaction;
import ol.layer.LayerOptions;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.TileWms;
import ol.source.TileWmsOptions;
import ol.source.TileWmsParams;
import ol.source.WmsServerType;
import ol.tilegrid.TileGrid;
import ol.tilegrid.TileGridOptions;
import proj4.Proj4;

public class GeodiensteMapPreset implements MapPreset {
    public static double resolutions[] = new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 };

    @Override
    public Map getMap(String mapId) {
        Proj4.defs("EPSG:2056", "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode("EPSG:2056");
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));
        Projection projection = new Projection(projectionOptions);
        Projection.addProjection(projection);

        
        ol.layer.Tile layer;        
        {
            TileWmsParams imageWMSParams = OLFactory.createOptions();
            imageWMSParams.setLayers("daten");
            imageWMSParams.set("FORMAT", "image/png; mode=8bit");
            imageWMSParams.set("TRANSPARENT", "false");
            imageWMSParams.set("TILED", "true");

            TileWmsOptions imageWMSOptions = OLFactory.createOptions();
            imageWMSOptions.setUrl("https://geodienste.ch/db/av_situationsplan_oereb_0");
//            imageWMSOptions.setUrl("wms");
            imageWMSOptions.setParams(imageWMSParams);
            imageWMSOptions.setServerType(WmsServerType.MAPSERVER);

            TileWms imageWMSSource = new TileWms(imageWMSOptions);
           
            TileGridOptions tileGridOptions = OLFactory.createOptions();
            tileGridOptions.setTileSize(new ol.Size(512, 512));
            tileGridOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));
            tileGridOptions.setResolutions(new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 });
            
            TileGrid tileGrid = new TileGrid(tileGridOptions);
            imageWMSSource.setTileGridForProjection(projection, tileGrid);
            
            LayerOptions layerOptions = OLFactory.createOptions();
            layerOptions.setSource(imageWMSSource);
            
            layer = new ol.layer.Tile(layerOptions);
        }
        
        ViewOptions viewOptions = OLFactory.createOptions();
        viewOptions.setProjection(projection);
        viewOptions.setResolutions(new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 });
        View view = new View(viewOptions);
        //Coordinate centerCoordinate = new Coordinate(2616491, 1240287);
        //Coordinate centerCoordinate = new Coordinate(2600474,1215428);
        //Coordinate centerCoordinate = new Coordinate(2607358,1228752); // SO (Polygon mit Loch)
        Coordinate centerCoordinate = new Coordinate(2599785,1215908);
        //Coordinate centerCoordinate = new Coordinate(2683354.500,1248769.250); // ZH
        //Coordinate centerCoordinate = new Coordinate(2646625, 1248730); // AG

        view.setCenter(centerCoordinate);
        //view.setZoom(6);
        view.setZoom(13);
        
        MapOptions mapOptions = OLFactory.createOptions();
        mapOptions.setTarget(mapId);
        mapOptions.setView(view);
        mapOptions.setControls(new Collection<Control>());

        DefaultInteractionsOptions interactionOptions = new ol.interaction.DefaultInteractionsOptions();
        interactionOptions.setPinchRotate(false);
        mapOptions.setInteractions(Interaction.defaults(interactionOptions));

        Map map = new Map(mapOptions);
        map.addLayer(layer);
        
        return map;
    }    
}
