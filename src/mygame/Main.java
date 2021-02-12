package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import com.jme3.util.mikktspace.MikktspaceTangentGenerator;
import com.sun.corba.se.spi.extension.ZeroPortPolicy;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    private static final float DAY_TIME_RATION=2f;
    private static final float RISE_SUNSET_OFFSET=0.1f; // not to show much light when near horizontal lighing

    private AmbientLight al0;
    private DirectionalLight dl0;
    private float[] sunRotation=new float[]{0,0,FastMath.PI-FastMath.QUARTER_PI}; //Starting near to the sunrise
    private Quaternion sunRotationQuat=new Quaternion();
    
    private ColorRGBA nightColor=ColorRGBA.Black;
    private ColorRGBA riseColor=new ColorRGBA(0.2f,0.2f,0.5f,1f);
    private ColorRGBA noonColor=ColorRGBA.White;
    private ColorRGBA sunsetColor=ColorRGBA.Orange;
    
    private ColorRGBA ambientNightColor=ColorRGBA.White.mult(0.1f);
    private ColorRGBA ambientRiseColor=riseColor.mult(0.2f);
    private ColorRGBA ambientNoonColor=noonColor.mult(0.2f);
    private ColorRGBA ambientSunsetColor=ColorRGBA.Red.mult(0.2f);

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //Remove mouse catching
        flyCam.setDragToRotate(true);
        cam.setLocation(new Vector3f(15,20,46));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        
        //Set lights scene
        PointLight pl0=new PointLight(new Vector3f(0,3,0), ColorRGBA.White.mult(.5f));
        pl0.setRadius(25);
        rootNode.addLight(pl0);
        
        al0=new AmbientLight(ColorRGBA.White.mult(0.1f));
        dl0=new DirectionalLight(Vector3f.UNIT_X, ColorRGBA.White);
        rootNode.addLight(al0);
        rootNode.addLight(dl0);

        //Build mats
        Material matWhiteUnshaded = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matWhiteUnshaded.setColor("Color", ColorRGBA.White);
        
        Material matWhite = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        Material matRed = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matRed.setBoolean("UseMaterialColors", true);
        matRed.setColor("Diffuse", ColorRGBA.Red);
        
        Material matGreen = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matGreen.setBoolean("UseMaterialColors", true);
        matGreen.setColor("Diffuse", ColorRGBA.Blue);

        Material matBlue = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matBlue.setBoolean("UseMaterialColors", true);
        matBlue.setColor("Diffuse", ColorRGBA.Blue);
        
        Material matGrass = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matGrass.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/splat/grass.jpg"));
        matGrass.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg"));
        
        Material matPond = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.j3m");
        
        Material matRock = assetManager.loadMaterial("Textures/Terrain/Rock/Rock.j3m");
        
        Material matRoad = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matGrass.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/splat/road.jpg"));
        matGrass.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/splat/road_normal.png"));
        
        //For the floor only
        Material matBrickWall = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m");
        
        
        //Build Scene
        float sphereRadius=3f;
        Sphere s = new Sphere(16,16,sphereRadius);
        TangentBinormalGenerator.generate(s);
        
        //Add a small sphare to show the point light position
        Sphere sLight = new Sphere(16,16,1);
        Geometry plSphere=new Geometry("plSphere", sLight);
        plSphere.setLocalTranslation(pl0.getPosition());
        plSphere.setMaterial(matWhiteUnshaded);
        rootNode.attachChild(plSphere);
        
        Material []materials=new Material[]{matWhite, matRed, matGreen, matBlue, matGrass, matPond, matRock, matRoad};
        
        //Add plenty of spheres with different positions and materials
        /*
        Geometry geom = new Geometry("S0", s);
        geom.setMaterial(matBlue);
        rootNode.attachChild(geom);
        */
        
        float distanceOffset=10f;
        for(int i=0;i<9;++i)
        {
            for(int j=0;j<9;++j)
            {
                int id=9*i+j;
                Material mat=materials[id%materials.length];
                Geometry geom = new Geometry("S"+id, s);
                geom.setMaterial(mat);
                geom.setLocalTranslation(i*distanceOffset-4.5f*distanceOffset, sphereRadius, j*distanceOffset-4.5f*distanceOffset);
                rootNode.attachChild(geom);
                
            }
        }
        
        //Add the floor also
        Box floor=new Box(new Vector3f(0,-0.1f,0), 100, 0.1f, 100);
        floor.scaleTextureCoordinates(new Vector2f(10,10));
        TangentBinormalGenerator.generate(floor);
        Geometry floorGeom=new Geometry("floor", floor);
        floorGeom.setMaterial(matBrickWall);
        rootNode.attachChild(floorGeom);
        
        
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        //Rotate the sun
        sunRotation[2]+=tpf/DAY_TIME_RATION;
        sunRotation[2]%=FastMath.TWO_PI;
        sunRotationQuat.fromAngles(sunRotation);
        
        dl0.setDirection(sunRotationQuat.mult(Vector3f.UNIT_X));
        
        //Set colors according to current "time"
        // from 0 to PI it's sun under horizon (night)
        if(sunRotation[2]<=FastMath.PI)
        {
            dl0.setColor(this.nightColor);
        }
        //Otherwise it's day time
        else
        {
            float rangeStart=0, rangeEnd=0;
            ColorRGBA sunColorStart=ColorRGBA.Black, sunColorEnd=ColorRGBA.Black;
            ColorRGBA ambientColorStart=ColorRGBA.Black, ambientColorEnd=ColorRGBA.Black;
            
            //Rise
            if(FastMath.PI<sunRotation[2] && sunRotation[2]<=FastMath.PI+RISE_SUNSET_OFFSET)
            {
                //From night to rise 
                rangeStart=FastMath.PI;
                rangeEnd=FastMath.PI+RISE_SUNSET_OFFSET;
                sunColorStart=nightColor;
                sunColorEnd=riseColor;
                ambientColorStart=ambientNightColor;
                ambientColorEnd=ambientRiseColor;
                
            }
            else if(FastMath.PI+RISE_SUNSET_OFFSET<sunRotation[2] && sunRotation[2]<=FastMath.PI+FastMath.QUARTER_PI)
            {
                // Real sunrise
                rangeStart=FastMath.PI+RISE_SUNSET_OFFSET;
                rangeEnd=FastMath.PI+FastMath.QUARTER_PI;
                sunColorStart=riseColor;
                sunColorEnd=noonColor;
                ambientColorStart=ambientRiseColor;
                ambientColorEnd=ambientNoonColor;
            }
            //Noon
            else if(FastMath.PI+FastMath.QUARTER_PI<sunRotation[2] && sunRotation[2]<=FastMath.TWO_PI-FastMath.QUARTER_PI)
            {
                // Noon (not real lerp but saving values for later code compat)
                rangeStart=FastMath.PI+FastMath.QUARTER_PI;
                rangeEnd=FastMath.PI+FastMath.HALF_PI+FastMath.QUARTER_PI;
                sunColorStart=noonColor;
                sunColorEnd=noonColor;
                ambientColorStart=ambientNoonColor;
                ambientColorEnd=ambientNoonColor;
            }
            //Sunset
            else if(FastMath.HALF_PI+FastMath.QUARTER_PI<sunRotation[2] && sunRotation[2]<=FastMath.TWO_PI-RISE_SUNSET_OFFSET)
            {
                // Sunset
                rangeStart=FastMath.TWO_PI-FastMath.QUARTER_PI;
                rangeEnd=FastMath.TWO_PI-RISE_SUNSET_OFFSET;
                sunColorStart=noonColor;
                sunColorEnd=sunsetColor;
                ambientColorStart=ambientNoonColor;
                ambientColorEnd=ambientSunsetColor;
            }
            else if(FastMath.TWO_PI-RISE_SUNSET_OFFSET<sunRotation[2] && sunRotation[2]<=FastMath.TWO_PI)
            {
                // From sunset to night
                rangeStart=FastMath.TWO_PI-RISE_SUNSET_OFFSET;
                rangeEnd=FastMath.TWO_PI;
                sunColorStart=sunsetColor;
                sunColorEnd=nightColor;
                ambientColorStart=ambientSunsetColor;
                ambientColorEnd=ambientNightColor;
            }
            
            float rangeSize=rangeEnd-rangeStart;
            float percent=(sunRotation[2]-rangeStart)/rangeSize;
            
            dl0.getColor().interpolateLocal(sunColorStart, sunColorEnd, percent);
            al0.getColor().interpolateLocal(ambientColorStart, ambientColorEnd, percent);
            
        }
    }

}
