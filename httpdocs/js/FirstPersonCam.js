class FirstPersonCam{

    constructor(x, y, z, xRotate, yRotate, zRotate, activated, fxaaFlag, app)
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
            clearColor: new pc.Color(0.1, 0.2, 0.3)//background color
        });

        //storing the camera in a container and moving the container instead
        //allows you to move the camera when in vr
        this.cameraContainer = new pc.Entity();

        //draws crosshair on dom/canvas
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
            
        if(fxaaFlag) this.enableFxaa();
        //this.bokeh();

        if(activated)
        {  
            //move camera using the standard keys
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

    enableFxaa()
    {
        //--------------- POST EFFECT DEFINITION------------------------//
        pc.extend(pc, function () {

            /**
             * @name pc.FxaaEffect
             * @class Implements the FXAA post effect by NVIDIA
             * @constructor Creates new instance of the post effect.
             * @extends pc.PostEffect
             * @param {pc.GraphicsDevice} graphicsDevice The graphics device of the application
             */
            var FxaaEffect = function (graphicsDevice) {
                // Shaders
                var attributes = {
                    aPosition: pc.SEMANTIC_POSITION
                };

                var passThroughVert = [
                    "attribute vec2 aPosition;",
                    "",
                    "void main(void)",
                    "{",
                    "    gl_Position = vec4(aPosition, 0.0, 1.0);",
                    "}"
                ].join("\n");

                var fxaaFrag = [
                    "precision " + graphicsDevice.precision + " float;",
                    "",
                    "uniform sampler2D uColorBuffer;",
                    "uniform vec2 uResolution;",
                    "",
                    "#define FXAA_REDUCE_MIN   (1.0/128.0)",
                    "#define FXAA_REDUCE_MUL   (1.0/8.0)",
                    "#define FXAA_SPAN_MAX     8.0",
                    "",
                    "void main()",
                    "{",
                    "    vec3 rgbNW = texture2D( uColorBuffer, ( gl_FragCoord.xy + vec2( -1.0, -1.0 ) ) * uResolution ).xyz;",
                    "    vec3 rgbNE = texture2D( uColorBuffer, ( gl_FragCoord.xy + vec2( 1.0, -1.0 ) ) * uResolution ).xyz;",
                    "    vec3 rgbSW = texture2D( uColorBuffer, ( gl_FragCoord.xy + vec2( -1.0, 1.0 ) ) * uResolution ).xyz;",
                    "    vec3 rgbSE = texture2D( uColorBuffer, ( gl_FragCoord.xy + vec2( 1.0, 1.0 ) ) * uResolution ).xyz;",
                    "    vec4 rgbaM  = texture2D( uColorBuffer,  gl_FragCoord.xy  * uResolution );",
                    "    vec3 rgbM  = rgbaM.xyz;",
                    "    float opacity  = rgbaM.w;",
                    "",
                    "    vec3 luma = vec3( 0.299, 0.587, 0.114 );",
                    "",
                    "    float lumaNW = dot( rgbNW, luma );",
                    "    float lumaNE = dot( rgbNE, luma );",
                    "    float lumaSW = dot( rgbSW, luma );",
                    "    float lumaSE = dot( rgbSE, luma );",
                    "    float lumaM  = dot( rgbM,  luma );",
                    "    float lumaMin = min( lumaM, min( min( lumaNW, lumaNE ), min( lumaSW, lumaSE ) ) );",
                    "    float lumaMax = max( lumaM, max( max( lumaNW, lumaNE) , max( lumaSW, lumaSE ) ) );",
                    "",
                    "    vec2 dir;",
                    "    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));",
                    "    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));",
                    "",
                    "    float dirReduce = max( ( lumaNW + lumaNE + lumaSW + lumaSE ) * ( 0.25 * FXAA_REDUCE_MUL ), FXAA_REDUCE_MIN );",
                    "",
                    "    float rcpDirMin = 1.0 / ( min( abs( dir.x ), abs( dir.y ) ) + dirReduce );",
                    "    dir = min( vec2( FXAA_SPAN_MAX, FXAA_SPAN_MAX), max( vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX), dir * rcpDirMin)) * uResolution;",
                    "",
                    "    vec3 rgbA = 0.5 * (",
                    "        texture2D( uColorBuffer, gl_FragCoord.xy  * uResolution + dir * ( 1.0 / 3.0 - 0.5 ) ).xyz +",
                    "        texture2D( uColorBuffer, gl_FragCoord.xy  * uResolution + dir * ( 2.0 / 3.0 - 0.5 ) ).xyz );",
                    "",
                    "    vec3 rgbB = rgbA * 0.5 + 0.25 * (",
                    "        texture2D( uColorBuffer, gl_FragCoord.xy  * uResolution + dir * -0.5 ).xyz +",
                    "        texture2D( uColorBuffer, gl_FragCoord.xy  * uResolution + dir * 0.5 ).xyz );",
                    "",
                    "    float lumaB = dot( rgbB, luma );",
                    "",
                    "    if ( ( lumaB < lumaMin ) || ( lumaB > lumaMax ) )",
                    "    {",
                    "        gl_FragColor = vec4( rgbA, opacity );",
                    "    }",
                    "    else",
                    "    {",
                    "        gl_FragColor = vec4( rgbB, opacity );",
                    "    }",
                    "}"
                ].join("\n");

                this.fxaaShader = new pc.Shader(graphicsDevice, {
                    attributes: attributes,
                    vshader: passThroughVert,
                    fshader: fxaaFrag
                });

                // Uniforms
                this.resolution = new Float32Array(2);
            };

            FxaaEffect = pc.inherits(FxaaEffect, pc.PostEffect);

            FxaaEffect.prototype = pc.extend(FxaaEffect.prototype, {
                render: function (inputTarget, outputTarget, rect) {
                    var device = this.device;
                    var scope = device.scope;

                    this.resolution[0] = 1 / inputTarget.width;
                    this.resolution[1] = 1 / inputTarget.height;
                    scope.resolve("uResolution").setValue(this.resolution);
                    scope.resolve("uColorBuffer").setValue(inputTarget.colorBuffer);
                    pc.drawFullscreenQuad(device, outputTarget, this.vertexBuffer, this.fxaaShader, rect);
                }
            });

            return {
                FxaaEffect: FxaaEffect
            };
        }());

        //--------------- SCRIPT DEFINITION------------------------//
        var Fxaa = pc.createScript('fxaa');

        // initialize code called once per entity
        Fxaa.prototype.initialize = function() {

            console.log("Fxaa");

            this.effect = new pc.FxaaEffect(this.app.graphicsDevice);

            var queue = this.entity.camera.postEffects;
            queue.addEffect(this.effect);

            this.on('state', function (enabled) {
                if (enabled) {
                    queue.addEffect(this.effect);
                } else {
                    queue.removeEffect(this.effect);
                }
            });

            this.on('destroy', function () {
                queue.removeEffect(this.effect);
            });
        };

        this.camera.addComponent('script');
        this.camera.script.create(Fxaa);  
    }

    bokeh()
    {
                // --------------- POST EFFECT DEFINITION --------------- //
        Object.assign(pc, function () {

            /**
             * @constructor
             * @name pc.BokehEffect
             * @classdesc Implements the BokehEffect post processing effect
             * @description Creates new instance of the post effect.
             * @extends pc.PostEffect
             * @param {pc.GraphicsDevice} graphicsDevice The graphics device of the application
             * @property {Number} maxBlur The maximum amount of blurring. Ranges from 0 to 1
             * @property {Number} aperture Bigger values create a shallower depth of field
             * @property {Number} focus Controls the focus of the effect
             * @property {Number} aspect Controls the blurring effect
             */
            var BokehEffect = function (graphicsDevice) {
                pc.PostEffect.call(this, graphicsDevice);

                this.needsDepthBuffer = true;

                /**
                * Shader author: alteredq / http://alteredqualia.com/
                * Depth-of-field shader with bokeh
                * ported from GLSL shader by Martins Upitis
                * http://artmartinsh.blogspot.com/2010/02/glsl-lens-blur-filter-with-bokeh.html
                */
                this.shader = new pc.Shader(graphicsDevice, {
                    attributes: {
                        aPosition: pc.SEMANTIC_POSITION
                    },
                    vshader: [
                        "attribute vec2 aPosition;",
                        "",
                        "varying vec2 vUv0;",
                        "",
                        "void main(void)",
                        "{",
                        "    gl_Position = vec4(aPosition, 0.0, 1.0);",
                        "    vUv0 = (aPosition.xy + 1.0) * 0.5;",
                        "}"
                    ].join("\n"),
                    fshader: [
                        "precision " + graphicsDevice.precision + " float;",
                        "",
                        "varying vec2 vUv0;",
                        "",
                        "uniform sampler2D uColorBuffer;",
                        "uniform sampler2D uDepthMap;",
                        "",
                        "uniform float uMaxBlur;",  // max blur amount
                        "uniform float uAperture;", // uAperture - bigger values for shallower depth of field
                        "",
                        "uniform float uFocus;",
                        "uniform float uAspect;",
                        "",
                        "void main()",
                        "{",
                        "    vec2 aspectCorrect = vec2( 1.0, uAspect );",
                        "",
                        "    vec4 depth1 = texture2D( uDepthMap, vUv0 );",
                        "",
                        "    float factor = depth1.x - uFocus;",
                        "",
                        "    vec2 dofblur = vec2 ( clamp( factor * uAperture, -uMaxBlur, uMaxBlur ) );",
                        "",
                        "    vec2 dofblur9 = dofblur * 0.9;",
                        "    vec2 dofblur7 = dofblur * 0.7;",
                        "    vec2 dofblur4 = dofblur * 0.4;",
                        "",
                        "    vec4 col;",
                        "",
                        "    col  = texture2D( uColorBuffer, vUv0 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.0,   0.4  ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.15,  0.37 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.29,  0.29 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.37,  0.15 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.40,  0.0  ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.37, -0.15 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.29, -0.29 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.15, -0.37 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.0,  -0.4  ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.15,  0.37 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.29,  0.29 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.37,  0.15 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.4,   0.0  ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.37, -0.15 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.29, -0.29 ) * aspectCorrect ) * dofblur );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.15, -0.37 ) * aspectCorrect ) * dofblur );",
                        "",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.15,  0.37 ) * aspectCorrect ) * dofblur9 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.37,  0.15 ) * aspectCorrect ) * dofblur9 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.37, -0.15 ) * aspectCorrect ) * dofblur9 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.15, -0.37 ) * aspectCorrect ) * dofblur9 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.15,  0.37 ) * aspectCorrect ) * dofblur9 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.37,  0.15 ) * aspectCorrect ) * dofblur9 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.37, -0.15 ) * aspectCorrect ) * dofblur9 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.15, -0.37 ) * aspectCorrect ) * dofblur9 );",
                        "",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.29,  0.29 ) * aspectCorrect ) * dofblur7 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.40,  0.0  ) * aspectCorrect ) * dofblur7 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.29, -0.29 ) * aspectCorrect ) * dofblur7 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.0,  -0.4  ) * aspectCorrect ) * dofblur7 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.29,  0.29 ) * aspectCorrect ) * dofblur7 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.4,   0.0  ) * aspectCorrect ) * dofblur7 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.29, -0.29 ) * aspectCorrect ) * dofblur7 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.0,   0.4  ) * aspectCorrect ) * dofblur7 );",
                        "",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.29,  0.29 ) * aspectCorrect ) * dofblur4 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.4,   0.0  ) * aspectCorrect ) * dofblur4 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.29, -0.29 ) * aspectCorrect ) * dofblur4 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.0,  -0.4  ) * aspectCorrect ) * dofblur4 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.29,  0.29 ) * aspectCorrect ) * dofblur4 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.4,   0.0  ) * aspectCorrect ) * dofblur4 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2( -0.29, -0.29 ) * aspectCorrect ) * dofblur4 );",
                        "    col += texture2D( uColorBuffer, vUv0 + ( vec2(  0.0,   0.4  ) * aspectCorrect ) * dofblur4 );",
                        "",
                        "    gl_FragColor = col / 41.0;",
                        "    gl_FragColor.a = 1.0;",
                        "}"
                    ].join("\n")
                });

                // Uniforms
                this.maxBlur = 1;
                this.aperture = 0.025;
                this.focus = 1;
                this.aspect = 1;
            };

            BokehEffect.prototype = Object.create(pc.PostEffect.prototype);
            BokehEffect.prototype.constructor = BokehEffect;

            Object.assign(BokehEffect.prototype, {
                render: function (inputTarget, outputTarget, rect) {
                    var device = this.device;
                    var scope = device.scope;

                    scope.resolve("uMaxBlur").setValue(this.maxBlur);
                    scope.resolve("uAperture").setValue(this.aperture);
                    scope.resolve("uFocus").setValue(this.focus);
                    scope.resolve("uAspect").setValue(this.aspect);
                    scope.resolve("uColorBuffer").setValue(inputTarget.colorBuffer);
                    scope.resolve("uDepthMap").setValue(this.depthMap);
                    pc.drawFullscreenQuad(device, outputTarget, this.vertexBuffer, this.shader, rect);
                }
            });

            return {
                BokehEffect: BokehEffect
            };
        }());

        // ----------------- SCRIPT DEFINITION ------------------ //
        var Bokeh = pc.createScript('bokeh');

        Bokeh.attributes.add('maxBlur', {
            type: 'number',
            default: 1,
            min: 0,
            max: 1,
            precision: 5,
            title: 'Max Blur'
        });

        Bokeh.attributes.add('aperture', {
            type: 'number',
            default: 0.025,
            min: 0,
            max: 1,
            precision: 5,
            title: 'Aperture'
        });

        Bokeh.attributes.add('focus', {
            type: 'number',
            default: 1,
            title: 'Focus'
        });

        Bokeh.attributes.add('aspect', {
            type: 'number',
            default: 1,
            title: 'Aspect'
        });

        Bokeh.prototype.initialize = function () {
            this.effect = new pc.BokehEffect(this.app.graphicsDevice);
            this.effect.maxBlur = this.maxBlur;
            this.effect.aperture = this.aperture;
            this.effect.focus = this.focus;
            this.effect.aspect = this.aspect;

            this.on('attr', function (name, value) {
                this.effect[name] = value;
            }, this);

            var queue = this.entity.camera.postEffects;

            queue.addEffect(this.effect);

            this.on('state', function (enabled) {
                if (enabled) {
                    queue.addEffect(this.effect);
                } else {
                    queue.removeEffect(this.effect);
                }
            });

            this.on('destroy', function () {
                queue.removeEffect(this.effect);
            });
        };

        this.camera.addComponent('script');
        this.camera.script.create(Bokeh); 

    }
}