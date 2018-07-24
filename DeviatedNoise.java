import java.util.Random;

public class DeviatedNoise
{	
	private final Random random;
	private final long seed;
	
	public DeviatedNoise(long seed)
	{
		this.seed = seed;
		random = new Random(seed);
	}
	
	public double GetNoise(double x, double y, double deviation, double minExp, double maxExp, int octaves)
	{
		double value = 0.0;
		double amplitude = 0.5;
		double frequency = 1.0;
		
		for (int i = 0; i < octaves; i++)
		{
			value += amplitude * GetNoise(x * frequency, y * frequency, deviation, minExp, maxExp);
			frequency *= 2.0;
			amplitude *= 0.5;
		}
		return value;
	}
	
	public double GetNoise(double x, double y, double deviation, double minExp, double maxExp)
	{
		double expMultiplier = maxExp - minExp;
		
		int intX = (int) x;
		int intY = (int) y;
		double posX = (x - intX) * 2.0 - 1.0;
		double posY = (y - intY) * 2.0 - 1.0;
		
		Vec4 center = Noise(intX, intY, deviation, minExp, expMultiplier);		
		Vec4 adjacentX = posX - center.x > 0.0 ? Noise(intX + 1, intY, deviation, minExp, expMultiplier).Add(2.0, 0.0) : Noise(intX - 1, intY, deviation, minExp, expMultiplier).Add(-2.0, 0.0);

		double adjacentXBlend = Distance(posX, adjacentX.x) / Distance(center.x, adjacentX.x);
		double height = Interpolate(adjacentX.y, center.y, adjacentXBlend);
		
		Vec4 adjacentY = new Vec4();
		Vec4 corner = new Vec4();
		if (posY - height > 0.0f)
		{
			adjacentY = Noise(intX, intY + 1, deviation, minExp, expMultiplier).Add(0.0, 2.0);
			
			if (posX - adjacentY.x > 0.0f)
				corner = Noise(intX + 1, intY + 1, deviation, minExp, expMultiplier).Add(2.0, 2.0);
			else
				corner = Noise(intX - 1, intY + 1, deviation, minExp, expMultiplier).Add(-2.0, 2.0);
		}
		else
		{
			adjacentY = Noise(intX, intY - 1, deviation, minExp, expMultiplier).Add(0.0, -2.0);
			
			if (posX - adjacentY.x > 0.0f)
				corner = Noise(intX + 1, intY - 1, deviation, minExp, expMultiplier).Add(2.0, -2.0);
			else
				corner = Noise(intX - 1, intY - 1, deviation, minExp, expMultiplier).Add(-2.0, -2.0);
		}
		double cornerXBlend = Distance(posX, corner.x) / Distance(adjacentY.x, corner.x);
		double highHeight = Interpolate(corner.y, adjacentY.y, cornerXBlend);
		
		double value = Math.pow(
				Interpolate(
					Interpolate(corner.z, adjacentY.z, cornerXBlend),
					Interpolate(adjacentX.z, center.z, adjacentXBlend),
					Distance(posY, highHeight) / Distance(height, highHeight)),
				Interpolate(
					Interpolate(corner.w, adjacentY.w, cornerXBlend),
					Interpolate(adjacentX.w, center.w, adjacentXBlend),
					Distance(posY, highHeight) / Distance(height, highHeight)));
		return value * 2.0 - 1.0;
	}
	
	private Vec4 Noise(int x, int y, double deviation, double minExp, double expMultiplier)
	{
		random.setSeed(x * -49614232L + y * 32516786776L + seed);
		return new Vec4((random.nextDouble() * 2.0 - 1.0) * deviation, (random.nextDouble() * 2.0 - 1.0) * deviation, random.nextDouble(), minExp + random.nextDouble() * expMultiplier);
	}
	
	private static double Interpolate(double a, double b, double blend)
	{
		double theta = blend * Math.PI;
		double f = (1.0 - Math.cos(theta)) * 0.5;
		return a * (1.0 - f) + b * f;	
	}
	
	private static double Distance(double x0, double x1)
	{
		return Math.sqrt((x1 - x0) * (x1 - x0));
	}
}

class Vec4
{
	double x, y, z, w;
	
	Vec4() {}
	
	Vec4(double x, double y, double z, double w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	Vec4 Add(double x, double y)
	{
		this.x += x;
		this.y += y;
		return this;
	}
}
