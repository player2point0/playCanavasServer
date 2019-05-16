package imageprocessing;

import rendering.RenderBuffer;
import rendering.VolumeBuffer;

public class MedialAxis 
{
	public static void calcAxisFromDistanceMap(RenderBuffer distanceMap,RenderBuffer output)
	{
		output.clear(0);
		for(int j=1;j<(distanceMap.height-1);j++)
		{
			for(int i=1;i<(distanceMap.width-1);i++)
			{
				int pi = i+j*distanceMap.width;
				int d = distanceMap.pixel[pi];
				
				//if input is inside then forget it
				if(d==0)
				{
					//output.pixel[pi] = 0x0;
					continue;
				}
				
				//Test all the 8 points arround this one
				int numlarger = 0;
				for(int dj=-1;dj<=1;dj++)
				{
					for(int di=-1;di<=1;di++)
					{
						if((di==0)&&(dj==0))
							continue;
						int pdi = pi+di+dj*distanceMap.width;
						int td = distanceMap.pixel[pdi];
						if(td==0)
						{
							numlarger = 3;
							break;
						}
						if(td>d)
							numlarger++;
					}					
				}
				//if more than 2 are larger then output is false
				//else output is 1
				if(numlarger<=2)
					output.pixel[pi] = 0xFFFFFF;
			}			
		}
	}

	public static void calcAxisFromDistanceVolume(VolumeBuffer distanceMap,VolumeBuffer output)
	{
		output.clear(0);
		for(int k=1;k<(distanceMap.depth-1);k++)
		{
		for(int j=1;j<(distanceMap.height-1);j++)
		{
			for(int i=1;i<(distanceMap.width-1);i++)
			{
				int pi = i+j*distanceMap.width+k*distanceMap.width*distanceMap.height;
				int d = distanceMap.voxels[pi];
				
				//if input is inside then forget it
				if(d==0)
				{
					//output.pixel[pi] = 0x0;
					continue;
				}
				
				//Test all the 8 points arround this one
				int numlarger = 0;
				for(int dk=-1;dk<=1;dk++)
				{
				for(int dj=-1;dj<=1;dj++)
				{
					for(int di=-1;di<=1;di++)
					{
						if((di==0)&&(dj==0))
							continue;
						int pdi = pi+di+dj*distanceMap.width+dk*distanceMap.width*distanceMap.height;
						int td = distanceMap.voxels[pdi];
						if(td==0)
						{
							numlarger = 3;
							break;
						}
						if(td>d)
							numlarger++;
					}					
				}
				}
				//if more than 2 are larger then output is false
				//else output is 1
				if(numlarger<=2)
					output.voxels[pi] = 0xFFFFFF;
			}			
		}
		}
	}
}
