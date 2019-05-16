package meshprocessing;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;

public class EditableMesh 
{
	public GeneralMatrixInt 	triangleIndexes;
	public GeneralMatrixFloat 	positions;

	public GeneralMatrixFloat 	textureCoordinates;
	
//	public GeneralMatrixInt 	quadIndexes;
//	public GeneralMatrixInt 	alltriangleIndexes;

	//Edges
	public GeneralMatrixInt 	edgeIndexes;
	public GeneralMatrixInt 	triangleEdges;

	//Normals
	//public GeneralMatrixFloat 	vertexTextureSpaceMatricies;
	
	//The triangles associated with each edge
	public GeneralMatrixInt 	edgeTriangles;
//	public GeneralMatrixInt 	quadEdges;

//	public Mesh mesh = new Mesh();
	
	public EditableMesh s0;
	
	//DEBUG
	/*
	public void buildQuad(GeneralMatrixFloat bounds,GeneralMatrixFloat centre)
	{
		positions = new GeneralMatrixFloat(3,4);
		textureCoordinates = new GeneralMatrixFloat(2,4);
		positions.set(0,0,bounds.value[0]);
		positions.set(1,0,bounds.value[1]);
		positions.set(2,0,centre.value[2]);
		textureCoordinates.set(0,0,0.0f);
		textureCoordinates.set(1,0,0.0f);

		positions.set(0,1,bounds.value[0+3]);
		positions.set(1,1,bounds.value[1]);
		positions.set(2,1,centre.value[2]);
		textureCoordinates.set(0,1,1.0f);
		textureCoordinates.set(1,1,0.0f);
		
		positions.set(0,2,bounds.value[0+3]);
		positions.set(1,2,bounds.value[1+3]);
		positions.set(2,2,centre.value[2]);
		textureCoordinates.set(0,2,1.0f);
		textureCoordinates.set(1,2,1.0f);

		positions.set(0,3,bounds.value[0]);
		positions.set(1,3,bounds.value[1+3]);
		positions.set(2,3,centre.value[2]);
		textureCoordinates.set(0,3,0.0f);
		textureCoordinates.set(1,3,1.0f);

		triangleIndexes = new GeneralMatrixInt();
		triangleIndexes.width = 3;
		triangleIndexes.height = 0;
		quadIndexes = new GeneralMatrixInt(4,1);
		quadIndexes.set(0,0,0);
		quadIndexes.set(1,0,1);
		quadIndexes.set(2,0,2);
		quadIndexes.set(3,0,3);
	}
	
	public void buildMesh(RenderBuffer tb,RenderBuffer nb)
	{
		if(alltriangleIndexes==null)
			buildAllTriIndexBuffer();
			
		mesh.tb = tb;
		mesh.nb = nb;
		mesh.positions = positions;
		mesh.textureCoordinates = textureCoordinates;
		mesh.triangleIndexes = alltriangleIndexes;
		mesh.vertexTextureSpaceMatricies = new GeneralMatrixFloat(mesh.positions.width,mesh.positions.height*3);

		mesh.computeBounds();
		System.out.println("Calced Bounds");			
		mesh.generateTriangleTextureSpaceMatrixes();

		mesh.computeNormals();
		System.out.println("Calced TTSM");			
		mesh.computeTangentAndBinormals();
		System.out.println("Calced VTSM");					
		mesh.preScaleUVs(false);		
	}
	
	public void updateMeshTangentSpace()
	{
		mesh.updateTriangleTextureSpaceMatrixes();
		mesh.computeNormals();
		mesh.computeTangentAndBinormals();		
	}

	int findVert(float px,float py,float pz)
	{
		float closest = 0.000001f;
		int closestIndex = -1;

		for(int j=0;j<positions.height;j++)
		{
			float rx = positions.get(0,j);
			float ry = positions.get(1,j);
			float rz = positions.get(2,j);
			float dist = (rx-px)*(rx-px)*1.0f+(ry-py)*(ry-py)*1.0f+(rz-pz)*(rz-pz);
			if(dist<closest)
			{
				closest = dist;
				closestIndex = j;
			}
		}
		return closestIndex;
	}
*/
	
//	public static int addEdge(int v1,int v2,GeneralMatrixInt eIndexes,int offset,int size)
//	{
//		if(v1>v2)
//		{
//			int temp = v2;
//			v2 = v1;
//			v1 = temp;
//		}
//		
//		//See if edge in list already
//		int e1,e2;
//		int existing = -1;
//		for(int j=0;j<size;j++)
//		{
//			e1 = eIndexes.value[(j+offset)*2+0];
//			e2 = eIndexes.value[(j+offset)*2+1];
//			if((e1==v1)&&(e2==v2))
//			{
//				existing = j;
//				break;
//			}
//		}
//		if(existing == -1)
//		{
//			eIndexes.appendRow();
//			eIndexes.value[(eIndexes.height-1)*2+0] = v1;
//			eIndexes.value[(eIndexes.height-1)*2+1] = v2;
//			existing = (eIndexes.height-1);
//		}
//		return existing;
//	}
	
