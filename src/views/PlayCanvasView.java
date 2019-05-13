package views;

import org.json.JSONObject;
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
            
            JSONObject entity1 = new JSONObject();
            JSONObject vertexData = new JSONObject();
            
            double[] positionsArr = new double[] {2, 1, 0, -1, 1, 0, -1, -1, 0, 1, -1, 0 };
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
            entity1.put("name", "box1");
            entity1.put("x", 0);
            entity1.put("y", 0);
            entity1.put("z", 0);
            //entity1.put("realtimeModel", true);
            entity1.put("vertexData", vertexData);
            //entity1.put("scriptName", "rotate1");
            //entity1.put("script", "this.entity.rotate(0, 10 * dt, 0);");
            
            entities.put(0, entity1);
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
            
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_HTML, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("playCanvasUpdate"))
        {  	
        	JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            JSONObject entity1 = new JSONObject();
            JSONObject vertexData = new JSONObject();
            
            double[] positionsArr = new double[] {2, 1, 0, -1, 1, 0, -1, -1, 0, 1, -1, 0 };
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
            entity1.put("name", "box1");
            entity1.put("x", 0);
            entity1.put("y", 0);
            entity1.put("z", 0);
            //entity1.put("realtimeModel", true);
            entity1.put("vertexData", vertexData);
            //entity1.put("scriptName", "rotate1");
            //entity1.put("script", "this.entity.rotate(0, 10 * dt, 0);");
            
            entities.put(0, entity1);
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
            
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_HTML, responseData.toString() );
            return true;
        }

        return false;
    }

}