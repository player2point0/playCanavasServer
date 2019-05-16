/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package meshprocessing;

import procedural.human.resources.Edges;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;

public class MeshSurface 
{
	public static void sortEdges(int[] edges,int[] quads)
	{
		int v0 = Edges.getVertex(quads, edges[0], 0);
		int v1 = Edges.getVertex(quads, edges[0], 1);
		GeneralMatrixInt edgesused = new GeneralMatrixInt(1,edges.length);
		edgesused.clear(-1);
		edgesused.value[0] = 0;
		System.out.println(""+edges[0]+",");

		GeneralMatrixInt edgeverts = new GeneralMatrixInt(2,edges.length);
		for(int ei=0;ei<edgeverts.height;ei++)
		{
			edgeverts.value[ei*2+0] = Edges.getVertex(quads, edges[ei], 0);
			edgeverts.value[ei*2+1] = Edges.getVertex(quads, edges[ei], 1);
		}
		
		int eind = 1;
		while(true)
		{
			boolean found = false;
			for(int ei=0;ei<edges.length;ei++)
			{
				if(edgesused.value[ei]!=-1)
					continue;
				
				int ov0 = Edges.getVertex(quads, edges[ei], 0);
				int ov1 = Edges.getVertex(quads, edges[ei], 1);
				if(ov0==v1)
				{
					System.out.println(""+edges[ei]+",");
					edgesused.value[ei] = eind;
					v0 = ov0;
					v1 = ov1;
					eind++;
					found = true;
				}
				else
				if(ov1==v1)
				{
					System.out.println(""+edges[ei]+",");
					edgesused.value[ei] = eind;
					v0 = ov1;
					v1 = ov0;
					eind++;
					found = true;
				}
			}
			if(!found)
			{
				if(eind!=edgesused.height)
					System.out.println("Edge problem "+v1);
				break;
			}
		}
	}
	
	public static void calculateTriNormals(GeneralMatrixInt triIndexes,
			GeneralMatrixFloat polygonVerts,
			//int polyamat,int polybmat,int polycmat,
			GeneralMatrixFloat normals)
	{
		normals.setDimensions(3, triIndexes.height);

		float epsilon = 0.0000000001f;
		int tripos = 0;
		int triSize = triIndexes.height;
		int vw = triIndexes.width/3;
		int tw = triIndexes.width;
		for(int i=0;i<triSize;i++)
		{
			int tri = i;
			int polyapos = triIndexes.value[tri*tw+0*vw]; int polybpos = triIndexes.value[tri*tw+1*vw]; int polycpos = triIndexes.value[tri*tw+2*vw];
			
			float wxf0 = polygonVerts.value[(polyapos+tripos)*3+0]*100.0f;
			float wxf1 = polygonVerts.value[(polybpos+tripos)*3+0]*100.0f;
			float wxf2 = polygonVerts.value[(polycpos+tripos)*3+0]*100.0f;
			float wyf0 = polygonVerts.value[(polyapos+tripos)*3+1]*100.0f;
			float wyf1 = polygonVerts.value[(polybpos+tripos)*3+1]*100.0f;
			float wyf2 = polygonVerts.value[(polycpos+tripos)*3+1]*100.0f;
			float wzf0 = polygonVerts.value[(polyapos+tripos)*3+2]*100.0f;
			float wzf1 = polygonVerts.value[(polybpos+tripos)*3+2]*100.0f;
			float wzf2 = polygonVerts.value[(polycpos+tripos)*3+2]*100.0f;

			float t12x = wxf1-wxf0;
			float t12y = wyf1-wyf0;
			float t12z = wzf1-wzf0;

			float t13x = wxf2-wxf0;
			float t13y = wyf2-wyf0;
			float t13z = wzf2-wzf0;

			float nzx = t12y*t13z-t13y*t12z;
			float nzy = t12z*t13x-t13z*t12x;
			float nzz = t12x*t13y-t13x*t12y;

			float nxyz = nzx*nzx+nzy*nzy+nzz*nzz;
			if(nxyz<epsilon)
			{
				nzx = 1.0f;
//				nzx = 0.0f;
//				nzy = 0.0f;
//				nzz = 1.0f;
			}
			else
			{
				nxyz = (float)Math.sqrt(nxyz);
				nzx /= nxyz;
				nzy /= nxyz;
				nzz /= nxyz;
			}
			
			normals.value[i*3+0] = nzx;
			normals.value[i*3+1] = nzy;
			normals.value[i*3+2] = nzz;
		}
	}
	
