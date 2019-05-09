var canvas;
var app;
const url = "http://localhost:8080/";

boilerPlate();
serverWork();

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
    light.setEulerAngles(45, 0, 0);

    // Resize the canvas when the window is resized
    window.addEventListener('resize', function () {
        app.resizeCanvas(canvas.width, canvas.height);
    });
}

async function serverWork()
{
    var startData = await getServerData("playCanvasStart");
    var entities = startData.entities;    

    for(var i = 0;i<entities.length;i++)
    {
        var entity = entities[i];
        var tempEntity = new pc.Entity();

        if(entity.model)
        {
            tempEntity.addComponent('model', {
                type: entity.model
            });
        }

        // Add to hierarchy
        app.root.addChild(tempEntity);

        if(entity.script)
        {
            var scriptName = "default"; 

            if(entity.scriptName)
            {
                scriptName = entity.scriptName;
            }

            var tempScript = pc.createScript(scriptName);
            tempScript.prototype.update = function (dt) {
                eval(entity.script);
            };

            tempEntity.addComponent('script');
            tempEntity.script.create(scriptName);
        }
    }
}
 