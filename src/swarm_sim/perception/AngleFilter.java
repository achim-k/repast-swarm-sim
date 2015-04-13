package swarm_sim.perception;

import java.util.ArrayList;
import java.util.List;

public class AngleFilter {

	double objectRadius;
	List<AngleSegment> filterSegments = new ArrayList<>();

	public AngleFilter(double objectRadius) {
		super();
		this.objectRadius = objectRadius;
	}

	public List<AngleSegment> getFilterSegments() {
		return filterSegments;
	}

	public void add(double distance, double direction, double radius) {
		if(distance < radius || distance < objectRadius)
			return;
		
		double filterAngle = 4 * Math.asin(radius / distance);
		
		double startAngle = normAngle(direction - filterAngle/2);
		double endAngle = normAngle(direction + filterAngle/2);
		
		
		
		if(startAngle > endAngle) {
			/* crosses Math.Pi border → adding to segments */
			if(startAngle - endAngle < 0.01)
				endAngle = startAngle;
			
			filterSegments.add(new AngleSegment(startAngle, Math.PI));
			filterSegments.add(new AngleSegment(-Math.PI, endAngle));
		}
		else {
			filterSegments.add(new AngleSegment(startAngle, endAngle));
		}
	}

	public void clear() {
		filterSegments.clear();
	}
	
	public static double normAngle(double angle) {
		if(angle > Math.PI)
			return -Math.PI + angle - Math.PI;
		if(angle < -Math.PI)
			return Math.PI - (-Math.PI - angle);
		return angle;
	}

	public void add(double distance, double direction) {
		double startAngle = 0, endAngle = 0;
		
		if(distance < 2*objectRadius)
		{
			startAngle = normAngle(direction - Math.PI/2);
			endAngle = normAngle(direction + Math.PI/2);
		} else {
			double filterAngle = 4 * Math.asin(objectRadius / distance);
			
			startAngle = normAngle(direction - filterAngle/2);
			endAngle = normAngle(direction + filterAngle/2);
		}
		
		if(startAngle > endAngle) {
			/* crosses Math.Pi border → adding to segments */
			if(startAngle - endAngle < 0.01)
				endAngle = startAngle;
			
			filterSegments.add(new AngleSegment(startAngle, Math.PI));
			filterSegments.add(new AngleSegment(-Math.PI, endAngle));
		}
		else {
			filterSegments.add(new AngleSegment(startAngle, endAngle));
		}
		
	}
}
