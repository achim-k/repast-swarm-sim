package swarm_sim;

public class ScanData {
	private int binCount;
	private double[] angleData;
	
	public ScanData(int binCount) {
		this.binCount = binCount;
		angleData = new double[binCount];
	}
	
	public void addData(double angle, double distance) {
		int bin = (int) ((angle + Math.PI)/(2*Math.PI/binCount));
//		angleData[bin] += distance;
		angleData[bin] = 1;
	}
	
	public int getBinCount() {
		return binCount;
	}
	
	public void clear() {
		for (int i = 0; i < angleData.length; i++) {
			angleData[i] = 0.0;
		}
	}
	
	public double[] getData() {
		return angleData;
	}
	
	public void normalize() {
		double sum = 0;
		for (int i = 0; i < angleData.length; i++) {
			sum += angleData[i];
		}
		if(sum <= 0)
			return;
		for (int i = 0; i < angleData.length; i++) {
			angleData[i] /= sum;
		}
	}
	
	public String getPrintable(String a) {
		String ret = "";
		
		for (int i = 0; i < angleData.length; i++) {
			if(angleData[i] <= 0) {
				ret += "_";
			} else {
				ret += a;
			}
		}
		
		return ret;
	}
}
