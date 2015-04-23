package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.communication.DefaultNetworkAgent;
import swarm_sim.perception.AngleFilter;

public class DefaultAgent extends DefaultNetworkAgent implements IAgent {

    static int agentNo = 0;

    protected int agentId;
    protected Context<IAgent> context;
    protected Network<IAgent> commNet;
    protected ContinuousSpace<IAgent> space;
    protected AdvancedGridValueLayer exploredArea;
    protected Scenario scenario;
    public NdPoint currentLocation;

    protected int consecutiveMoveCount = 1;
    protected double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI,
	    Math.PI);
    protected List<FieldDistancePair> surroundingFields = new ArrayList<>();
    protected AngleFilter collisionAngleFilter = new AngleFilter(1);

    @SuppressWarnings("unchecked")
    public DefaultAgent(Context<IAgent> context) {
	this.context = context;
	this.space = (ContinuousSpace<IAgent>) context.getProjection(
		ContinuousSpace.class, "space_continuous");
	this.commNet = (Network<IAgent>) context.getProjection(Network.class,
		"network_comm");
	this.exploredArea = (AdvancedGridValueLayer) context
		.getValueLayer("layer_explored");
	this.scenario = Scenario.getInstance();
	this.agentId = ++agentNo;
    }

    @Override
    public AgentType getAgentType() {
	return null;
    }

    protected void defaultStepStart() {
	surroundingFields.clear();
	collisionAngleFilter.clear();

	if (currentLocation == null)
	    currentLocation = space.getLocation(this);
	surroundingFields = exploredArea.getFieldsRadial(currentLocation,
		scenario.perceptionScope);

	/* check for obstacles */
	for (FieldDistancePair field : surroundingFields) {
	    if (field.fieldType == FieldType.Obstacle) {
		if (field.distance <= scenario.maxMoveDistance + 1
			&& field.distance > 0) {
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, new NdPoint(field.x + .5,
				    field.y + .5));
		    collisionAngleFilter.add(field.distance, angle);
		}
	    }
	}
    }

    protected void defaultStepEnd() {

	/* set area as explored */
	for (FieldDistancePair field : surroundingFields) {
	    if (field.fieldType != FieldType.Obstacle) {
		if (field.value == 0)
		    scenario.exploredAreaCount++;
		else
		    scenario.redundantExploredAreaCount++;
	    }
	    exploredArea.set(field.value + 1, field.x, field.y);
	}
    }

    public VSpatial getShape(ShapeFactory2D shapeFactory) {
	return shapeFactory.createCircle(3, 16);
    }
}
