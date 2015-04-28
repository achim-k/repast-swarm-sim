package swarm_sim;

/**
 * Singleton class which stores the data of the simulation run. Needs to implement IAgent interface in order
 * to be added to the context (needed for repast data sink mechanism)
 * 
 * @author achim
 *
 */
public class DataCollection implements IAgent {

    private static DataCollection instance = null;

    /* Data to be collected */
    public int fieldsExplored = 0;
    public int fieldsRedundantlyExplored = 0;
    public int deliveredResources = 0;
    public int messageCount = 0;
    public int moveCount = 0;
    public int wanderingAgents = 0;
    public int acquiringAgents = 0;
    public int deliveringAgents = 0;
    public double execTimeProcessMessages = 0;
    public double execTimeSendMessages = 0;
    public double execTimeMoveDecision = 0;
    public double execTimeScanEnv = 0;
    public double execTimeNetworkCalculation = 0;

    public long startTime;

    private DataCollection() {
    };

    public static DataCollection getInstance() {
	if (instance == null)
	    instance = new DataCollection();

	return instance;
    }

    public void reset() {
	fieldsExplored = 0;
	fieldsRedundantlyExplored = 0;
	deliveredResources = 0;
	messageCount = 0;
	moveCount = 0;
	wanderingAgents = 0;
	acquiringAgents = 0;
	deliveringAgents = 0;
	execTimeProcessMessages = 0;
	execTimeSendMessages = 0;
	execTimeMoveDecision = 0;
	execTimeScanEnv = 0;
	execTimeNetworkCalculation = 0;
	startTime = System.nanoTime();
    }

    public int getFieldsExplored() {
	return fieldsExplored;
    }

    public int getFieldsRedundantlyExplored() {
	return fieldsRedundantlyExplored;
    }

    public int getDeliveredResources() {
	return deliveredResources;
    }

    public int getMessageCount() {
	return messageCount;
    }

    public int getMoveCount() {
	return moveCount;
    }

    public int getWanderingAgents() {
	int tmp = wanderingAgents;
	wanderingAgents = 0;
	return tmp;
    }

    public int getAcquiringAgents() {
	int tmp = acquiringAgents;
	acquiringAgents = 0;
	return tmp;
    }

    public int getDeliveringAgents() {
	int tmp = deliveringAgents;
	deliveringAgents = 0;
	return tmp;
    }

    public double getExecTimeProcessMessages() {
	double tmp = execTimeProcessMessages;
	execTimeProcessMessages = 0;
	return tmp;
    }

    public double getExecTimeSendMessages() {
	double tmp = execTimeSendMessages;
	execTimeSendMessages = 0;
	return tmp;
    }

    public double getExecTimeMoveDecision() {
	double tmp = execTimeMoveDecision;
	execTimeMoveDecision = 0;
	return tmp;
    }

    public double getExecTimeScanEnv() {
	double tmp = execTimeScanEnv;
	execTimeScanEnv = 0;
	return tmp;
    }

    public double getExecTimeNetworkCalculation() {
	double tmp = execTimeNetworkCalculation;
	execTimeNetworkCalculation = 0;
	return tmp;
    }

    public double getTotalExecTime() {
	return System.nanoTime() - startTime;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.DataCollection;
    }
}
