package animation;

public class PathPolynomialInterpolateFloat extends PathFloat
{
	public int order = 3;
	public float[] samples;

	public PathPolynomialInterpolateFloat(int o,int w)
	{
		order = o;
		width = w;
		value = new float[w];
		samples = new float[w*(o+1)];
	}
	
	public void ensureCapacity(int width,int order)
	{
		int ssize = width*(order+1);
		if((samples==null)||(samples.length<ssize))
		{
			samples = new float[ssize];
		}
		if((value==null)||(value.length<width))
		{
			value = new float[width];
		}
		this.width = width;
		this.order = order;
	}
	
	public void evaluate(float fractionAlong)
	{
		if(fractionAlong<0.0f)
			fractionAlong = 0.0f;
		if(fractionAlong>1.0f)
			fractionAlong = 1.0f;
		float ifraction = 1.0f-fractionAlong;
		
		switch(order)
		{
		case 0:
			System.arraycopy(samples, 0, value, 0, width);
			break;
		case 1:
			for(int i=0;i<width;i++)
			{
				value[i] = samples[i]*ifraction+samples[i+width]*fractionAlong;
			}
			break;
		case 2:
		{
			float f2 = fractionAlong*fractionAlong;
			float if2 = ifraction*ifraction;
			for(int i=0;i<width;i++)
			{
				value[i] = samples[i]*if2+2.0f*samples[i+width]*fractionAlong*ifraction+samples[i+width*2]*f2;
			}
			break;
		}
		case 3:
		{
			float f2 = fractionAlong*fractionAlong;
			float f3 = f2*fractionAlong;
			float if2 = ifraction*ifraction;
			float if3 = if2*ifraction;
			for(int i=0;i<width;i++)
			{
				value[i] = samples[i]*if3+3.0f*samples[i+width]*fractionAlong*if2+3.0f*samples[i+width*2]*f2*ifraction+samples[i+width*3]*f3;
			}
			break;
		}
		}
	}
}
