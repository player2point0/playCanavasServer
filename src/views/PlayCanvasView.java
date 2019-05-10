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
            JSONObject entity2 = new JSONObject();
            
            entity1.put("model", "asset");
            entity1.put("assetFilePath", "./Hovership/Hovership.json");//"./model.json");
            entity1.put("name", "box1");
            entity1.put("x", 5);
            entity1.put("y", 0);
            entity1.put("z", 0);
            entity1.put("scriptName", "rotate1");
            entity1.put("script", "this.entity.rotate(0, 10 * dt, 0);");
            
            entity2.put("model", "box");
            entity2.put("name", "box2");
            entity2.put("x", -5);
            entity2.put("y", 0);
            entity2.put("z", 0);
            entity2.put("scriptName", "rotate2");
            entity2.put("script", "this.entity.rotate(0, 100 * dt, 0);");
            
            entities.put(1, entity1);
            entities.put(0, entity2);
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
            
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_HTML, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("playCanvasUpdate"))
        {
        	
            JSONObject responseData = new JSONObject();

            responseData.put("time", System.currentTimeMillis()); 
            
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_HTML, responseData.toString() );
            return true;
        }

        return false;
    }

}