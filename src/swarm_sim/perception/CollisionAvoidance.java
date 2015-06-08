package swarm_sim.perception;

import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AbstractAgent;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;

public class CollisionAvoidance {

    public static void setForbiddenSegmentsForAgent(
	    ContinuousSpace<AbstractAgent> space, NdPoint currLoc,
	    PDDP smd, NdPoint agentLoc) { 
	
	double distance = space.getDistance(currLoc, agentLoc);
	if(distance  > 1.5 || distance < 0.5)
	    return;
	
	double angle = SpatialMath.calcAngleFor2DMovement(space, currLoc, agentLoc);
	
	double filterAngle = 2 * Math.asin(1 / distance);
	if(filterAngle > 1.57)
	    filterAngle = 1.57;
	
	double angleStart = AngleFilter.normAngle(angle - filterAngle);
	double angleEnd =  AngleFilter.normAngle(angle + filterAngle);
	smd.setInvalidSegments(angleStart, angleEnd);
    }
    
    
    public static void setForbiddenSegmentsForObstacle(
	    ContinuousSpace<AbstractAgent> space, NdPoint currLoc,
	    PDDP smd, FieldDistancePair obs) {
	NdPoint start;
	NdPoint end;
	
	double displacement[] = space.getDisplacement(currLoc, obs.loc);
	
	if(displacement[0] > .5) {
	    if(displacement[1] > .5) {
		start = CornerBR(obs.loc);
		end = CornerTL(obs.loc);
	    } else if(displacement[1] < -.5) {
		start = CornerBL(obs.loc);
		end = CornerTR(obs.loc);
	    } else {
		/* On the axis */
		start = CornerBL(obs.loc);
		end = CornerTL(obs.loc);
	    }	    
	} else if(displacement[0] < -.5) {
	    if(displacement[1] > .5) {
		start = CornerTR(obs.loc);
		end = CornerBL(obs.loc);
	    } else if(displacement[1] < -.5) {
		start = CornerTL(obs.loc);
		end = CornerBR(obs.loc);
	    } else {
		/* On the axis */
		start = CornerTR(obs.loc);
		end = CornerBR(obs.loc);
	    }
	} else {
	    /* On the axis */
	    if(displacement[1] > 0) {
		start = CornerBR(obs.loc);
		end = CornerBL(obs.loc);
	    } else {
		start = CornerTL(obs.loc);
		end = CornerTR(obs.loc);
	    }
	}
	
	double angleStart = SpatialMath.calcAngleFor2DMovement(space, currLoc, start);
	double angleEnd = SpatialMath.calcAngleFor2DMovement(space, currLoc, end);
	smd.setInvalidSegments(angleStart, angleEnd);
    }
    
    private static NdPoint CornerTR(NdPoint obstacle) {
	return new NdPoint(obstacle.getX() + .5, obstacle.getY() + .5);
    }
    
    private static NdPoint CornerTL(NdPoint obstacle) {
	return new NdPoint(obstacle.getX() - .5, obstacle.getY() + .5);
    }
    
    private static NdPoint CornerBR(NdPoint obstacle) {
	return new NdPoint(obstacle.getX() + .5, obstacle.getY() - .5);
    }
    
    private static NdPoint CornerBL(NdPoint obstacle) {
	return new NdPoint(obstacle.getX() - .5, obstacle.getY() - .5);
    }
}
