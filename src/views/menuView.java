package views;

import org.json.JSONArray;
import org.json.JSONObject;

import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class menuView extends DynamicWebPage {
	
	private double offset = 0;
	
    public menuView(DatabaseInterface db, FileStoreInterface fs) {
		super(db, fs);
		// TODO Auto-generated constructor stub
	} 
    
    public boolean process(WebRequest toProcess)
    {
        if(toProcess.path.equalsIgnoreCase("menu"))
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
        
        else if(toProcess.path.equalsIgnoreCase("menu/playCanvasStart"))
        { 	  	
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            
            JSONObject topLeft = menuTile(-1, 1, 0, "scenedetect");
            entities.put(0, topLeft);
            JSONObject top = menuTile(0, 1, 0, "bodydetect");
            entities.put(1, top);
            JSONObject topRight = menuTile(1, 1, 0, "facedetect");
            entities.put(2, topRight);
            JSONObject centerLeft = menuTile(-1, 0, 0, "voicedetect");
            entities.put(3, centerLeft);
            JSONObject center = menuTile(0, 0, 0, "auto");
            entities.put(4, center);
            JSONObject centerRight = menuTile(3, 0, 0, "virtualni");
            entities.put(5, centerRight);
            JSONObject bottomLeft = menuTile(-1, -1, 0, "virtualnistats");
            entities.put(6, bottomLeft);
            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("menu/playCanvasUpdate"))
        {  	
        	JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
         
        return false;
    }
    
    
    public JSONObject menuTile(double x, double y, double z, String link)
    {
        JSONObject entity = new JSONObject();
        entity.put("model", "plane");
        entity.put("x", x);
        entity.put("y", y);
        entity.put("z", z);
        entity.put("xRotate", 90);
        entity.put("clickLink", link);
        entity.put("boundingBoxX", 0.5);
        entity.put("boundingBoxY", 0.5);
        entity.put("boundingBoxZ", 0.5);
        
        return entity;
    }
    
}
    