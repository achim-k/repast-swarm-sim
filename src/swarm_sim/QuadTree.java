package swarm_sim;

import repast.simphony.space.continuous.NdPoint;

public class QuadTree {

    private Node root;

    private double minQuadrantEdgeSize;

    // helper node data type
    public class Node {
	double x, y; // x- and y- coordinates
	double width, height;

	Node NW, NE, SE, SW; // four subtrees
	boolean isFilled = false; // associated data
	int depth;

	Node(double x, double y, double width, double height, boolean value,
		int depth) {
	    this.x = x;
	    this.y = y;
	    this.isFilled = value;
	    this.width = width;
	    this.height = height;
	    this.depth = depth;
	}

	public Node copy() {
	    Node n = new Node(x, y, width, height, isFilled, depth);
	    if (NE != null)
		n.NE = NE.copy();
	    if (NW != null)
		n.NW = NW.copy();
	    if (SE != null)
		n.SE = SE.copy();
	    if (SW != null)
		n.SW = SW.copy();

	    return n;
	}

	public boolean isFilled() {
	    if (NW == null || NE == null || SE == null || SW == null)
		return isFilled;

	    isFilled = NW.isFilled() && NE.isFilled() && SE.isFilled()
		    && SW.isFilled();
	    if (isFilled)
		setFilled();

	    return isFilled;
	}

	public void setFilled() {
	    isFilled = true;
	    NW = NE = SE = SW = null;
	}

	public void merge(Node n) {
	    if (n == null)
		return;

	    if (isFilled || n.isFilled) {
		setFilled();
		return;
	    }

	    if (n.NE != null) {
		if (NE == null)
		    NE = n.NE.copy();
		else
		    NE.merge(n.NE);
	    }

	    if (n.NW != null) {
		if (NW == null)
		    NW = n.NW.copy();
		else
		    NW.merge(n.NW);
	    }

	    if (n.SE != null) {
		if (SE == null)
		    SE = n.SE.copy();
		else
		    SE.merge(n.SE);
	    }

	    if (n.SW != null) {
		if (SW == null)
		    SW = n.SW.copy();
		else
		    SW.merge(n.SW);
	    }

	    isFilled();
	}
    }

    public QuadTree(double spaceWidth, double spaceHeight,
	    double perceptionScope) {
	super();

	this.minQuadrantEdgeSize = 2 * perceptionScope;
	root = new Node(0, 0, spaceWidth, spaceHeight, false, 0);
    }

    public void setLocation(double posX, double posY) {
	root = createUntilPosReached(root, root.depth, root.x, root.y,
		root.width, root.height, posX, posY);
    }

    public Node getSmallestUnfilledNode(double posX, double posY) {
	return getSmallesUnfilledNode(root, null, posX, posY);
    }

    public NdPoint getUnfilledNodeCenter(Node parent, NdPoint location) {
	if (parent.NW == null || !parent.NW.isFilled())
	    return new NdPoint(parent.x + parent.width / 4, parent.y + 3
		    * parent.height / 4);
	else if (parent.NE == null || !parent.NE.isFilled())
	    return new NdPoint(parent.x + 3 * parent.width / 4, parent.y + 3
		    * parent.height / 4);
	else if (parent.SE == null || !parent.SE.isFilled())
	    return new NdPoint(parent.x + 3 * parent.width / 4, parent.y
		    + parent.height / 4);
	else if (parent.SW == null || !parent.SW.isFilled())
	    return new NdPoint(parent.x + parent.width / 4, parent.y
		    + parent.height / 4);

	return null;
    }

    /**
     * That one!
     * 
     * @param n
     * @param x
     * @param y
     * @param width
     * @param height
     * @param posX
     * @param posY
     * @return
     */
    private Node createUntilPosReached(Node n, int depth, double x, double y,
	    double width, double height, double posX, double posY) {
	if (n == null)
	    n = new Node(x, y, width, height, false, depth);

	if (n.isFilled)
	    return n;

	if (width <= minQuadrantEdgeSize && height <= minQuadrantEdgeSize) {
	    return new Node(x, y, width, height, true, depth); /* lowest node */
	}

	if (n.x + n.width / 2 > posX) {
	    /* West */
	    if (n.y + n.height / 2 > posY) {
		n.SW = createUntilPosReached(n.SW, depth + 1, x, y, width / 2d,
			height / 2d, posX, posY);
	    } else {
		n.NW = createUntilPosReached(n.NW, depth + 1, x, y + height
			/ 2d, width / 2d, height / 2d, posX, posY);
	    }
	} else {
	    /* East */
	    if (n.y + n.height / 2 > posY) {
		n.SE = createUntilPosReached(n.SE, depth + 1, x + width / 2d,
			y, width / 2d, height / 2d, posX, posY);
	    } else {
		n.NE = createUntilPosReached(n.NE, depth + 1, x + width / 2d, y
			+ height / 2d, width / 2d, height / 2d, posX, posY);
	    }
	}

	n.isFilled();

	return n;
    }

    private Node getSmallesUnfilledNode(Node n, Node parent, double posX,
	    double posY) {
	if (n == null || n.isFilled())
	    return parent;

	if (posY < n.height / 2d) {
	    /* South quadrant */
	    if (posX < n.width / 2d) {
		return getSmallesUnfilledNode(n.SW, n, posX, posY);
	    } else {
		return getSmallesUnfilledNode(n.SE, n, posX - n.width / 2d,
			posY);
	    }
	} else {
	    /* North quadrant */
	    if (posX < n.width / 2d) {
		return getSmallesUnfilledNode(n.NW, n, posX, posY - n.height
			/ 2d);
	    } else {
		return getSmallesUnfilledNode(n.NE, n, posX - n.width / 2d,
			posY - n.height / 2d);
	    }
	}
    }

    private Node getNodeToPosition(Node n, NdPoint location) {
	if (n.isFilled || n.SE == null)
	    return n;

	if (location.getY() < n.height / 2d) {
	    /* South quadrant */
	    if (location.getX() < n.width / 2d) {
		return getNodeToPosition(n.SW, location);
	    } else {
		NdPoint subLocation = new NdPoint(location.getX() - n.width
			/ 2d, location.getY());
		return getNodeToPosition(n.SE, subLocation);
	    }
	} else {
	    /* North quadrant */
	    if (location.getX() < n.width / 2d) {
		NdPoint subLocation = new NdPoint(location.getX(),
			location.getY() - n.height / 2d);
		return getNodeToPosition(n.NW, subLocation);
	    } else {
		NdPoint subLocation = new NdPoint(location.getX() - n.width
			/ 2d, location.getY() - n.height / 2d);
		return getNodeToPosition(n.NE, subLocation);
	    }
	}
    }

    public void merge(QuadTree q) {
	root.merge(q.root);
    }

    public void print() {
	print(root);
    }

    private void print(Node n) {
	if (n == null) {
	    return;
	}

	for (int i = 0; i < n.depth; i++) {
	    System.out.print("  ");
	}
	System.out.print("(" + n.x + ", " + n.y + ")" + " [" + n.width + ", "
		+ n.height + "]\n");

	print(n.NW);
	print(n.NE);
	print(n.SE);
	print(n.SW);
    }

}
