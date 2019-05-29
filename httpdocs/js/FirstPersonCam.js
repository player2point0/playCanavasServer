class FirstPersonCam{

    constructor(x, y, z, xRotate, yRotate, zRotate, activated, app)
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

        this.cameraContainer = new pc.Entity();

        var crosshair = document.createElement("h1");
        crosshair.innerHTML = "+";
        crosshair.style = "position: absolute;"+
            "color: red;"+
            "top: 50%;"+
            "left: 50%;"+
            "transform: translate(-50%, -50%);";
        document.body.appendChild(crosshair);
        
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
            
        if(activated)
        {
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

                this.cameraContainer.setPosition(this.x, this.y, this.z);
                
                //console.log("position "+this.camera.position);
            });
        }

        this.cameraContainer.addChild(this.camera);
        app.root.addChild(this.cameraContainer);
        this.camera.setPosition(0,0,0);
        this.cameraContainer.setPosition(this.x, this.y, this.z);
    }

}