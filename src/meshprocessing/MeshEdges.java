/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package meshprocessing;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;

public class MeshEdges 
{
	public static int QUAD_EDGE_TOP = 0;
	public static int QUAD_EDGE_RIGHT = 1;
	public static int QUAD_EDGE_BOTTOM = 2;
	public static int QUAD_EDGE_LEFT = 3;
	
	public static void SplitIntoRegions(
			int[][] edges,
			GeneralMatrixInt neighbouringquads,
			GeneralMatrixObject segments)
	{
		//invalidate the neighbours that cross one of the edge lists
		for(int edgei=0;edgei<edges.length;edgei++)
		{
			int[] segmentedges = edges[edgei];
			for(int ei=0;ei<segmentedges.length;ei++)
			{
				int quad = segmentedges[ei]/4;
				int qe = segmentedges[ei]%4;
				
				int otherquad = neighbouringquads.value[quad*4+qe];
				if(otherquad!=-1)
				{
					for(int oqi=0;oqi<4;oqi++)
					{
						if(neighbouringquads.value[otherquad*4+oqi]==quad)
						{
							neighbouringquads.value[otherquad*4+oqi] = -1;
							break;
						}
					}
				}
				else
				{
					System.out.println("Eh!");
				}
				neighbouringquads.value[quad*4+qe] = -1;
			}
		}
		//segment the mesh into regions using the edges
		GeneralMatrixInt segmentedMesh = new GeneralMatrixInt(1,neighbouringquads.height);
		segmentedMesh.clear(-1);
		
		GeneralMatrixInt quadqueue = new GeneralMatrixInt(1);
		
		while(true)
		{
			System.out.println("*"+segments.height);
			
			GeneralMatrixInt segment = null;

			quadqueue.height = 0;
			//find the first unsegmented mesh
			for(int qi=0;qi<neighbouringquads.height;qi++)
			{
				if(segmentedMesh.value[qi]==-1)
				{
					quadqueue.push_back(qi);
					segment = new GeneralMatrixInt(1);
					segmentedMesh.value[qi] = segments.height;
					segment.push_back(qi);						
					//System.out.println(""+qi+",");
					break;
				}
			}
			
			if(quadqueue.height==0)
				break;
			
			
			while(quadqueue.height!=0)
			{
				int quad = quadqueue.value[quadqueue.height-1];
				quadqueue.height--;
				for(int oqi=0;oqi<4;oqi++)
				{
					int oquad = neighbouringquads.value[quad*4+oqi];
					if((oquad!=-1)&&(segmentedMesh.value[oquad]==-1))
					{
						quadqueue.push_back(oquad);
						segmentedMesh.value[oquad] = segments.height;
						segment.push_back(oquad);			
						//System.out.println(""+oquad+",");
					}
				}
				//
			}
			
			segments.push_back(segment);
		}
	}
	
