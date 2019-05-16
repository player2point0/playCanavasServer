package meshprocessing;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.distance.Line;

public class JoinMesh 
{
	//
	static final float C_EPS = 1.0e-7f;		/* tolerance value: Used for making */

	static boolean FP_EQUAL(float s, float t)
	{
		return (Math.abs(s - t) <= C_EPS);
	}
	
	public static void mergeReusedUVs(GeneralMatrixFloat uv,
			GeneralMatrixInt tri)
	{
		GeneralMatrixInt uvrenumber = new GeneralMatrixInt(1,uv.height);
		GeneralMatrixFloat newuvs = new GeneralMatrixFloat(2);
		for(int i=0;i<uv.height;i++)
		{
			uvrenumber.value[i] = findOrAddUV(uv.value[i*2+0], uv.value[i*2+1], newuvs);
		}
		for(int ti=0;ti<tri.height;ti++)
		{
			for(int vi=0;vi<3;vi++)
			{
				int ind = ti*6+vi*2+1;
				int uvi = tri.value[ind];
				tri.value[ind] = uvrenumber.value[uvi];
			}
		}
		uv.set(newuvs);
	}
	
	static int findOrAddUV(float u,float v,
			GeneralMatrixFloat uv)
	{
		for(int i=0;i<uv.height;i++)
		{
			if(FP_EQUAL(u,uv.value[i*2+0])&&FP_EQUAL(v,uv.value[i*2+1]))
			{
				return i;
			}
		}
		return uv.push_back_row(u, v);
	}
	public static void join(GeneralMatrixFloat pos,
			GeneralMatrixFloat uv,
			GeneralMatrixInt tri,
			int stri)
	{
		mergeReusedUVs(uv, tri);
		
		float thresh = 0.00001f;
		//GeneralMatrixFloat closestLinePoint = new GeneralMatrixFloat(3,1);
		GeneralMatrixFloat lineExtent = new GeneralMatrixFloat(1,1);
		
		GeneralMatrixInt tristoremove = new GeneralMatrixInt(2);
		GeneralMatrixFloat uvtoadd = new GeneralMatrixFloat(2);
		
		//int tricount = 4;
		//For each uv referenced by the tris, test against all the edges of the existing triangles
		for(int ti=stri;ti<tri.height;ti++)
		{
			for(int vi=0;vi<3;vi++)
			{
				int uvi = tri.value[ti*6+vi*2+1];
				int posi = tri.value[ti*6+vi*2+0];
				
				tristoremove.height = 0;
				uvtoadd.height = 0;
				float u = uv.value[uvi*2+0];
				float v = uv.value[uvi*2+1];
				float x = pos.value[posi*3+0];
				float y = pos.value[posi*3+1];
				float z = pos.value[posi*3+2];
				//Now need to test if this uv intersects with one of the edges of any of the other polygons
				for(int oti=stri;oti<tri.height;oti++)
				{
					if(oti==ti)
						continue;
					int bestei = 0;
					float bestd = Float.MAX_VALUE;
					float bestu = 0.0f;
					float bestv = 0.0f;
					for(int ei=0;ei<3;ei++)
					{
						int vert0 = ei;
						int vert1 = (ei+1)%3;
						
						int uv0 = tri.value[oti*6+vert0*2+1];
						int uv1 = tri.value[oti*6+vert1*2+1];
						int pos0 = tri.value[oti*6+vert0*2+0];
						int pos1 = tri.value[oti*6+vert1*2+0];
						
						float u0 = uv.value[uv0*2+0];
						float v0 = uv.value[uv0*2+1];
						float u1 = uv.value[uv1*2+0];
						float v1 = uv.value[uv1*2+1];

						float x0 = pos.value[pos0*3+0];
						float y0 = pos.value[pos0*3+1];
						float z0 = pos.value[pos0*3+2];
						float x1 = pos.value[pos1*3+0];
						float y1 = pos.value[pos1*3+1];
						float z1 = pos.value[pos1*3+2];
						
						//float d = Line.GetPointToLineSquared(u, v, u0, v0, u1, v1, closestLinePoint, lineExtent);
						float d = Line.GetPointToLineSquared(x, y, z, x0, y0, z0, x1, y1, z1, null, lineExtent);
						
						if(d<bestd)
						{
							float le = lineExtent.value[0];
							if((le>thresh)&&((1.0f-le)>thresh))
							{
								bestd = d;
//								if((oti==15)&&(bestd<thresh))
//								{
//									System.out.println("debug");
//								}
								bestei = ei;
								bestu = u1*le+u0*(1.0f-le);
								bestv = v1*le+v0*(1.0f-le);
//								System.out.println("le="+le);
//								System.out.println("bestu="+bestu);
//								System.out.println("bestv="+bestv);
							}
						}
					}
					if(bestd<thresh)
					{
						tristoremove.push_back_row(oti,bestei);
						uvtoadd.push_back_row(bestu, bestv);
					}
				}
				
				//Now remove all the tris that are in the tristoremove list, they are in consequtive order so they should be removable in reverse order
				for(int rti=tristoremove.height-1;rti>=0;rti--)
				{
					int rt = tristoremove.value[rti*2+0];
					int ei = tristoremove.value[rti*2+1];
					int p0 = tri.value[rt*6+0];
					int uv0 = tri.value[rt*6+1];
					int p1 = tri.value[rt*6+2];
					int uv1 = tri.value[rt*6+3];
					int p2 = tri.value[rt*6+4];
					int uv2 = tri.value[rt*6+5];
					
					float u0 = uv.value[uv0*2+0];
					float v0 = uv.value[uv0*2+1];
					float u1 = uv.value[uv1*2+0];
					float v1 = uv.value[uv1*2+1];
					float u2 = uv.value[uv2*2+0];
					float v2 = uv.value[uv2*2+1];

//					if(rt==15)
//					{
//						System.out.println("debug");
//					}
//					System.out.println("splitting tri "+rt);
//					System.out.println("adding uv="+uvtoadd.value[rti*2+0]+","+uvtoadd.value[rti*2+1]);
					//int ui = uv.push_back_row(uvtoadd.value[rti*2+0], uvtoadd.value[rti*2+1]);//findOrAddUV(uvtoadd.value[rti*2+0], uvtoadd.value[rti*2+1], uv);
					int ui = findOrAddUV(uvtoadd.value[rti*2+0], uvtoadd.value[rti*2+1], uv);
					//tri.removeRow(rt);
					if(ei==0)
					{
						tri.value[rt*6+3] = ui;
						tri.value[rt*6+2] = posi;
						tri.push_back_row(posi, ui, p1, uv1, p2, uv2);
					}
					else
					if(ei==1)
					{
						tri.value[rt*6+5] = ui;
						tri.value[rt*6+4] = posi;
						tri.push_back_row(posi, ui, p2, uv2, p0, uv0);
					}
					else
					if(ei==2)
					{
						tri.value[rt*6+5] = ui;
						tri.value[rt*6+4] = posi;
						tri.push_back_row(p1, uv1, p2, uv2, posi, ui);
					}
						
					break;
				}
//				if(tristoremove.height>0)
//					break;
			}
//			if(tristoremove.height>0)
//			{
//				tricount--;
//				if(tricount==0)
//					break;
//			}
		}
	}
}
