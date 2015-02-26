package swarm_sim;

import javax.measure.unit.SI;

import org.apache.poi.hssf.record.chart.AxisUsedRecord;

import repast.simphony.space.gis.GeometryUtils;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.collections.IndexedIterable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


public class CommNetEdge<Object> extends RepastEdge<Agent> implements Agent {
	
	private Geometry geom; // the shape (line) of the edge
	
	public CommNetEdge(Agent source, Agent target, Boolean isDirected, double weight) {
		super(source, target, isDirected, weight);
		
		GeometryFactory fac = new GeometryFactory(); 
		Coordinate ar[] = new Coordinate[]{source.getGeometry().getCoordinate(), target.getGeometry().getCoordinate()};
		this.geom = fac.createLineString(ar);
	}
	
	public String getName() {
		return("Source: "+this.source.getName()+"Target:"+this.target.getName());
	}

	@Override
	public Geometry getGeometry() {
		// TODO Auto-generated method stub
		return geom;
	}
	
	public double getLinkDistance() {
		return geom.getLength();
	}
}