	public static void calcQuadEdges(int[] quadsi,int nqs,int npos,
			int qstride,int pstride,
			GeneralMatrixInt edges,
			GeneralMatrixInt edgesUVs,
			GeneralMatrixInt edgesQuads,
			GeneralMatrixInt quadsEdges,
			GeneralMatrixInt neighbouringquads)
	{
		GeneralMatrixInt edgesfirst = new GeneralMatrixInt(1);
		
		GeneralMatrixInt edgeLookup = new GeneralMatrixInt(6*2,npos);
		edgeLookup.clear(-1);
		
		edges.height = 0;
		edgesUVs.height = 0;
		edgesQuads.height = 0;
		quadsEdges.setDimensions(4, nqs);
		neighbouringquads.setDimensions(4, nqs);
		
		//build the edge table for the mesh
		for(int qi=0;qi<nqs;qi++)
		{
			if((qi%10000)==0)
				System.out.println("calcQuadEdges:"+qi+"/"+nqs);
			for(int ei=0;ei<4;ei++)
			{
				int v0 = quadsi[qi*qstride+ei*pstride];
				int v1 = quadsi[qi*qstride+((ei+1)%4)*pstride];

				int uv0 = quadsi[qi*qstride+ei*pstride+1];
				int uv1 = quadsi[qi*qstride+((ei+1)%4)*pstride+1];
				if(v1<v0)
				{
					int temp = v0;
					v0 = v1;
					v1 = temp;
					
					temp = uv0;
					uv0 = uv1;
					uv1 = temp;
				}

				
				if(v0>npos)
				{
					System.out.println("err");
				}
				
				int existing = -1;
				int eoff = v0*edgeLookup.width+0;
				boolean toomanyedges = true;
				for(int li=0;li<6;li++)
				{
					int lv1 = edgeLookup.value[eoff+li*2+0];
					if(lv1==-1)
					{
						eoff+=li*2;
						toomanyedges = false;
						break;
					}
					else
					if(lv1==v1)
					{
						existing = edgeLookup.value[eoff+li*2+1];
						toomanyedges = false;
						break;
					}
				}
				if(toomanyedges)
					existing = edges.find(v0, v1);
				if(existing==-1)
				{
					edges.push_back_row(v0, v1);
					if(edgesUVs.width==2)
						edgesUVs.push_back_row(uv0, uv1);
					else
						edgesUVs.push_back_row(uv0, uv1, -1, -1);
					edgesQuads.push_back_row(qi,ei,-1,-1);
					edgesfirst.push_back(ei);
					quadsEdges.value[qi*4+ei] = edges.height-1;
					if(!toomanyedges)
					{
						edgeLookup.value[eoff+0] = v1;
						edgeLookup.value[eoff+1] = edges.height-1;
					}
				}
				else
				{
					//existing /= 2;
					//both edges connected, set the neighbour of this quad
					int neighbour = edgesQuads.value[existing*4+0];
					int neighboure = edgesfirst.value[existing];
					neighbouringquads.value[qi*4+ei] = neighbour;
					neighbouringquads.value[neighbour*4+neighboure] = qi;
					edgesQuads.value[existing*4+2] = qi;
					edgesQuads.value[existing*4+3] = ei;
					quadsEdges.value[qi*4+ei] = existing;
					
					if(edgesUVs.width>2)
					{
						int ouv0 = edgesUVs.value[existing*4+0];
						int ouv1 = edgesUVs.value[existing*4+1];
						if((ouv0!=uv0)||(ouv1!=uv1))
						{
							edgesUVs.value[existing*4+2] = uv0;
							edgesUVs.value[existing*4+3] = uv1;
						}
					}
				}
			}
		}
//		int s;
//		long[][] compr;
//		
//		s = neighbouringquads.width*neighbouringquads.height;
//		 compr = DataCompressionLossy.compressLs(neighbouringquads.value, s, 8207);
//
//		 for(int lsi=0;lsi<compr.length;lsi++)
//		 {
//			 String out = "";
//			 for(int li=0;li<compr[lsi].length;li++)
//			 {
//				 out += compr[lsi][li]+"L,\n";
//				 //System.out.println(""+compr[li]+"L,");
//			 }
//			 TextImportExport.saveTextFile(root+"nquads"+lsi+".txt", out);
//		 }
	}
	
