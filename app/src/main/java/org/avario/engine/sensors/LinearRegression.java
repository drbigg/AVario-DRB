package org.avario.engine.sensors;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.datastore.DataAccessObject;

public class LinearRegression implements MovementFactor {

	private Queue<Sample> samples = new ArrayDeque<Sample>();
	private boolean needNewSlope = false;
	private float currentSlope = 0f;
	//private IIRFilter slopeFilter = new IIRFilter(0.5f);

	// / Invariants
	private double sumx;
	private float sumy;

	@Override
	public synchronized void reset() {
		samples.clear();
		sumx = 0;
		sumy = 0f;
	}

	@Override
	public synchronized void notify(double x, float y) {
		Sample newSample = new Sample(x, y);
		sumx += x;
		sumy += y;
		samples.add(newSample);
		needNewSlope = true;
		// Cull old entries
		float sensitivity = DataAccessObject.get().getSensitivity();
		double oldest = x - sensitivity * sensitivity;
		while (samples.peek().x < oldest) {
			Sample s = samples.remove();
			sumx -= s.x;
			sumy -= s.y;
		}
	}

	@Override
	public synchronized float getValue() {
		if (!needNewSlope) {
			return currentSlope * 1000f;
		}

		needNewSlope = false;
		double xbar = sumx / (double) samples.size();
		float ybar = sumy / (float) samples.size();
		float xxbar = 0.0f, xybar = 0.0f;
		for (Sample s : samples) {
			xxbar += (s.x - xbar) * (s.x - xbar);
			xybar += (s.x - xbar) * (s.y - ybar);
		}
		float currSlope = xybar / xxbar;
//		float sensitivity = 1.0f - Math.min(0.3f, DataAccessObject.get().getSensitivity() * 0.01f);
//		slopeFilter.setFactor(sensitivity);
		//currSlope = slopeFilter.doFilter(currSlope)[0];
		return currSlope * 1000f;
	}

}
