package rendering;

import mathematics.GeneralMatrixFloat;

public class VolumeBuffer 
{
	//A generalisation of a render buffer to a volume
	public int width;
	public int height;
	public int depth;
	public int[] voxels;

	public VolumeBuffer()
	{}
	
	public VolumeBuffer(int w,int h,int d)
	{
		width = w;
		height = h;
		depth = d;
		voxels = new int[w*h*d];
	}
	
	public void setDimensions(int w,int h,int d)
	{
		width = w;
		height = h;
		depth = d;
		voxels = new int[w*h*d];
	}
	
	public void getPlaneAsImage(int dim,int plane,RenderBuffer img)
	{
		if(dim==2)
		{
			img.resize(width, height);
			System.arraycopy(voxels, plane*width*height, img.pixel, 0, width*height);
			img.flipy();
		}
		else
		if(dim==1)
		{
			img.resize(width, depth);
			//img.clear(0);
			for(int k=0;k<depth;k++)
			{
				int j = plane;
				//for(int j=0;j<height;j++)
				{
					for(int i=0;i<width;i++)
					{
						int pi = i+j*width+k*width*height;
						int ipi = i+k*width;
						int c = voxels[pi];
						//if(c!=0)
						{
							//int oc = img.pixel[ipi];
							//coloured (is special doesn't get overridden by white)
							//if((oc==0xFFFFFF)||(oc==0)) 
							img.pixel[ipi] = c;
						}
					}
				}				
			}
		}
		else
		if(dim==0)
		{
			img.resize(depth,height);
			img.clear(0);
			for(int k=0;k<depth;k++)
			{
				for(int j=0;j<height;j++)
				{
					int i = plane;
					//for(int i=0;i<width;i++)
					{
						int pi = i+j*width+k*width*height;
						int ipi = k+(height-1-j)*depth;
						int c = voxels[pi];
						//if(c!=0)
						{
//							int oc = img.pixel[ipi];
//							//coloured (is special doesn't get overridden by white)
//							if((oc==0xFFFFFF)||(oc==0)) 
								img.pixel[ipi] = c;
						}
					}
				}				
			}
		}
	}

	public void getSilhouette(int dim,RenderBuffer img,GeneralMatrixFloat com)
	{
		com.setDimensions(3, 1);
		
		if(dim==2)
		{
			img.resize(width, height);
			img.clear(0);
			for(int k=0;k<depth;k++)
			{
				for(int j=0;j<height;j++)
				{
					for(int i=0;i<width;i++)
					{
						int pi = i+j*width+k*width*height;
						int ipi = i+j*width;
						int c = voxels[pi];
						if(c!=0)
						{
							int oc = img.pixel[ipi];
							if(oc==0)
							{
								com.value[0] += i;
								com.value[1] += j;
								com.value[2] += 1;
							}
							//coloured (is special doesn't get overridden by white)
							if((oc==0xFFFFFF)||(oc==0)) 
								img.pixel[ipi] = c;
						}
					}
				}				
			}
		}
		else
		if(dim==1)
		{
			img.resize(width, depth);
			img.clear(0);
			for(int k=0;k<depth;k++)
			{
				for(int j=0;j<height;j++)
				{
					for(int i=0;i<width;i++)
					{
						int pi = i+j*width+k*width*height;
						int ipi = i+k*width;
						int c = voxels[pi];
						if(c!=0)
						{
							int oc = img.pixel[ipi];
							if(oc==0)
							{
								com.value[0] += i;
								com.value[1] += k;
								com.value[2] += 1;
							}
							//coloured (is special doesn't get overridden by white)
							if((oc==0xFFFFFF)||(oc==0)) 
								img.pixel[ipi] = c;
						}
					}
				}				
			}
		}
		else
		if(dim==0)
		{
			img.resize(height, depth);
			img.clear(0);
			for(int k=0;k<depth;k++)
			{
				for(int j=0;j<height;j++)
				{
					for(int i=0;i<width;i++)
					{
						int pi = i+j*width+k*width*height;
						int ipi = j+k*height;
						int c = voxels[pi];
						if(c!=0)
						{
							int oc = img.pixel[ipi];
							if(oc==0)
							{
								com.value[0] += j;
								com.value[1] += k;
								com.value[2] += 1;
							}
							//coloured (is special doesn't get overridden by white)
							if((oc==0xFFFFFF)||(oc==0)) 
								img.pixel[ipi] = c;
						}
					}
				}				
			}
		}
	}
	