	public void buildEdgeIndexes()
	{
		GeneralMatrixInt eIndexes = new GeneralMatrixInt(2);
		triangleEdges = new GeneralMatrixInt(3,triangleIndexes.height);
		int v1,v2;
		//For every tri
		for(int i=0;i<triangleIndexes.height;i++)
		{
			v1 = triangleIndexes.get(0, i);
			v2 = triangleIndexes.get(1, i);			
			triangleEdges.set(0,i,addEdge(v1, v2, eIndexes, 0, eIndexes.height));
			v1 = triangleIndexes.get(1, i);
			v2 = triangleIndexes.get(2, i);			
			triangleEdges.set(1,i,addEdge(v1, v2, eIndexes, 0, eIndexes.height));
			v1 = triangleIndexes.get(2, i);
			v2 = triangleIndexes.get(0, i);			
			triangleEdges.set(2,i,addEdge(v1, v2, eIndexes, 0, eIndexes.height));
		}
		//For every quad
//		quadEdges = new GeneralMatrixInt(4,quadIndexes.height);
//		for(int i=0;i<quadIndexes.height;i++)
//		{
//			v1 = quadIndexes.get(0, i);
//			v2 = quadIndexes.get(1, i);			
//			quadEdges.set(0,i,addEdge(v1, v2, eIndexes));
//			v1 = quadIndexes.get(1, i);
//			v2 = quadIndexes.get(2, i);			
//			quadEdges.set(1,i,addEdge(v1, v2, eIndexes));
//			v1 = quadIndexes.get(2, i);
//			v2 = quadIndexes.get(3, i);			
//			quadEdges.set(2,i,addEdge(v1, v2, eIndexes));
//			v1 = quadIndexes.get(3, i);
//			v2 = quadIndexes.get(0, i);			
//			quadEdges.set(3,i,addEdge(v1, v2, eIndexes));
//		}
		edgeIndexes = eIndexes;
	}
	
	
	public void calcEdgeTriangles()
	{
		edgeTriangles = new GeneralMatrixInt(2,edgeIndexes.height);
		edgeTriangles.clear(-1);
		for(int i=0;i<triangleIndexes.height;i++)
		{
			int e0 = triangleEdges.value[i*3+0];
			int e1 = triangleEdges.value[i*3+1];
			int e2 = triangleEdges.value[i*3+2];
			if(edgeTriangles.value[e0*2+0]==-1)
				edgeTriangles.value[e0*2+0] = i;
			else
				edgeTriangles.value[e0*2+1] = i;
			if(edgeTriangles.value[e1*2+0]==-1)
				edgeTriangles.value[e1*2+0] = i;
			else
				edgeTriangles.value[e1*2+1] = i;
			if(edgeTriangles.value[e2*2+0]==-1)
				edgeTriangles.value[e2*2+0] = i;
			else
				edgeTriangles.value[e2*2+1] = i;
		}
	}
	