	public static void calcUnNormalisedTriangleNormals(
			float wxf0,float wyf0,float wzf0,
			float wxf1,float wyf1,float wzf1,
			float wxf2,float wyf2,float wzf2,
			int v0,int v1,int v2,
			GeneralMatrixFloat pnrm)
	{

		float t12x = wxf1-wxf0;
		float t12y = wyf1-wyf0;
		float t12z = wzf1-wzf0;

		float t13x = wxf2-wxf0;
		float t13y = wyf2-wyf0;
		float t13z = wzf2-wzf0;

		float nzx = t12y*t13z-t13y*t12z;
		float nzy = t12z*t13x-t13z*t12x;
		float nzz = t12x*t13y-t13x*t12y;

		float nxyz = nzx*nzx+nzy*nzy+nzz*nzz;
		if(nxyz<GeneralMatrixFloat.EPSILON)
		{
			nzx = 1.0f;
//			nzx = 0.0f;
//			nzy = 0.0f;
//			nzz = 1.0f;
		}
		else
		{
			nxyz = (float)Math.sqrt(nxyz);
			nzx /= nxyz;
			nzy /= nxyz;
			nzz /= nxyz;
		}
								
		pnrm.value[v0*3+0] += nzx;
		pnrm.value[v0*3+1] += nzy;
		pnrm.value[v0*3+2] += nzz;
		pnrm.value[v1*3+0] += nzx;
		pnrm.value[v1*3+1] += nzy;
		pnrm.value[v1*3+2] += nzz;
		pnrm.value[v2*3+0] += nzx;
		pnrm.value[v2*3+1] += nzy;
		pnrm.value[v2*3+2] += nzz;
	}
	public static void calcUnNormalisedTriangleTangentAndBinormals(
			float wxf0,float wyf0,float wzf0,
			float wxf1,float wyf1,float wzf1,
			float wxf2,float wyf2,float wzf2,
			float u1,float v1,
			float u2,float v2,
			float u3,float v3,
			int polyamat,int polybmat,int polycmat,
			GeneralMatrixFloat surfaceMatrices)
	{
		final float epsilon = GeneralMatrixFloat.EPSILON;
		float t12x = wxf1-wxf0;
		float t12y = wyf1-wyf0;
		float t12z = wzf1-wzf0;

		float t13x = wxf2-wxf0;
		float t13y = wyf2-wyf0;
		float t13z = wzf2-wzf0;

		float nzx = t12y*t13z-t13y*t12z;
		float nzy = t12z*t13x-t13z*t12x;
		float nzz = t12x*t13y-t13x*t12y;

		float nxyz = nzx*nzx+nzy*nzy+nzz*nzz;
		if(nxyz<epsilon)
		{
			nzx = 1.0f;
//			nzx = 0.0f;
//			nzy = 0.0f;
//			nzz = 1.0f;
		}
		else
		{
			nxyz = (float)Math.sqrt(nxyz);
			nzx /= nxyz;
			nzy /= nxyz;
			nzz /= nxyz;
		}
		
		//Generate tangent space normals
		float u12 = u2-u1;
		float u13 = u3-u1;

		float v12 = v2-v1;
		float v13 = v3-v1;
		
		float nxx,nxy,nxz;
		float nyx,nyy,nyz;
		
		//Normal along u is (remove v, choose sign)
		if(Math.abs(u12)<epsilon)
		{
			if(Math.abs(u13)<epsilon)
			{
				//System.out.println("Degenerate");
				//Degenerate
				nxx = wxf2-wxf0;
				nxy = wyf2-wyf0;
				nxz = wzf2-wzf0;
				
				nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
				if(nxyz<epsilon)
				{
					nxx = 1.0f;
//					nxx = 1.0f;
//					nxy = 0.0f;
//					nxz = 0.0f;
				}
				else
				{
					nxyz = (float)Math.sqrt(nxyz);
					nxx /= nxyz;
					nxy /= nxyz;
					nxz /= nxyz;
				}

				nyx = nzy*nxz-nxy*nzz;
				nyy = nzz*nxx-nxz*nzx;
				nyz = nzx*nxy-nxx*nzy;
			}
			else
			if(java.lang.Math.abs(v12)<epsilon)
			{
				//System.out.println("Degenerate");
				//Degenerate
				nxx = wxf2-wxf0;
				nxy = wyf2-wyf0;
				nxz = wzf2-wzf0;
				
				nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
				if(nxyz<epsilon)
				{
					nxx = 1.0f;
//					nxx = 1.0f;
//					nxy = 0.0f;
//					nxz = 0.0f;
				}
				else
				{
					nxyz = (float)Math.sqrt(nxyz);
					nxx /= nxyz;
					nxy /= nxyz;
					nxz /= nxyz;
				}

				nyx = nzy*nxz-nxy*nzz;
				nyy = nzz*nxx-nxz*nzx;
				nyz = nzx*nxy-nxx*nzy;
			}
			else
			{
				//u12==0.0
				//u13!=0.0
				//v12!=0.0
				nyx = t12x;
				nyy = t12y;
				nyz = t12z;

				nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
				if(nxyz<epsilon)
				{
					nyx = 1.0f;
//					nyx = 0.0f;
//					nyy = 1.0f;
//					nyz = 0.0f;
				}
				else
				{
					nxyz = (float)Math.sqrt(nxyz);
					nyx /= nxyz;
					nyy /= nxyz;
					nyz /= nxyz;
				}

				if(v12<0.0)
				{
					nyx = -nyx;
					nyy = -nyy;
					nyz = -nyz;
				}
				
				if(java.lang.Math.abs(v13)<epsilon)
				{
					nxx = t13x;
					nxy = t13y;
					nxz = t13z;

					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					if(u13<0.0)
					{
						nxx = -nxx;
						nxy = -nxy;
						nxz = -nxz;
					}
				}
				else
				{
					//u12==0.0
					//u13!=0.0
					//v12!=0.0
					//v13!=0.0
					//remove v from 13
					float scale12 = -v13/v12;
					float utemp = u13+scale12*u12;
					nxx = t12x*scale12;
					nxy = t12y*scale12;
					nxz = t12z*scale12;

					nxx += t13x;
					nxy += t13y;
					nxz += t13z;
					
					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					if(utemp<0.0)
					{
						nxx = -nxx;
						nxy = -nxy;
						nxz = -nxz;
					}
				}
			}
		}
		else
		{
			//u12!=0.0
			if(java.lang.Math.abs(v12)<epsilon)
			{
				nxx = t12x;
				nxy = t12y;
				nxz = t12z;

				nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
				if(nxyz<epsilon)
				{
					nxx = 1.0f;
//					nxx = 1.0f;
//					nxy = 0.0f;
//					nxz = 0.0f;
				}
				else
				{
					nxyz = (float)Math.sqrt(nxyz);
					nxx /= nxyz;
					nxy /= nxyz;
					nxz /= nxyz;
				}

				if(u12<0.0)
				{
					nxx = -nxx;
					nxy = -nxy;
					nxz = -nxz;
				}

				//u12!=0.0
				//v12==0.0
				if(java.lang.Math.abs(v13)<epsilon)
				{
					//System.out.println("Degenerate");
					//Degenerate
					nxx = wxf2-wxf0;
					nxy = wyf2-wyf0;
					nxz = wzf2-wzf0;

					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					nyx = nzy*nxz-nxy*nzz;
					nyy = nzz*nxx-nxz*nzx;
					nyz = nzx*nxy-nxx*nzy;
				}
				else
				if(java.lang.Math.abs(u13)<epsilon)
				{
					//u12!=0.0
					//v12==0.0
					//u13==0.0
					//v13!=0.0

					nyx = t13x;
					nyy = t13y;
					nyz = t13z;

					nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
					if(nxyz<epsilon)
					{
						nyx = 1.0f;
//						nyx = 0.0f;
//						nyy = 1.0f;
//						nyz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nyx /= nxyz;
						nyy /= nxyz;
						nyz /= nxyz;
					}

					if(v13<0.0)
					{
						nyx = -nyx;
						nyy = -nyy;
						nyz = -nyz;
					}
				}
				else
				{
					//u12!=0.0
					//v12==0.0
					//u13!=0.0
					//v13!=0.0

					//Remove u from 13
					float scale12 = -u13/u12;
					float vtemp = v13+scale12*v12;

					nyx = t12x*scale12;
					nyy = t12y*scale12;
					nyz = t12z*scale12;

					nyx += t13x;
					nyy += t13y;
					nyz += t13z;

					nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
					if(nxyz<epsilon)
					{
						nyx = 1.0f;
//						nyx = 0.0f;
//						nyy = 1.0f;
//						nyz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nyx /= nxyz;
						nyy /= nxyz;
						nyz /= nxyz;
					}

					if(vtemp<0.0)
					{
						nyx = -nyx;
						nyy = -nyy;
						nyz = -nyz;
					}
				}
			}
			else
			if(java.lang.Math.abs(v13)<epsilon)
			{
				//u12!=0.0
				//v12!=0.0
				//v13==0.0
				
				nxx = t13x;
				nxy = t13y;
				nxz = t13z;
				
				nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
				if(nxyz<epsilon)
				{
					nxx = 1.0f;
//					nxx = 1.0f;
//					nxy = 0.0f;
//					nxz = 0.0f;
				}
				else
				{
					nxyz = (float)Math.sqrt(nxyz);
					nxx /= nxyz;
					nxy /= nxyz;
					nxz /= nxyz;
				}

				if(u13<0.0)
				{
					nxx = -nxx;
					nxy = -nxy;
					nxz = -nxz;
				}

				if(java.lang.Math.abs(u13)<epsilon)
				{
					//System.out.println("Degenerate");
					//Degenerate
					nxx = wxf2 - wxf0;
					nxy = wyf2 - wyf0;
					nxz = wzf2 - wzf0;

					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					nyx = nzy*nxz-nxy*nzz;
					nyy = nzz*nxx-nxz*nzx;
					nyz = nzx*nxy-nxx*nzy;
				}
				else
				{
					//Remove u from 12
					float scale13 = -u12/u13;
					float vtemp = v12+scale13*v13;
					nyx = t13x*scale13;
					nyy = t13y*scale13;
					nyz = t13z*scale13;

					nyx += t12x;
					nyy += t12y;
					nyz += t12z;

					nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
					if(nxyz<epsilon)
					{
						nyx = 1.0f;
//						nyx = 0.0f;
//						nyy = 1.0f;
//						nyz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nyx /= nxyz;
						nyy /= nxyz;
						nyz /= nxyz;
					}

					if(vtemp<0.0)
					{
						nyx = -nyx;
						nyy = -nyy;
						nyz = -nyz;
					}
				}
			}
			else
			{
				//u12!=0.0
				//v12!=0.0
				//v13!=0.0

				//remove v from 12
				float scale13 = -v12/v13;
				float utemp = u12+scale13*u13;

				nxx = t13x*scale13;
				nxy = t13y*scale13;
				nxz = t13z*scale13;

				nxx += t12x;
				nxy += t12y;
				nxz += t12z;
				
				nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
				if(nxyz<epsilon)
				{
					nxx = 1.0f;
//					nxx = 1.0f;
//					nxy = 0.0f;
//					nxz = 0.0f;
				}
				else
				{
					nxyz = (float)Math.sqrt(nxyz);
					nxx /= nxyz;
					nxy /= nxyz;
					nxz /= nxyz;
				}

				if(utemp<0.0)
				{
					nxx = -nxx;
					nxy = -nxy;
					nxz = -nxz;
				}

				if(java.lang.Math.abs(u13)<epsilon)
				{
					//remove u from 12
					nyx = t13x;
					nyy = t13y;
					nyz = t13z;

					nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
					if(nxyz<epsilon)
					{
						nyy = 1.0f;
//						nyx = 0.0f;
//						nyy = 1.0f;
//						nyz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nyx /= nxyz;
						nyy /= nxyz;
						nyz /= nxyz;
					}

					if(v13<0.0)
					{
						nyx = -nyx;
						nyy = -nyy;
						nyz = -nyz;
					}
				}
				else
				{
					//u12!=0.0
					//v12!=0.0
					//u13!=0.0
					//v13!=0.0

					//Remove u from 12
					scale13 = -u12/u13;
					float vtemp = v12+scale13*v13;
					
					nyx = t13x*scale13;
					nyy = t13y*scale13;
					nyz = t13z*scale13;

					nyx += t12x;
					nyy += t12y;
					nyz += t12z;

					nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
					if(nxyz<epsilon)
					{
						nyx = 1.0f;
//						nyx = 0.0f;
//						nyy = 1.0f;
//						nyz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nyx /= nxyz;
						nyy /= nxyz;
						nyz /= nxyz;
					}

					if(vtemp<0.0)
					{
						nyx = -nyx;
						nyy = -nyy;
						nyz = -nyz;
					}
				}
			}
		}
		
//		triangleTextureSpaceMatricies.value[i*9+0] = nxx;
//		triangleTextureSpaceMatricies.value[i*9+1] = nxy;
//		triangleTextureSpaceMatricies.value[i*9+2] = nxz;
//		triangleTextureSpaceMatricies.value[i*9+3] = nyx;
//		triangleTextureSpaceMatricies.value[i*9+4] = nyy;
//		triangleTextureSpaceMatricies.value[i*9+5] = nyz;
//		triangleTextureSpaceMatricies.value[i*9+6] = nzx;
//		triangleTextureSpaceMatricies.value[i*9+7] = nzy;
//		triangleTextureSpaceMatricies.value[i*9+8] = nzz;
		
		int sw = surfaceMatrices.width;
		surfaceMatrices.value[(polyamat)*sw+0]+=nxx;
		surfaceMatrices.value[(polybmat)*sw+0]+=nxx;
		surfaceMatrices.value[(polycmat)*sw+0]+=nxx;
		surfaceMatrices.value[(polyamat)*sw+1]+=nxy;
		surfaceMatrices.value[(polybmat)*sw+1]+=nxy;
		surfaceMatrices.value[(polycmat)*sw+1]+=nxy;
		surfaceMatrices.value[(polyamat)*sw+2]+=nxz;
		surfaceMatrices.value[(polybmat)*sw+2]+=nxz;
		surfaceMatrices.value[(polycmat)*sw+2]+=nxz;

		surfaceMatrices.value[(polyamat)*sw+3]+=nyx;
		surfaceMatrices.value[(polybmat)*sw+3]+=nyx;
		surfaceMatrices.value[(polycmat)*sw+3]+=nyx;
		surfaceMatrices.value[(polyamat)*sw+4]+=nyy;
		surfaceMatrices.value[(polybmat)*sw+4]+=nyy;
		surfaceMatrices.value[(polycmat)*sw+4]+=nyy;
		surfaceMatrices.value[(polyamat)*sw+5]+=nyz;
		surfaceMatrices.value[(polybmat)*sw+5]+=nyz;
		surfaceMatrices.value[(polycmat)*sw+5]+=nyz;
	}
	
