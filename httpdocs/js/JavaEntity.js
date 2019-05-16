class JavaEntity
{

    constructor(entityData, app) {

        this.app = app;
        this.Entity = new pc.Entity();
        this.x = 0;
        this.y = 0;
        this.z = 0;

        if(entityData.model)
        {
            this.Entity.addComponent('model', {
                type: entityData.model
            });
        }

        if(entityData.vertexData)
        {
            this.node = new pc.GraphNode();
            this.material = new pc.StandardMaterial();
            this.vertexPos = entityData.vertexData.position;

            this.mesh = pc.createMesh(this.app.graphicsDevice, this.vertexPos, {
                normals: pc.calculateNormals(this.vertexPos, entityData.vertexData.indices),
                uvs: entityData.vertexData.uvs,
                indices: entityData.vertexData.indices
            });
            
            var meshInstance = new pc.MeshInstance(this.node, this.mesh, this.material);
            
            var model = new pc.Model();
            model.graph = this.node;
            model.meshInstances.push(meshInstance);
            this.Entity.model.model = model;
        }

        if(entityData.name)
        {
            this.Entity.name = entityData.name;
        }

        if(entityData.x) this.x = entityData.x;
        if(entityData.y) this.y = entityData.y;
        if(entityData.z) this.z = entityData.z;

        this.Entity.setPosition(this.x, this.y, this.z);

        if(entityData.script)
        {
            var scriptName = "default"; 

            if(entityData.scriptName)
            {
                scriptName = entityData.scriptName;
            }
            
            var tempScript = pc.createScript(scriptName);
            tempScript.prototype.update = function (dt) {
                eval(entityData.script);
            };
            
            this.Entity.addComponent('script');
            this.Entity.script.create(tempScript);            
        }

        if(entityData.texture)
        {              
            /*  
            if(entityData.texture)
            {
                var m = this.Entity.model.model.meshInstances[0].material;
                console.log(m);
                m.diffuseMap = getTexture();
                m.update();
    
                function getTexture () {
                    var texture = new pc.gfx.Texture(app.graphicsDevice);
                    
                    var img = new Image();
                    img.onload = function () {
                        texture.minFilter = pc.gfx.FILTER_LINEAR;
                        texture.magFilter = pc.gfx.FILTER_LINEAR;
                        texture.addressU = pc.gfx.ADDRESS_CLAMP_TO_EDGE;
                        texture.addressV = pc.gfx.ADDRESS_CLAMP_TO_EDGE;
                        texture.setSource(img);
                    };
                    img.src = entityData.texture;
                    return texture;
                }
                
            }
            */
        }

        // Add to hierarchy
        this.app.root.addChild(this.Entity);
    }

    changeMesh(vertexData)
    {
        //remove the previous graphic buffers from the graphics device
        this.mesh.indexBuffer[0].destroy();
        this.mesh.vertexBuffer.destroy();

        this.vertexPos = vertexData.position;

        this.mesh = pc.createMesh(this.app.graphicsDevice, this.vertexPos, {
            normals: pc.calculateNormals(this.vertexPos, vertexData.indices),
            uvs: vertexData.uvs,
            indices: vertexData.indices
        });

        this.Entity.model.model.meshInstances[0].mesh = this.mesh;
 
        //console.log(this.Entity.model.model.meshInstances[0].mesh);
        //console.log(this.app.graphicsDevice.buffers);
    }

    morphMesh(finalVertetxMesh, steps)
    {
        var tempVertex = this.vertexPos;
        var morphStep = [];//disance to add each time

        //calculate the difference
        for(var j = 0;j<tempVertex.length;j++)
        {
            var dis = finalVertetxMesh.position[j] - tempVertex[j];
            morphStep.push(dis / steps);
        }

        for(var i = 0;i<steps;i++)
        {
            //move the vertex slightly towards the new pos
            for(var k = 0;k<tempVertex.length;k++)
            {
                
            }   
        
        }
    }

}