	public static void calcQuadEdges(GeneralMatrixInt quads,GeneralMatrixInt quaduvs,
			GeneralMatrixInt edges,
			GeneralMatrixInt edgesUVs,
			GeneralMatrixInt edgesQuads,
			GeneralMatrixInt quadsEdges,
			GeneralMatrixInt neighbouringquads)
	{
		GeneralMatrixInt edgesfirst = new GeneralMatrixInt(1);
		
		GeneralMatrixInt edgeLookup = new GeneralMatrixInt(6*2,quads.height*4);
		edgeLookup.clear(-1);
		
		edges.height = 0;
		edgesUVs.height = 0;
		edgesQuads.height = 0;
		quadsEdges.setDimensions(4, quads.height);
		neighbouringquads.setDimensions(4, quads.height);
		
		//build the edge table for the mesh
		for(int qi=0;qi<quads.height;qi++)
		{
			if((qi%10000)==0)
				System.out.println("calcQuadEdges:"+qi+"/"+quads.height);
			for(int ei=0;ei<4;ei++)
			{
				int v0 = quads.value[qi*4+ei];
				int v1 = quads.value[qi*4+((ei+1)%4)];

				int uv0 = quaduvs.value[qi*4+ei];
				int uv1 = quaduvs.value[qi*4+((ei+1)%4)];
				if(v1<v0)
				{
					int temp = v0;
					v0 = v1;
					v1 = temp;
					
					temp = uv0;
					uv0 = uv1;
					uv1 = temp;
				}

				
				if(v0>edgeLookup.height)
				{
					System.out.println("err");
				}
				
				int existing = -1;
				int eoff = v0*edgeLookup.width+0;
				boolean toomanyedges = true;
				for(int li=0;li<6;li++)
				{
					int lv1 = edgeLookup.value[eoff+li*2+0];
					if(lv1==-1)
					{
						eoff+=li*2;
						toomanyedges = false;
						break;
					}
					else
					if(lv1==v1)
					{
						existing = edgeLookup.value[eoff+li*2+1];
						toomanyedges = false;
						break;
					}
				}
				if(toomanyedges)
					existing = edges.find(v0, v1);
				if(existing==-1)
				{
					edges.push_back_row(v0, v1);
					if(edgesUVs.width==2)
						edgesUVs.push_back_row(uv0, uv1);
					else
						edgesUVs.push_back_row(uv0, uv1, -1, -1);
					edgesQuads.push_back_row(qi,ei,-1,-1);
					edgesfirst.push_back(ei);
					quadsEdges.value[qi*4+ei] = edges.height-1;
					if(!toomanyedges)
					{
						edgeLookup.value[eoff+0] = v1;
						edgeLookup.value[eoff+1] = edges.height-1;
					}
				}
				else
				{
					//existing /= 2;
					//both edges connected, set the neighbour of this quad
					int neighbour = edgesQuads.value[existing*4+0];
					int neighboure = edgesfirst.value[existing];
					neighbouringquads.value[qi*4+ei] = neighbour;
					neighbouringquads.value[neighbour*4+neighboure] = qi;
					edgesQuads.value[existing*4+2] = qi;
					edgesQuads.value[existing*4+3] = ei;
					quadsEdges.value[qi*4+ei] = existing;
					
					if(edgesUVs.width>2)
					{
						int ouv0 = edgesUVs.value[existing*4+0];
						int ouv1 = edgesUVs.value[existing*4+1];
						if((ouv0!=uv0)||(ouv1!=uv1))
						{
							edgesUVs.value[existing*4+2] = uv0;
							edgesUVs.value[existing*4+3] = uv1;
						}
					}
				}
			}
		}
	}

	public static void calcTriEdges(int[] trisi,int nqs,
			int qstride,int pstride,
			GeneralMatrixInt edges,
			GeneralMatrixInt edgesTris,
			GeneralMatrixInt neighbouringtris)
	{
		GeneralMatrixInt edgesfirst = new GeneralMatrixInt(1);
		
		//build the edge table for the mesh
		for(int qi=0;qi<nqs;qi++)
		{
			for(int ei=0;ei<3;ei++)
			{
				int v0 = trisi[qi*qstride+ei*pstride];
				int v1 = trisi[qi*qstride+((ei+1)%3)*pstride];
				if(v1<v0)
				{
					int temp = v0;
					v0 = v1;
					v1 = temp;
				}
				int existing = edges.find(v0, v1);
				if(existing==-1)
				{
					edges.push_back_row(v0, v1);
					edgesTris.push_back_row(qi,ei,-1,-1);
					edgesfirst.push_back(ei);
				}
				else
				{
					//existing /= 2;
					//both edges connected, set the neighbour of this quad
					int neighbour = edgesTris.value[existing*4+0];
					int neighboure = edgesfirst.value[existing];
					neighbouringtris.value[qi*3+ei] = neighbour;
					neighbouringtris.value[neighbour*3+neighboure] = qi;
					edgesTris.value[existing*4+2] = qi;
					edgesTris.value[existing*4+3] = ei;
				}
			}
		}
//		int s;
//		long[][] compr;
//		
//		s = neighbouringtris.width*neighbouringtris.height;
//		 compr = DataCompressionLossy.compressLs(neighbouringtris.value, s, 8207);
//
//		 for(int lsi=0;lsi<compr.length;lsi++)
//		 {
//			 String out = "";
//			 for(int li=0;li<compr[lsi].length;li++)
//			 {
//				 out += compr[lsi][li]+"L,\n";
//				 //System.out.println(""+compr[li]+"L,");
//			 }
//			 TextImportExport.saveTextFile(root+"ntris"+lsi+".txt", out);
//		 }
	}
	
