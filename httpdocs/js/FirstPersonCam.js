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
            this.ray = new pc.Ray();
            
            var current = this;

            addEventListener("mousedown",  e => {
                canvas.requestPointerLock();

                // Initialise the ray and work out the direction of the ray from the a screen position
                current.ray.origin.copy(current.camera.getPosition());
                current.ray.direction.copy(current.camera.forward);
                
                // Test the ray against all the objects registered to this picker
                for (var i = 0; i < current.app.root._children.length; ++i) {
                    var pickableShape = current.app.root._children[i].aabb ;
                    var position = current.app.root._children[i].getPosition();

                    if(!pickableShape) continue;

                    var result = pickableShape.intersectsRay(current.ray, null);                    

                    if (result) {
                        console.log(position);
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