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
            
            JSONObject entity = new JSONObject();
            
            entity.put("model", "box");
            entity.put("scriptName", "rotate");
            entity.put("script", "this.entity.rotate(100 * dt, 20 * dt, 15 * dt);");
            
            
            entities.put(0, entity);
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