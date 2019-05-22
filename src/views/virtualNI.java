package views;

import org.json.JSONObject;

import animation.Animate;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
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

import java.util.HashMap;

import org.json.JSONArray;
import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class virtualNI extends DynamicWebPage {
	
	private double offset = 0;
	
    public virtualNI(DatabaseInterface db, FileStoreInterface fs) {
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
            			"<script src=\"./js/FirstPersonCam.js\"></script>\n" + 
                        "<link href=\"css/stats.css\" rel=\"stylesheet\" type=\"text/css\">" +
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
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            int i = 0;
            
            for(int x = 0;x<10;x++)
            {
            	for(int y = 0;y<10;y++)
                {
            		JSONObject entity1 = new JSONObject();
                    entity1.put("model", "box");
                    entity1.put("x", x * 2);
                    entity1.put("z", y * 2);
                    entity1.put("y", -5);
                    
                    entity1.put("yScale", Math.random() * 3);
                    
                    entities.put(i, entity1);       	
                    i++;
                }
            }
                
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
         
        return false;
    }
    
}