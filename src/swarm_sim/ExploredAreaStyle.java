package swarm_sim;

import java.awt.Color;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;
import swarm_sim.AdvancedGridValueLayer.FieldType;

public class ExploredAreaStyle implements ValueLayerStyleOGL {
    private AdvancedGridValueLayer layer;
    int defaultRed = 130;
    int defaultGreen = 130;
    int defaultBlue = 130;

    @Override
    public Color getColor(double... coordinates) {
	double val = layer.get(coordinates);
	FieldType fieldType = layer.getFieldType(coordinates);
	int deltaRed = 0, deltaGreen = 0, deltaBlue = 0;
	double tmpRed = 0, tmpBlue = 0, tmpGreen = 0;

	if (fieldType == FieldType.Obstacle) {
	    if (val > 0)
		return Color.DARK_GRAY;
	    return Color.BLACK;
	}

	if (val >= 1) {
	    deltaRed = 70;
	    deltaGreen = 40;
	    deltaBlue = 20;
	}

	for (int i = 1; i < val; i++) {
	    tmpRed += 1;
	    tmpGreen += 1;
	    tmpBlue += 0.5;
	}

	deltaRed += tmpRed;
	deltaGreen += tmpGreen;
	deltaBlue += tmpBlue;

	deltaRed = defaultRed + deltaRed > 255 ? 255 : defaultRed + deltaRed;
	deltaGreen = defaultGreen + deltaGreen > 255 ? 255 : defaultGreen
		+ deltaGreen;
	deltaBlue = defaultBlue + deltaBlue > 255 ? 255 : defaultBlue
		+ deltaBlue;

	return new Color(deltaRed, deltaGreen, deltaBlue, 255);
    }

    @Override
    public float getCellSize() {
	return 15;
    }

    @Override
    public void init(ValueLayer layer) {
	this.layer = (AdvancedGridValueLayer) layer;
    }

}
