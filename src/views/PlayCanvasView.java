package views;

import org.json.JSONObject;

import animation.Animate;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixString;
import procedural.human.HumanBody;
import procedural.human.HumanVolume;
import procedural.human.resources.Bones;
import procedural.human.resources.Sknb;
import procedural.human.resources.Sknw;
import procedural.primitives.Skeleton;
import procedural.primitives.Skin;
import procedural.primitives.SkinnedVolume;

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
        	/*
			GeneralMatrixString morphnames = new GeneralMatrixString(1);
			GeneralMatrixFloat morphmagnitudes = new GeneralMatrixFloat(1);
			
			//morphnames.push_back("NeutralMaleChild");
			morphnames.push_back("NeutralFemaleYoung");
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
			
			selectedSkel = skel;
			
			skel.lpos.setDimensions(3,1+(Bones.bones.length/2));
			
			trimesh.pos.setDimensions(3, skin.bpos.height);

			skin.sb = Sknb.get();
			skin.sw = Sknw.get();
			HumanVolume.bonemapping(skin.bpos, skel.vpos, skel.boneJoints, skin.sb, skin.sw, svol.pbone, svol.pbextent);
			
			skel.tvpos.setDimensions(skel.vpos.width,skel.vpos.height);
			skel.tbmats.setDimensions(skel.bmats.width,skel.bmats.height);

			Animate.transformWithParams(skel.boneJoints.value, skel.boneParents.value,
					skel.bonelengths, skel.localbindbmats,
					skel.tvpos, skel.tbmats, skel.lpos);

	    	Animate.updateSkinUsingSkeleton(skel.tvpos, skel.tbmats, skel.vpos, skel.bmats, skin.bpos, Bones.bones, skin.sb, skin.sw, trimesh.pos);
			*/
        	
        	
        	
        	
        	
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            JSONObject entity1 = new JSONObject();
            JSONObject vertexData = new JSONObject();
            
            double[] positionsArr = new double[] {1 * Math.random(), 1 * Math.random(), 0,
            		-1 * Math.random(), 1 * Math.random(), 0,
            		-1 * Math.random(), -1 * Math.random(), 0,
            		1 * Math.random(), -1 * Math.random(), 0 };
            JSONArray positions = new JSONArray(positionsArr);     
            double[] normalsArr = new double[] {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};
            JSONArray normals = new JSONArray(normalsArr);
            double[] uvsArr = new double[] {};
            JSONArray uvs = new JSONArray(uvsArr);
            double[] indiciesArr = new double[] {0, 1, 2, 0, 2, 3};
            JSONArray indicies = new JSONArray(indiciesArr);
            
            vertexData.put("position", positions);
            vertexData.put("normals", normals);
            vertexData.put("uvs", uvs);
            vertexData.put("indices", indicies);
            
            entity1.put("model", "asset");
            entity1.put("name", "box10");
            entity1.put("x", 0);
            entity1.put("y", 0);
            entity1.put("z", 0);
            entity1.put("realtimeModel", true);
            entity1.put("vertexData", vertexData);
            //entity1.put("scriptName", "rotate1");
            //entity1.put("script", "this.entity.rotate(0, 10 * dt, 0);");
            
            entities.put(0, entity1);
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        /*
        else if(toProcess.path.equalsIgnoreCase("playCanvasUpdate"))
        {  	
        	JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            JSONObject entity1 = new JSONObject();
            JSONObject vertexData = new JSONObject();
            
            double[] positionsArr = new double[] {1 * Math.random(), 1 * Math.random(), 0,
            		-1 * Math.random(), 1 * Math.random(), 0,
            		-1 * Math.random(), -1 * Math.random(), 0,
            		1 * Math.random(), -1 * Math.random(), 0 };
            JSONArray positions = new JSONArray(positionsArr);     
            double[] normalsArr = new double[] {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};
            JSONArray normals = new JSONArray(normalsArr);
            double[] uvsArr = new double[] {};
            JSONArray uvs = new JSONArray(uvsArr);
            double[] indiciesArr = new double[] {0, 1, 2, 0, 2, 3};
            JSONArray indicies = new JSONArray(indiciesArr);
            
            vertexData.put("position", positions);
            vertexData.put("normals", normals);
            vertexData.put("uvs", uvs);
            vertexData.put("indices", indicies);
            
            entity1.put("name", "box1");
            entity1.put("vertexData", vertexData);
            
            entities.put(0, entity1);
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
         */
        return false;
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