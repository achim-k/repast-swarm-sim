package swarm_sim;

public class DataCollection {

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
    public double totalExecTime = 0;

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
	totalExecTime = 0;
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
	return wanderingAgents;
    }

    public int getAcquiringAgents() {
	return acquiringAgents;
    }

    public int getDeliveringAgents() {
	return deliveringAgents;
    }

    public double getExecTimeProcessMessages() {
	return execTimeProcessMessages;
    }

    public double getExecTimeSendMessages() {
	return execTimeSendMessages;
    }

    public double getExecTimeMoveDecision() {
	return execTimeMoveDecision;
    }

    public double getExecTimeScanEnv() {
	return execTimeScanEnv;
    }

    public double getExecTimeNetworkCalculation() {
	return execTimeNetworkCalculation;
    }

    public double getTotalExecTime() {
	return totalExecTime;
    }
}