	public void setFromVolumes(GeneralMatrixFloat rvolumes,float voxelwidth, int emptycolour, int solidcolour)
	{
		setFromVolumes(rvolumes, voxelwidth, emptycolour, solidcolour, null);
	}
	public void setFromVolumes(GeneralMatrixFloat rvolumes,float voxelwidth, int emptycolour, int solidcolour,
			GeneralMatrixFloat origin)
	{
		setFromVolumes(rvolumes, voxelwidth, emptycolour, solidcolour, origin, false);
	}
	public void setFromRotatedVolumes(GeneralMatrixFloat rvolumes,float voxelwidth,GeneralMatrixFloat rotation, int emptycolour, int solidcolour,
			GeneralMatrixFloat origin, boolean fillregions)
	{
		//Calc bounds of volume
		float minx = Float.MAX_VALUE;
		float miny = Float.MAX_VALUE;
		float minz = Float.MAX_VALUE;
		float maxx = -Float.MAX_VALUE;
		float maxy = -Float.MAX_VALUE;
		float maxz = -Float.MAX_VALUE;

		float meanx = 0.0f;
		float meany = 0.0f;
		float meanz = 0.0f;
		
		float sdx = 0.0f;
		float sdy = 0.0f;
		float sdz = 0.0f;
		
		float boundx = 0.0f;
		float boundy = 0.0f;
		float boundz = 0.0f;
		
		int num = 0;
		for(int iv=0;iv<(rvolumes.height/2);iv++)
		{
			float orvx = rvolumes.value[iv*6+0]+rvolumes.value[iv*6+0+3];
			float orvy = rvolumes.value[iv*6+1]+rvolumes.value[iv*6+1+3];
			float orvz = rvolumes.value[iv*6+2]+rvolumes.value[iv*6+2+3];
			
			orvx*=0.5f;
			orvy*=0.5f;
			orvz*=0.5f;
			
			float rvx = rotation.value[3*0+0]*orvx+rotation.value[3*0+1]*orvy+rotation.value[3*0+2]*orvz;
			float rvy = rotation.value[3*1+0]*orvx+rotation.value[3*1+1]*orvy+rotation.value[3*1+2]*orvz;
			float rvz = rotation.value[3*2+0]*orvx+rotation.value[3*2+1]*orvy+rotation.value[3*2+2]*orvz;
			
			sdx += rvx*rvx;
			sdy += rvy*rvy;
			sdz += rvz*rvz;
			
			meanx += rvolumes.value[iv*6+0];
			meany += rvolumes.value[iv*6+1];
			meanz += rvolumes.value[iv*6+2];

			meanx += rvolumes.value[iv*6+0+3];
			meany += rvolumes.value[iv*6+1+3];
			meanz += rvolumes.value[iv*6+2+3];
		
			if(minx>rvx)
				minx=rvx;
			if(miny>rvy)
				miny=rvy;
			if(minz>rvz)
				minz=rvz;
			if(maxx<rvx)
				maxx=rvx;
			if(maxy<rvy)
				maxy=rvy;
			if(maxz<rvz)
				maxz=rvz;

			num += 2;
		}
		
		if(num==0)
		{
			meanx = Float.MAX_VALUE;
			meany = Float.MAX_VALUE;
			meanz = Float.MAX_VALUE;
			
			return;
		}
		else
		{
			meanx /= num;
			meany /= num;
			meanz /= num;
			
			sdx /= num;
			sdy /= num;
			sdz /= num;

			sdx -= meanx*meanx; 
			sdy -= meany*meany; 
			sdz -= meanz*meanz; 
			
			sdx = (float)Math.sqrt(sdx);
			sdy = (float)Math.sqrt(sdy);
			sdz = (float)Math.sqrt(sdz);
			
			boundx = maxx-minx;
			boundy = maxy-miny;
			boundz = maxz-minz;
		}

		float voxelscale = 1.0f/voxelwidth;
		
		width = (int)(boundx*voxelscale);
		height = (int)(boundy*voxelscale);
		depth = (int)(boundz*voxelscale);
	
		voxels = new int[width*height*depth];
		clear(emptycolour);
		
		int numvols = (rvolumes.height/2);
		for(int iv=0;iv<numvols;iv++)
		{
//			if((iv%100)==0)
//				System.out.println(""+iv+"/"+numvols);
			float orvx = rvolumes.value[iv*6+0]+rvolumes.value[iv*6+0+3];
			float orvy = rvolumes.value[iv*6+1]+rvolumes.value[iv*6+1+3];
			float orvz = rvolumes.value[iv*6+2]+rvolumes.value[iv*6+2+3];
			
			orvx*=0.5f;
			orvy*=0.5f;
			orvz*=0.5f;
			
			float rvx = rotation.value[3*0+0]*orvx+rotation.value[3*0+1]*orvy+rotation.value[3*0+2]*orvz;
			float rvy = rotation.value[3*1+0]*orvx+rotation.value[3*1+1]*orvy+rotation.value[3*1+2]*orvz;
			float rvz = rotation.value[3*2+0]*orvx+rotation.value[3*2+1]*orvy+rotation.value[3*2+2]*orvz;

			//float rvx = rvolumes.value[iv*6+0]+rvolumes.value[iv*6+0+3];
			rvx -= minx;
			rvy -= miny;
			rvz -= minz;
						
			float xf = (rvx*voxelscale);
			//float yf = height-1-(rvy*voxelscale); 
			float yf = (rvy*voxelscale); 
			float zf = (rvz*voxelscale); 

			if(!fillregions)
			{
				int x = (int)(xf+0.5f);
				int y = (int)(yf+0.5f);
				int z = (int)(zf+0.5f);
				
				if((x>=0)&&(x<width)&&(y>=0)&&(y<height)&&(z>0)&&(z<depth))
				{
					int ind = z*height*width+y*width+x;
					voxels[ind] = solidcolour;
				}
			}
		}

		if(origin!=null)
		{
			origin.value[0] = minx;
			origin.value[1] = miny;
			origin.value[2] = minz;
		}

	}
	public void setFromVolumes(GeneralMatrixFloat rvolumes,float voxelwidth, int emptycolour, int solidcolour,
			GeneralMatrixFloat origin, boolean fillregions)
	{
		//Calc bounds of volume
		float minx = Float.MAX_VALUE;
		float miny = Float.MAX_VALUE;
		float minz = Float.MAX_VALUE;
		float maxx = -Float.MAX_VALUE;
		float maxy = -Float.MAX_VALUE;
		float maxz = -Float.MAX_VALUE;

		float meanx = 0.0f;
		float meany = 0.0f;
		float meanz = 0.0f;
		
		float sdx = 0.0f;
		float sdy = 0.0f;
		float sdz = 0.0f;
		
		float boundx = 0.0f;
		float boundy = 0.0f;
		float boundz = 0.0f;
		
		int num = 0;
		for(int iv=0;iv<(rvolumes.height/2);iv++)
		{
			float rvx = rvolumes.value[iv*6+0]+rvolumes.value[iv*6+0+3];
			float rvy = rvolumes.value[iv*6+1]+rvolumes.value[iv*6+1+3];
			float rvz = rvolumes.value[iv*6+2]+rvolumes.value[iv*6+2+3];
			
			rvx*=0.5f;
			rvy*=0.5f;
			rvz*=0.5f;
			
			sdx += rvx*rvx;
			sdy += rvy*rvy;
			sdz += rvz*rvz;
			
			meanx += rvolumes.value[iv*6+0];
			meany += rvolumes.value[iv*6+1];
			meanz += rvolumes.value[iv*6+2];

			meanx += rvolumes.value[iv*6+0+3];
			meany += rvolumes.value[iv*6+1+3];
			meanz += rvolumes.value[iv*6+2+3];
		
			if(minx>rvolumes.value[iv*6+0])
				minx=rvolumes.value[iv*6+0];
			if(miny>rvolumes.value[iv*6+1])
				miny=rvolumes.value[iv*6+1];
			if(minz>rvolumes.value[iv*6+2])
				minz=rvolumes.value[iv*6+2];
			if(maxx<rvolumes.value[iv*6+0+3])
				maxx=rvolumes.value[iv*6+0+3];
			if(maxy<rvolumes.value[iv*6+1+3])
				maxy=rvolumes.value[iv*6+1+3];
			if(maxz<rvolumes.value[iv*6+2+3])
				maxz=rvolumes.value[iv*6+2+3];

			num += 2;
		}
		
		if(num==0)
		{
			meanx = Float.MAX_VALUE;
			meany = Float.MAX_VALUE;
			meanz = Float.MAX_VALUE;
			
			return;
		}
		else
		{
			meanx /= num;
			meany /= num;
			meanz /= num;
			
			sdx /= num;
			sdy /= num;
			sdz /= num;

			sdx -= meanx*meanx; 
			sdy -= meany*meany; 
			sdz -= meanz*meanz; 
			
			sdx = (float)Math.sqrt(sdx);
			sdy = (float)Math.sqrt(sdy);
			sdz = (float)Math.sqrt(sdz);
			
			boundx = maxx-minx;
			boundy = maxy-miny;
			boundz = maxz-minz;
		}

		float voxelscale = 1.0f/voxelwidth;
		
		width = (int)(boundx*voxelscale);
		height = (int)(boundy*voxelscale);
		depth = (int)(boundz*voxelscale);
	
		voxels = new int[width*height*depth];
		clear(emptycolour);
		
		int numvols = (rvolumes.height/2);
		for(int iv=0;iv<numvols;iv++)
		{
//			if((iv%100)==0)
//				System.out.println(""+iv+"/"+numvols);
			
			//float rvx = rvolumes.value[iv*6+0]+rvolumes.value[iv*6+0+3];
			float rvx = rvolumes.value[iv*6+0]-minx;
			float rvy = rvolumes.value[iv*6+1]-miny;
			float rvz = rvolumes.value[iv*6+2]-minz;
			
			float rwx = rvolumes.value[iv*6+0]+rvolumes.value[iv*6+0+3];
			float rwy = rvolumes.value[iv*6+1]+rvolumes.value[iv*6+1+3];
			float rwz = rvolumes.value[iv*6+2]+rvolumes.value[iv*6+2+3];
			
			float xf = (rvx*voxelscale);
			//float yf = height-1-(rvy*voxelscale); 
			float yf = (rvy*voxelscale); 
			float zf = (rvz*voxelscale); 

			float xwf = rwx*voxelscale;
			float ywf = rwy*voxelscale;
			float zwf = rwz*voxelscale;

			if(!fillregions)
			{
				int x = (int)(xf+0.5f);
				int y = (int)(yf+0.5f);
				int z = (int)(zf+0.5f);
				
				if((x>=0)&&(x<width)&&(y>=0)&&(y<height)&&(z>0)&&(z<depth))
				{
					int ind = z*height*width+y*width+x;
					voxels[ind] = solidcolour;
				}
			}
			else
			{
				int sx = (int)(xf);
				int sy = (int)(yf);
				int sz = (int)(zf);
				int ex = (int)(xf+xwf);
				int ey = (int)(yf+ywf);
				int ez = (int)(zf+zwf);
				if(sx<0)
					sx = 0;
				if(sy<0)
					sy = 0;
				if(sz<0)
					sz = 0;
				if(ex>width)
					ex = width;
				if(ey>height)
					ey = height;
				if(ez>depth)
					ez = depth;
				for(int z=sz;z<ez;z++)
				{
					for(int y=sy;y<ey;y++)
					{
						for(int x=sx;x<ex;x++)
						{
							int ind = z*height*width+y*width+x;
							voxels[ind] = solidcolour;
						}					
					}					
				}
			}
		}

		if(origin!=null)
		{
			origin.value[0] = minx;
			origin.value[1] = miny;
			origin.value[2] = minz;
		}
	}

	public void clear(final int value,int x,int y,int z,int w,int h,int d)
	{

		int mink = z;
		int maxk = z+d;
		if(mink<0)
			mink=0;
		if(maxk>depth)
			maxk=depth;
		int minj = y;
		int maxj = y+h;
		if(minj<0)
			minj=0;
		if(maxj>height)
			maxj=height;
		int mini = x;
		int maxi = x+w;
		if(mini<0)
			mini=0;
		if(maxi>height)
			maxi=height;
		
		for(int k=mink;k<maxk;k++)
		{
			for(int j=minj;j<maxj;j++)
			{
				for(int i=mini;i<maxi;i++)
				{
					int ind = i+j*width+k*width*height;
					voxels[ind] = value;
				}
			}
		}
	}
	
	public void clear(final int value)
	{
		if(voxels==null)
			return;
		int size=voxels.length-1;
		int cleared=1;
		int index=1;
		voxels[0]=value;

		while (cleared<size)
		{
			System.arraycopy(voxels,0,voxels,index,cleared);
			size-=cleared;
			index+=cleared;
			cleared<<=1;
		}
		System.arraycopy(voxels,0,voxels,index,size);
	}

}
