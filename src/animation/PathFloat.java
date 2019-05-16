package animation;

public abstract class PathFloat 
{
	public int width = 0;
	public float[] value;
	
	public abstract void evaluate(float fractionAlong);
}