	public static int addEdge(int v1,int v2,GeneralMatrixInt eIndexes,int offset,int size)
	{
		if(v1>v2)
		{
			int temp = v2;
			v2 = v1;
			v1 = temp;
		}
		
		//See if edge in list already
		int e1,e2;
		int existing = -1;
		for(int j=0;j<size;j++)
		{
			e1 = eIndexes.value[(j+offset)*2+0];
			e2 = eIndexes.value[(j+offset)*2+1];
			if((e1==v1)&&(e2==v2))
			{
				existing = j;
				break;
			}
		}
		if(existing == -1)
		{
			eIndexes.appendRow();
			eIndexes.value[(eIndexes.height-1)*2+0] = v1;
			eIndexes.value[(eIndexes.height-1)*2+1] = v2;
			existing = (eIndexes.height-1);
		}
		return existing;
	}	
	
	public static void calculateEdgesOfTrimesh(GeneralMatrixInt triMeshtris,
			GeneralMatrixInt edges)
	{
		int v1,v2;
		//Append the edges for this mesh
		for(int i=0;i<triMeshtris.height;i++)
		{
			v1 = triMeshtris.value[i*9+0*3];
			v2 = triMeshtris.value[i*9+1*3];

			int currentEdgeSize = edges.height;
			MeshEdges.addEdge(v1, v2, edges, 0, currentEdgeSize);

			v1 = triMeshtris.value[i*9+1*3];
			v2 = triMeshtris.value[i*9+2*3];

			currentEdgeSize = edges.height;
			MeshEdges.addEdge(v1, v2, edges, 0, currentEdgeSize);
			
			v1 = triMeshtris.value[i*9+2*3];
			v2 = triMeshtris.value[i*9+0*3];

			currentEdgeSize = edges.height;
			MeshEdges.addEdge(v1, v2, edges, 0, currentEdgeSize);
		}	
	}

	public static void calculateMirroredVertPositions(
			GeneralMatrixFloat triMeshPositions,
			GeneralMatrixInt triMeshMirroredVertPositions)
	{
		triMeshMirroredVertPositions.ensureCapacityNoCopy(triMeshPositions.height);
		triMeshMirroredVertPositions.width = 1;
		triMeshMirroredVertPositions.height = triMeshPositions.height;
		triMeshMirroredVertPositions.clear(-1);
		
		int vst = 0;
		int vsz = triMeshPositions.height;
		for(int j=vst;j<(vst+vsz);j++)
		{
			if(triMeshMirroredVertPositions.value[j]!=-1)
				continue;

			if(Math.abs(triMeshPositions.value[j*3+0])<0.00001f)
			{
				triMeshMirroredVertPositions.value[j] = j;
				continue;
			}
			
			//Otherwise find the point that is closest to its x mirror
			float bestDist = Float.MAX_VALUE;
			int bestk = j;
			for(int k=j+1;k<(vst+vsz);k++)
			{
				float dx = triMeshPositions.value[j*3+0]+triMeshPositions.value[k*3+0];
				float dy = triMeshPositions.value[j*3+1]-triMeshPositions.value[k*3+1];
				float dz = triMeshPositions.value[j*3+2]-triMeshPositions.value[k*3+2];
				float d = dx*dx+dy*dy+dz*dz;
				if(d<bestDist)
				{
					bestDist = d;
					bestk = k;
				}
			}
			triMeshMirroredVertPositions.value[j] = bestk;
			triMeshMirroredVertPositions.value[bestk] = j;
		}
	}
}
