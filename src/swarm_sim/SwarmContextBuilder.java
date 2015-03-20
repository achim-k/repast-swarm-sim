package swarm_sim;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.valueLayer.ContinuousValueLayer;
import swarm_sim.neural.ActorCritic;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SwarmContextBuilder implements ContextBuilder<Agent> {

	@Override
	public Context build(Context<Agent> context) {
		context.setId("swarm_sim");
		
		/* Create geography space */
//		GeographyParameters geoParams = new GeographyParameters();
//		Geography geography = GeographyFactoryFinder.createGeographyFactory(null)
//				.createGeography("geography", context, geoParams);

		/* Create continuous space */
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Agent> space = spaceFactory.createContinuousSpace(
				"space_continuous", context, new RandomCartesianAdder<Agent>(),
				new repast.simphony.space.continuous.StickyBorders(), 50,
				50);

		/* Create communication network */
//		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("asdf", context, false);
//		netBuilder.buildNetwork();

//		CommNet commNet = new CommNet("comm_network", context);
//		context.addProjection(commNet);
		
		
		/* Value layer for painting? */
		ContinuousValueLayer exploredArea = new ContinuousValueLayer(
				"explored_area", 50, 50);
		context.addValueLayer(exploredArea);
		
//		NetworkDisplayLayerGIS dge = new NetworkDisplayLayerGIS(net, geography, style);
		

		/* add the agents to the context */	
		int agentCount = 1;
//		Agent agents[] = new Agent[10];
//		GeometryFactory fac = new GeometryFactory();

		PrintWriter writer = null;
		try {
			writer = new PrintWriter("the-file-name.txt", "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		ActorCritic ac = new ActorCritic(0.4, 1, 0);
		ac.loadSomNeuronsFromFile("data/som.txt", ",");
		// Generate some points
		for (int i = 0; i < agentCount; i++) {
			SwarmAgent agent = new SwarmAgent(context, writer, ac);
			context.add(agent);

//			Coordinate coord = new Coordinate(-87.9 + 0.3* Math.random(), 41.8 + 0.3 * Math.random());
//			Point geom = fac.createPoint(coord);
//			
//			geography.move(agent, geom);
//			agents[i] = agent;
		}
		
		
		int pheromoneCount = 31;
		for (int i = 0; i < pheromoneCount; i++) {
			context.add(new Pheromone());
		}
		
		/* Add dummy edge to context here, otherwise it won't work in the robot class */
//		Agent agent = new CommNetEdge<>(agents[0], agents[1], false, 0);
//		context.add(agent);
//		geography.move(agent, agent.getGeometry());
//		//context.remove(agent);
//		context.add(new ControllerAgent(context, commNet, geography));
//		
//		/* dummy pheromone */
//		Pheromone p = new Pheromone(agents[0].getGeometry());
//		context.add(p);
//		geography.move(p, p.getGeometry());
//		
		

		// TODO GIS: use an example of ShapefileLoader
		
		// Load Features from shapefiles
//		loadFeatures( "data/Zones2.shp", context, geography);
//		loadFeatures( "data/Agents2.shp", context, geography);
//		loadFeatures( "data/WaterLines.shp", context, geography);

		return context;
	}
	
	/**
	 * Loads features from the specified shapefile.  The appropriate type of agents
	 * will be created depending on the geometry type in the shapefile (point, 
	 * line, polygon).
	 * 
	 * @param filename the name of the shapefile from which to load agents
	 * @param context the context
	 * @param geography the geography
	 */
	private void loadFeatures (String filename, Context context, Geography geography){
		URL url = null;
		try {
			url = new File(filename).toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
		// Try to load the shapefile
		SimpleFeatureIterator fiter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);

		try {
			fiter = store.getFeatureSource().getFeatures().features();

			while(fiter.hasNext()){
				features.add(fiter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			fiter.close();
			store.dispose();
		}
		
		// For each feature in the file
		int count = 0;
		for (SimpleFeature feature : features){
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			Object agent = null;

			// For Polygons, create ZoneAgents
			if (geom instanceof MultiPolygon){
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				geom = (Polygon)mp.getGeometryN(0);

				// Read the feature attributes and assign to the ZoneAgent
				String name = (String)feature.getAttribute("name");
				double taxRate = (double)feature.getAttribute("Tax_Rate");

//				agent = new ZoneAgent(name,taxRate);

//				// Create a BufferZoneAgent around the zone, just for visualization
//				Geometry buffer = GeometryUtils.generateBuffer(geography, geom, zoneDistance);
//				BufferZoneAgent bufferZone = new BufferZoneAgent("Buffer: " + name, 
//						zoneDistance, (ZoneAgent)agent);
//				context.add(bufferZone);
//				geography.move(bufferZone, buffer);
			}

//			// For Points, create RadioTower agents
//			else if (geom instanceof Point){
//				geom = (Point)feature.getDefaultGeometry();				
//
//				// Read the feature attributes and assign to the ZoneAgent
//				String name = (String)feature.getAttribute("Name");
//				agent = new RadioTower(name);
//			}

//			// For Lines, create WaterLines
//			if (geom instanceof MultiLineString){
//				MultiLineString line = (MultiLineString)feature.getDefaultGeometry();
//				geom = (LineString)line.getGeometryN(0);
//
//				// Read the feature attributes and assign to the ZoneAgent
//				String name = (String)feature.getAttribute("Name");
//				double flowRate = (Long)feature.getAttribute("Flow_Rate");
//				agent = new WaterLine(name, flowRate);
//			}

			if (agent != null){
				context.add(agent);
				geography.move(agent, geom);
				System.out.println("Added agent " + count++);
			}
			else{
				System.out.println("Error creating agent for  " + geom);
			}
		}				
	}

}