	/*
	void buildAllTriIndexBuffer()
	{
		alltriangleIndexes = new GeneralMatrixInt(3,triangleIndexes.height+quadIndexes.height*2);
		
		alltriangleIndexes.copy(triangleIndexes);
		int ati = triangleIndexes.height;
		for(int i=0;i<quadIndexes.height;i++)
		{
			int v1 = quadIndexes.get(0, i);
			int v2 = quadIndexes.get(1, i);
			int v3 = quadIndexes.get(2, i);
			int v4 = quadIndexes.get(3, i);
			
			alltriangleIndexes.set(0,ati,v1);
			alltriangleIndexes.set(1,ati,v2);
			alltriangleIndexes.set(2,ati,v3);
			ati++;
			alltriangleIndexes.set(0,ati,v3);
			alltriangleIndexes.set(1,ati,v4);
			alltriangleIndexes.set(2,ati,v1);
			ati++;
		}
	}
	
	void buildIndexLists(GeneralMatrixFloat quadWorld,GeneralMatrixFloat triWorld,GeneralMatrixInt maskFile)
	{
		ScalableMatrixInt tIndex = new ScalableMatrixInt(3);
		ScalableMatrixInt qIndex = new ScalableMatrixInt(4);

		for(int i=0;i<triWorld.height;i++)
		{
			float px = triWorld.get(0,i);
			float py = triWorld.get(1,i);
			float pz = triWorld.get(2,i);
			
			int v1 = findVert(px,py,pz);
			
			if((v1==-1)||(maskFile.value[v1]==0))
			{
				continue;
			}

			px = triWorld.get(0+3,i);
			py = triWorld.get(1+3,i);
			pz = triWorld.get(2+3,i);
			
			int v2 = findVert(px,py,pz);
			
			if((v2==-1)||(maskFile.value[v2]==0))
			{
				continue;
			}

			px = triWorld.get(0+6,i);
			py = triWorld.get(1+6,i);
			pz = triWorld.get(2+6,i);
			
			int v3 = findVert(px,py,pz);
			
			if((v3==-1)||(maskFile.value[v3]==0))
			{
				continue;
			}
			
			tIndex.appendRow();
			tIndex.set(0,tIndex.height-1,v1);
			tIndex.set(1,tIndex.height-1,v2);
			tIndex.set(2,tIndex.height-1,v3);
		}

		for(int i=0;i<quadWorld.height;i++)
		{
			float px = quadWorld.get(0,i);
			float py = quadWorld.get(1,i);
			float pz = quadWorld.get(2,i);
			
			int v1 = findVert(px,py,pz);
			
			if((v1==-1)||(maskFile.value[v1]==0))
			{
				continue;
			}

			px = quadWorld.get(0+3,i);
			py = quadWorld.get(1+3,i);
			pz = quadWorld.get(2+3,i);
			
			int v2 = findVert(px,py,pz);
			
			if((v2==-1)||(maskFile.value[v2]==0))
			{
				continue;
			}

			px = quadWorld.get(0+6,i);
			py = quadWorld.get(1+6,i);
			pz = quadWorld.get(2+6,i);
			
			int v3 = findVert(px,py,pz);
			
			if((v3==-1)||(maskFile.value[v3]==0))
			{
				continue;
			}
			
			px = quadWorld.get(0+9,i);
			py = quadWorld.get(1+9,i);
			pz = quadWorld.get(2+9,i);
			
			int v4 = findVert(px,py,pz);
			
			if((v4==-1)||(maskFile.value[v4]==0))
			{
				continue;
			}
			
			qIndex.appendRow();
			qIndex.set(0,qIndex.height-1,v1);
			qIndex.set(1,qIndex.height-1,v2);
			qIndex.set(2,qIndex.height-1,v3);
			qIndex.set(3,qIndex.height-1,v4);
		}
		
		triangleIndexes = tIndex;
		quadIndexes = qIndex;
	}
		*/

