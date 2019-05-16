package views;

import org.json.JSONObject;

import animation.Animate;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;
import meshprocessing.MeshEdges;
import procedural.human.HumanBody;
import procedural.human.HumanVolume;
import procedural.human.resources.Bones;
import procedural.human.resources.Sknb;
import procedural.human.resources.Sknw;
import procedural.human.resources.makehuman.QuadMesh;
import procedural.human.resources.makehuman.QuadUvs;
import procedural.human.resources.makehuman.Uvs;
import procedural.primitives.Mesh;
import procedural.primitives.MeshConnectivity;
import procedural.primitives.MeshMirror;
import procedural.primitives.Skeleton;
import procedural.primitives.Skin;
import procedural.primitives.SkinnedVolume;
import rendering.RenderBuffer;
import rendering.shaders.constructed.MeshRasteriser3DUV;

import org.json.JSONArray;
import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class PlayCanvasView extends DynamicWebPage {
	
    public PlayCanvasView(DatabaseInterface db, FileStoreInterface fs) {
		super(db, fs);
		// TODO Auto-generated constructor stub
	}
    
    public boolean process(WebRequest toProcess)
    {
        if(toProcess.path.equalsIgnoreCase("playCanvas"))
        {
            String stringToSendToWebBrowser = "<html>\n" + 
        			"<head>"+
            			"<script src=\"https://code.playcanvas.com/playcanvas-latest.js\"></script>\n" + 
            			"<script src=\"./js/JavaEntity.js\"></script>\n" + 
            		"</head>"+
            		"<body>\n" + 
            		"<canvas id=\"application-canvas\"></canvas>\n" + 
            		"<script src=\"js/playCanvas.js\"></script>\n"+
            		"  </body>\n" + 
            		"</html>";

            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_HTML, stringToSendToWebBrowser );
            return true;
        }

        else if(toProcess.path.equalsIgnoreCase("playCanvasStart"))
        {
        	
        	GeneralMatrixObject skeletons = new GeneralMatrixObject(1);
        	GeneralMatrixObject skins = new GeneralMatrixObject(1);
        	GeneralMatrixObject volumes = new GeneralMatrixObject(1);
			Mesh trimesh = new Mesh();

			GeneralMatrixString morphnames = new GeneralMatrixString(1);
			GeneralMatrixFloat morphmagnitudes = new GeneralMatrixFloat(1);
			
			morphnames.push_back("NeutralMaleChild");
			//morphnames.push_back("NeutralFemaleYoung");
			morphmagnitudes.push_back(1.0f);
			
			Skeleton skel = new Skeleton();
			Skin skin = new Skin();
			SkinnedVolume svol = new SkinnedVolume();
			
			skel.boneParents.setDimensions(1, Bones.names.length);
			skel.boneParents.set(Bones.bprnts);
			skel.boneJoints.setDimensions(2,Bones.names.length);
			skel.boneJoints.set(Bones.bones);
			HumanBody.createMorphedBody(morphnames,morphmagnitudes, skin.bpos, 
					skel.vpos,skel.bmats,skel.bonelengths,skel.localbindbmats);

			skeletons.push_back(skel);
			skins.push_back(skin);
			volumes.push_back(svol);
						
			skel.lpos.setDimensions(3,1+(Bones.bones.length/2));
			
			trimesh.pos.setDimensions(3, skin.bpos.height);

			skin.sb = Sknb.get();
			skin.sw = Sknw.get();
			HumanVolume.bonemapping(skin.bpos, skel.vpos, skel.boneJoints, skin.sb, skin.sw, svol.pbone, svol.pbextent);
			
			skel.tvpos.setDimensions(skel.vpos.width,skel.vpos.height);
			skel.tbmats.setDimensions(skel.bmats.width,skel.bmats.height);

	    	int[] quads = QuadMesh.get();
			trimesh.quads.setDimensions(4,quads.length/4);
			trimesh.quads.set(quads);
			int[] quvs = QuadUvs.get();
			trimesh.quaduvs.setDimensions(4,quads.length/4);
			trimesh.quaduvs.set(quvs);
			float[] uvs = Uvs.get();
			trimesh.uvs.setDimensions(2, uvs.length/2);
			trimesh.uvs.set(uvs);
			trimesh.quadnrms.setDimensions(4, quads.length/4);
			
        	double[] meshPointsArr = new double[trimesh.pos.height * 3];
        	int[] meshIndicesArr = new int[trimesh.quads.height * 6];
        	double[] meshUvsArr = new double[trimesh.quads.height * 6];
        	
        	Animate.transformWithParams(skel.boneJoints.value, skel.boneParents.value,
					skel.bonelengths, skel.localbindbmats,
					skel.tvpos, skel.tbmats, skel.lpos);

	    	Animate.updateSkinUsingSkeleton(skel.tvpos, skel.tbmats, skel.vpos, skel.bmats, skin.bpos, Bones.bones, skin.sb, skin.sw, trimesh.pos);
			

        	//line 847 renderTris()				
			int j = 0;	
			
			for(int qi=0;qi<trimesh.quads.height;qi++)
			{	
				int v0 = trimesh.quads.value[qi*trimesh.quads.width+0];
				int v1 = trimesh.quads.value[qi*trimesh.quads.width+1];
				int v2 = trimesh.quads.value[qi*trimesh.quads.width+2];
				int v3 = trimesh.quads.value[qi*trimesh.quads.width+3];

				int uv0 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+0];
				int uv1 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+1];
				int uv2 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+2];
				int uv3 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+3];

				meshIndicesArr[j] = v0;
				meshIndicesArr[j+1] = v1;
				meshIndicesArr[j+2] = v2;
				meshIndicesArr[j+3] = v0;
				meshIndicesArr[j+4] = v2;
				meshIndicesArr[j+5] = v3;
				
				meshUvsArr[j] = uv0;
				meshUvsArr[j+1] = uv1;
				meshUvsArr[j+2] = uv2;
				meshUvsArr[j+3] = uv0;
				meshUvsArr[j+4] = uv2;
				meshUvsArr[j+5] = uv3;
				
				j+=6;			
			}
    				
        	
        	for(int i = 0;i<trimesh.pos.height;i++)
        	{
        		meshPointsArr[i*3+0] = trimesh.pos.value[i*3];
        		meshPointsArr[i*3+1] = trimesh.pos.value[(i*3)+1];
        		meshPointsArr[i*3+2] = trimesh.pos.value[(i*3)+2];        	
        	}
        	
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            double[] normalsArr = new double[] {};
            double[] uvsArr = new double[] {};
            /*
            double[] meshPointsArr = new double[] {1, 1, 0,
            		-1, 1, 0,
            		-1, -1, 0,
            		1, -1, 0};
            int[] meshIndicesArr = new int[] { 0, 1, 2, 0, 2, 3};
            */
            JSONObject entity1 = makeEntity(meshPointsArr, uvsArr, meshIndicesArr, "asset", "box1", 0, 0, 0, false);
            
            entities.put(0, entity1);
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("playCanvasUpdate"))
        {  	
        	GeneralMatrixObject skeletons = new GeneralMatrixObject(1);
        	GeneralMatrixObject skins = new GeneralMatrixObject(1);
        	GeneralMatrixObject volumes = new GeneralMatrixObject(1);
			Mesh trimesh = new Mesh();

			GeneralMatrixString morphnames = new GeneralMatrixString(1);
			GeneralMatrixFloat morphmagnitudes = new GeneralMatrixFloat(1);
			
			morphnames.push_back("NeutralMaleChild");
			//morphnames.push_back("NeutralFemaleYoung");
			morphmagnitudes.push_back(1.0f);
			
			Skeleton skel = new Skeleton();
			Skin skin = new Skin();
			SkinnedVolume svol = new SkinnedVolume();
			
			skel.boneParents.setDimensions(1, Bones.names.length);
			skel.boneParents.set(Bones.bprnts);
			skel.boneJoints.setDimensions(2,Bones.names.length);
			skel.boneJoints.set(Bones.bones);
			HumanBody.createMorphedBody(morphnames,morphmagnitudes, skin.bpos, 
					skel.vpos,skel.bmats,skel.bonelengths,skel.localbindbmats);

			skeletons.push_back(skel);
			skins.push_back(skin);
			volumes.push_back(svol);
						
			skel.lpos.setDimensions(3,1+(Bones.bones.length/2));
			
			trimesh.pos.setDimensions(3, skin.bpos.height);

			skin.sb = Sknb.get();
			skin.sw = Sknw.get();
			HumanVolume.bonemapping(skin.bpos, skel.vpos, skel.boneJoints, skin.sb, skin.sw, svol.pbone, svol.pbextent);
			
			skel.tvpos.setDimensions(skel.vpos.width,skel.vpos.height);
			skel.tbmats.setDimensions(skel.bmats.width,skel.bmats.height);

	    	int[] quads = QuadMesh.get();
			trimesh.quads.setDimensions(4,quads.length/4);
			trimesh.quads.set(quads);
			int[] quvs = QuadUvs.get();
			trimesh.quaduvs.setDimensions(4,quads.length/4);
			trimesh.quaduvs.set(quvs);
			float[] uvs = Uvs.get();
			trimesh.uvs.setDimensions(2, uvs.length/2);
			trimesh.uvs.set(uvs);
			trimesh.quadnrms.setDimensions(4, quads.length/4);
			
        	double[] meshPointsArr = new double[trimesh.pos.height * 3];
        	int[] meshIndicesArr = new int[trimesh.quads.height * 6];
        	double[] meshUvsArr = new double[trimesh.quads.height * 6];
        	
        	Animate.transformWithParams(skel.boneJoints.value, skel.boneParents.value,
					skel.bonelengths, skel.localbindbmats,
					skel.tvpos, skel.tbmats, skel.lpos);

	    	Animate.updateSkinUsingSkeleton(skel.tvpos, skel.tbmats, skel.vpos, skel.bmats, skin.bpos, Bones.bones, skin.sb, skin.sw, trimesh.pos);
			

        	//line 847 renderTris()				
			int j = 0;	
			
			for(int qi=0;qi<trimesh.quads.height;qi++)
			{	
				int v0 = trimesh.quads.value[qi*trimesh.quads.width+0];
				int v1 = trimesh.quads.value[qi*trimesh.quads.width+1];
				int v2 = trimesh.quads.value[qi*trimesh.quads.width+2];
				int v3 = trimesh.quads.value[qi*trimesh.quads.width+3];

				int uv0 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+0];
				int uv1 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+1];
				int uv2 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+2];
				int uv3 = trimesh.quaduvs.value[qi*trimesh.quaduvs.width+3];

				meshIndicesArr[j] = v0;
				meshIndicesArr[j+1] = v1;
				meshIndicesArr[j+2] = v2;
				meshIndicesArr[j+3] = v0;
				meshIndicesArr[j+4] = v2;
				meshIndicesArr[j+5] = v3;
				
				meshUvsArr[j] = uv0;
				meshUvsArr[j+1] = uv1;
				meshUvsArr[j+2] = uv2;
				meshUvsArr[j+3] = uv0;
				meshUvsArr[j+4] = uv2;
				meshUvsArr[j+5] = uv3;
				
				j+=6;			
			}
    				
        	
        	for(int i = 0;i<trimesh.pos.height;i++)
        	{
        		meshPointsArr[i*3+0] = trimesh.pos.value[i*3];
        		meshPointsArr[i*3+1] = trimesh.pos.value[(i*3)+1];
        		meshPointsArr[i*3+2] = trimesh.pos.value[(i*3)+2];        	
        	}
        		
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            double[] normalsArr = new double[] {};
            double[] uvsArr = new double[] {};

            JSONObject entity1 = makeEntity(meshPointsArr, uvsArr, meshIndicesArr, "box1");
  
            entities.put(0, entity1);
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
         
        return false;
    }
    //update entity
    public JSONObject makeEntity(double[] positionsArr, double[] uvsArr, int[] indiciesArr, String name)
    {
    	 JSONObject entity = new JSONObject();
         JSONObject vertexData = new JSONObject(); 
         
         JSONArray positions = new JSONArray(positionsArr);     
         JSONArray uvs = new JSONArray(uvsArr);
         JSONArray indicies = new JSONArray(indiciesArr);
         
         vertexData.put("position", positions);
         vertexData.put("uvs", uvs);
         vertexData.put("indices", indicies);
         
         entity.put("vertexData", vertexData);
         entity.put("name", name);
         
         return entity;
    }
    //start entity
    public JSONObject makeEntity(double[] positionsArr, double[] uvsArr, int[] indiciesArr, String  model, String name, double x, double y, double z, boolean realTimeModel)
    {
    	 JSONObject entity = new JSONObject();
         JSONObject vertexData = new JSONObject(); 
         
         JSONArray positions = new JSONArray(positionsArr);     
         JSONArray uvs = new JSONArray(uvsArr);
         JSONArray indicies = new JSONArray(indiciesArr);
         
         vertexData.put("position", positions);
         vertexData.put("uvs", uvs);
         vertexData.put("indices", indicies);
         
         entity.put("model", model);
         entity.put("name", name);
         entity.put("x", x);
         entity.put("y", y);
         entity.put("z", z);
         entity.put("realtimeModel", realTimeModel);
         entity.put("vertexData", vertexData);
         //entity.put("scriptName", "rotate1");
         //entity.put("script", "this.entity.rotate(0, 10 * dt, 0);");
         
         return entity;
    }
    
    public JSONArray randomCube()
    {
    	double[] points = new double[] {
    			// Front face
			  -1.0 * Math.random(), -1.0 * Math.random(),  1.0 * Math.random(),
			   1.0 * Math.random(), -1.0 * Math.random(),  1.0 * Math.random(),
			   1.0 * Math.random(),  1.0 * Math.random(),  1.0 * Math.random(),
			  -1.0 * Math.random(),  1.0 * Math.random(),  1.0 * Math.random(),
			  
			  // Back face
			  -1.0 * Math.random(), -1.0 * Math.random(), -1.0 * Math.random(),
			  -1.0 * Math.random(),  1.0 * Math.random(), -1.0 * Math.random(),
			   1.0 * Math.random(),  1.0 * Math.random(), -1.0 * Math.random(),
			   1.0, -1.0, -1.0,
			  
			  // Top face
			  -1.0 * Math.random(),  1.0 * Math.random(), -1.0 * Math.random(),
			  -1.0 * Math.random(),  1.0 * Math.random(),  1.0 * Math.random(),
			   1.0 * Math.random(),  1.0 * Math.random(),  1.0 * Math.random(),
			   1.0 * Math.random(),  1.0 * Math.random(), -1.0 * Math.random(),
			  
			  // Bottom face
			  -1.0 * Math.random(), -1.0 * Math.random(), -1.0 * Math.random(),
			   1.0 * Math.random(), -1.0 * Math.random(), -1.0 * Math.random(),
			   1.0 * Math.random(), -1.0 * Math.random(),  1.0 * Math.random(),
			  -1.0 * Math.random(), -1.0 * Math.random(),  1.0 * Math.random(),
			  
			  // Right face
			   1.0 * Math.random(), -1.0 * Math.random(), -1.0 * Math.random(),
			   1.0 * Math.random(),  1.0 * Math.random(), -1.0 * Math.random(),
			   1.0 * Math.random(),  1.0 * Math.random(),  1.0 * Math.random(),
			   1.0 * Math.random(), -1.0 * Math.random(),  1.0 * Math.random(),
			  
			  // Left face
			  -1.0 * Math.random(), -1.0 * Math.random(), -1.0 * Math.random(),
			  -1.0 * Math.random(), -1.0 * Math.random(),  1.0 * Math.random(),
			  -1.0 * Math.random(),  1.0 * Math.random(),  1.0 * Math.random(),
			  -1.0 * Math.random(),  1.0 * Math.random(), -1.0 * Math.random() };
    	
    	return new JSONArray(points);
    }
    

}