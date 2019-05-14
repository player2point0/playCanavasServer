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
            var currentClass = this;

            var VertexInitialize = pc.createScript('VertexInitialize');

            VertexInitialize.prototype.initialize = function() {
    
                this.app.scene.removeModel(this.entity.model.model);
    
                currentClass.node = new pc.GraphNode();
                currentClass.material = new pc.StandardMaterial();
                //this.normals = pc.calculateNormals(this.positions, this.indices);
    
                currentClass.mesh = pc.createMesh(this.app.graphicsDevice, entityData.vertexData.position, {
                    normals: entityData.vertexData.normals,
                    uvs: entityData.vertexData.uvs,
                    indices: entityData.vertexData.indices
                });
                
                var meshInstance = new pc.MeshInstance(currentClass.node, currentClass.mesh, currentClass.material);
                
                var model = new pc.Model();
                model.graph = currentClass.node;
                model.meshInstances.push(meshInstance);
                this.entity.model.model = model;//the entity that the script is attatched to
    
                this.app.scene.addModel(this.entity.model.model);
            };
    
    
            VertexInitialize.prototype.swap = function(old) {
                this.entity.removeComponent('script');
            }
            
            this.Entity.addComponent('script');
            this.Entity.script.create(VertexInitialize);         
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
                eval(entity.script);
            };
            
            this.Entity.addComponent('script');
            this.Entity.script.create(tempScript);            
        }

        // Add to hierarchy
        this.app.root.addChild(this.Entity);
    }


    changeMesh(vertexData)
    {
        //remove the previous graphic buffers from the graphics device
        this.mesh.indexBuffer[0].destroy();
        this.mesh.vertexBuffer.destroy();

        this.mesh = pc.createMesh(this.app.graphicsDevice, vertexData.position, {
            normals: vertexData.normals,
            uvs: vertexData.uvs,
            indices: vertexData.indices
        });

        this.Entity.model.model.meshInstances[0].mesh = this.mesh;
 
        //console.log(this.Entity.model.model.meshInstances[0].mesh);
        //console.log(this.app.graphicsDevice.buffers);
    }

}