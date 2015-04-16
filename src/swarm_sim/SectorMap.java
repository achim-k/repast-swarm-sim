package swarm_sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;

public class SectorMap {

    SectorMap parent = null;
    SectorMap map[][];
    Dimensions dims;
    int x, y, sectorsX, sectorsY, degree;
    boolean filled = false;
    int sectorMoveCount = 0;

    int posX, posY;
    int lastPosX, lastPosY;
    int targetX, targetY;

    public SectorMap(Dimensions dims, int sectorsX, int sectorsY, int degree) {
	init(dims, sectorsX, sectorsY, degree);
    }

    public SectorMap(SectorMap parent, int x, int y, Dimensions dims,
	    int sectorsX, int sectorsY, int degree) {
	this.parent = parent;
	this.x = x;
	this.y = y;
	init(dims, sectorsX, sectorsY, degree);
    }

    private void init(Dimensions dims, int sectorsX, int sectorsY, int degree) {
	this.dims = dims;
	this.sectorsX = sectorsX;
	this.sectorsY = sectorsY;
	this.degree = degree;

	targetX = RandomHelper.nextIntFromTo(0, sectorsX - 1);
	targetY = RandomHelper.nextIntFromTo(0, sectorsY - 1);

	if ((sectorsX == 1 && sectorsY == 1) || degree == 0)
	    map = null;
	else {
	    map = new SectorMap[sectorsX][sectorsY];
	    for (int x = 0; x < sectorsX; x++) {
		for (int y = 0; y < sectorsY; y++) {
		    map[x][y] = new SectorMap(this, x, y, new Dimensions(
			    dims.getWidth() / sectorsX, dims.getHeight()
				    / sectorsY), sectorsX, sectorsY, degree - 1);
		}
	    }
	}
    }

    @Override
    public boolean equals(Object obj) {
	if (obj.getClass() != SectorMap.class)
	    return false;

	SectorMap s = (SectorMap) obj;

	return s.x == x && s.y == y && s.degree == degree;
    }

    public Dimensions getDims() {
	return dims;
    }

    public boolean isFilled() {
	if (degree == 0) /* Recursion stop */
	    return filled;

	for (int x = 0; x < sectorsX; x++) {
	    for (int y = 0; y < sectorsY; y++) {
		if (!map[x][y].isFilled())
		    return false;
	    }
	}
	return true;
    }

    public NdPoint getSectorCenter(SectorMap sector) {
	if (sector.degree > degree)
	    return null;

	if (degree - 1 == sector.degree) {
	    double x = sector.x * dims.getWidth() / sectorsX;
	    double y = sector.y * dims.getHeight() / sectorsY;
	    return new NdPoint(x, y);
	}

	return null;
    }

    public void setPosition(NdPoint position) {
	lastPosX = posX;
	lastPosY = posY;
	posX = (int) (position.getX() * sectorsX / dims.getWidth());
	posY = (int) (position.getY() * sectorsY / dims.getHeight());

	/* TODO: Change! */
	map[posX][posY].sectorMoveCount++;
	if (map[posX][posY].sectorMoveCount > 2)
	    map[posX][posY].filled = true;

	if (degree > 1) {
	    double posChildX = position.getX() - posX * dims.getWidth();
	    double posChildY = position.getY() - posY * dims.getHeight();
	    NdPoint posChild = new NdPoint(posChildX, posChildY);
	    map[posX][posY].setPosition(posChild);
	}
    }

    public double getNewMoveAngle() {
	if (!map[targetX][targetY].isFilled()) {
	    if (posX != targetX || posY != targetY)
		return SpatialMath.angleFromDisplacement(targetX - posX,
			targetY - posY);

	    return RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
	} else {
	    if (posY == sectorsY - 1) {
		if (map[posX][posY - 1].filled)
		    targetX++;
		else
		    targetY--;
	    } else if (map[posX][posY + 1].filled)
		targetY--;
	    else
		targetY++;
	}

	if (targetY >= sectorsY) {
	    targetY = sectorsY - 1;
	    targetX++;
	}
	if (targetY < 0) {
	    targetY = 0;
	    targetX++;
	}
	if (targetX >= sectorsX)
	    targetX = 0;

	if (map[targetX][targetY].isFilled()) {
	    SectorMap s = getCloseUnfilledSector(posX, posY);
	    if (s != null) {
		targetX = s.x;
		targetY = s.y;
	    } else
		return RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
	}

	return SpatialMath
		.angleFromDisplacement(targetX - posX, targetY - posY);
    }

    private List<SectorMap> getNeighboringSectors(int secX, int secY, int depth) {
	List<SectorMap> neighbors = new ArrayList<>();

	for (int x = secX - depth; x <= secX + depth; x++) {
	    int yUpper = secY + depth;
	    int yLower = secY - depth;

	    if (x >= 0 && x < sectorsX && yUpper < sectorsY)
		neighbors.add(map[x][yUpper]);
	    if (x >= 0 && x < sectorsX && yLower >= 0)
		neighbors.add(map[x][yLower]);
	}

	for (int y = secY - depth + 1; y <= secY + depth - 1; y++) {
	    int xLeft = secX - depth;
	    int xRight = secX + depth;

	    if (y >= 0 && y < sectorsY && xLeft >= 0)
		neighbors.add(map[xLeft][y]);
	    if (y >= 0 && y < sectorsY && xRight < sectorsX)
		neighbors.add(map[xRight][y]);
	}

	return neighbors;
    }
    
    public NdPoint getCloseUnfilledSector() {
	SectorMap s = getCloseUnfilledSector(posX, posY);
	if(s != null) {
	    double x = s.x * dims.getWidth() / sectorsX;
	    double y = s.y * dims.getHeight() / sectorsY;
	    return new NdPoint(x, y);
	}
	return null;
    }

    private SectorMap getCloseUnfilledSector(int secX, int secY) {
	List<SectorMap> neighbors = null;
	int degree = 1;

	do {
	    neighbors = getNeighboringSectors(secX, secY, degree++);
	    Collections.shuffle(neighbors);
	    for (SectorMap s : neighbors) {
		if (!s.filled) {
		    return s;
		}
	    }
	} while (neighbors.size() > 0);

	return null;
    }

    public void merge(SectorMap sm) {
	if (sm.x != x || sm.y != y || sm.degree != degree)
	    return;

	for (int x = 0; x < sectorsX; x++) {
	    for (int y = 0; y < sectorsY; y++) {
		if (sm.map[x][y].isFilled())
		    map[x][y].filled = true;
	    }
	}
    }

    public void chooseNewTargetSector() {
	SectorMap newTarget = getCloseUnfilledSector(posX, posY);
	if (newTarget == null)
	    return;

	if (newTarget.x != targetX || newTarget.y != targetY) {
	    targetX = newTarget.x;
	    targetY = newTarget.y;
	    return;
	}

	/* Same sector again, look for sector with higher depth */
	int depth = Math.max(Math.abs(targetX - newTarget.x),
		Math.abs(targetY - newTarget.y));
	List<SectorMap> neighbors = getNeighboringSectors(posX, posY, depth + 1);
	if (neighbors == null) {
	    System.err.println("No new target found");
	    return;
	}

	newTarget = neighbors.get(RandomHelper.nextIntFromTo(0,
		neighbors.size() - 1));
	targetX = newTarget.x;
	targetY = newTarget.y;
    }

    public SectorMap getTargetSector() {
	return map[targetX][targetY];
    }
}
