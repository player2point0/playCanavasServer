package mathematics;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Random;

public class Rand 
{
	public static Rand singleton = new Rand(); 

	//public Random value = new Random();
	public final boolean load = false;
	public final int MAX = 0x7FFF;
	static final String randPath = "C:\\debug.txt";
	static BufferedReader in;

	private boolean haveNextNextGaussian;
	private double nextNextGaussian;
	public long seed;

	public void setLiteralSeed(long seed)
	{
	}
	public void setSeed(long seed)
	{
		 this.seed = (seed ^ 0x5DEECE66DL) & ((1L<<48) - 1);
		 haveNextNextGaussian = false;
	}
	public int next(int bits)
	{
	     seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
	     return (int) (seed >>> (48 - bits));
	}
	public int nextInt()
	{
		return next(32);
	}	
	public int nextInt(int n)
	 {
	    if (n <= 0)
	      throw new IllegalArgumentException("n must be positive");
	  
	    if ((n & -n) == n)  // i.e., n is a power of 2
	      return (int)((n * (long) next(31)) >> 31);
	  
	    int bits, val;
	    do
	    {
	      bits = next(31);
	      val = bits % n;
	    }
	    while(bits - val + (n-1)< 0);
	  
	    return val;
	}
	public long nextLong()
	 {
	      return ((long) next(32) << 32) + next(32);
	 }
	public boolean nextBoolean()
	{
	 return next(1) != 0;
	}
	public float nextFloat()
	{
	 return next(24) / (float) (1 << 24);
	}
	
	public double nextDouble()
	{
		return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
	}
	
	public synchronized double nextGaussian()
	  {
	    if (haveNextNextGaussian)
	    {
	      haveNextNextGaussian = false;
	      return nextNextGaussian;
	    }
	    else
	    {
	      double v1, v2, s;
	      do
	      {
	        v1 = 2 * nextDouble() - 1; // between -1.0 and 1.0
	        v2 = 2 * nextDouble() - 1; // between -1.0 and 1.0
	        s = v1 * v1 + v2 * v2;
	      }
	      while (s >= 1);
	  
	      double norm = Math.sqrt(-2 * Math.log(s) / s);
	      nextNextGaussian = v2 * norm;
	      haveNextNextGaussian = true;
	      return v1 * norm;
	    }
	 }
	public static void permute(GeneralMatrixInt permute)
	{
		GeneralMatrixInt p = new GeneralMatrixInt(1,permute.height);
		p.enumerate();
		
		int pi = 0;
		for(int i=1;i < permute.height; i++, pi++)
		{
		     int m = singleton.randInt() % (permute.height - i) + 1;
		     permute.value[i] = p.value[pi+m];
		      if (m != 1)
			p.value[pi+m] = p.value[pi+1];
		}
	}
	
	public void init()
	{
		setSeed(0);
		if(load)
		{
			File base = new File(randPath);
			try
			{
				in = new BufferedReader(new InputStreamReader(base.toURL().openStream()));
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}
	}

	public int randInt()
	{
		if(load)
		{
			try
			{
				String s = in.readLine();
				long l = Long.parseLong(s);
				int res = (int)(l&0xFFFFFFL);
				return res;
			}
			catch(Exception e)
			{
				return nextInt(MAX);
			}
		}
		else
		{
			return nextInt(MAX);
		}
	}

	//A float between 0 and 1
	public float randFloat()
	{
		if(load)
		{
			try
			{
				String s = in.readLine();
				float f = Float.parseFloat(s);
				return f;
			}
			catch(Exception e)
			{
				return nextFloat();
			}
		}
		else
		{
			return nextFloat();
		}
	}

	public double randDouble()
	{
		if(load)
		{
			try
			{
				String s = in.readLine();
				double f = Double.parseDouble(s);
				return f;
			}
			catch(Exception e)
			{
				return nextDouble();
			}
		}
		else
		{
			return nextDouble();
		}
	}

	public double randGaussian()
	{
		if(load)
		{
			try
			{
				String s = in.readLine();
				double d = Double.parseDouble(s);
				return d;
			}
			catch(Exception e)
			{
				return nextGaussian();
			}
		}
		else
		{
			return nextGaussian();
		}
	}
}