	public void interpolateMorphTransforms(GeneralMatrixFloat oldMeshTransforms,
									GeneralMatrixFloat meshTransforms)
	{
		meshTransforms.setSubset(oldMeshTransforms, 0, 0);
		
		//Put the edge verts in place
		for(int i=0;i<edgeIndexes.height;i++)
		{
			int v1 = edgeIndexes.get(0, i)*meshTransforms.width;
			int v2 = edgeIndexes.get(1, i)*meshTransforms.width;
			int pi = (positions.height+i)*meshTransforms.width;
			
			for(int j=0;j<meshTransforms.width;j++)
			{
				meshTransforms.value[pi+j] = (oldMeshTransforms.value[v1+j]+oldMeshTransforms.value[v2+j])*0.5f;
			}
		}
	}
	
	void updateSubdivided(boolean updateTanSpace)
	{
		s0.positions.setSubset(positions, 0, 0);

		//Put the edge verts in place
		for(int i=0;i<edgeIndexes.height;i++)
		{
			int v1 = edgeIndexes.get(0, i);
			int v2 = edgeIndexes.get(1, i);
			int pi = positions.height+i;
			float x = (positions.get(0,v1)+positions.get(0,v2))*0.5f;
			float y = (positions.get(1,v1)+positions.get(1,v2))*0.5f;
			float z = (positions.get(2,v1)+positions.get(2,v2))*0.5f;

			s0.positions.set(0, pi, x);
			s0.positions.set(1, pi, y);
			s0.positions.set(2, pi, z);
		}

//		//Put the quad face verts in place
//		for(int i=0;i<quadIndexes.height;i++)
//		{
//			int v1 = quadIndexes.get(0, i);
//			int v2 = quadIndexes.get(1, i);
//			int v3 = quadIndexes.get(2, i);
//			int v4 = quadIndexes.get(3, i);
//			int pi = positions.height+edgeIndexes.height+i;
//			float x = (positions.get(0,v1)+positions.get(0,v2)+positions.get(0,v3)+positions.get(0,v4))*0.25f;
//			float y = (positions.get(1,v1)+positions.get(1,v2)+positions.get(1,v3)+positions.get(1,v4))*0.25f;
//			float z = (positions.get(2,v1)+positions.get(2,v2)+positions.get(2,v3)+positions.get(2,v4))*0.25f;
//
//			s0.positions.set(0, pi, x);
//			s0.positions.set(1, pi, y);
//			s0.positions.set(2, pi, z);
//		}		
		
		if(updateTanSpace)
		{
			//s0.updateMeshTangentSpace();			
		}
	}
	
	public void subdivide()
	{
		if(s0==null)
		{
			if(edgeIndexes==null)
				buildEdgeIndexes();
			if(edgeTriangles==null)
				calcEdgeTriangles();
			s0 = new EditableMesh();
			subdivide(s0);
		}
	}
	
