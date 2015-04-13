package swarm_sim;

import repast.simphony.space.Dimensions;

public class SectorMap {
	
	SectorMap parent;
	SectorMap map[][];
	Dimensions dims;
	int sectorsX, sectorsY, degree;
	
	
	
	public SectorMap(SectorMap parent, Dimensions dims, int sectorsX,
			int sectorsY, int degree) {
		this.parent = parent;
		this.dims = dims;
		this.sectorsX = sectorsX;
		this.sectorsY = sectorsY;
		this.degree = degree;
		
		if(sectorsX == 1 && sectorsY == 1)
			map = null;
		else {
//			for
		}
	}
	public SectorMap getParent() {
		return parent;
	}
	public Dimensions getDims() {
		return dims;
	}
	
	
	
	
	
}