	public static void calculateTriMeshMatrices(GeneralMatrixInt triIndexes,
												GeneralMatrixFloat polygonVerts,
												GeneralMatrixFloat polygonUVs,
												GeneralMatrixFloat surfaceMatrices)
	{
		float epsilon = 0.000000001f;
		
		int tripos = 0;
		int trimat = 0;
		int triuv = 0;
		int startTri = 0;
		
		int triSize = triIndexes.height;

		GeneralMatrixFloat triangleTextureSpaceMatricies = new GeneralMatrixFloat(3,triSize*3);
		
		for(int i=0;i<triSize;i++)
		{
			int tri = startTri+i;
			int polyamat = triIndexes.value[tri*9+2]; int polybmat = triIndexes.value[tri*9+5]; int polycmat = triIndexes.value[tri*9+8];

			surfaceMatrices.value[(polyamat+trimat)*9+0] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+0] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+0] = 0.0f;
			surfaceMatrices.value[(polyamat+trimat)*9+1] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+1] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+1] = 0.0f;
			surfaceMatrices.value[(polyamat+trimat)*9+2] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+2] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+2] = 0.0f;

			surfaceMatrices.value[(polyamat+trimat)*9+3] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+3] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+3] = 0.0f;
			surfaceMatrices.value[(polyamat+trimat)*9+4] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+4] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+4] = 0.0f;
			surfaceMatrices.value[(polyamat+trimat)*9+5] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+5] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+5] = 0.0f;

			surfaceMatrices.value[(polyamat+trimat)*9+6] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+6] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+6] = 0.0f;
			surfaceMatrices.value[(polyamat+trimat)*9+7] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+7] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+7] = 0.0f;
			surfaceMatrices.value[(polyamat+trimat)*9+8] = 0.0f;
			surfaceMatrices.value[(polybmat+trimat)*9+8] = 0.0f;
			surfaceMatrices.value[(polycmat+trimat)*9+8] = 0.0f;
		}
		
		for(int i=0;i<triSize;i++)
		{
			int tri = startTri+i;
			int polyapos = triIndexes.value[tri*9+0]; int polybpos = triIndexes.value[tri*9+3]; int polycpos = triIndexes.value[tri*9+6];
			int polyauv = triIndexes.value[tri*9+1]; int polybuv = triIndexes.value[tri*9+4]; int polycuv = triIndexes.value[tri*9+7];
			int polyamat = triIndexes.value[tri*9+2]; int polybmat = triIndexes.value[tri*9+5]; int polycmat = triIndexes.value[tri*9+8];

			float wxf0 = polygonVerts.value[(polyapos+tripos)*3+0]*100.0f;
			float wxf1 = polygonVerts.value[(polybpos+tripos)*3+0]*100.0f;
			float wxf2 = polygonVerts.value[(polycpos+tripos)*3+0]*100.0f;
			float wyf0 = polygonVerts.value[(polyapos+tripos)*3+1]*100.0f;
			float wyf1 = polygonVerts.value[(polybpos+tripos)*3+1]*100.0f;
			float wyf2 = polygonVerts.value[(polycpos+tripos)*3+1]*100.0f;
			float wzf0 = polygonVerts.value[(polyapos+tripos)*3+2]*100.0f;
			float wzf1 = polygonVerts.value[(polybpos+tripos)*3+2]*100.0f;
			float wzf2 = polygonVerts.value[(polycpos+tripos)*3+2]*100.0f;

			float t12x = wxf1-wxf0;
			float t12y = wyf1-wyf0;
			float t12z = wzf1-wzf0;

			float t13x = wxf2-wxf0;
			float t13y = wyf2-wyf0;
			float t13z = wzf2-wzf0;

			float nzx = t12y*t13z-t13y*t12z;
			float nzy = t12z*t13x-t13z*t12x;
			float nzz = t12x*t13y-t13x*t12y;

			float nxyz = nzx*nzx+nzy*nzy+nzz*nzz;
			if(nxyz<epsilon)
			{
				nzx = 1.0f;
//				nzx = 0.0f;
//				nzy = 0.0f;
//				nzz = 1.0f;
			}
			else
			{
				nxyz = (float)Math.sqrt(nxyz);
				nzx /= nxyz;
				nzy /= nxyz;
				nzz /= nxyz;
			}

			surfaceMatrices.value[(polyamat+trimat)*9+6]+=nzx;
			surfaceMatrices.value[(polybmat+trimat)*9+6]+=nzx;
			surfaceMatrices.value[(polycmat+trimat)*9+6]+=nzx;
			surfaceMatrices.value[(polyamat+trimat)*9+7]+=nzy;
			surfaceMatrices.value[(polybmat+trimat)*9+7]+=nzy;
			surfaceMatrices.value[(polycmat+trimat)*9+7]+=nzy;
			surfaceMatrices.value[(polyamat+trimat)*9+8]+=nzz;
			surfaceMatrices.value[(polybmat+trimat)*9+8]+=nzz;
			surfaceMatrices.value[(polycmat+trimat)*9+8]+=nzz;
			
			float u1 = polygonUVs.value[(polyauv+triuv)*2+0];
			float u2 = polygonUVs.value[(polybuv+triuv)*2+0];
			float u3 = polygonUVs.value[(polycuv+triuv)*2+0];
			float v1 = polygonUVs.value[(polyauv+triuv)*2+1];
			float v2 = polygonUVs.value[(polybuv+triuv)*2+1];
			float v3 = polygonUVs.value[(polycuv+triuv)*2+1];

			//Generate tangent space normals
			float u12 = u2-u1;
			float u13 = u3-u1;

			float v12 = v2-v1;
			float v13 = v3-v1;
			
			float nxx,nxy,nxz;
			float nyx,nyy,nyz;
			
			//Normal along u is (remove v, choose sign)
			if(Math.abs(u12)<epsilon)
			{
				if(Math.abs(u13)<epsilon)
				{
					//System.out.println("Degenerate");
					//Degenerate
					nxx = wxf2-wxf0;
					nxy = wyf2-wyf0;
					nxz = wzf2-wzf0;
					
					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					nyx = nzy*nxz-nxy*nzz;
					nyy = nzz*nxx-nxz*nzx;
					nyz = nzx*nxy-nxx*nzy;
				}
				else
				if(java.lang.Math.abs(v12)<epsilon)
				{
					//System.out.println("Degenerate");
					//Degenerate
					nxx = wxf2-wxf0;
					nxy = wyf2-wyf0;
					nxz = wzf2-wzf0;
					
					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					nyx = nzy*nxz-nxy*nzz;
					nyy = nzz*nxx-nxz*nzx;
					nyz = nzx*nxy-nxx*nzy;
				}
				else
				{
					//u12==0.0
					//u13!=0.0
					//v12!=0.0
					nyx = t12x;
					nyy = t12y;
					nyz = t12z;

					nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
					if(nxyz<epsilon)
					{
						nyx = 1.0f;
//						nyx = 0.0f;
//						nyy = 1.0f;
//						nyz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nyx /= nxyz;
						nyy /= nxyz;
						nyz /= nxyz;
					}

					if(v12<0.0)
					{
						nyx = -nyx;
						nyy = -nyy;
						nyz = -nyz;
					}
					
					if(java.lang.Math.abs(v13)<epsilon)
					{
						nxx = t13x;
						nxy = t13y;
						nxz = t13z;

						nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
						if(nxyz<epsilon)
						{
							nxx = 1.0f;
//							nxx = 1.0f;
//							nxy = 0.0f;
//							nxz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nxx /= nxyz;
							nxy /= nxyz;
							nxz /= nxyz;
						}

						if(u13<0.0)
						{
							nxx = -nxx;
							nxy = -nxy;
							nxz = -nxz;
						}
					}
					else
					{
						//u12==0.0
						//u13!=0.0
						//v12!=0.0
						//v13!=0.0
						//remove v from 13
						float scale12 = -v13/v12;
						float utemp = u13+scale12*u12;
						nxx = t12x*scale12;
						nxy = t12y*scale12;
						nxz = t12z*scale12;

						nxx += t13x;
						nxy += t13y;
						nxz += t13z;
						
						nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
						if(nxyz<epsilon)
						{
							nxx = 1.0f;
//							nxx = 1.0f;
//							nxy = 0.0f;
//							nxz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nxx /= nxyz;
							nxy /= nxyz;
							nxz /= nxyz;
						}

						if(utemp<0.0)
						{
							nxx = -nxx;
							nxy = -nxy;
							nxz = -nxz;
						}
					}
				}
			}
			else
			{
				//u12!=0.0
				if(java.lang.Math.abs(v12)<epsilon)
				{
					nxx = t12x;
					nxy = t12y;
					nxz = t12z;

					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					if(u12<0.0)
					{
						nxx = -nxx;
						nxy = -nxy;
						nxz = -nxz;
					}

					//u12!=0.0
					//v12==0.0
					if(java.lang.Math.abs(v13)<epsilon)
					{
						//System.out.println("Degenerate");
						//Degenerate
						nxx = wxf2-wxf0;
						nxy = wyf2-wyf0;
						nxz = wzf2-wzf0;

						nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
						if(nxyz<epsilon)
						{
							nxx = 1.0f;
//							nxx = 1.0f;
//							nxy = 0.0f;
//							nxz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nxx /= nxyz;
							nxy /= nxyz;
							nxz /= nxyz;
						}

						nyx = nzy*nxz-nxy*nzz;
						nyy = nzz*nxx-nxz*nzx;
						nyz = nzx*nxy-nxx*nzy;
					}
					else
					if(java.lang.Math.abs(u13)<epsilon)
					{
						//u12!=0.0
						//v12==0.0
						//u13==0.0
						//v13!=0.0

						nyx = t13x;
						nyy = t13y;
						nyz = t13z;

						nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
						if(nxyz<epsilon)
						{
							nyx = 1.0f;
//							nyx = 0.0f;
//							nyy = 1.0f;
//							nyz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nyx /= nxyz;
							nyy /= nxyz;
							nyz /= nxyz;
						}

						if(v13<0.0)
						{
							nyx = -nyx;
							nyy = -nyy;
							nyz = -nyz;
						}
					}
					else
					{
						//u12!=0.0
						//v12==0.0
						//u13!=0.0
						//v13!=0.0

						//Remove u from 13
						float scale12 = -u13/u12;
						float vtemp = v13+scale12*v12;

						nyx = t12x*scale12;
						nyy = t12y*scale12;
						nyz = t12z*scale12;

						nyx += t13x;
						nyy += t13y;
						nyz += t13z;

						nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
						if(nxyz<epsilon)
						{
							nyx = 1.0f;
//							nyx = 0.0f;
//							nyy = 1.0f;
//							nyz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nyx /= nxyz;
							nyy /= nxyz;
							nyz /= nxyz;
						}

						if(vtemp<0.0)
						{
							nyx = -nyx;
							nyy = -nyy;
							nyz = -nyz;
						}
					}
				}
				else
				if(java.lang.Math.abs(v13)<epsilon)
				{
					//u12!=0.0
					//v12!=0.0
					//v13==0.0
					
					nxx = t13x;
					nxy = t13y;
					nxz = t13z;
					
					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					if(u13<0.0)
					{
						nxx = -nxx;
						nxy = -nxy;
						nxz = -nxz;
					}

					if(java.lang.Math.abs(u13)<epsilon)
					{
						//System.out.println("Degenerate");
						//Degenerate
						nxx = wxf2 - wxf0;
						nxy = wyf2 - wyf0;
						nxz = wzf2 - wzf0;

						nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
						if(nxyz<epsilon)
						{
							nxx = 1.0f;
//							nxx = 1.0f;
//							nxy = 0.0f;
//							nxz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nxx /= nxyz;
							nxy /= nxyz;
							nxz /= nxyz;
						}

						nyx = nzy*nxz-nxy*nzz;
						nyy = nzz*nxx-nxz*nzx;
						nyz = nzx*nxy-nxx*nzy;
					}
					else
					{
						//Remove u from 12
						float scale13 = -u12/u13;
						float vtemp = v12+scale13*v13;
						nyx = t13x*scale13;
						nyy = t13y*scale13;
						nyz = t13z*scale13;

						nyx += t12x;
						nyy += t12y;
						nyz += t12z;

						nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
						if(nxyz<epsilon)
						{
							nyx = 1.0f;
//							nyx = 0.0f;
//							nyy = 1.0f;
//							nyz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nyx /= nxyz;
							nyy /= nxyz;
							nyz /= nxyz;
						}

						if(vtemp<0.0)
						{
							nyx = -nyx;
							nyy = -nyy;
							nyz = -nyz;
						}
					}
				}
				else
				{
					//u12!=0.0
					//v12!=0.0
					//v13!=0.0

					//remove v from 12
					float scale13 = -v12/v13;
					float utemp = u12+scale13*u13;

					nxx = t13x*scale13;
					nxy = t13y*scale13;
					nxz = t13z*scale13;

					nxx += t12x;
					nxy += t12y;
					nxz += t12z;
					
					nxyz = nxx*nxx+nxy*nxy+nxz*nxz;
					if(nxyz<epsilon)
					{
						nxx = 1.0f;
//						nxx = 1.0f;
//						nxy = 0.0f;
//						nxz = 0.0f;
					}
					else
					{
						nxyz = (float)Math.sqrt(nxyz);
						nxx /= nxyz;
						nxy /= nxyz;
						nxz /= nxyz;
					}

					if(utemp<0.0)
					{
						nxx = -nxx;
						nxy = -nxy;
						nxz = -nxz;
					}

					if(java.lang.Math.abs(u13)<epsilon)
					{
						//remove u from 12
						nyx = t13x;
						nyy = t13y;
						nyz = t13z;

						nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
						if(nxyz<epsilon)
						{
							nyy = 1.0f;
//							nyx = 0.0f;
//							nyy = 1.0f;
//							nyz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nyx /= nxyz;
							nyy /= nxyz;
							nyz /= nxyz;
						}

						if(v13<0.0)
						{
							nyx = -nyx;
							nyy = -nyy;
							nyz = -nyz;
						}
					}
					else
					{
						//u12!=0.0
						//v12!=0.0
						//u13!=0.0
						//v13!=0.0

						//Remove u from 12
						scale13 = -u12/u13;
						float vtemp = v12+scale13*v13;
						
						nyx = t13x*scale13;
						nyy = t13y*scale13;
						nyz = t13z*scale13;

						nyx += t12x;
						nyy += t12y;
						nyz += t12z;

						nxyz = nyx*nyx+nyy*nyy+nyz*nyz;
						if(nxyz<epsilon)
						{
							nyx = 1.0f;
//							nyx = 0.0f;
//							nyy = 1.0f;
//							nyz = 0.0f;
						}
						else
						{
							nxyz = (float)Math.sqrt(nxyz);
							nyx /= nxyz;
							nyy /= nxyz;
							nyz /= nxyz;
						}

						if(vtemp<0.0)
						{
							nyx = -nyx;
							nyy = -nyy;
							nyz = -nyz;
						}
					}
				}
			}
			
			triangleTextureSpaceMatricies.value[i*9+0] = nxx;
			triangleTextureSpaceMatricies.value[i*9+1] = nxy;
			triangleTextureSpaceMatricies.value[i*9+2] = nxz;
			triangleTextureSpaceMatricies.value[i*9+3] = nyx;
			triangleTextureSpaceMatricies.value[i*9+4] = nyy;
			triangleTextureSpaceMatricies.value[i*9+5] = nyz;
			triangleTextureSpaceMatricies.value[i*9+6] = nzx;
			triangleTextureSpaceMatricies.value[i*9+7] = nzy;
			triangleTextureSpaceMatricies.value[i*9+8] = nzz;
			
			surfaceMatrices.value[(polyamat+trimat)*9+0]+=nxx;
			surfaceMatrices.value[(polybmat+trimat)*9+0]+=nxx;
			surfaceMatrices.value[(polycmat+trimat)*9+0]+=nxx;
			surfaceMatrices.value[(polyamat+trimat)*9+1]+=nxy;
			surfaceMatrices.value[(polybmat+trimat)*9+1]+=nxy;
			surfaceMatrices.value[(polycmat+trimat)*9+1]+=nxy;
			surfaceMatrices.value[(polyamat+trimat)*9+2]+=nxz;
			surfaceMatrices.value[(polybmat+trimat)*9+2]+=nxz;
			surfaceMatrices.value[(polycmat+trimat)*9+2]+=nxz;

			surfaceMatrices.value[(polyamat+trimat)*9+3]+=nyx;
			surfaceMatrices.value[(polybmat+trimat)*9+3]+=nyx;
			surfaceMatrices.value[(polycmat+trimat)*9+3]+=nyx;
			surfaceMatrices.value[(polyamat+trimat)*9+4]+=nyy;
			surfaceMatrices.value[(polybmat+trimat)*9+4]+=nyy;
			surfaceMatrices.value[(polycmat+trimat)*9+4]+=nyy;
			surfaceMatrices.value[(polyamat+trimat)*9+5]+=nyz;
			surfaceMatrices.value[(polybmat+trimat)*9+5]+=nyz;
			surfaceMatrices.value[(polycmat+trimat)*9+5]+=nyz;
		}

		normaliseSurfaceMatrices(surfaceMatrices);		
	}
	
	public static void normaliseSurfaceMatrices(GeneralMatrixFloat surfaceMatrices)
	{
		float epsilon = 0.000000001f;
		
		int trimat = 0;		
		int matSize = surfaceMatrices.height;

		for(int i=0;i<matSize;i++)
		{
			int mat = trimat+i;
			
			float nx = surfaceMatrices.value[(mat)*9+6];
			float ny = surfaceMatrices.value[(mat)*9+7];
			float nz = surfaceMatrices.value[(mat)*9+8];
			
			float tx = surfaceMatrices.value[(mat)*9+0];
			float ty = surfaceMatrices.value[(mat)*9+1];
			float tz = surfaceMatrices.value[(mat)*9+2];

			float nxyz = nx*nx+ny*ny+nz*nz;
			if(nxyz<epsilon)
			{
//				nx = 0.0f;
//				ny = 0.0f;
				nz = 1.0f;
			}
			else
			{
				nxyz = (float)Math.sqrt(nxyz);
				nx /= nxyz;
				ny /= nxyz;
				nz /= nxyz;
			}
//			float txyz = tx*tx+ty*ty+tz*tz;
//			if(txyz<epsilon)
//			{
//				tx = 1.0f;
//				ty = 0.0f;
//				tz = 0.0f;
//			}
//			else
//			{
//				txyz = (float)Math.sqrt(txyz);
//				tx /= txyz;
//				ty /= txyz;
//				tz /= txyz;
//			}
			
			//Calc binormal
			float bx = ty*nz-ny*tz;
			float by = tz*nx-nz*tx;
			float bz = tx*ny-nx*ty;
			//Normalise
			float sum = (float)Math.sqrt(bx*bx+by*by+bz*bz);
			if(sum<GeneralMatrixFloat.EPSILON)
			{
				//Argh find orthonormal basis
				//n Cross y=1
				bx = nz;
				by = 0.0f;
				bz = -nx;
				sum = (float)Math.sqrt(bx*bx+by*by+bz*bz);
				if(sum<GeneralMatrixFloat.EPSILON)
				{
					//n Cross x=1
					bx = 0.0f;
					by = -nz;
					bz = ny;				
					sum = (float)Math.sqrt(bx*bx+by*by+bz*bz);
				}
			}

			bx /= sum;
			by /= sum;
			bz /= sum;
			
			//Calc tangent
			tx = by*nz-ny*bz;
			ty = bz*nx-nz*bx;
			tz = bx*ny-nx*by;

			surfaceMatrices.value[(mat)*9+0] = tx;
			surfaceMatrices.value[(mat)*9+1] = ty;
			surfaceMatrices.value[(mat)*9+2] = tz;			

			surfaceMatrices.value[(mat)*9+3] = bx;
			surfaceMatrices.value[(mat)*9+4] = by;
			surfaceMatrices.value[(mat)*9+5] = bz;

			surfaceMatrices.value[(mat)*9+6] = nx;
			surfaceMatrices.value[(mat)*9+7] = ny;
			surfaceMatrices.value[(mat)*9+8] = nz;			
		}
	}
}
