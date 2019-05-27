class JavaEntity
{

    constructor(entityData, app) {
        
        this.app = app;
        this.Entity = new pc.Entity();
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.xRotate = 0;
        this.yRotate = 0;
        this.zRotate = 0;
        this.xScale = 1;
        this.yScale = 1;
        this.zScale = 1;
        this.textureURL = "";

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

        if(entityData.xRotate) this.xRotate = entityData.xRotate;
        if(entityData.yRotate) this.yRotate = entityData.yRotate;
        if(entityData.zRotate) this.zRotate = entityData.zRotate;
        this.Entity.setEulerAngles(this.xRotate, this.yRotate, this.zRotate);

        if(entityData.xScale) this.xScale = entityData.xScale;
        if(entityData.yScale) this.yScale = entityData.yScale;
        if(entityData.zScale) this.zScale = entityData.zScale;
        this.Entity.setLocalScale(this.xScale, this.yScale, this.zScale);

        //for raycasting
        if(entityData.boundingBoxX && entityData.boundingBoxY && entityData.boundingBoxZ)
        {
            //	Half the distance across the box in each axis
            var center = new pc.Vec3(this.x, this.y, this.z);
            var bounds = new pc.Vec3(entityData.boundingBoxX, entityData.boundingBoxY, entityData.boundingBoxZ);
            this.Entity.aabb = new pc.BoundingBox(center, bounds);
            this._min = new pc.Vec3;
            this._max = new pc.Vec3;
        }

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

        if(entityData.textureURL)
        {
            this.textureURL = entityData.textureURL;
        }

        if(entityData.texture)
        {   
            this.texture = new pc.Texture( this.app.graphicsDevice);

            this.texture.minFilter = pc.FILTER_LINEAR;
            this.texture.magFilter = pc.FILTER_LINEAR;
            this.texture.addressU = pc.ADDRESS_CLAMP_TO_EDGE;
            this.texture.addressV = pc.ADDRESS_CLAMP_TO_EDGE;

            this.img = document.createElement( 'img' );
            this.img.src = entityData.texture;
            this.img.crossOrigin = 'anonymous';
            this.img.onload = ( e ) => {
                this.texture.setSource( this.img );
            };

            this.Entity.model.model.meshInstances[0].material.diffuseMap = this.texture;
            this.Entity.model.model.meshInstances[0].material.opacityMap = this.texture;
            
            this.Entity.model.model.meshInstances[0].material.blendType = pc.BLEND_NORMAL;
            
            this.Entity.model.model.meshInstances[0].material.update();   
        }        

        if(entityData.sketchFabFolder)
        {
            var current = this;

            app.assets.loadFromUrl('./sketchFab/'+entityData.sketchFabFolder+'/scene.gltf', 'json', function (err, asset) {
            
                var json = asset.resource;
                var gltf = JSON.parse(json);

                loadGltf(gltf, app.graphicsDevice, function (model, textures, animationClips) {
                    current.Entity.model.model = model;
                }, {
                    basePath: './sketchFab/'+entityData.sketchFabFolder+'/'//path for textures and bin
                });
            });
        }


        // Add to hierarchy
        this.app.root.addChild(this.Entity);
    }

    updateEntity(entityData)
    {
        if(entityData.vertexData)
        {
            this.changeMesh(entityData.vertexData);
        }

        if(entityData.x) this.x = entityData.x;
        if(entityData.y) this.y = entityData.y;
        if(entityData.z) this.z = entityData.z;
        this.Entity.setPosition(this.x, this.y, this.z);

        if(entityData.xRotate) this.xRotate = entityData.xRotate;
        if(entityData.yRotate) this.yRotate = entityData.yRotate;
        if(entityData.zRotate) this.zRotate = entityData.zRotate;
        this.Entity.setEulerAngles(this.xRotate, this.yRotate, this.zRotate);

        if(entityData.xScale) this.xScale = entityData.xScale;
        if(entityData.yScale) this.yScale = entityData.yScale;
        if(entityData.zScale) this.zScale = entityData.zScale;
        this.Entity.setLocalScale(this.xScale, this.yScale, this.zScale);

        if(entityData.texture)
        {
            this.changeTexture(entityData.texture);   
        }
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

    changeTexture(texture)
    {
        if(texture)
        {
            this.img.src = texture;
            this.img.onload = ( e ) => {
                this.texture.setSource( this.img );
            };
    
            this.Entity.model.model.meshInstances[0].material.diffuseMap = this.texture;
            this.Entity.model.model.meshInstances[0].material.opacityMap = this.texture;
                        
            this.Entity.model.model.meshInstances[0].material.update();             
        }
    }

    //untested
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
                tempVertex[k] += morphStep[k];
            }   

            tempVertexData = {
                position: tempVertex,
                uvs: finalVertetxMesh.uvs,
                undices: finalVertetxMesh.indices
            };

            changeMesh(tempVertexData);
        }
    }

    async getTextureFromURL(url)
    {
        var url = "http://169.254.100.32:8080/captureKinectImage";
    
        fetch(url).then((response) => {
          response.arrayBuffer().then((buffer) => {
            var base64Flag = 'data:image/jpeg;base64,';
            var imageStr = arrayBufferToBase64(buffer);
            
            return base64Flag+imageStr;
          });
        });
        
        function arrayBufferToBase64(buffer) {
          var binary = '';
          var bytes = [].slice.call(new Uint8Array(buffer));
        
          bytes.forEach((b) => binary += String.fromCharCode(b));
        
          return window.btoa(binary);
        };
    }

}