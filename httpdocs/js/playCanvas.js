var canvas;//dom canvas element
var app;//playcanvas application created and displayed on the canvas
var camera;//player camera
const host = window.location.hostname; //"http://169.254.100.33:8080/";//"http://localhost:8080/";
const port = window.location.port;//server port
var currentPage = window.location.pathname;//current server page
const loopDelay = 100;//how often to call loop - takes about 15 for server response
var sceneEntity;//stores all the server entities and allows them to be easily removed
var entities = [];//all of the server entities
var realtimeEntities = [];//the server entities that are updated in the loop;
var lastVRButton = Date.now();//prevents vr button spamming

boilerPlate();
serverWork();
vrBoilerPlate();
setTimeout(loop, loopDelay);

async function getServerData(endpoint)
{
    var url = "http://"+host+":"+port+currentPage+"/"+endpoint;
    let response = await fetch(url);//synchronous server request
    let data = await response.json();//convert java string to json object

    return data;
}

function boilerPlate()
{
    // Create a PlayCanvas application
    canvas = document.getElementById("application-canvas");
    app = new pc.Application(canvas, {});
    app.start();

    //console.log("skybox:");
    //console.log(app.scene._skyboxCubeMap);

    // Fill the available space at full resolution
    app.setCanvasFillMode(pc.FILLMODE_FILL_WINDOW);
    app.setCanvasResolution(pc.RESOLUTION_AUTO);

    // Create camera entity
    camera = new FirstPersonCam(0, 8, 0, 0, 0, 0, false, true, app);

    //create lights
    var topLight = new pc.Entity();
    topLight.addComponent('light', {
        type: "point",
        color: new pc.Color(1, 1, 1),
        intensity: 0.4,
        range: 1000,
        castShadows: false,
    });    
    topLight.setEulerAngles(4, 5, 6);
    topLight.setPosition(0, 10, 0);
    app.root.addChild(topLight);

    var frontLight = new pc.Entity();
    frontLight.addComponent('light', {
        type: "directional",
        color: new pc.Color(1, 1, 1),
        intensity: 0.4,
        range: 100,
        castShadows: false,
    });    
    frontLight.setEulerAngles(45, 0, 0);
    frontLight.setPosition(0, 10, 10);
    app.root.addChild(frontLight);
    
    // Resize the canvas when the window is resized
    window.addEventListener('resize', function () {
        app.resizeCanvas(canvas.width, canvas.height);
    });
}

function vrBoilerPlate()
{
    if (window.WebVRConfig) WebVRConfig.BUFFER_SCALE = 0.5;

    app.vr = new pc.VrManager(app);
    var current = this;

    addEventListener("mousedown",  e => 
    {
        //mouse down for vr
        if(current.app.vr.display)
        {
            //was in example code
            if (current.app.vr.display.display.bufferScale_) current.app.vr.display.display.bufferScale_ = 0.5;

            alert("entering vr");
            current.camera.camera.camera.enterVr(function (err) {
                if (err) {
                    console.warn(err);
                }

                else
                {     
                    //move the camera container down so the camera appears to be in the same place
                    var camX = current.camera.cameraContainer.getPosition().x;
                    var camZ = current.camera.cameraContainer.getPosition().z; 
                    var camY = current.camera.cameraContainer.getPosition().y; 
                    current.camera.cameraContainer.setPosition(camX, camY - 1.5, camZ);                    
                    
                    window.addEventListener("gamepadconnected", function(e) {
            
                        //the vr reticle
                        var reticle = new pc.Entity();
                        reticle.addComponent("model", {
                            type: "sphere"
                        });
                        reticle.setLocalScale(0.075, 0.075, 0.075);       
                        current.app.root.addChild(reticle);
                        
                        //add to camera so that it is called every frame
                        var gamePadController = pc.createScript("gamePadController");
                        gamePadController.prototype.update = function (dt) {            
                
                            var gp = current.navigator.getGamepads()[0];           
              
                            if(gp.pose.orientation)
                            {
                                var pitch = gp.pose.orientation[0];
                                var yaw = gp.pose.orientation[1];
                                var roll = gp.pose.orientation[2];
                                var w = gp.pose.orientation[3];

                                var rotation = new pc.Quat(pitch, yaw, roll, w);//vr controller rotation
                                var v = new pc.Vec3(0, 0, -1);//forward vector

                                var controllerVector = rotation.transformVector(v);//vector to place the reticle at
                                var direction = rotation.transformVector(v);//direction vector of the vr controller
                                controllerVector.add(current.camera.camera.getPosition());//position reticle relative to camera position
                                reticle.setPosition(controllerVector);

                                //could add better delay between button presses - detect button up
                                //prevents button spamming by forcing a 200ms delay between raycasts
                                if((current.lastVRButton + 200) > Date.now()) return;
                                
                                //checks if trigger pressed or touchpad pressed
                                if(gp.buttons[0].value > 0 || gp.buttons[0].pressed == true || gp.buttons[1].value > 0 || gp.buttons[1].pressed == true)
                                {
                                    current.lastVRButton = Date.now();
                                    var raycastResult = current.raycast(current.camera.camera.getPosition(), direction, current);//was an object hit

                                    if(!raycastResult) return;

                                    //teleport player to position on ground
                                    if(raycastResult.obj.Entity.name == "ground")
                                    {
                                        var hitX = raycastResult.hit.x;
                                        var camY = current.camera.y;
                                        var hitZ = raycastResult.hit.z;
                            
                                        current.camera.cameraContainer.setPosition(hitX, camY, hitZ);                    
                                        return;   
                                    }
                            
                                    var link = raycastResult.obj.clickLink;
                                    //redirect to new scene/page on server
                                    if(link)
                                    {
                                        changeScene(link);
                                        return;    
                                    } 
                                }
                            }
                        };
                
                        current.camera.camera.addComponent('script');
                        current.camera.camera.script.create(gamePadController);  
                    });
                }
            });
        }

        //mouse down for non-vr
        else
        {
            //check if the pointer is already locked
            //if it is then we can click on links in playcanvas
            //prevents initial click triggering playcanvas links
            var pointerLockElement = document.pointerLockElement;
            canvas.requestPointerLock();
            if(pointerLockElement == null) return;
    
            var raycastResult = raycast(current.camera.cameraContainer.getPosition(), current.camera.camera.forward, current);

            if(!raycastResult) return;

            //teleport player to position on ground
            if(raycastResult.obj.Entity.name == "ground")
            {
                var hitX = raycastResult.hit.x;
                var camY = current.camera.y;
                var hitZ = raycastResult.hit.z;

                current.camera.cameraContainer.setPosition(hitX, camY, hitZ);                    
                return;   
            }

            var link = raycastResult.obj.clickLink;
            //redirect to new scene/page on server
            if(link)
            {
                changeScene(link);
                return;    
            } 
        }    
    });
}