	boolean setButterflyEdgePosition(int edge,GeneralMatrixFloat s0positions,int pi)
	{
//		if(edge==91)
//			System.out.println("Edge "+edge);
		
		int t1 = edgeTriangles.value[edge*2+0];
		int t2 = edgeTriangles.value[edge*2+1];
	
		if((t1<0)||(t2<0))
			return false;
			
		int v1 = edgeIndexes.value[edge*2+0];
		int v2 = edgeIndexes.value[edge*2+1];

		if((v1<0)||(v2<0))
		{
			System.out.println("Test");
		}
		int v3 = triangleIndexes.value[t1*3+0];
		if((v3==v1)||(v3==v2))
			v3 = triangleIndexes.value[t1*3+1];
		if((v3==v1)||(v3==v2))
			v3 = triangleIndexes.value[t1*3+2];
		
		int v4 = triangleIndexes.value[t2*3+0];
		if((v4==v1)||(v4==v2))
			v4 = triangleIndexes.value[t2*3+1];
		if((v4==v1)||(v4==v2))
			v4 = triangleIndexes.value[t2*3+2];
	
		if((v3<0)||(v4<0))
		{
			System.out.println("Test");
		}

		int e1 = triangleEdges.value[t1*3+0];
		if(!((e1!=edge)&&((edgeIndexes.value[e1*2+0]==v1)||(edgeIndexes.value[e1*2+1]==v1))))
			e1 = triangleEdges.value[t1*3+1];
		if(!((e1!=edge)&&((edgeIndexes.value[e1*2+0]==v1)||(edgeIndexes.value[e1*2+1]==v1))))
			e1 = triangleEdges.value[t1*3+2];

		int e2 = triangleEdges.value[t1*3+0];
		if(!((e2!=edge)&&((edgeIndexes.value[e2*2+0]==v2)||(edgeIndexes.value[e2*2+1]==v2))))
			e2 = triangleEdges.value[t1*3+1];
		if(!((e2!=edge)&&((edgeIndexes.value[e2*2+0]==v2)||(edgeIndexes.value[e2*2+1]==v2))))
			e2 = triangleEdges.value[t1*3+2];

		int e3 = triangleEdges.value[t2*3+0];
		if(!((e3!=edge)&&((edgeIndexes.value[e3*2+0]==v1)||(edgeIndexes.value[e3*2+1]==v1))))
			e3 = triangleEdges.value[t2*3+1];
		if(!((e3!=edge)&&((edgeIndexes.value[e3*2+0]==v1)||(edgeIndexes.value[e3*2+1]==v1))))
			e3 = triangleEdges.value[t2*3+2];

		int e4 = triangleEdges.value[t2*3+0];
		if(!((e4!=edge)&&((edgeIndexes.value[e4*2+0]==v2)||(edgeIndexes.value[e4*2+1]==v2))))
			e4 = triangleEdges.value[t2*3+1];
		if(!((e4!=edge)&&((edgeIndexes.value[e4*2+0]==v2)||(edgeIndexes.value[e4*2+1]==v2))))
			e4 = triangleEdges.value[t2*3+2];
		
		int t3 = edgeTriangles.value[e1*2+0];
		if(t3==t1)
			t3 = edgeTriangles.value[e1*2+1];

		int t4 = edgeTriangles.value[e2*2+0];
		if(t4==t1)
			t4 = edgeTriangles.value[e2*2+1];

		int t5 = edgeTriangles.value[e3*2+0];
		if(t5==t2)
			t5 = edgeTriangles.value[e3*2+1];

		int t6 = edgeTriangles.value[e4*2+0];
		if(t6==t2)
			t6 = edgeTriangles.value[e4*2+1];
	
		if((t3<0)||(t4<0)||(t5<0)||(t6<0))
		{
			return false;
		}
		
		int v5 = triangleIndexes.value[t3*3+0];
		if((v5==v1)||(v5==v3))
			v5 = triangleIndexes.value[t3*3+1];
		if((v5==v1)||(v5==v3))
			v5 = triangleIndexes.value[t3*3+2];

		int v6 = triangleIndexes.value[t4*3+0];
		if((v6==v2)||(v6==v3))
			v6 = triangleIndexes.value[t4*3+1];
		if((v6==v2)||(v6==v3))
			v6 = triangleIndexes.value[t4*3+2];

		int v7 = triangleIndexes.value[t5*3+0];
		if((v7==v1)||(v7==v4))
			v7 = triangleIndexes.value[t5*3+1];
		if((v7==v1)||(v7==v4))
			v7 = triangleIndexes.value[t5*3+2];

		int v8 = triangleIndexes.value[t6*3+0];
		if((v8==v2)||(v8==v4))
			v8 = triangleIndexes.value[t6*3+1];
		if((v8==v2)||(v8==v4))
			v8 = triangleIndexes.value[t6*3+2];
		
		for(int i = 0;i<3;i++)
		{
			s0positions.value[pi*3+i] = (s0positions.value[v1*3+i]+s0positions.value[v2*3+i])*0.5f;
			s0positions.value[pi*3+i] += (s0positions.value[v3*3+i]+s0positions.value[v4*3+i])*0.125f;
			s0positions.value[pi*3+i] += (s0positions.value[v5*3+i]+s0positions.value[v6*3+i]+s0positions.value[v7*3+i]+s0positions.value[v8*3+i])*-0.0625f;
		}
		return true;
	}
	
