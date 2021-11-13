package decoder;

public class BiquadFilter {

	

	private double x1r, x2r, y1r, y2r;
	private double b0, b1, b2, a1, a2;
	
	private void setFilter(double b0, double b1, double b2, double a1, double a2) {
		this.b0 = b0;
		this.b1 = b1;
		this.b2 = b2;
		this.a1 = a1;
		this.a2 = a2;
	}

	public double update(double x) {
		double y = b0 * x + b1 * x1r + b2 * x2r - a1 * y1r - a2 * y2r;
		x2r = x1r;
		x1r = x;
		y2r = y1r;
		y1r = y;
		return y;
	}
	
	public void setLowPass(double frequency, double sampleRate, double q) {
		double freq = 2.0 * Math.PI * frequency / sampleRate;
		double alpha = Math.sin(freq) / (2.0 * q);
		double b0 = (1.0 - Math.cos(freq)) / 2.0;
		double b1 = 1.0 - Math.cos(freq);
		double b2 = (1.0 - Math.cos(freq)) / 2.0;
		double a0 = 1.0 + alpha;
		double a1 = -2.0 * Math.cos(freq);
		double a2 = 1.0 - alpha;
		setFilter(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0);
	}

	public void setHighPass(double frequency, double sampleRate, double q) {
		double freq = 2.0 * Math.PI * frequency / sampleRate;
		double alpha = Math.sin(freq) / (2.0 * q);
		double b0 = (1.0 + Math.cos(freq)) / 2.0;
		double b1 = -(1.0 + Math.cos(freq));
		double b2 = (1.0 + Math.cos(freq)) / 2.0;
		double a0 = 1.0 + alpha;
		double a1 = -2.0 * Math.cos(freq);
		double a2 = 1.0 - alpha;
		setFilter(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0);
	}

}
