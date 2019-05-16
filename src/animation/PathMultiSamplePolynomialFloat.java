package animation;

public class PathMultiSamplePolynomialFloat extends PathFloat
{
	public int order = 3;
	public float[] samples;
	public float[] fraction;
	public int numSamples;
	
	public PathMultiSamplePolynomialFloat()
	{
	}
	
	public PathMultiSamplePolynomialFloat(int o,int w,int numSamples)
	{
		resize(o,w,numSamples);
	}

	public void resize(int o,int w,int numSamples)
	{
		order = o;
		width = w;
		value = new float[w];
		//In blocks for each sample		
		int numSampleValues = w*(order+1)*(numSamples);
		samples = new float[numSampleValues];
		this.numSamples = numSamples;
		fraction = new float[numSamples+1];
	}
	
	public void evaluate(float fractionAlong)
	{
		if(fractionAlong<0.0f)
			fractionAlong = 0.0f;
		if(fractionAlong>1.0f)
			fractionAlong = 1.0f;

		//Before curve
		if(fractionAlong<=fraction[0])
		{
			System.arraycopy(samples, 0, value, 0, width);
			return;
		}
		
		//After curve (calc the max for the last sample
		if(fractionAlong>fraction[numSamples])
		{
			System.arraycopy(samples, (numSamples-1+order)*width, value, 0, width);
			return;
		}
		
		int sampleS = 0;
		for(;sampleS<numSamples;sampleS++)
		{
			if(fractionAlong<=fraction[sampleS+1])
			{
				break;
			}
			sampleS++;
		}

		float fstart = fraction[sampleS];
		float fend = fraction[sampleS+1];
		float nf = (fractionAlong-fstart)/(fend-fstart);

		fractionAlong = nf;
		float ifraction = 1.0f-fractionAlong;

		//How to do the interpolation		
		int offset = sampleS*width*(order+1);
		switch(order)
		{
		case 0:
			System.arraycopy(samples, offset, value, 0, width);
			break;
		case 1:
			for(int i=0;i<width;i++)
			{
				value[i] = samples[offset+i]*ifraction+samples[offset+i+width]*fractionAlong;
			}
			break;
		case 2:
		{
			float f2 = fractionAlong*fractionAlong;
			float if2 = ifraction*ifraction;
			for(int i=0;i<width;i++)
			{
				value[i] = samples[offset+i]*if2+2.0f*samples[offset+i+width]*fractionAlong*ifraction+samples[offset+i+width*2]*f2;
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
				value[i] = samples[offset+i]*if3+3.0f*samples[offset+i+width]*fractionAlong*if2+3.0f*samples[offset+i+width*2]*f2*ifraction+samples[offset+i+width*3]*f3;
			}
			break;
		}
		}
	}
}