	void subdivide(EditableMesh s)
	{
		try
		{
		//For every edge add a vert in the centre between the two edge verts 
		//for every quad add a vert in the centroid of the quad
		s.positions = new GeneralMatrixFloat(3,positions.height+edgeIndexes.height/*+quadIndexes.height*/);
		s.textureCoordinates = new GeneralMatrixFloat(2,textureCoordinates.height+edgeIndexes.height/*+quadIndexes.height*/);

		GeneralMatrixFloat 	s0positions = s.positions;
		GeneralMatrixFloat 	s0textureCoordinates = s.textureCoordinates;

		s0positions.setSubset(positions, 0, 0);
		s0textureCoordinates.setSubset(textureCoordinates, 0, 0);

		//Put the edge verts in place
		for(int i=0;i<edgeIndexes.height;i++)
		{
			int v1 = edgeIndexes.get(0, i);
			int v2 = edgeIndexes.get(1, i);
			int pi = positions.height+i;
			
			float v1x = positions.value[v1*3+0];
			float v1y = positions.value[v1*3+1];
			float v1z = positions.value[v1*3+2];

			float v2x = positions.value[v2*3+0];
			float v2y = positions.value[v2*3+1];
			float v2z = positions.value[v2*3+2];
			
//			float x = (positions.get(0,v1)+positions.get(0,v2))*0.5f;
//			float y = (positions.get(1,v1)+positions.get(1,v2))*0.5f;
//			float z = (positions.get(2,v1)+positions.get(2,v2))*0.5f;
			
			//Find the butterfly subdivision for the added edge vertex
			

			boolean success = setButterflyEdgePosition(i,s0positions,pi);
			
			if(!success)
			{
				System.out.println("Contour edge "+i);
				float x = (v1x+v2x)*0.5f;
				float y = (v1y+v2y)*0.5f;
				float z = (v1z+v2z)*0.5f;
				s0positions.set(0, pi, x);
				s0positions.set(1, pi, y);
				s0positions.set(2, pi, z);
			}
			
			/*
			if(vertexTextureSpaceMatricies!=null)
			{
				float d1x = x-v1x;
				float d1y = y-v1y;
				float d1z = z-v1z;
				
				float d2x = x-v2x;
				float d2y = y-v2y;
				float d2z = z-v2z;
				
				float d1 = d1x*vertexTextureSpaceMatricies.value[9*v1+6];
				d1 += d1y*vertexTextureSpaceMatricies.value[9*v1+7];
				d1 += d1z*vertexTextureSpaceMatricies.value[9*v1+8];
				
				float d2 = d2x*vertexTextureSpaceMatricies.value[9*v2+6];
				d2 += d2y*vertexTextureSpaceMatricies.value[9*v2+7];
				d2 += d2z*vertexTextureSpaceMatricies.value[9*v2+8];
				
				//Remove movement along normal for both verts
				//Average result
				x -= (d1*vertexTextureSpaceMatricies.value[9*v1+6]+d2*vertexTextureSpaceMatricies.value[9*v2+6])*0.5f;
				y -= (d1*vertexTextureSpaceMatricies.value[9*v1+7]+d2*vertexTextureSpaceMatricies.value[9*v2+7])*0.5f;
				z -= (d1*vertexTextureSpaceMatricies.value[9*v1+8]+d2*vertexTextureSpaceMatricies.value[9*v2+8])*0.5f;
			}
			*/
			
			float u = (textureCoordinates.get(0,v1)+textureCoordinates.get(0,v2))*0.5f;
			float v = (textureCoordinates.get(1,v1)+textureCoordinates.get(1,v2))*0.5f;
			
			s0textureCoordinates.set(0,pi,u);
			s0textureCoordinates.set(1,pi,v);
		}

		/*
		//Put the quad face verts in place
		for(int i=0;i<quadIndexes.height;i++)
		{
			int v1 = quadIndexes.get(0, i);
			int v2 = quadIndexes.get(1, i);
			int v3 = quadIndexes.get(2, i);
			int v4 = quadIndexes.get(3, i);
			int pi = positions.height+edgeIndexes.height+i;
			float x = (positions.get(0,v1)+positions.get(0,v2)+positions.get(0,v3)+positions.get(0,v4))*0.25f;
			float y = (positions.get(1,v1)+positions.get(1,v2)+positions.get(1,v3)+positions.get(1,v4))*0.25f;
			float z = (positions.get(2,v1)+positions.get(2,v2)+positions.get(2,v3)+positions.get(2,v4))*0.25f;

			float u = (textureCoordinates.get(0,v1)+textureCoordinates.get(0,v2)+textureCoordinates.get(0,v3)+textureCoordinates.get(0,v4))*0.25f;
			float v = (textureCoordinates.get(1,v1)+textureCoordinates.get(1,v2)+textureCoordinates.get(1,v3)+textureCoordinates.get(1,v4))*0.25f;

			s0positions.set(0, pi, x);
			s0positions.set(1, pi, y);
			s0positions.set(2, pi, z);

			s0textureCoordinates.set(0, pi, u);
			s0textureCoordinates.set(1, pi, v);
		}
		*/
		
		s.triangleIndexes = new GeneralMatrixInt(3,triangleIndexes.height*4);
//		s.quadIndexes = new GeneralMatrixInt(4,quadIndexes.height*4);
//		GeneralMatrixInt 	s0quadIndexes = s.quadIndexes;
		GeneralMatrixInt 	s0triangleIndexes = s.triangleIndexes;
		
		//For each tri, lookup the vert that is the edge split i.e. get edge index
		//Add three tris using the new vert indecies
		for(int i=0;i<triangleIndexes.height;i++)
		{
			int v1 = triangleIndexes.get(0,i);
			int v2 = triangleIndexes.get(1,i);
			int v3 = triangleIndexes.get(2,i);
			int ev1 = triangleEdges.get(0,i)+positions.height;
			int ev2 = triangleEdges.get(1,i)+positions.height;
			int ev3 = triangleEdges.get(2,i)+positions.height;
			
			s0triangleIndexes.set(0, i*4+0, ev3);
			s0triangleIndexes.set(1, i*4+0, v1);
			s0triangleIndexes.set(2, i*4+0, ev1);

			s0triangleIndexes.set(0, i*4+1, ev1);
			s0triangleIndexes.set(1, i*4+1, v2);
			s0triangleIndexes.set(2, i*4+1, ev2);

			s0triangleIndexes.set(0, i*4+2, ev2);
			s0triangleIndexes.set(1, i*4+2, v3);
			s0triangleIndexes.set(2, i*4+2, ev3);

			s0triangleIndexes.set(0, i*4+3, ev1);
			s0triangleIndexes.set(1, i*4+3, ev2);
			s0triangleIndexes.set(2, i*4+3, ev3);
		}
		//For each quad, lookup the vert that is the edge split i.e. get edge indexes
		//Add 4 quads using the new vert indecies
//		for(int i=0;i<quadIndexes.height;i++)
//		{
//			int v1 = quadIndexes.get(0,i);
//			int v2 = quadIndexes.get(1,i);
//			int v3 = quadIndexes.get(2,i);
//			int v4 = quadIndexes.get(3,i);
//			int ev1 = quadEdges.get(0,i)+positions.height;
//			int ev2 = quadEdges.get(1,i)+positions.height;
//			int ev3 = quadEdges.get(2,i)+positions.height;
//			int ev4 = quadEdges.get(3,i)+positions.height;
//			int fv = i+positions.height+edgeIndexes.height;
//
//			s0quadIndexes.set(0, i*4+0, ev4);
//			s0quadIndexes.set(1, i*4+0, v1);
//			s0quadIndexes.set(2, i*4+0, ev1);
//			s0quadIndexes.set(3, i*4+0, fv);
//
//			s0quadIndexes.set(0, i*4+1, fv);
//			s0quadIndexes.set(1, i*4+1, ev1);
//			s0quadIndexes.set(2, i*4+1, v2);
//			s0quadIndexes.set(3, i*4+1, ev2);
//
//			s0quadIndexes.set(0, i*4+2, ev2);
//			s0quadIndexes.set(1, i*4+2, v3);
//			s0quadIndexes.set(2, i*4+2, ev3);
//			s0quadIndexes.set(3, i*4+2, fv);
//
//			s0quadIndexes.set(0, i*4+3, fv);
//			s0quadIndexes.set(1, i*4+3, ev3);
//			s0quadIndexes.set(2, i*4+3, v4);
//			s0quadIndexes.set(3, i*4+3, ev4);
//		}
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}
	
	/*
    public void save(java.net.URL file)
    {
		try
		{
			FileOutputStream f = new FileOutputStream(file.getPath());
			DataOutputStream d = new DataOutputStream(f);
			
			d.writeInt(quadIndexes.height);
			for(int j=0;j<quadIndexes.height;j++)
			{
				d.writeInt(quadIndexes.get(0, j));
				d.writeInt(quadIndexes.get(1, j));
				d.writeInt(quadIndexes.get(2, j));
				d.writeInt(quadIndexes.get(3, j));
			}
			d.writeInt(triangleIndexes.height);
			for(int j=0;j<triangleIndexes.height;j++)
			{
				d.writeInt(triangleIndexes.get(0, j));
				d.writeInt(triangleIndexes.get(1, j));
				d.writeInt(triangleIndexes.get(2, j));
			}

			d.writeInt(edgeIndexes.height);
			for(int j=0;j<edgeIndexes.height;j++)
			{
				d.writeInt(edgeIndexes.get(0, j));
				d.writeInt(edgeIndexes.get(1, j));
			}

			d.writeInt(quadEdges.height);
			for(int j=0;j<quadEdges.height;j++)
			{
				d.writeInt(quadEdges.get(0, j));
				d.writeInt(quadEdges.get(1, j));
				d.writeInt(quadEdges.get(2, j));
				d.writeInt(quadEdges.get(3, j));
			}

			d.writeInt(triangleEdges.height);
			for(int j=0;j<triangleEdges.height;j++)
			{
				d.writeInt(triangleEdges.get(0, j));
				d.writeInt(triangleEdges.get(1, j));
				d.writeInt(triangleEdges.get(2, j));
			}
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
    }
    
    public void load(java.net.URL file)
    {
    	try
    	{
    	FileInputStream f = new FileInputStream(file.getPath());
    	DataInputStream d = new DataInputStream(f);

    	quadIndexes = new GeneralMatrixInt(4,d.readInt());
		for(int j=0;j<quadIndexes.height;j++)
		{
			quadIndexes.set(0, j,d.readInt());
			quadIndexes.set(1, j,d.readInt());
			quadIndexes.set(2, j,d.readInt());
			quadIndexes.set(3, j,d.readInt());
		}
    	triangleIndexes = new GeneralMatrixInt(3,d.readInt());
		for(int j=0;j<triangleIndexes.height;j++)
		{
			triangleIndexes.set(0, j,d.readInt());
			triangleIndexes.set(1, j,d.readInt());
			triangleIndexes.set(2, j,d.readInt());
		}
    	
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
    }
    */
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
			EditableMesh.addEdge(v1, v2, edges, 0, currentEdgeSize);

			v1 = triMeshtris.value[i*9+1*3];
			v2 = triMeshtris.value[i*9+2*3];

			currentEdgeSize = edges.height;
			EditableMesh.addEdge(v1, v2, edges, 0, currentEdgeSize);
			
			v1 = triMeshtris.value[i*9+2*3];
			v2 = triMeshtris.value[i*9+0*3];

			currentEdgeSize = edges.height;
			EditableMesh.addEdge(v1, v2, edges, 0, currentEdgeSize);
		}	
	}
}
