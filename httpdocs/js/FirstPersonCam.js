class FirstPersonCam{

    constructor(x, y, z, xRotate, yRotate, zRotate, activated, raycast, app)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRotate = xRotate;
        this.yRotate = yRotate;
        this.zRotate = zRotate;
        this.app = app;
        
        this.camera = new pc.Entity();
        this.camera.addComponent('camera', {
            clearColor: new pc.Color(0.1, 0.2, 0.3)
        });
        

        if(raycast)
        {
            //could sort by distance

            this.reticle = new pc.Entity();
            this.reticle.addComponent("model", {
                type: "sphere"
            });
            this.reticle.setLocalScale(0.5, 0.5, 0.5);

            this.app.root.addChild(this.reticle);

            this.ray = new pc.Ray();
            
            var current = this;

            addEventListener("mousedown",  e => {
                canvas.requestPointerLock();

                // Initialise the ray and work out the direction of the ray from the a screen position
                current.ray.origin.copy(current.camera.getPosition());
                current.ray.direction.copy(current.camera.forward);
                
                var pickable = [];
                var distancePickables = [];
                
                // all objects with a collider
                for (var i = 0; i < current.app.root._children.length; ++i) 
                {
                    var pickableShape = current.app.root._children[i];

                    if(pickableShape.aabb) pickable.push(pickableShape);
                }

                distancePickables = pickable.sort(function(a, b){
                    var aPos = a.getPosition(); 
                    var bPos = b.getPosition(); 
                
                    var aDiff = aPos.sub(current.camera.getPosition());
                    var bDiff = bPos.sub(current.camera.getPosition());
                
                    var aDis = Math.abs(aDiff.length());
                    var bDis = Math.abs(bDiff.length());

                    return aDis - bDis;
                });

                
                for (var i = 0; i < distancePickables.length; ++i) {
                    var center = distancePickables[i].getPosition();
                    var pickableShape = distancePickables[i].aabb;
                    pickableShape.center = center;

                    var hit = new pc.Vec3();
                    var result = pickableShape.intersectsRay(current.ray, hit);                    

                    if (result) 
                    {
                        this.reticle.setPosition(hit);
                        console.log(hit);
                        break;
                    }  
                }    
            }); 

        }

        if(activated)
        {
            //first person camera  
            addEventListener("mousemove", e => {
                
                // If pointer is disabled
                // If the left mouse button is down update the camera from mouse movement
                if (pc.Mouse.isPointerLocked() || e.buttons[0]) 
                {
                    var speed = 0.1;

                    this.camera.eulerAngles.x = this.camera.eulerAngles.x - speed * e.movementY;
                    this.camera.eulerAngles.y = this.camera.eulerAngles.y - speed * e.movementX;

                    this.camera.setEulerAngles(this.camera.eulerAngles.x, this.camera.eulerAngles.y, 0);

                    //console.log("rotation "+this.camera.eulerAngles);
                }
            });
            
            addEventListener("mousedown",  e => {
                canvas.requestPointerLock()
            });   

            addEventListener("keypress", e => {
                
                var speed = 1;
                var forward = this.camera.forward;
                var right = this.camera.right;
                var up = this.camera.up;

                if(e.key == "w")
                {
                    this.x += forward.x * speed;
                    this.z += forward.z * speed;
                }
                if(e.key == "s")
                {
                    this.x -= forward.x * speed;
                    this.z -= forward.z * speed;
                }
                if(e.key == "a")
                {
                    this.x -= right.x * speed;
                    this.z -= right.z * speed;
                }
                if(e.key == "d")
                {
                    this.x += right.x * speed;
                    this.z += right.z * speed;
                }
                if(e.key == "q")
                {
                    this.y += up.y * speed;
                }
                if(e.key == "e")
                {
                    this.y -= up.y * speed;
                }

                this.camera.setPosition(this.x, this.y, this.z);
                
                //console.log("position "+this.camera.position);
            });
        }

        app.root.addChild(this.camera);
        this.camera.setPosition(this.x, this.y, this.z);
    }

}