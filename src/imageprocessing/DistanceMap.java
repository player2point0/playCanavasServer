package imageprocessing;

import rendering.RenderBuffer;
import rendering.VolumeBuffer;

public class DistanceMap 
{
	/** unit in 16-bit EDM image: this value corresponds to a distance of one pixel */
	public static final int ONE = 41;
	/** in 16-bit EDM image this value corresponds to a pixel distance of sqrt(2) */
	public static final int SQRT2 = 58; // ~ 41 * sqrt(2)
	/** in 16-bit EDM image this value corresponds to a pixel distance of sqrt(3) */
	public static final int SQRT3 = 71; // ~ 41 * sqrt(5)
	/** in 16-bit EDM image this value corresponds to a pixel distance of sqrt(5) */
	public static final int SQRT5 = 92; // ~ 41 * sqrt(5)

	public static int maxDist = 0;

	public static void distanceMap(RenderBuffer segmentation,
			int valueNotToBe,
			RenderBuffer distanceToNonBackgroundEdge)
	{
		final int ONE = 41;
		final int SQRT2 = 58; // ~ 41 * sqrt(2)
		final int SQRT5 = 92; // ~ 41 * sqrt(5)
		final int MAX = 0xFFFFFF;

		distanceToNonBackgroundEdge.clear(MAX);
		
		for(int j=0;j<segmentation.height;j++)
		{
			int joff = j*segmentation.width;
			for(int i=0;i<segmentation.width;i++)
			{
				if((segmentation.pixel[i+joff])!=valueNotToBe)
				{
					distanceToNonBackgroundEdge.pixel[i+joff] = 0;
				}
				else
				{
					if(i==0)
					{
						distanceToNonBackgroundEdge.pixel[i+joff] = MAX;
					}
					else
					{
						if(distanceToNonBackgroundEdge.pixel[i-1+joff]<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i-1+joff]+ONE;
						}
						else
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = MAX;
						}
					}
					if(j==0)
					{
						//Do nothing pixel is labelled
					}
					else
					{
						if((distanceToNonBackgroundEdge.pixel[i+joff-segmentation.width]+ONE)<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+joff-segmentation.width]+ONE;
						}
						if(i!=0)
						{
							if((distanceToNonBackgroundEdge.pixel[i-1+joff-segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i-1+joff-segmentation.width]+SQRT2;
							}
						}
						if(i!=(segmentation.width-1))
						{
							if((distanceToNonBackgroundEdge.pixel[i+1+joff-segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+1+joff-segmentation.width]+SQRT2;
							}
						}
					}
				}
			}
		}
		//*
		for(int j=(segmentation.height-1);j>=0;j--)
		{
			int joff = j*segmentation.width;
			for(int i=(segmentation.width-1);i>=0;i--)
			{
				if((segmentation.pixel[i+joff])!=valueNotToBe)
				{
				}
				else
				{
					if(i==(segmentation.width-1))
					{
					}
					else
					{
						if((distanceToNonBackgroundEdge.pixel[i+1+joff]+ONE)<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+1+joff]+ONE;
						}
					}
					if(j==(segmentation.height-1))
					{
						//Do nothing pixel is labelled
					}
					else
					{
						if((distanceToNonBackgroundEdge.pixel[i+joff+segmentation.width]+ONE)<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+joff+segmentation.width]+ONE;
						}
						if(i!=0)
						{
							if((distanceToNonBackgroundEdge.pixel[i-1+joff+segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i-1+joff+segmentation.width]+SQRT2;
							}
						}
						if(i!=(segmentation.width-1))
						{
							if((distanceToNonBackgroundEdge.pixel[i+1+joff+segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+1+joff+segmentation.width]+SQRT2;
							}
						}

					}
				}
			}
		}
		//*/
	}

	public static void distanceMap(RenderBuffer segmentation,
			int valueNotToBe,
			RenderBuffer nearestNonBackgroundEdge,
			RenderBuffer distanceToNonBackgroundEdge)
	{
		final int ONE = 41;
		final int SQRT2 = 58; // ~ 41 * sqrt(2)
		final int SQRT5 = 92; // ~ 41 * sqrt(5)
		final int MAX = 0xFFFFFF;

		distanceToNonBackgroundEdge.clear(MAX);
		nearestNonBackgroundEdge.clear(-1);
		
		for(int j=0;j<segmentation.height;j++)
		{
			int joff = j*segmentation.width;
			for(int i=0;i<segmentation.width;i++)
			{
				if((segmentation.pixel[i+joff])!=valueNotToBe)
				{
					distanceToNonBackgroundEdge.pixel[i+joff] = 0;
					nearestNonBackgroundEdge.pixel[i+joff] = i+joff;
				}
				else
				{
					if(i==0)
					{
						distanceToNonBackgroundEdge.pixel[i+joff] = MAX;
						nearestNonBackgroundEdge.pixel[i+joff] = -1;
					}
					else
					{
						if(distanceToNonBackgroundEdge.pixel[i-1+joff]<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i-1+joff]+ONE;
							nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i-1+joff];
						}
						else
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = MAX;
							nearestNonBackgroundEdge.pixel[i+joff] = -1;
						}
					}
					if(j==0)
					{
						//Do nothing pixel is labelled
					}
					else
					{
						if((distanceToNonBackgroundEdge.pixel[i+joff-segmentation.width]+ONE)<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+joff-segmentation.width]+ONE;
							nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i+joff-segmentation.width];
						}
						if(i!=0)
						{
							if((distanceToNonBackgroundEdge.pixel[i-1+joff-segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i-1+joff-segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i-1+joff-segmentation.width];
							}
						}
						if(i!=(segmentation.width-1))
						{
							if((distanceToNonBackgroundEdge.pixel[i+1+joff-segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+1+joff-segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i+1+joff-segmentation.width];
							}
						}
					}
				}
			}
		}
		//*
		for(int j=(segmentation.height-1);j>=0;j--)
		{
			int joff = j*segmentation.width;
			for(int i=(segmentation.width-1);i>=0;i--)
			{
				if((segmentation.pixel[i+joff])!=valueNotToBe)
				{
				}
				else
				{
					if(i==(segmentation.width-1))
					{
					}
					else
					{
						if((distanceToNonBackgroundEdge.pixel[i+1+joff]+ONE)<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+1+joff]+ONE;
							nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i+1+joff];
						}
					}
					if(j==(segmentation.height-1))
					{
						//Do nothing pixel is labelled
					}
					else
					{
						if((distanceToNonBackgroundEdge.pixel[i+joff+segmentation.width]+ONE)<distanceToNonBackgroundEdge.pixel[i+joff])
						{
							distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+joff+segmentation.width]+ONE;
							nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i+joff+segmentation.width];
						}
						if(i!=0)
						{
							if((distanceToNonBackgroundEdge.pixel[i-1+joff+segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i-1+joff+segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i-1+joff+segmentation.width];
							}
						}
						if(i!=(segmentation.width-1))
						{
							if((distanceToNonBackgroundEdge.pixel[i+1+joff+segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.pixel[i+joff])
							{
								distanceToNonBackgroundEdge.pixel[i+joff] = distanceToNonBackgroundEdge.pixel[i+1+joff+segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.pixel[i+joff] = nearestNonBackgroundEdge.pixel[i+1+joff+segmentation.width];
							}
						}

					}
				}
			}
		}
		//*/
	}

	public static void distanceMap(VolumeBuffer segmentation,
			int valueNotToBe,
			VolumeBuffer nearestNonBackgroundEdge,
			VolumeBuffer distanceToNonBackgroundEdge)
	{
		final int ONE = 41;
		final int SQRT2 = 58; // ~ 41 * sqrt(2)
		final int SQRT5 = 92; // ~ 41 * sqrt(5)
		final int MAX = 0xFFFFFF;

		int planedelta = segmentation.width*segmentation.height;
		for(int k=0;k<segmentation.depth;k++)
		{
			//System.out.println(""+k+"/"+segmentation.depth);
			int koff = k*segmentation.height*segmentation.width;
		for(int j=0;j<segmentation.height;j++)
		{
			int joff = j*segmentation.width;//+koff;
			for(int i=0;i<segmentation.width;i++)
			{
				if((segmentation.voxels[i+joff+koff])!=valueNotToBe)
				{
					distanceToNonBackgroundEdge.voxels[i+joff+koff] = 0;
					nearestNonBackgroundEdge.voxels[i+joff+koff] = i+joff+koff;
				}
				else
				{
					if(i==0)
					{
						distanceToNonBackgroundEdge.voxels[i+joff+koff] = MAX;
						nearestNonBackgroundEdge.voxels[i+joff+koff] = -1;
					}
					else
					{
						if(distanceToNonBackgroundEdge.voxels[i-1+joff+koff]<MAX)
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff]+ONE;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff];
						}
						else
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = MAX;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = -1;
						}
					}
					if(j==0)
					{
						//Do nothing pixel is labelled
					}
					else
					{
						if((distanceToNonBackgroundEdge.voxels[i+joff+koff-segmentation.width]+ONE)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff-segmentation.width]+ONE;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff-segmentation.width];
						}
						if(i!=0)
						{
							if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width];
							}
						}
						if(i!=(segmentation.width-1))
						{
							if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width];
							}
						}
					}
					if(k==0)
					{
						
					}
					else
					{
						//Check the previous planes points
						if((distanceToNonBackgroundEdge.voxels[i+joff+koff-planedelta]+ONE)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff-planedelta]+ONE;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff-planedelta];
						}
						//x delta
						if(i!=0)
						{
						if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff-planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff-planedelta]+SQRT2;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff-planedelta];
						}
						}
						if(i!=(segmentation.width-1))
						{
						if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff-planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff-planedelta]+SQRT2;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff-planedelta];
						}
						}
						if(j!=0)
						{
							if((distanceToNonBackgroundEdge.voxels[i+joff+koff-segmentation.width-planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff-segmentation.width-planedelta]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff-segmentation.width-planedelta];
							}
							if(i!=0)
							{
								if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width-planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width-planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width-planedelta];
								}
							}
							if(i!=(segmentation.width-1))
							{
								if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width-planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width-planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width-planedelta];
								}
							}
						}
						if(j!=(segmentation.height-1))
						{
							if((distanceToNonBackgroundEdge.voxels[i+joff+koff+segmentation.width-planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff+segmentation.width-planedelta]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff+segmentation.width-planedelta];
							}
							if(i!=0)
							{
								if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width-planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width-planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width-planedelta];
								}
							}
							if(i!=(segmentation.width-1))
							{
								if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width-planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width-planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width-planedelta];
								}
							}
						}
					}
				}
			}
		}
		}
		
		//*
		for(int k=(segmentation.depth-1);k>=0;k--)
		{
			int koff = k*segmentation.height*segmentation.width;
			
		for(int j=(segmentation.height-1);j>=0;j--)
		{
			int joff = j*segmentation.width;
			for(int i=(segmentation.width-1);i>=0;i--)
			{
				if((segmentation.voxels[i+joff+koff])!=valueNotToBe)
				{
				}
				else
				{
					if(i==(segmentation.width-1))
					{
					}
					else
					{
						if(distanceToNonBackgroundEdge.voxels[i+1+joff+koff]<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff]+ONE;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff];
						}
					}
					if(j==(segmentation.height-1))
					{
						//Do nothing pixel is labelled
					}
					else
					{
						if((distanceToNonBackgroundEdge.voxels[i+joff+koff+segmentation.width]+ONE)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff+segmentation.width]+ONE;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff+segmentation.width];
						}
						if(i!=0)
						{
							if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width];
							}
						}
						if(i!=(segmentation.width-1))
						{
							if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width];
							}
						}
					}
					if(k==(segmentation.depth-1))
					{
						
					}
					else
					{
						//Check the previous planes points
						if((distanceToNonBackgroundEdge.voxels[i+joff+koff+planedelta]+ONE)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff+planedelta]+ONE;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff+planedelta];
						}
						//x delta
						if(i!=0)
						{
						if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff+planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff+planedelta]+SQRT2;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff+planedelta];
						}
						}
						if(i!=(segmentation.width-1))
						{
						if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff+planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
						{
							distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff+planedelta]+SQRT2;
							nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff+planedelta];
						}
						}
						if(j!=0)
						{
							if((distanceToNonBackgroundEdge.voxels[i+joff+koff-segmentation.width+planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff-segmentation.width+planedelta]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff-segmentation.width+planedelta];
							}
							if(i!=0)
							{
								if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width+planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width+planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff-segmentation.width+planedelta];
								}
							}
							if(i!=(segmentation.width-1))
							{
								if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width+planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width+planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff-segmentation.width+planedelta];
								}
							}
						}
						if(j!=(segmentation.height-1))
						{
							if((distanceToNonBackgroundEdge.voxels[i+joff+koff+segmentation.width+planedelta]+SQRT2)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
							{
								distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+joff+koff+segmentation.width+planedelta]+SQRT2;
								nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+joff+koff+segmentation.width+planedelta];
							}
							if(i!=0)
							{
								if((distanceToNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width+planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width+planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i-1+joff+koff+segmentation.width+planedelta];
								}
							}
							if(i!=(segmentation.width-1))
							{
								if((distanceToNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width+planedelta]+SQRT3)<distanceToNonBackgroundEdge.voxels[i+joff+koff])
								{
									distanceToNonBackgroundEdge.voxels[i+joff+koff] = distanceToNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width+planedelta]+SQRT3;
									nearestNonBackgroundEdge.voxels[i+joff+koff] = nearestNonBackgroundEdge.voxels[i+1+joff+koff+segmentation.width+planedelta];
								}
							}
						}
					}
				}

			}
		}
		}
		//*/
	}

	public static void make16bitEDM(int[] input,int[] output,int width, int height)
	{
		make16bitEDM(input, output, width, height, 0xFFFFF, false);
	}
	/**	Calculates a 16-bit grayscale Euclidean Distance Map for a binary 8-bit image.
	 * Each foreground (black) pixel in the binary image is
	 * assigned a value equal to its distance from the nearest
	 * background (white) pixel.  Uses the two-pass EDM algorithm
	 * from the "Image Processing Handbook" by John Russ.
	 */
	public static void make16bitEDM(int[] input,int[] output,int width, int height,float maxdistance,boolean invert) 
	{
		int xmax, ymax;
		int offset, rowsize;

		maxDist = 0;

		int mind= 0;
		int maxd= (int)(maxdistance*41);
		if (invert)
		{
			mind = maxd;
			maxd = 0;
		}
		rowsize = width;
		xmax    = width - 2;
		ymax    = height - 2;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				if((input[x+y*width]&0x000000FF)>0)
					output[x+y*width] = mind;
				else
					output[x+y*width] = maxd;
				//output[x+y*width] = Integer.MAX_VALUE-SQRT5*4;
			}
		}
		int[] image16 = output;

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				offset = x + y * rowsize;
				if ((image16[offset] > 0)) {
					//output[x+y*width] = 0x255;
					//*
					if ((x<=1) || (x>=xmax) || (y<=1) || (y>=ymax))
						setEdgeValue(offset, rowsize, image16, x, y, xmax, ymax);
					else
						setValue(offset, rowsize, image16);
					//*/
				}
				else
				{
					//output[x+y*width] = 0;
				}
			} // for x
		} // for y

		for (int y=height-1; y>=0; y--) {
			for (int x=width-1; x>=0; x--) {
				offset = x + y * rowsize;
				if ((image16[offset] > 0)) {
					//output[x+y*width] = 0x255;
					//*
					if ((x<=1) || (x>=xmax) || (y<=1) || (y>=ymax))
						setEdgeValue(offset, rowsize, image16, x, y, xmax, ymax);
					else
						setValue(offset, rowsize, image16);
					//*/
				}
				else
				{
					//output[x+y*width] = 0;
				}
			} // for x
		} // for y
		//(new ImagePlus("EDM16", ip16.duplicate())).show();
		//System.out.println("maxDist:"+maxDist);
	} // make16bitEDM(ip)

	static void setValue(int offset, int rowsize, int[] image16) {
		int  v;
		int r1  = offset - rowsize - rowsize - 2;
		int r2  = r1 + rowsize;
		int r3  = r2 + rowsize;
		int r4  = r3 + rowsize;
		int r5  = r4 + rowsize;
		//int min = 32767;
		//int min = Integer.MAX_VALUE;
		int min = image16[r2 + 2] + ONE;

		v = image16[r2 + 2] + ONE;
		if (v < min)
			min = v;
		v = image16[r3 + 1] + ONE;
		if (v < min)
			min = v;
		v = image16[r3 + 3] + ONE;
		if (v < min)
			min = v;
		v = image16[r4 + 2] + ONE;
		if (v < min)
			min = v;

		v = image16[r2 + 1] + SQRT2;
		if (v < min)
			min = v;
		v = image16[r2 + 3] + SQRT2;
		if (v < min)
			min = v;
		v = image16[r4 + 1] + SQRT2;
		if (v < min)
			min = v;
		v = image16[r4 + 3] + SQRT2;
		if (v < min)
			min = v;

		v = image16[r1 + 1] + SQRT5;
		if (v < min)
			min = v;
		v = image16[r1 + 3] + SQRT5;
		if (v < min)
			min = v;
		v = image16[r2 + 4] + SQRT5;
		if (v < min)
			min = v;
		v = image16[r4 + 4] + SQRT5;
		if (v < min)
			min = v;
		v = image16[r5 + 3] + SQRT5;
		if (v < min)
			min = v;
		v = image16[r5 + 1] + SQRT5;
		if (v < min)
			min = v;
		v = image16[r4] + SQRT5;
		if (v < min)
			min = v;
		v = image16[r2] + SQRT5;
		if (v < min)
			min = v;

		image16[offset] = min;

		if(min>maxDist)
		{
			maxDist = min;
		}
	} // setValue()

	static void setEdgeValue(int offset, int rowsize, int[] image16, int x, int y, int xmax, int ymax) 
	{
		int  v;
		int r1 = offset - rowsize - rowsize - 2;
		int r2 = r1 + rowsize;
		int r3 = r2 + rowsize;
		int r4 = r3 + rowsize;
		int r5 = r4 + rowsize;
		//int min = 32767;
		//int min = 1<<31;
		int offimage = image16[r3 + 2];
		int min = Integer.MAX_VALUE;

		if (y<1)
			v = offimage + ONE;
		else
			v = image16[r2 + 2] + ONE;
		if (v < min)
			min = v;

		if (x<1)
			v = offimage + ONE;
		else
			v = image16[r3 + 1] + ONE;
		if (v < min)
			min = v;

		if (x>xmax)
			v = offimage + ONE;
		else
			v = image16[r3 + 3] + ONE;
		if (v < min)
			min = v;

		if (y>ymax)
			v = offimage + ONE;
		else
			v = image16[r4 + 2] + ONE;
		if (v < min)
			min = v;

		if ((x<1) || (y<1))
			v = offimage + SQRT2;
		else
			v = image16[r2 + 1] + SQRT2;
		if (v < min)
			min = v;

		if ((x>xmax) || (y<1))
			v = offimage + SQRT2;
		else
			v = image16[r2 + 3] + SQRT2;
		if (v < min)
			min = v;

		if ((x<1) || (y>ymax))
			v = offimage + SQRT2;
		else
			v = image16[r4 + 1] + SQRT2;
		if (v < min)
			min = v;

		if ((x>xmax) || (y>ymax))
			v = offimage + SQRT2;
		else
			v = image16[r4 + 3] + SQRT2;
		if (v < min)
			min = v;

		if ((x<1) || (y<=1))
			v = offimage + SQRT5;
		else
			v = image16[r1 + 1] + SQRT5;
		if (v < min)
			min = v;

		if ((x>xmax) || (y<=1))
			v = offimage + SQRT5;
		else
			v = image16[r1 + 3] + SQRT5;
		if (v < min)
			min = v;

		if ((x>=xmax) || (y<1))
			v = offimage + SQRT5;
		else
			v = image16[r2 + 4] + SQRT5;
		if (v < min)
			min = v;

		if ((x>=xmax) || (y>ymax))
			v = offimage + SQRT5;
		else
			v = image16[r4 + 4] + SQRT5;
		if (v < min)
			min = v;

		if ((x>xmax) || (y>=ymax))
			v = offimage + SQRT5;
		else
			v = image16[r5 + 3] + SQRT5;
		if (v < min)
			min = v;

		if ((x<1) || (y>=ymax))
			v = offimage + SQRT5;
		else
			v = image16[r5 + 1] + SQRT5;
		if (v < min)
			min = v;

		if ((x<=1) || (y>ymax))
			v = offimage + SQRT5;
		else
			v = image16[r4] + SQRT5;
		if (v < min)
			min = v;

		if ((x<=1) || (y<1))
			v = offimage + SQRT5;
		else
			v = image16[r2] + SQRT5;
		if (v < min)
			min = v;

		image16[offset] = min;

		if(min>maxDist)
		{
			maxDist = min;
		}
	} // setEdgeValue()
}
