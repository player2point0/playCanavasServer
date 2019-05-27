var canvas;
var app;
var camera;
var vr;
const url = "http://169.254.100.33:8080/";//"http://localhost:8080/";
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

    //console.log("skybox:");
    //console.log(app.scene._skyboxCubeMap);

    // Fill the available space at full resolution
    app.setCanvasFillMode(pc.FILLMODE_FILL_WINDOW);
    app.setCanvasResolution(pc.RESOLUTION_AUTO);

    // Create camera entity
    camera = new FirstPersonCam(0, 0, 30, 0, 0, 0, true, app);

    // Create directional light entity
    var mainLight = new pc.Entity();
    mainLight.addComponent('light');
    mainLight.setEulerAngles(45, 0, 0);
    mainLight.setPosition(0, 0, 30);
    app.root.addChild(mainLight);

    var sideLight = new pc.Entity();
    sideLight.addComponent('light');
    sideLight.setEulerAngles(-10, -50, 0);
    sideLight.setPosition(-25, 10, 30);
    app.root.addChild(sideLight);
    

    // Add to hierarchy
    // Set up initial positions and orientations
    /*
    light.setPosition(0, -30, 10);
    light.setEulerAngles(60, 0, 0);
    */

    
    //pbr
    app.scene.defaultMaterial.useMetalness = true;
    
    app.scene.defaultMaterial.metalness = 0;
    app.scene.defaultMaterial.shininess = 100;
    
    // Resize the canvas when the window is resized
    window.addEventListener('resize', function () {
        app.resizeCanvas(canvas.width, canvas.height);
    });
}

async function serverWork()
{
    var startData = await getServerData("playCanvasStart");
    
    if(startData.vr)
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

            alert("entering vr");
            current.camera.camera.camera.enterVr(function (err) {
                if (err) {
                    var h1 = document.createElement("h1");
                    h1.innerHTML = err;
                    document.body.appendChild(h1);
                }
            });
        });         
    }

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
    //console.log(app.stats.frame);
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