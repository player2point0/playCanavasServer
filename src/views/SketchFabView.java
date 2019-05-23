package views;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class SketchFabView extends DynamicWebPage {
		
    public SketchFabView(DatabaseInterface db, FileStoreInterface fs) {
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
            			"<script src=\"./js/playcanvas-anim.js\"></script>\n" + 
            			"<script src=\"./js/playcanvas-gltf.js\"></script>\n" +
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
            
            String path = "C:\\Users\\makerspacestaff\\Documents\\playcanvas\\playCanvasServer\\httpdocs\\sketchFab\\b52a08120c059008.zip\\out\\b52a08120c059008\\"+"ad10226b4f7a451ea23920a556c72a90.zip";
    		String path1 = "C:\\Users\\makerspacestaff\\Documents\\playcanvas\\playCanvasServer\\httpdocs\\sketchFab\\chicken";
            
            unzip(path, path1);
            
            
            JSONObject entity1 = new JSONObject();
            entity1.put("model", "box");
            entity1.put("sketchFabFolder", "chicken");
            entity1.put("scriptName", "rotate1");
            entity1.put("script", "this.entity.rotate(0, 10 * dt, 0);");
            
            entities.put(0, entity1);
                            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("playCanvasUpdate"))
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
    
    private static void unzip(String zipFilePath, String destDir)
    {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    

}