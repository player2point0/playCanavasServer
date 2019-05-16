var canvas;
var app;
const url = "http://localhost:8080/";
const loopDelay = 0;//takes about 15 for server response
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

    console.log(data);
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
    var camera = new pc.Entity();
    camera.addComponent('camera', {
        clearColor: new pc.Color(0.1, 0.2, 0.3)
    });

    // Create directional light entity
    var light = new pc.Entity();
    light.addComponent('light');

    // Add to hierarchy
    app.root.addChild(camera);
    app.root.addChild(light);

    // Set up initial positions and orientations
    camera.setPosition(0, 0, 3);
    light.setEulerAngles(0, 0, 0);

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
        entities.push(newEntity);
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
                var newVertexData = loopEntities[i].vertexData;
                entities[j].changeMesh(newVertexData);
                break;
            }
        }
    }

    setTimeout(loop, loopDelay);
}