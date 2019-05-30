var canvas;
var app;
var camera;
var vr;
const host = window.location.hostname; //"http://169.254.100.33:8080/";//"http://localhost:8080/";
const port = window.location.port;
var currentPage = window.location.pathname;
const loopDelay = 100;//takes about 15 for server response
var sceneEntity;
var entities = [];
var realtimeEntities = [];

var lastVRButton = Date.now();

boilerPlate();
serverWork();
vrBoilerPlate();
setTimeout(loop, loopDelay);

function changeScene(newPage)
{
    //clear current scene
    sceneEntity.destroy();
    currentPage = "/"+newPage;
    serverWork();
}

async function getServerData(endpoint)
{
    var url1 = "http://"+host+":"+port+currentPage+"/"+endpoint;
    let response = await fetch(url1);
    
    let data = await response.json();

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
    camera = new FirstPersonCam(0, 5, 0, 0, 0, 0, false, app);

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

    // Add to hierarchy
    // Set up initial positions and orientations
    /*
    light.setPosition(0, -30, 10);
    light.setEulerAngles(60, 0, 0);
    */
    
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
        if (current.app.vr && current.app.vr.display) {
            current.app.vr.display.on("presentchange", current._onVrPresentChange, current);
            if (current.app.vr.display.display.bufferScale_) current.app.vr.display.display.bufferScale_ = 0.5;
        }

        if(!current.app.vr.display) return;

        alert("entering vr");
        current.camera.camera.camera.enterVr(function (err) {
            if (err) {
                var h1 = document.createElement("h1");
                h1.innerHTML = err;
                document.body.appendChild(h1);
            }

            else
            {              
                window.addEventListener("gamepadconnected", function(e) {
                    
                    var beam = new pc.Entity();
                    beam.addComponent("model", {
                        type: "sphere"
                    });
                    beam.setLocalScale(0.1, 0.1, 0.1);
                    
                    current.app.root.addChild(beam);
                    
                    var gamePadController = pc.createScript("gamePadController");
                    gamePadController.prototype.update = function (dt) {            
            
                        var gp = current.navigator.getGamepads()[0];           

                        if(gp.pose.hasOrientation)
                        {
                            if(gp.pose.orientation)
                            {
                                var pitch = gp.pose.orientation[0];
                                var yaw = gp.pose.orientation[1];
                                var roll = gp.pose.orientation[2];
                                var w = gp.pose.orientation[3];

                                var rotation = new pc.Quat(pitch, yaw, roll, w);

                                var v = new pc.Vec3(0, 0, -1);

                                var controllerVector = rotation.transformVector(v);
                                var direction = rotation.transformVector(v);
                                controllerVector.add(current.camera.camera.getPosition());

                                beam.setPosition(controllerVector);

                                //add better delay between button presses - detect button up
                                if((current.lastVRButton + 200) > Date.now()) return;

                                if(gp.buttons[0].value > 0 || gp.buttons[0].pressed == true || gp.buttons[1].value > 0 || gp.buttons[1].pressed == true)
                                {
                                    current.lastVRButton = Date.now();
                                    var result = current.raycast(current.camera.camera.getPosition(), direction, current);

                                    if(!result) return;

                                    if(result.obj.Entity.name == "ground")
                                    {
                                        var hitX = result.hit.x;
                                        var camY = current.camera.y;
                                        var hitZ = result.hit.z;

                                        current.camera.cameraContainer.setPosition(hitX, camY, hitZ);                    
                                        return;   
                                    }

                                    var link = result.obj.clickLink;
                                    
                                    if(link)
                                    {
                                        changeScene(link);
                                        return;    
                                    } 
                                }
                            }
                        }
                    };
            
                    current.camera.camera.addComponent('script');
                    current.camera.camera.script.create(gamePadController);  
                });
            }
        });
    });
    
     
    var current = this;

    addEventListener("mousedown",  e => {

        var pointerLockElement = document.pointerLockElement;

        canvas.requestPointerLock();

        if(pointerLockElement == null) return;

        var result = raycast(current.camera.cameraContainer.getPosition(), current.camera.camera.forward, current)

        if(!result) return;

        if(result.obj.Entity.name == "ground")
        {
            var hitX = result.hit.x;
            var camY = current.camera.y;
            var hitZ = result.hit.z;

            current.camera.cameraContainer.setPosition(hitX, camY, hitZ);                    
            return;   
        }

        var link = result.obj.clickLink;
        
        if(link)
        {
            changeScene(link);
            return;    
        } 
    });
}

async function serverWork()
{
    var startData = await getServerData("playCanvasStart");
    sceneEntity = new pc.Entity();

    var serverEntities = startData.entities;    

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
            realtimeEntities.push(newEntity);
        }
    }

    this.app.root.addChild(sceneEntity);
}

function raycast(origin, direction, current)
{
    // Initialise the ray and work out the direction of the ray from the a screen position
    this.ray = new pc.Ray();  

    current.ray.origin.copy(origin);
    current.ray.direction.copy(direction);

    var pickable = [];
    var distancePickables = [];

    // all objects with a collider
    for (var i = 0; i < entities.length; ++i) 
    {
        var pickableShape = entities[i];

        if(pickableShape.Entity.aabb) pickable.push(pickableShape);
    }

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
        var center = distancePickables[i].Entity.getPosition();
        var pickableShape = distancePickables[i].Entity.aabb;
        pickableShape.center = center;

        var hit = new pc.Vec3();
        var result = pickableShape.intersectsRay(current.ray, hit);                    

        if (result) 
        {
            var output = {
                obj: distancePickables[i],
                hit: hit
            };

            return output;        
        }  
    }    
}

async function loop()
{
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