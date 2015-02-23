package swarm_sim;

import java.awt.Color;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class ExploredAreaStyle implements ValueLayerStyleOGL {
	private ValueLayer layer;

	@Override
	public Color getColor(double... coordinates) {
		double val = layer.get(coordinates);
		if (val > 5) {
			return new Color(240, 240, 240);
		}
		else
			return new Color(170, 170, 170);
	}

	@Override
	public float getCellSize() {
		return 15;
	}

	@Override
	public void init(ValueLayer layer) {
		this.layer = layer;
	}

}
