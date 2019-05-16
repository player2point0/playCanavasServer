package imageprocessing;

import mathematics.GeneralMatrixInt;
import rendering.VolumeBuffer;

public class VolumeDifference 
{
	public static float grayscaleDifference(VolumeBuffer vba,VolumeBuffer vbb,int xd,int yd,int zd,
											GeneralMatrixInt bdelta)
	{
		float bdiff = Float.MAX_VALUE;
		
		int mk = Math.max(vba.depth, vbb.depth);
		int mj = Math.max(vba.height, vbb.height);
		int mi = Math.max(vba.width, vbb.width);
		
		int kmd = Math.abs(vba.depth-vbb.depth);
		int jmd = Math.abs(vba.height-vbb.height);
		int imd = Math.abs(vba.width-vbb.width);
		
		for(int kd=-zd;kd<(kmd+zd);kd++)
		{
		for(int jd=-yd;jd<(jmd+yd);jd++)
		{
		for(int id=-xd;id<(imd+xd);id++)
		{
		
			float diff = 0.0f;
			int totalset = 0;
			
		for(int k=0;k<mk;k++)
		{
		for(int j=0;j<mj;j++)
		{
		for(int i=0;i<mi;i++)
		{
			int c1 = 0;
			int c2 = 0;
			int i2 = i+id;
			int j2 = j+jd;
			int k2 = k+kd;
			if((i<vba.width)&&(j<vba.width)&&(k<vba.width))
			{
				int ind = i+j*vba.width+k*vba.width*vba.depth;
				c1 = vba.voxels[ind];
			}
			if((i2>=0)&&(j2>=0)&&(k2>=0)&&
					(i2<vbb.width)&&(j2<vbb.width)&&(k2<vbb.width))
			{
				int ind = i2+j2*vbb.width+k2*vbb.width*vbb.depth;
				c2 = vbb.voxels[ind];
			}

			//int r1 = (c1&0xFF0000)>>16;
			//int g1 = (c1&0xFF00)>>8;
			int b1 = (c1&0xFF);

			//int r2 = (c2&0xFF0000)>>16;
			//int g2 = (c2&0xFF00)>>8;
			int b2 = (c2&0xFF);
			
			if(b1!=0)
				totalset++;
			if(b2!=0)
				totalset++;
			
			//float dr = (r1-r2)/255.0f;
			//float dg = (g1-g2)/255.0f;
			float db = (b1-b2)/255.0f;
			
			diff += Math.abs(db);
		}
		}
		}
		diff /= totalset;
		
		if(diff<bdiff)
		{
			bdiff = diff;
			bdelta.value[0] = id;
			bdelta.value[1] = jd;
			bdelta.value[2] = kd;
		}
		
		}
		}
		}

		return bdiff;

	}
}
