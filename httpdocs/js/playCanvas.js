var canvas;
var app;
var camera;
var camRotationX = 0;
var camRotationY = 0;
var camX = 0;
var camZ = 0;
const url = "http://localhost:8080/";
const loopDelay = 100;//takes about 15 for server response
var entities = [];

var lastTime = Date.now();
var total = 0;
var count = 0;

boilerPlate();
serverWork();
setTimeout(loop, loopDelay);

async function getServerData(endpoint)
{
    let response = await fetch(url+endpoint);
    
    let data = await response.json();

    return data;
}

function boilerPlate()
{
    // Create a PlayCanvas application
    canvas = document.getElementById("application-canvas");
    app = new pc.Application(canvas, {});
    app.start();

    // Fill the available space at full resolution
    app.setCanvasFillMode(pc.FILLMODE_FILL_WINDOW);
    app.setCanvasResolution(pc.RESOLUTION_AUTO);

    // Create camera entity
    camera = new pc.Entity();
    camera.addComponent('camera', {
        clearColor: new pc.Color(0.1, 0.2, 0.3)
    });
    
    
    //first person camera
    //this.force = new pc.Vec3();
    //this.camera = null;
    //this.eulers = new pc.Vec3();
    
    // Listen for mouse move events
    addEventListener("mousemove", function (e) {
        // If pointer is disabled
        // If the left mouse button is down update the camera from mouse movement
        if (pc.Mouse.isPointerLocked() || e.buttons[0]) 
        {
            camera.eulerAngles.x = camera.eulerAngles.x - 0.1 * e.movementY;
            camera.eulerAngles.y = camera.eulerAngles.y - 0.1 * e.movementX;

            camera.setEulerAngles(camera.eulerAngles.x, camera.eulerAngles.y, 0);
        }
    });

    addEventListener("mousedown", function () {
        canvas.requestPointerLock()
    });
    
    addEventListener("keypress", function(e){
        
        console.log(camera.eulerAngles);

        var speed = 1;
        var forward = this.camera.forward;
        var right = this.camera.right;

        if(e.key == "w")
        {
            camX += forward.x * speed;
            camZ += forward.z * speed;
        }
        if(e.key == "s")
        {
            camX -= forward.x * speed;
            camZ -= forward.z * speed;
        }
        if(e.key == "a")
        {
            camX -= right.x * speed;
            camZ -= right.z * speed;
        }
        if(e.key == "d")
        {
            camX += right.x * speed;
            camZ += right.z * speed;
        }

        camera.setPosition(camX, camera.position.y, camZ);
    
        console.log(camera.eulerAngles);
    });


    // Create directional light entity
    var light = new pc.Entity();
    light.addComponent('light');

    // Add to hierarchy
    app.root.addChild(camera);
    app.root.addChild(light);

    camera.setPosition(10, 0, 30);
    light.setEulerAngles(45, 0, 0);
    // Set up initial positions and orientations
    /*
    camera.setPosition(0, -30, 10);
    camera.setEulerAngles(70, 0, 0);

    light.setPosition(0, -30, 10);
    light.setEulerAngles(60, 0, 0);
    */

    // Resize the canvas when the window is resized
    window.addEventListener('resize', function () {
        app.resizeCanvas(canvas.width, canvas.height);
    });
}

async function serverWork()
{
    var startData = await getServerData("playCanvasStart");
    var serverEntities = startData.entities;    

    for(var i = 0;i<serverEntities.length;i++)
    {
        var entity = serverEntities[i];
        var newEntity = new JavaEntity(entity, app); 
        
        if((entity.name != undefined) && (newEntity.Entity.name == "Untitled")) 
        {
            //bug fix for first entity
            newEntity.Entity.name = entity.name;
        }

        if(entity.realtimeModel)
        {
            entities.push(newEntity);
        }
    }
}

async function loop()
{
    /*
    var endTime = Date.now(); 
    total += endTime - lastTime;
    count++;
    console.log(total / count);

    lastTime = endTime;
    */
    var loopData = await getServerData("playCanvasUpdate");
    var loopEntities = loopData.entities;

    //match names
    for(var i = 0;i<loopEntities.length;i++)
    {
        for(var j = 0;j<entities.length;j++)
        {
            if(loopEntities[i].name == entities[j].Entity.name)
            {
                entities[j].updateEntity(loopEntities[i]);
                
                break;
            }
        }
    }

    //call update directly
    //app.update(dt);

    setTimeout(loop, loopDelay);
}