async function serverWork()
{
    var startData = await getServerData("playCanvasStart");//get the initial page data from the server
    var serverEntities = startData.entities;//the json data from the server    
    sceneEntity = new pc.Entity();//stores all the entities for the current scene 

    for(var i = 0;i<serverEntities.length;i++)
    {
        var entity = serverEntities[i];
        var newEntity = new JavaEntity(entity, app, sceneEntity); 

        if((entity.name != undefined) && (newEntity.Entity.name == "Untitled")) 
        {
            //bug fix for first entity
            newEntity.Entity.name = entity.name;
        }

        entities.push(newEntity);

        if(entity.realtimeModel)
        {
            //add the entities that are updated in the loop to the realtime array
            realtimeEntities.push(newEntity);
        }
    }

    this.app.root.addChild(sceneEntity);
}

function raycast(origin, direction, current)
{
    // Initialise the ray and work out the direction of the ray from the screen position
    this.ray = new pc.Ray();  

    current.ray.origin.copy(origin);//where the raycast starts from
    current.ray.direction.copy(direction);//where the raycast moves towards

    var pickable = [];// all entities with a collider
    var distancePickables = [];//all entities sorted by closest

    //get all entities with a collider
    for (var i = 0; i < entities.length; ++i) 
    {
        var pickableShape = entities[i];
        if(pickableShape.Entity.aabb) pickable.push(pickableShape);
    }

    //sort by distance from camera
    distancePickables = pickable.sort(function(a, b){
        var aPos = a.Entity.getPosition(); 
        var bPos = b.Entity.getPosition(); 

        var aDiff = aPos.sub(origin);
        var bDiff = bPos.sub(origin);

        var aDis = Math.abs(aDiff.length());
        var bDis = Math.abs(bDiff.length());

        return aDis - bDis;
    });

    for (var i = 0; i < distancePickables.length; ++i) 
    {
        //the position of the collider isn't updated as the entity moves
        //so reposition the collider incase the entity has moved
        var center = distancePickables[i].Entity.getPosition();
        var pickableShape = distancePickables[i].Entity.aabb;
        pickableShape.center = center;

        //location of raycast hit
        //will be closest hit to the camera
        var hit = new pc.Vec3();
        var result = pickableShape.intersectsRay(current.ray, hit);                    

        if (result) 
        {
            //return json object so that hit position and object can be accessed
            return {
                obj: distancePickables[i],
                hit: hit
            };      
        }  
    }    
}

function changeScene(newPage)
{
    //clear current scene
    sceneEntity.destroy();
    //remove entities
    entities = [];
    realtimeEntities = [];
    //change server page to new page 
    currentPage = "/"+newPage;
    //reinitialise the scene
    serverWork();
}

async function loop()
{
    //json data that will update the realtime entities
    var loopData = await getServerData("playCanvasUpdate");
    var loopEntities = loopData.entities;

    //match names
    for(var i = 0;i<loopEntities.length;i++)
    {
        for(var j = 0;j<realtimeEntities.length;j++)
        {
            if(loopEntities[i].name == realtimeEntities[j].Entity.name)
            {
                realtimeEntities[j].updateEntity(loopEntities[i]);     
                break;
            }
        }
    }

    //call update directly
    //app.update(dt);
    setTimeout(loop, loopDelay);
}