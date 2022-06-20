import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class GraphicsForGamesProject extends PApplet {



// trackball stuff
SimSphere trackBall;

SimObjectManager simObjectManager = new SimObjectManager();
SimpleUI myUI;
SimCamera myCamera;

boolean run = true;
boolean simStarted;

//Models
SimSurfaceMesh terrain;
SimSurfaceMesh terrainWall;
SimSurfaceMesh terrainWall2;
SimSurfaceMesh terrainWall3;
SimSurfaceMesh terrainWall4;

AntColony antColony;
SimSphere colonyHome;

public void setup() {
    
    
    float ballRadius = 20;
    
    ////////////////////////////
    //create the SimCamera
    myCamera = new SimCamera();
    myCamera.setPositionAndLookat(vec(0, -350,1),vec(0,0,0)); 
    myCamera.setHUDArea(0,0,200,750);
    
    //Create UI
    myUI = new SimpleUI();
    myUI.addPlainButton("Start Simulation", 50,25);
    myUI.getWidget("Start Simulation").widgetWidth = 100;
    myUI.addSlider("Number of Ants", 50,100);
    myUI.getWidget("Number of Ants").widgetWidth = 100;
    myUI.addToggleButton("Spawn Food", 50,175);
    myUI.getWidget("Spawn Food").widgetWidth = 100;
    myUI.addToggleButton("Show Way Home Pheromones", 10,250);
    myUI.getWidget("Show Way Home Pheromones").widgetWidth = 180;
    myUI.addToggleButton("Show Found Food Pheromones", 10,325);
    myUI.getWidget("Show Found Food Pheromones").widgetWidth = 180;
    myUI.addToggleButton("Hide Ants", 10,400);
    myUI.getWidget("Hide Ants").widgetWidth = 180;
    myUI.addPlainButton("Restart Simulation", 10,475);
    myUI.getWidget("Restart Simulation").widgetWidth = 180;
    
    terrain = new SimSurfaceMesh(20, 20, 20.0f);
    terrain.setTransformAbs(1, 0,0,0, vec(- 200,ballRadius, - 200));
    simObjectManager.addSimObject(terrain,"terrain");
    
    // terrainWall = new SimSurfaceMesh(20, 20, 20.0);
    // terrainWall.setTransformAbs(1, radians(90),radians(180),0, vec(200,ballRadius, - 200));
    // simObjectManager.addSimObject(terrainWall,"terrainWall");
    
    // terrainWall2 = new SimSurfaceMesh(20, 20, 20.0);
    // terrainWall2.setTransformAbs(1, radians(90),0,0, vec(- 200,ballRadius,200));
    // simObjectManager.addSimObject(terrainWall2,"terrainWall2");
    
    // terrainWall3 = new SimSurfaceMesh(20, 20, 20.0);
    // terrainWall3.setTransformAbs(1, radians(180),0,radians(90), vec(-200,-380,200));
    // simObjectManager.addSimObject(terrainWall3,"terrainWall3");

    // terrainWall4 = new SimSurfaceMesh(20, 20, 20.0);
    // terrainWall4.setTransformAbs(1, 0,0,radians(90), vec(200,-380,-200));
    // simObjectManager.addSimObject(terrainWall4,"terrainWall4");

    antColony = new AntColony(new SimSphere(15), new PVector(0,20,0), simObjectManager);

    colonyHome = new SimSphere(15);
    colonyHome.setTransformRel(1,0,0,0, vec(0,20,0));
    simObjectManager.addSimObject(colonyHome,"colonyHome");
    
    // antColony.noneFoodObjs +=5;
    
    ////////////////////////////
    //create a 3D tarckball to see where we click
    trackBall = new SimSphere(2);
    trackBall.setTransformRel(1,0,0,0, vec(0,200,0));
}

public void draw() {
    background(0);
    lights();
    
    //Draw floor mesh
    fill(150,200, 150);
    noStroke();
    simObjectManager.getSimObject("terrain").drawMe();

    fill(255,0,0);
    trackBall.drawMe();

    fill(144,108, 63);
    simObjectManager.getSimObject("colonyHome").drawMe();
    

    // println(frameRate);
    //Update and draw ant colony
    antColony.display();
    if(run){
        antColony.update();
    }
    
    //Update camera and draw UI
    myCamera.startDrawHUD();
    myUI.update();
    myCamera.update();
    myCamera.endDrawHUD();
    myCamera.update_FlyingCamera();
}

public void mouseDragged() {
    updateTrackball();
}

public void mousePressed() {
    updateTrackball();
}

public void updateTrackball() { 
    if (myCamera.mouseInHUDArea()) return;
    if (mouseButton == RIGHT) return;
    boolean pickedSomething = false;
    
    
    int numObjects = simObjectManager.getNumSimObjects();
    
    for (int n = 0; n < numObjects; n++) {
        SimTransform obj = simObjectManager.getSimObject(n);
        
        SimRay mr =  myCamera.getMouseRay();
        mr.setID("mouseRay");
        
        if (mr.calcIntersection(obj)) {
            PVector intersectionPt = mr.getIntersectionPoint();
            trackBall.setTransformAbs(1,0,0,0,intersectionPt);
            // println(intersectionPt);
            // printRayIntersectionDetails(obj.getID(), mr);
            if (antColony.canMakeFood)
            {
                antColony.foodIndex += antColony.foodSpawnRate;
                antColony.makeSomeFood(mr,antColony.foodIndex);
            } 
            pickedSomething = true;
        }
        
    }
    
    if (pickedSomething) return;
    
    //should no intersection happen, the trackball is drawn at z=1000 in eye space in the scene 
    SimRay mr =  myCamera.getMouseRay();
    PVector mp = mr.getPointAtDistance(1000);
    printRayIntersectionDetails("nothing", mr);
    trackBall.setTransformAbs(1,0,0,0,mp);
    
}

public void printRayIntersectionDetails(String what, SimRay sr) {
    
    PVector intersectionPt = sr.getIntersectionPoint();
    PVector intersectionNormal = sr.getIntersectionNormal();
    int hits = sr.getNumIntersections();
    
    println("That ray hit ", what ,"with " ,  hits , " intersections ");
    println("Intersection at ", intersectionPt);
    println("Surface Normal at ", intersectionNormal);
    println("ID of object hit ", sr.getColliderID());
}

// you MUST have this function declared.. it receives all the user-interface events
public void handleUIEvent(UIEventData uied) {
    //here we just get the event to print its self
    //with "verbosity" set to 1, (2 = medium, 3 = high, 0 = do not print anything)
    //uied.print(2);
    if (uied.eventIsFromWidget("Start Simulation")) {
        if(!simStarted){
            antColony.numberOfAnts = round(myUI.getSliderValue("Number of Ants") * antColony.maxNumberOfAnts);
            if(antColony.numberOfAnts < antColony.minNumberOfAnts){
                antColony.numberOfAnts = antColony.minNumberOfAnts;
            }else if(antColony.numberOfAnts > antColony.maxNumberOfAnts){
                antColony.numberOfAnts = antColony.maxNumberOfAnts;
            }
            antColony.spawnAnts();
            println(antColony.ants.size());
            for (Ant ant : antColony.ants) {
                ant.searching = true;
            }
            simStarted = true;
        }
    }

    if (uied.eventIsFromWidget("Spawn Food")) {
        antColony.canMakeFood = myUI.getToggleButtonState("Spawn Food");
    }

    if(uied.eventIsFromWidget("Show Way Home Pheromones")) {
        if(myUI.getToggleButtonState("Show Way Home Pheromones") == true && myUI.getToggleButtonState("Show Found Food Pheromones") == false){
            run = false;  
            antColony.stopTime = true;
        }else if(myUI.getToggleButtonState("Show Way Home Pheromones") == false && myUI.getToggleButtonState("Show Found Food Pheromones") == false){
            run = true;
            antColony.stopTime = false;
        }
        antColony.drawWayHomePheromones = myUI.getToggleButtonState("Show Way Home Pheromones");
    }

    if(uied.eventIsFromWidget("Show Found Food Pheromones")) {
        if(myUI.getToggleButtonState("Show Way Home Pheromones") == false && myUI.getToggleButtonState("Show Found Food Pheromones") == true){
            run = false;  
            antColony.stopTime = true;
        }else if(myUI.getToggleButtonState("Show Way Home Pheromones") == false && myUI.getToggleButtonState("Show Found Food Pheromones") == false){
            run = true;
            antColony.stopTime = false;
        }
        antColony.drawFoodFoundPheromones = myUI.getToggleButtonState("Show Found Food Pheromones");
    }

    if(uied.eventIsFromWidget("Hide Ants")) {
        antColony.showAnts = !myUI.getToggleButtonState("Hide Ants");
    }


    if(uied.eventIsFromWidget("Restart Simulation")) {
        if(simStarted){
            for (int i = antColony.ants.size()-1; i>= 0; i--) {
                antColony.ants.remove(i);
            }
            for (int i = antColony.wayHomePheromones.size()-1; i>= 0; i--) {
                antColony.wayHomePheromones.remove(i);
            }
            for (int i = antColony.foundFoodPheromones.size()-1; i>= 0; i--) {
                antColony.foundFoodPheromones.remove(i);
            }
            simObjectManager.removeAllObjectsWithTag("Food");
            simStarted = false;
        }
    }

    

}
public class Ant  {
    
    //SimModel model;
    SimSphere model;
    Mover mover;
    SimObjectManager simObjectManagerRef;
    
    SimSphere trackBall;
    boolean searchingRandom = false;
    
    float searchRadius = 100;
    float searchForFoodWithinLocationRadius = 30;
    float searchForWayHomePheromone = 50;
    float searchForFoodFoundPheromone = 50;
    float pheromoneLifeSpan = 720;

    ArrayList<SimTransform> food = new ArrayList<SimTransform>();

    ArrayList<Pheromone> foodFoundPheromones = new ArrayList<Pheromone>();

    PVector colonyLocation;
    float colonyRadius = 50;
    int foodFoundPheromonesLeft = 0;
    boolean drawPheromones;
    boolean foodPheromonesNearby;
    int wayHomePheromoneRadius = 1;
    int pheromonesLeft = 0;
    float numberOfWayHomePheromonesToLeave = 3;
    PVector recentPheromone;
    Pheromone recentPheromoneObject;

    PVector foodFoundAtLocation;

    float scalar = 0f;
    PVector startingLocation;
    boolean startingLocationSet;
    

    float x = 0;
    float y = 0;
    float z = 0;

    PVector boundsX;
    PVector boundsZ;
    
    PVector randomLocaiton;
    PVector movingToLocation;
    
    boolean grounded;
    boolean pathingHome;
    boolean searching;
    boolean goingToFood;
    boolean gotFood;
    boolean canMove = true;

    int antSize = 1;
    
    public Ant(AntColony antColony, SimObjectManager _simObjectManagerRef) {
        //Set bounds for ant
        boundsX = new PVector(200,-200);
        boundsZ = new PVector(200,-200);
        //Create model
        model = new SimSphere(2);
        model.setColliderID("Ant");
        //Set start pos within colony
        colonyLocation = antColony.getLocation();
        model.setTransformAbs(1, 0, radians(180),PI, vec(colonyLocation.x, 20, colonyLocation.z));
        //Add mover and set location to model
        mover = new Mover();
        mover.location = model.getOrigin();
        mover.mass = 2;
        //Add ball that ant tracks to
        trackBall = new SimSphere(2);
        trackBall.setTransformRel(1,0,0,0, vec(100,20,100));
        //
        simObjectManagerRef = _simObjectManagerRef;
        //Initialise most recent homePheromoneSet
        recentPheromone = new PVector(0,0,0);
        //
        recentPheromoneObject = new Pheromone(new SimSphere(1),pheromoneLifeSpan,0);
        recentPheromoneObject.setTag("Unset");
        //
        startingLocation = new PVector(0,0,0);
        startingLocationSet = false;
        //Get food
        updateFoodList();
        //Set search location
        setSearchLocation(null);
    }
    
    public void update() {
        if(searching){
            pathToAntTrackBall();
            movingAnt();
        }
        mover.update();   
    }
    
    public void display() {
        fill(0,0,0);
        noStroke();
        model.drawMe();
    }
    
    public void setAcceleration(PVector acceleration) {
        mover.acceleration = acceleration;
    }
    
    public PVector getRandomLocaion() {
        
        x = random(mover.location.x - searchRadius,mover.location.x + searchRadius);
        y = model.getOrigin().y;
        z = random(mover.location.z - searchRadius,mover.location.z + searchRadius);
        
        return new PVector(x,y,z);
    }

    public PVector getRandomLocaionWithinLimits(float xUpperLimit,float xLowerLimit, float zUpperLimit, float zLowerLimit) {
        
        x = random(xLowerLimit,xUpperLimit);
        y = model.getOrigin().y;
        z = random(zLowerLimit,zUpperLimit);

        return new PVector(x,y,z);
    }
    
    public void setSearchLocation(PVector specificMovingToLocation) {
        //PVector randomAcc = getRandomLocaion();
        //Only search for valid locations within set bounds
        float xUpperLimit = mover.location.x + searchRadius;
        float xLowerLimit = mover.location.x - searchRadius;
        float zUpperLimit =  mover.location.z + searchRadius;
        float zLowerLimit =  mover.location.z - searchRadius;

        if(xUpperLimit > boundsX.x)
        {
            xUpperLimit = xUpperLimit - searchRadius;
        }
        if(xLowerLimit < boundsX.y)
        {
            xLowerLimit = xLowerLimit + searchRadius;
        }
        if(zUpperLimit > boundsZ.x)
        {
            zUpperLimit = zUpperLimit - searchRadius;
        }
        if(zLowerLimit < boundsZ.y)
        {
            zLowerLimit = zLowerLimit + searchRadius;
        }

        if(specificMovingToLocation == null){
            PVector randomAcc = getRandomLocaionWithinLimits(xUpperLimit,xLowerLimit,zUpperLimit,zLowerLimit);
            movingToLocation = new PVector(randomAcc.x,model.getOrigin().y,randomAcc.z);
        }else{
            movingToLocation = specificMovingToLocation;
        }
        
        trackBall.setTransformAbs(1,0,0,0, movingToLocation);
    }
    
    public PVector getMoveToLocation() {
        return movingToLocation;        
    }
    
    public void movingAnt() {
        PVector twoDHeading = new PVector(mover.acceleration.x,mover.acceleration.z);
        model.setTransformAbs(antSize, 0,twoDHeading.heading() + radians(90) ,PI, vec(mover.location.x,model.getOrigin().y,mover.location.z));
    }
    
    public void pathToAntTrackBall() {
        if (searchingRandom)
        {
            trackBall.setTransformAbs(1,0,0,0,getMoveToLocation());
        }
        // SimTransform boundingShape = ant1.model.getPreferredBoundingVolume();
        PVector trackBallOrigin = trackBall.getOrigin();
        PVector desiredLocation = new PVector(trackBallOrigin.x,trackBallOrigin.y - 20,trackBallOrigin.z);
        
        //Round locations to test easier
        PVector moverLoc = new PVector(round(mover.location.x),round(mover.location.y),round(mover.location.z));
        if(!startingLocationSet){
            startingLocation = moverLoc;
            startingLocationSet = true;
        }

        PVector goingToLoc = new PVector(round(desiredLocation.x),round(desiredLocation.y),round(desiredLocation.z));
        
        if(!goingToFood && !pathingHome && !foodPheromonesNearby){
            searchForFoodWithinLocation(moverLoc);
            lookForFoodFoundPheromones(moverLoc);
        }

        if(foodPheromonesNearby && !goingToFood && !pathingHome && !gotFood){
            searchForFoodWithinLocation(moverLoc);
        }

        boolean isAntWithinColonyRadius = pow(moverLoc.x - colonyLocation.x,2) + pow(moverLoc.z - colonyLocation.z,2) < pow(colonyRadius,2);
        if(pathingHome && gotFood){
            //is current location close enough to colony?
            if(isAntWithinColonyRadius){
                setSearchLocation(colonyLocation);    
            }
        }

        //Show line to where Ant going
        // pushStyle();
        // stroke(255);
        // line(moverLoc.x, 20,moverLoc.z, goingToLoc.x, 20 ,goingToLoc.z);
        // popStyle();

        if(scalar == 0f){
            scalar  = getPheromoneScalar(scalar);
        }

        PVector recentPheromoneObjectOrigin = recentPheromoneObject.getOrigin();
        if(recentPheromoneObject.getTag() == "Unset"){
            recentPheromoneObject = LeavePheromoneObject(startingLocation,goingToLoc,scalar);
        }

        if(pheromonesLeft < numberOfWayHomePheromonesToLeave || pathingHome){
            if(model.collidesWith(recentPheromoneObject.model) && recentPheromoneObject.getTag() == "WayHome"){
                scalar = getPheromoneScalar(scalar);
                recentPheromoneObject = LeavePheromoneObject(startingLocation,goingToLoc, scalar);
            }
        }
        
        //Has ant reached search location?
        if (moverLoc.x == goingToLoc.x && moverLoc.z == goingToLoc.z)
        {
            //Reset pheromone stuff
            pheromonesLeft = 0;
            recentPheromoneObject.setTag("Unset");
            startingLocationSet = false;
            scalar = 0;
            //We were going to food, and we're at food now.
            if(goingToFood && !gotFood){
                //Remove food
                pathingHome = true;
                goingToFood = false;
                gotFood = true;
                foodFoundAtLocation = antColony.removeCollidingObjectWithTag(model,"Food");
            }else if(pathingHome && gotFood){ //We're pathing home and we're at the coloby or a wayHomePheromone
                if(moverLoc.x == colonyLocation.x && moverLoc.z == colonyLocation.z){
                    pathingHome = false;
                    gotFood = false;
                }else{
                    lookForWayHomePheromones(moverLoc);
                }
            }else if(foodPheromonesNearby){ //We were going to a foundfoundPheromone and we're at one
                searchForFoodWithinLocation(moverLoc);
                lookForFoodFoundPheromones(moverLoc);
                foodPheromonesNearby = false;
            }
            else{
                setSearchLocation(null);    
            }
        }
        
        desiredLocation.sub(mover.location);
        desiredLocation.setMag(0.2f);
        
        setAcceleration(new PVector(desiredLocation.x, 0,desiredLocation.z));
    }

    public void lookForWayHomePheromones(PVector currentLocation){
        int closePheromones = 0;
        Pheromone strongestPheromone = new Pheromone(); 

        for (Pheromone wayHomePheromone : antColony.wayHomePheromones) {
            PVector wayHomePheromoneOrigin = wayHomePheromone.getOrigin();

            boolean isPheromoneWithinSearchingCircle = pow(wayHomePheromoneOrigin.x - currentLocation.x,2) + pow(wayHomePheromoneOrigin.z - currentLocation.z,2) < pow(searchForWayHomePheromone,2);
            //is pheromone close enough to the colony?
            if(isPheromoneWithinSearchingCircle && wayHomePheromone.intensity > strongestPheromone.intensity){
                strongestPheromone = wayHomePheromone;
                closePheromones++;
            }
        }
        if(closePheromones == 0){
            pathingHome = false;
        }else{
            setSearchLocation(strongestPheromone.getOrigin());
        }
    }

    public void lookForFoodFoundPheromones(PVector currentLocation){
        if(!pathingHome){
            for (Pheromone foundFoodPhermone : antColony.foundFoodPheromones) {
                PVector foundFoodPhermoneOrigin = foundFoodPhermone.getOrigin();

                boolean isPheromoneWithinSearchingCircle = pow(foundFoodPhermoneOrigin.x - currentLocation.x,2) + pow(foundFoodPhermoneOrigin.z - currentLocation.z,2) < pow(searchForFoodFoundPheromone,2);
                //is pheromone close enough to the colony?
                if(isPheromoneWithinSearchingCircle){
                    foodPheromonesNearby = true;
                    setSearchLocation(foundFoodPhermoneOrigin);
                    break;
                }
            }
        }
    }

    public void updateFoodList(){
        food = simObjectManagerRef.getSimObjectsWithColliderId("Food");
    }

    public void searchForFoodWithinLocation(PVector currentLocation){
        for (SimTransform foodObj : food) {
            PVector foodObjOrigin = foodObj.getOrigin();
            boolean isFoodWithinSearchingCircle = pow(foodObjOrigin.x - currentLocation.x,2) + pow(foodObjOrigin.z - currentLocation.z,2) < pow(searchForFoodWithinLocationRadius,2);
            if(isFoodWithinSearchingCircle){
                goingToFood = true;
                setSearchLocation(foodObjOrigin);
                break;
            }
        }
    }

    //foundFoodPheremons get used up and dont get reset
    public Pheromone LeavePheromoneObject(PVector moverLocation, PVector goingToLocation, float scalar){
        float distX = goingToLocation.x - moverLocation.x;
        float distZ = goingToLocation.z - moverLocation.z;

        float modX = (distX * scalar) + moverLocation.x;
        float modZ = (distZ * scalar) + moverLocation.z;

        Pheromone pheromone = new Pheromone(new SimSphere(wayHomePheromoneRadius), pheromoneLifeSpan,0);
        pheromone.model.setTransformAbs(1,0,0,0, vec(modX,20,modZ));
        if(!pathingHome){
            float distanceFromColony = 1000 - moverLocation.dist(colonyLocation);
            pheromone.intensity = distanceFromColony;
            antColony.addWayHomePheromones(pheromone);
            pheromone.setTag("WayHome");
            pheromonesLeft++;   
        }else if(gotFood){
            float distanceFromFood = 1000 - moverLocation.dist(foodFoundAtLocation);
            pheromone.intensity = distanceFromFood;
            antColony.addFoundFoodPheromones(pheromone);
        }
        return pheromone;
    }

    public float getPheromoneScalar(float currentScalar){
        if(currentScalar == 0){
            if(numberOfWayHomePheromonesToLeave > 2){
                return 1 / (numberOfWayHomePheromonesToLeave);
            }else{
                return 1 / (numberOfWayHomePheromonesToLeave + 1);
            }
        }else if(pheromonesLeft == numberOfWayHomePheromonesToLeave - 1){
            return 1;
        }else if(currentScalar + currentScalar > 1){
            return 1;
        }else{
            return currentScalar += currentScalar;
        }
    }
}
public class AntColony  {
    
    ArrayList<Pheromone> wayHomePheromones;
    ArrayList<Pheromone> foundFoodPheromones;
    
    SimObjectManager simObjectManagerRef;
    
    SimSphere model;
    
    ArrayList<Ant> ants = new ArrayList<Ant>();
    
    int minNumberOfAnts = 25;
    int maxNumberOfAnts = 250;
    int numberOfAnts = 50;
    boolean showAnts = true;
    
    //Food stuffs
    int noneFoodObjs = 0;
    int foodIndex = 0;
    int foodSpawnRate = 10;
    boolean canMakeFood;
    
    boolean drawWayHomePheromones = false;
    boolean drawFoodFoundPheromones = false;

    boolean stopTime = false;

    float maxWayHomeIntensity = 1000; //At home
    float minWayHomeIntensity = 717.16f; //1000 - 282.84 //In the corner of the map

    float maxFoodIntensity = 1000; //On the food
    float minFoodIntensity = 600; //Food is on the other side of the map

    float colourValue;

    
    public AntColony(SimSphere _model, PVector _location, SimObjectManager _simObjectManagerRef) {
        model = _model;
        model.setTransformAbs(1,0,0,0, vec(_location.x,_location.y,_location.z));
        
        simObjectManagerRef = _simObjectManagerRef;
        
        wayHomePheromones = new ArrayList<Pheromone>();
        foundFoodPheromones = new ArrayList<Pheromone>();

        // spawnAnts();
    }
    
    public void update() {
        
        //Update ants
        for (Ant ant : ants) {
            ant.update();
        }

        for (Pheromone pheromone : wayHomePheromones) {
            pheromone.update();
        }
        
        for (Pheromone pheromone : foundFoodPheromones) {
            pheromone.update();
        }

        //Kill pheromones 
        for (int i = wayHomePheromones.size() - 1; i >= 0; i--) {
            Pheromone pheromone = wayHomePheromones.get(i);
            if (pheromone.lifeSpan <= 0) {
                wayHomePheromones.remove(pheromone);
            }
        }
        
        for (int i = foundFoodPheromones.size() - 1; i >= 0; i--) {
            Pheromone pheromone = foundFoodPheromones.get(i);
            if (pheromone.lifeSpan <= 0) {
                foundFoodPheromones.remove(pheromone);
            }
        }
        
    }
    
    public void display() {

        //Draw food
        drawTheFood();
        
        //Draw ants
        if(showAnts){
            for (Ant ant : ants) {
                ant.display();
            }
        }
        
        //Draw Pheromnes
        for (Pheromone pheromone : wayHomePheromones) {
            if (drawWayHomePheromones) {
                int colour = color(255,255,0, getAlphaFromIntensity(pheromone.intensity,true));
                fill(colour);
                pheromone.drawMe();
            }
            // pheromone.update();
        }
        
        for (Pheromone pheromone : foundFoodPheromones) {
            if (drawFoodFoundPheromones) {
                int colour = color(0,255,255, getAlphaFromIntensity(pheromone.intensity,false));
                fill(colour);
                pheromone.drawMe();
            }
            // pheromone.update();
        }

        if(stopTime){
            for (Ant ant : ants) {
                ant.mover.timer.getElapsedTime();
            }
        }
    }
    
    public void spawnAnts() {
        for (int i = 0; i < numberOfAnts; ++i) {
            Ant ant = new Ant(this,simObjectManagerRef);
            simObjectManager.addSimObject(ant.model,"ant_" + i);
            ants.add(ant);
            noneFoodObjs++;
        }
    }
    
    public void updateFoodList() {
        for (Ant ant : ants) {
            ant.updateFoodList();
        }
    }

    public float getAlphaFromIntensity(float _intensity,boolean wayHomePheromone){
        float max = 1;
        float min = 1;
        if(wayHomePheromone){
            max = maxWayHomeIntensity;
            min = minWayHomeIntensity;
        }else{
            max = maxFoodIntensity;
            min = minFoodIntensity;
        }
        if(_intensity == max){
            return 1;
        }else if( _intensity == minWayHomeIntensity){
            return 255;
        }else{
            return 255 * (_intensity - min) / (max - min);
        }
    }
    
    public PVector removeCollidingObjectWithTag(SimTransform _object, String _tag) {
        for (int i = simObjectManagerRef.simObjList.size() - 1; i >= 0; i--) {
            SimTransform object = simObjectManagerRef.simObjList.get(i);
            PVector location = object.getOrigin();
            if (object.collidesWith(_object) && object.getTag() == _tag) {
                simObjectManagerRef.simObjList.remove(i);
                updateFoodList();
                return location; 
            }
        }
        return new PVector(0,0,0);
    }
    
    public void makeSomeFood(SimRay simRay, int foodIndex) {
        //noneFoodObjs = simObjectManagerRef.getNumSimObjects();
        PVector intersectionPt = new PVector(0,0,0);
        intersectionPt = simRay.getIntersectionPoint();
        
        for (int n = 0; n < foodSpawnRate; n++) {
            float foodRadius = 2;
            float clusterRadius = random(5,10);
            float x = random(intersectionPt.x - clusterRadius,intersectionPt.x + clusterRadius);
            //float y = intersectionPt.y - foodRadius;
            float y = 20;
            float z = random(intersectionPt.z - clusterRadius,intersectionPt.z + clusterRadius);
            SimSphere food = new SimSphere(vec(x,y,z), foodRadius);
            food.levelOfDetail = 8;
            food.setColliderID("Food");
            food.setTag("Food");
            int foodN = n + foodIndex;
            simObjectManagerRef.addSimObject(food, "food_" + foodN);
            for (Ant ant : ants) {
                ant.updateFoodList();
            }
        }
    }
    
    public void drawTheFood() {
        ArrayList<SimTransform> foods = simObjectManagerRef.getAllObjectsWithTag("Food");
        for (SimTransform food : foods) {
            noStroke();
            fill(0,0,255);
            food.drawMe();
        }
    }
    
    public PVector getLocation() {
        return model.getOrigin();
    }
    
    public void addWayHomePheromones(Pheromone pheromone) {
        wayHomePheromones.add(pheromone);
    }
    
    public void addFoundFoodPheromones(Pheromone pheromone) {
        foundFoodPheromones.add(pheromone);
    }
}
// Complete Mover class
// With collision detection and response
//
// This mover class accelerates according to the force accumulated over TIME
// MASS is taken into consideration by using F=MA (or acceleation = force/mass)
// Mass is represented by the surface area of the ball
// 
// The system works thus:-
// within each FRAME of the system
// 1/ calculate the cumulative acceleration (by acceleration += force/mass) by adding all the forces, including friction
// 2/ scale the acceleration by the elapsed time since the last frame (will be about 1/60th second)
// 3/ Add this acceleration to the velocity
// 5/ Move the ball by the velocity scaled by the elapsed time since the last frame
// 5/ Set the acceleration back to zero again
// repeat

class Mover {

  Timer timer = new Timer();
  SimModel modelToDraw;
  PVector location = new PVector(width/2, height/2);
  PVector velocity = new PVector(0, 0);
  PVector acceleration = new PVector(0,0);
  PVector distanceMoved = new PVector(0,0,0);;
  float topspeed;
  private float mass = 1;
  float radius;
  float frictionAmount = 25;
  boolean canMove = true;

  Mover() {
    setMass(1);
    topspeed = 50;
    //modelToDraw = model;
    //location = modelToDraw.getOrigin();
  }
  
  ////////////////////////////////////////////////////////////
  // movement code has not changed except we now set the mass
  // by a method, which calculates the radius of the ball
  // required for drawing and collision checking
  
  public void setMass(float m){
    // converts mass into surface area
    mass=m;
    radius = 60 * sqrt( mass/ PI );
    
  }
  
  
  public void update() {
    if(!canMove) return;
    velocity.add(acceleration);
    velocity.limit(topspeed);
    location.add(velocity);
    float ellapsedTime = timer.getElapsedTime();
    
    applyFriction();
    
    // // scale the acceleration by time elapsed
    PVector accelerationOverTime = PVector.mult(acceleration, ellapsedTime);
    velocity.add(accelerationOverTime);
    
    // // scale the movement by time elapsed
    distanceMoved = PVector.mult(velocity, ellapsedTime);
    location.add(distanceMoved);
    // location.add(velocity);
    
    // // now that you have "used" your accleration it needs to be re-zeroed
    //acceleration = new PVector(0,0,0);
    // PVector twoDHeading = new PVector(acceleration.x,acceleration.z);
    // modelToDraw.setTransformAbs(1000, 0,twoDHeading.heading() + radians(90) ,PI, vec(location.x,modelToDraw.getOrigin().y,location.z));

    // checkForBounceOffEdges();
  }
  
  public void addForce(PVector f){
    // use F= MA or (A = F/M) to calculated acceleration caused by force
    PVector accelerationEffectOfForce = PVector.div(f, mass);
    acceleration.add(accelerationEffectOfForce);
    // PVector twoDHeading = new PVector(location.x,location.z);
    //println(degrees(twoDHeading.heading()));

  }

  public void display() {
    fill(0,0,0);
    stroke(0,0,0);
    //modelToDraw.drawMe();
  }
  
 
  
  public void applyFriction(){
    // modify the acceleration by applying
    // a force in the opposite direction to its velociity
    // to simulate friction
    PVector reverseForce = PVector.mult( velocity, -frictionAmount );
    addForce(reverseForce);
  }
  
  ////////////////////////////////////////////////////////////
  // new collision code
  // call collisionCheck just before or after update in the "main" tab
  
  public boolean collisionCheck(Mover otherMover){
    
    if(otherMover == this) return false; // can't collide with yourself!
    
    float distance = otherMover.location.dist(this.location);
    float minDist = otherMover.radius + this.radius;
    if (distance < minDist)  return true;
    return false;
  }
  
  
  public void collisionResponse(Mover otherMover) {
    // based on 
    // https://en.wikipedia.org/wiki/Elastic_collision
    
     if(otherMover == this) return; // can't collide with yourself!
     
     
    PVector v1 = this.velocity;
    PVector v2 = otherMover.velocity;
    
    PVector cen1 = this.location;
    PVector cen2 = otherMover.location;
    
    // calculate v1New, the new velocity of this mover
    float massPart1 = 2*otherMover.mass / (this.mass + otherMover.mass);
    PVector v1subv2 = PVector.sub(v1,v2);
    PVector cen1subCen2 = PVector.sub(cen1,cen2);
    float topBit1 = v1subv2.dot(cen1subCen2);
    float bottomBit1 = cen1subCen2.mag()*cen1subCen2.mag();
    
    float multiplyer1 = massPart1 * (topBit1/bottomBit1);
    PVector changeV1 = PVector.mult(cen1subCen2, multiplyer1);
    
    PVector v1New = PVector.sub(v1,changeV1);
    
    // calculate v2New, the new velocity of other mover
    float massPart2 = 2*this.mass/(this.mass + otherMover.mass);
    PVector v2subv1 = PVector.sub(v2,v1);
    PVector cen2subCen1 = PVector.sub(cen2,cen1);
    float topBit2 = v2subv1.dot(cen2subCen1);
    float bottomBit2 = cen2subCen1.mag()*cen2subCen1.mag();
    
    float multiplyer2 = massPart2 * (topBit2/bottomBit2);
    PVector changeV2 = PVector.mult(cen2subCen1, multiplyer2);
    
    PVector v2New = PVector.sub(v2,changeV2);
    
    this.velocity = v1New;
    otherMover.velocity = v2New;
    ensureNoOverlap(otherMover);
  }
  
 
  public void ensureNoOverlap(Mover otherMover){
    // the purpose of this method is to avoid Movers sticking together:
    // if they are overlapping it moves this Mover directly away from the other Mover to ensure
    // they are not still overlapping come the next collision check 
    
    
    PVector cen1 = this.location;
    PVector cen2 = otherMover.location;
    
    float cumulativeRadii = (this.radius + otherMover.radius)+2; // extra fudge factor
    float distanceBetween = cen1.dist(cen2);
    
    float overlap = cumulativeRadii - distanceBetween;
    if(overlap > 0){
      // move this away from other
      PVector vectorAwayFromOtherNormalized = PVector.sub(cen1, cen2).normalize();
      PVector amountToMove = PVector.mult(vectorAwayFromOtherNormalized, overlap);
      this.location.add(amountToMove);
    }
  }
  
  
  
  public void checkForBounceOffEdges() {
    if (location.x > width || location.x < 0) {
      velocity.x *= -1;
    } 
    if (location.y > height || location.y < 0) {
      velocity.y *= -1;
    } 
  }

}
public class Pheromone  {
    
    SimSphere model;
    float lifeSpan;
    float intensity;
    
    public Pheromone() {
        model = new SimSphere(2);
        lifeSpan = 720;
        intensity = 0;
    }
    
    public Pheromone(SimSphere _model, float _lifeSpan, float _intensity) {
        model = _model;
        lifeSpan = _lifeSpan;
        intensity = _intensity;
    }
    
    public void update() {
        lifeSpan--;
    }
    
    public void drawMe() {
        model.drawMe();
    }
    
    public float getIntensity() {
        return intensity;
    }
    
    public PVector getOrigin() {
        return model.getOrigin();
    }
    
    public String getTag() {
        return model.getTag();
    } 
    
    public void setTag(String _tag) {
        model.setTag(_tag);
    }
}

class SimCamera{

  
  PVector initialCameraPosition;
  PVector restoreCameraPosition;
  PVector restoreCameraLookat;
  
  PVector cameraUpVector = new PVector(0,-1,0);
  PVector cameraPos;
  PVector cameraLookat;

  float forwardSpeed = 3.0f;
  
  //Timer pauseTimer;
  boolean isMoving = true;
  
  SimRect hudArea = null;
  
  public SimCamera() {

    setViewParameters(-1, 1,100000);
    cameraPos = discoverCameraPosition();
    initialCameraPosition = cameraPos.copy();
    //println("initial camera pos", initialCameraPosition);
    cameraLookat = PVector.add(cameraPos, vec(0, 0, -1));
  }
  
   public void setPositionAndLookat(PVector pos, PVector lookat) {
    cameraPos = pos.copy();
    cameraLookat = lookat.copy();
    updateCameraPosition();
  }
  
  public void setHUDArea(int left, int top, int right, int bottom){
    hudArea = new SimRect(left,top,right,bottom);
  }
  
  public void setSpeed(float s){
    forwardSpeed = s;
  }
  
  public void setViewParameters(float fov, float nearClip, float farClip){
   // default FOV is PI/3;
    if( fov == -1 ) fov = PI/3.0f;
    
    perspective(fov, PApplet.parseFloat(width)/PApplet.parseFloat(height), nearClip, farClip);
  }
  
 
  
  public void setActive(boolean b){
    isMoving = b;
  }
  
  
  public void update() {
    if( isMoving == false) return;
    
    update_FlyingCamera();
    updateCameraPosition();
  }




 
  
  public void updateCameraPosition(){
    camera(cameraPos.x, cameraPos.y, cameraPos.z, cameraLookat.x, cameraLookat.y, cameraLookat.z, 0, 1, 0);
  }

  public PVector getPosition() { 
    return cameraPos;
  }
  public PVector getLookat() { 
    return cameraLookat;
  }
  
 
  
  public PVector discoverCameraPosition() {
    PMatrix3D mat = (PMatrix3D)getMatrix(); //Get the model view matrix
    mat.invert();
    return new PVector( mat.m03, mat.m13, mat.m23 );
  }
  
 
  public void startDrawHUD() {
    restoreCameraPosition = getPosition();
    restoreCameraLookat = getLookat();

    setPositionAndLookat(initialCameraPosition, PVector.add(initialCameraPosition, vec(0,0,-1)));
    hint(DISABLE_DEPTH_TEST);
    
    if( hudArea == null) return;
    fill(0,255,0,100);
    rect( hudArea.left, hudArea.top, hudArea.getWidth(), hudArea.getHeight());
  }

  public void endDrawHUD() {
    setPositionAndLookat(restoreCameraPosition, restoreCameraLookat);
    hint(ENABLE_DEPTH_TEST);
  }
  
  public boolean mouseInHUDArea(){
    if(hudArea == null) return false;
    return hudArea.isPointInside(getMousePos());
  }

  public PVector getMousePos() {
    return new PVector(mouseX, mouseY, 0);
  }

  public PVector getForwardVector() {
    return PVector.sub(cameraLookat, cameraPos).normalize();
  }

  ///////////////////////////////////////////////////////////////////////////////////////////
  //
  //


  
  public void update_FlyingCamera () {
    
    if( mouseInHUDArea() ) return;
    //println("in update_FlyingCamera", mouseX,mouseY);
    float rotationAmount = 1;
 
    if(key == 'w' && keyPressed) moveCameraForward(forwardSpeed);
    if(key == 's' && keyPressed) moveCameraForward(-forwardSpeed);
    // if(arrowKeyPressed(LEFT)) strafe( LEFT );
    // if(arrowKeyPressed(RIGHT)) strafe( RIGHT );
    // if(arrowKeyPressed(UP)) strafe( UP );
    // if(arrowKeyPressed(DOWN)) strafe( DOWN );
    
    if (mousePressed && mouseButton == RIGHT) {
      PVector mqv = getMouseQuadrantVector();
      
      if(mqv.mag()>0){
        rotatecameraLeftRight(rotationAmount*mqv.x);
        rotatecameraUpDown(rotationAmount*-mqv.y);
      }
      
    }

  }
  
  public void strafe(int dir){
    PVector cameraForwardVector = getForwardVector();
    PVector sideVector = cameraForwardVector.cross(cameraUpVector);
    sideVector.mult(forwardSpeed);
    if(dir == LEFT){
      cameraPos = PVector.add(cameraPos, sideVector);
      cameraLookat = PVector.add(cameraLookat, sideVector);
    }
    if(dir == RIGHT){
      cameraPos = PVector.sub(cameraPos, sideVector);
      cameraLookat = PVector.sub(cameraLookat, sideVector);
    }
    
    if(dir == UP){
      PVector cameraUpScaled = PVector.mult(cameraUpVector, forwardSpeed);
      cameraPos = PVector.add(cameraPos, cameraUpScaled);
      cameraLookat = PVector.add(cameraLookat, cameraUpScaled);
    }
    if(dir == DOWN){
      PVector cameraUpScaled = PVector.mult(cameraUpVector, forwardSpeed);
      cameraPos = PVector.sub(cameraPos, cameraUpScaled);
      cameraLookat = PVector.sub(cameraLookat, cameraUpScaled);
    }
    
  }
  
  public boolean arrowKeyPressed(int dir){
    if(keyPressed && key == CODED){
      if(keyCode == dir){
        return true;
      }
    }
    return false;
  }
  
  public PVector getMouseQuadrantVector(){
    
    
    PVector centre = getCentre3DWindow();
    PVector mousePos = getMousePos();
    
    float dx = (mousePos.x - centre.x)/centre.x;
    float dy = (mousePos.y - centre.y)/centre.y;
    if(abs(dx) < 0.1f) dx = 0;
    if(abs(dy) < 0.1f) dy = 0;
    
    //println("dx dy ", dx, dy);
    return new PVector(dx,dy);
  }
  
  public void setForwardSpeed(float moreOrLess){
    forwardSpeed = constrain(forwardSpeed + moreOrLess, 0.5f,15);
  }

  public void moveCameraForward(float amt) {
    PVector cameraForwardVector = getForwardVector();
    PVector movement = PVector.mult(cameraForwardVector, amt);
    //println("forward motion ", movement);
    cameraPos = PVector.add(cameraPos, movement);
    cameraLookat = PVector.add(cameraPos, cameraForwardVector);
    setPositionAndLookat(cameraPos, cameraLookat);
  }


  public void rotatecameraLeftRight(float degs) {
    PVector cameraForwardVector = getForwardVector();
    cameraForwardVector =  rotateVectorRoundAxis(cameraForwardVector, cameraUpVector, degs);
    cameraLookat = PVector.add(cameraPos, cameraForwardVector);
    setPositionAndLookat(cameraPos, cameraLookat);
  }
  
  public void rotatecameraUpDown(float degs) {
    PVector cameraForwardVector = getForwardVector();
    PVector sideVector = cameraForwardVector.cross(cameraUpVector);
    cameraForwardVector =  rotateVectorRoundAxis(cameraForwardVector, sideVector, degs);
    cameraLookat = PVector.add(cameraPos, cameraForwardVector);
    setPositionAndLookat(cameraPos, cameraLookat);
  }
  
  
  
  
  ///////////////////////////////////////////////////////////////////////////////////
  // camera rays
  //
  public SimRay getWindowRay(PVector winPos){
    // returns a ray into the view at position p
    PVector mp = projectWindowPosInto3D(winPos);
    PVector cameraPos = this.getPosition();
    SimRay mouseRay = new SimRay(cameraPos,mp);
    return mouseRay;
  }
  

  public SimRay getMouseRay(){
    
    PVector mp = projectWindowPosInto3D(getMousePos());
    PVector cameraPos = this.getPosition();
    SimRay mouseRay = new SimRay(cameraPos,mp);
    return mouseRay;
  }
  
  

  public PVector getCentre3DWindow(){
    
    float xc = width*0.5f;
    float yc = height*0.5f;
    return new PVector(xc,yc);
  }
  
 
    
    
  //////////////////////////////////////////////////////////////////////////////////////////
  // Performs conversion to the local coordinate system
  //( reverse projection ) from the window coordinate system
  // i.e. EyeSpace -> WorldSpace
  
  public PVector projectWindowPosInto3D(PVector winPos){
    PVector pos3d = this.unProject(winPos.x, winPos.y, 0);
    //println("mousPos2D", winPos.x, winPos.y, 0, " mouse pos 3D ", pos3d);
    return pos3d;
  }
  
  
  public PVector unProject(float winX, float winY, float winZ) {
    PMatrix3D mat = getMatrixLocalToWindow();  
    mat.invert();
   
    float[] in = {winX, winY, winZ, 1.0f};
    float[] out = new float[4];
    mat.mult(in, out);  // Do not use PMatrix3D.mult(PVector, PVector)
   
    if (out[3] == 0 ) {
      return null;
    }
   
    PVector result = new PVector(out[0]/out[3], out[1]/out[3], out[2]/out[3]);  
    return result;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////
  // Function to compute the viewport transformation matrix to the window 
  // coordinate system from the local coordinate system
  public PMatrix3D getMatrixLocalToWindow() {
    PMatrix3D projection = ((PGraphics3D)g).projection; 
    PMatrix3D modelview = ((PGraphics3D)g).modelview;   
   
    // viewport transf matrix
    PMatrix3D viewport = new PMatrix3D();
    viewport.m00 = viewport.m03 = width/2;
    viewport.m11 = -height/2;
    viewport.m13 =  height/2;
   
    // Calculate the transformation matrix to the window 
    // coordinate system from the local coordinate system
    viewport.apply(projection);
    viewport.apply(modelview);
    return viewport;
}
  
  public PVector rotateVectorRoundAxis(PVector vec, PVector axis, float degs){
    // remember this is in radians
    float theta = radians(degs);
    float x, y, z;
    float u, v, w;
    x=vec.x;
    y=vec.y;
    z=vec.z;
    
    u=axis.x;
    v=axis.y;
    w=axis.z;
    
    float xrot = u*(u*x + v*y + w*z)*(1.0f - cos(theta)) + x* cos(theta) + (-w*y + v*z)* sin(theta);
    float yrot = v*(u*x + v*y + w*z)*(1.0f - cos(theta)) + y* cos(theta) + ( w*x - u*z)* sin(theta);
    float zrot = w*(u*x + v*y + w*z)*(1.0f - cos(theta)) + z* cos(theta) + (-v*x + u*y)* sin(theta);
    return new PVector(xrot, yrot, zrot);
  }
  

  
}
///////////////////////////////////////////////////////////////////////////////////////////
//  maths functions, and short-hands, generally used

// shorthand to get a PVector
public PVector vec(float x, float y, float z) {
  return new PVector(x, y, z);
}

public static float sqr(float a) {
  return a*a;
}

public boolean isBetweenInc(float v, float lo, float hi) {

  float sortedLo = min(lo, hi);
  float sortedHi = max(lo, hi);

  if (v >= sortedLo && v <= sortedHi) return true;
  return false;
}

public boolean nearZero(float v) {

  if ( abs(v) <= EPSILON ) return true;
  return false;
}



public void setCamera(PVector pos, PVector lookat) {
  camera(pos.x, pos.y, pos.z, lookat.x, lookat.y, lookat.z, 0, 1, 0);
}

public void drawMajorAxis(PVector p, float len) { 

  PVector topOfLine = new PVector(p.x, p.y+len, p.z);
  PVector intoScene = new PVector(p.x, p.y, p.z+len);
  PVector sideways  = new PVector(p.x+len, p.y, p.z);

  hint(DISABLE_DEPTH_TEST);
  // line x (red)
  stroke(255, 0, 0);
  line(p.x, p.y, p.z, sideways.x, sideways.y, sideways.z);

  // line y (green)
  stroke(0, 255, 0);
  line(p.x, p.y, p.z, topOfLine.x, topOfLine.y, topOfLine.z);

  // line z (blue)
  stroke(0, 0, 255);
  line(p.x, p.y, p.z, intoScene.x, intoScene.y, intoScene.z);
  hint(ENABLE_DEPTH_TEST);
}



/////////////////////////////////////////////////////////////////
// SphereGraphic class
// Just draws a sphere from scratch so we don't have to use
// processings Sphere, with it's inherent problems of updating draw style.
class SimSphereGraphic {
  SimSphere parent;
  PVector[][] globe;
  int levelOfDetail = 10;

  public SimSphereGraphic(SimSphere owningObject, int lod) {
    parent = owningObject;
    levelOfDetail = lod;

    globe = new PVector[levelOfDetail+1][levelOfDetail+1];

    float r = 1;
    for (int i = 0; i < levelOfDetail+1; i++) {
      float lat = map(i, 0, levelOfDetail, 0, PI);
      for (int j = 0; j < levelOfDetail+1; j++) {
        float lon = map(j, 0, levelOfDetail, 0, TWO_PI);

        float x = r * sin(lat) * cos(lon);
        float y = r * cos(lat);
        float z = r * sin(lat) * sin(lon);

        globe[i][j] = new PVector(x, y, z);
      }
    }
  }


  public void drawMe() {


    float sw =  g.strokeWeight ;

    strokeWeight(sw / parent.radius );
    beginShape(TRIANGLE_STRIP);
    for (int i = 0; i < levelOfDetail; i++) {
      //beginShape(TRIANGLE_STRIP);
      for (int j = 0; j < levelOfDetail+1; j++) {
        PVector v1 = globe[i][j];
        vertex(v1.x, v1.y, v1.z);
        PVector v2 = globe[i+1][j];
        vertex(v2.x, v2.y, v2.z);
      }
      //endShape();
    }
    endShape();
    strokeWeight(sw);
  }// end drawMe()
}



/////////////////////////////////////////////////////////////////
// simple rectangle class for SimFunctions
//

class SimRect {

  float left, top, right, bottom;

  public SimRect(float x1, float y1, float x2, float y2) {
    setRect(x1, y1, x2, y2);
  }

  public void setRect(float x1, float y1, float x2, float y2) {
    this.left = x1;
    this.top = y1;
    this.right = x2;
    this.bottom = y2;
  }

  public PVector getCentre() {
    float cx =  (this.right - this.left)/2.0f;
    float cy =  (this.bottom - this.top)/2.0f;
    return new PVector(cx, cy);
  }

  public boolean isPointInside(PVector p) {
    // inclusive of the boundries
    if (   isBetweenInc(p.x, this.left, this.right) && isBetweenInc(p.y, this.top, this.bottom) ) return true;
    return false;
  }

  public float getWidth() {
    return (this.right - this.left);
  }

  public float getHeight() {
    return (this.bottom - this.top);
  }
}


///////////////////////////////////////////////////////////////////////////////////////
// SimShapes V Alpha 1.4 7 Feb 2020 by Simon Schofield
//
// SimShapes is a set of 3D shapes you can make, transform around 3D space, 
// and ... CRITICALLY... get their transformed geometry (locations/extents/ bounding sphere etc.)
// This is necessary for running physisc simulations.
//
// IN this version you can load an .obj file and transform it and get it's location etc.

///////////////////////////////////////////////////////////////////////////////////////
// SimObjectManager
// This is a sort of "database" for you to add SimObjects to. Once added
// You can get them from the manager via their "id" tag - a string (make sure its unique) 
// You can iterate through them usng a simple index number
// Other uses...
// Ray Intersection calculations - all objects can be intersected by a SimRay and return intersection points etc.
// Inter-object collision - The manager will determine all inter-object collitions and report them back (not finishe yet)
// Drawing them, all at once, or individually
// 

class SimObjectManager{
  ArrayList<SimTransform> simObjList = new ArrayList<SimTransform>();

  // void removeObjectAtLocationWithTag(PVector _location, String _tag){
  //   println("Trying to remove object at location:" + _location + " with tag: " + _tag);
  //   for (int i = simObjList.size() - 1; i >= 0; i--) {
  //       SimTransform object = simObjList.get(i);
  //       PVector location = object.getOrigin();
  //       println("Object Location: " + location);
  //       if(location.x == _location.x && location.z == _location.z && object.getTag() == _tag){
  //         println("Removing food");
  //         simObjList.remove(i);
  //       }
  //   }
  // }

  public void removeAllObjectsWithTag(String _tag){
    for (int i = simObjList.size() - 1; i >= 0; i--) {
        SimTransform object = simObjList.get(i);
        if(object.getTag() == _tag){
          simObjList.remove(i);
        }
    }
  }

  public ArrayList<SimTransform> getAllObjectsWithTag(String _tag){
    ArrayList<SimTransform> objectsWithTag = new ArrayList<SimTransform>();
    for (SimTransform object : simObjList) {
      if(object.getTag() == _tag){
        objectsWithTag.add(object);
      }
    }
    return objectsWithTag;
  }
  
  public void addSimObject(SimTransform obj, String id){
    obj.setID(id);
    simObjList.add(obj);
  }
  
  public SimTransform getSimObject(String id){
    for(SimTransform thisObj: simObjList){
      if( thisObj.idMatches(id) ) return thisObj;
    }
    // if it can't find a match then ...
    return null;
  }
  
  public int getNumSimObjects(){
    return simObjList.size();
  }
  
  public SimTransform getSimObject(int n){
    return simObjList.get(n);
  }

  public ArrayList<SimTransform> getSimObjectsWithColliderId(String _colliderId){

    ArrayList<SimTransform> simObjectsWithColliderId = new ArrayList<SimTransform>();

    for (SimTransform simObject : simObjList) {
      if(simObject.getColliderID() == _colliderId) {
        simObjectsWithColliderId.add(simObject);
      }
    }
    return simObjectsWithColliderId;
  }
  
  public void drawAll(){
    for(SimTransform obj: simObjList){
      obj.drawMe();
    }
  }
}






abstract class SimTransform{
  // this part of the class contains id information about this shape
  // and also stores the id of shapes which are colliding with this shape
  String id;
  String colliderID;
  String tag;

  public void setID(String i) {
    id = i;
  }

  public String getID() {
    return id;
  }

  public void setTag(String _tag){
    tag = _tag;
  }

  public String getTag(){
    return tag;
  }

  public String getColliderID() {
    return colliderID;
  }
  
  public void setColliderID(String n){
    colliderID = n;
  }

  public void swapColliderIDs(SimTransform otherthing) {
    this.colliderID = otherthing.getID();
    otherthing.setColliderID(this.id);
  }
  
  public boolean idMatches(String s){
    if( id.equals(s)) return true;
    return false;
  }
  
  public boolean isClass(Object o, String s){
    return (getClassName(o).equals(s));
  }
  
  public String getClassName(Object o){
    return o.getClass().getSimpleName();
  }
  
  // abstract methods your sub class has to implement
  public abstract boolean collidesWith(SimTransform c);
  
  public abstract boolean calcRayIntersection(SimRay sr);
  
  public abstract void drawMe();
  
  ///////////////////////////////////////////////////////////////////////
  // this part of the class is the main sim transform stuff to 
  // do with vertices and geometry transforms
  
  
  // all objects have one
  PVector origin = new PVector(0, 0, 0);

  float scale = 1;
  PVector translate = new PVector(0, 0, 0);
  float rotateX, rotateY, rotateZ = 0.0f;


  public void setTransformAbs(float scale, float rotateX, float rotateY, float rotateZ, PVector translate) {
    this.scale = scale;
    if (translate!=null) this.translate = translate.copy();
    this.rotateX = rotateX;
    this.rotateY = rotateY;
    this.rotateZ = rotateZ;
    //printCurrentTransform();
  }

  public void setTransformRel( float scale, float rotateX, float rotateY, float rotateZ, PVector translate) {
    this.scale *= scale;
    if (translate!=null) this.translate.add(translate);
    this.rotateX += rotateX;
    this.rotateY += rotateY;
    this.rotateZ += rotateZ;
  }

  public void setIdentityTransform() {
    setTransformAbs( 1, 0, 0, 0, vec(0, 0, 0));
  }

  public void printCurrentTransform() {

    println("Current transform:  Scale ", scale, " Rotxyz ", rotateX, rotateY, rotateZ, " Translate ", translate.x, translate.y, translate.z);
  }

  // given a cardinal shape vertex p, transform the point
  // scale
  // rotate
  // translate
  // This uses basic triganometry, could be sped up using matrices
  public PVector transform(PVector pIn) {
    // because we definately don't want to affect the vector coming in!
    PVector p = pIn.copy();

    // first scale the point
    PVector scaled = p.mult(this.scale);

    float x = scaled.x;
    float y = scaled.y;
    float z = scaled.z;

    // rotate round X axis
    float y1 = y*cos( rotateX ) - z*sin( rotateX );
    float z1 = y*sin( rotateX ) + z*cos( rotateX );
    float x1 = x;
    // rotate round Y axis
    float z2 = z1*cos( rotateY ) - x1*sin( rotateY );
    float x2 = z1*sin( rotateY ) + x1*cos( rotateY );
    float y2 = y1;
    // rotate round Z axis
    float x3 = x2*cos( rotateZ ) - y2*sin( rotateZ );
    float y3 = x2*sin( rotateZ ) + y2*cos( rotateZ );
    float z3 = z2;

    PVector rotated = new PVector(x3, y3, z3);

    PVector translated = rotated.add(translate);
    return translated;
  }

  // useful shorthand for subclasses to return either the cadinal or transformed values
  //public PVector transform(PVector vectorIn, boolean applyTransform){
  //  if(applyTransform) return transform(vectorIn);
  //  return vectorIn.copy();
  //  
  //}

  // useful shorthand function that draws a transformed vertices
  public void  drawTransformedVertex(PVector v) {
    PVector transformedVector = transform(v);
    vertex(transformedVector.x, transformedVector.y, transformedVector.z);
  }

  public PVector getOrigin() {
    return transform(this.origin);
  }

  // useful for things which can be Axis Aligned, or not
  public boolean isRotated() {
    if ( isQuarterTurn(this.rotateX)  && isQuarterTurn(this.rotateY) && isQuarterTurn(this.rotateZ)) return false;
    return true;
  }
  
  public boolean isQuarterTurn(float a){
    // return true is the value of a is (very close to) 0, 90 degrees, 
    int degs = (int) (degrees(a) + 0.5f);
    
    if(degs == 0 || degs == 90 || degs == 180 || degs == 270 || degs == 360) return true;
    return false;
  }
  ///////////////////////////////////////////////////////////////////////////////////////////////
  // useful function for all shapes made from a list of vertices
  // returns the extents in the array, in order...
  // the lower extent of the bounding box, 
  // the upper extent of the bounding, 
  // the centre point (of the above)
  // the furthest vertices from the centre point
  public PVector[] getExents_DoNotApplyTransform(PVector[] vertices){

    PVector[] extents = new PVector[4];
    float minx = Float.MAX_VALUE;
    float miny = Float.MAX_VALUE;
    float minz = Float.MAX_VALUE;

    float maxx = -Float.MAX_VALUE;
    float maxy = -Float.MAX_VALUE;
    float maxz = -Float.MAX_VALUE;
    for (PVector p : vertices) {
      if (p.x < minx) minx = p.x;
      if (p.y < miny) miny = p.y;
      if (p.z < minz) minz = p.z;
      if (p.x > maxx) maxx = p.x;
      if (p.y > maxy) maxy = p.y;
      if (p.z > maxz) maxz = p.z;
    }
    PVector minExtents = new PVector(minx, miny, minz);
    PVector maxExtents = new PVector(maxx, maxy, maxz);

    PVector centrePoint = midPoint(minExtents, maxExtents);
    // need to work out point furthest from the centre point
    PVector furthest = centrePoint.copy();
    for (PVector p : vertices) {
      if (centrePoint.dist(p) > centrePoint.dist(furthest)) {
        furthest = p.copy();
      }
    }

    extents[0] = minExtents;
    extents[1] = maxExtents;
    extents[2] = centrePoint;
    extents[3] = furthest;
    return extents;
    
  }
  
  public PVector[] getExtents(PVector[] vertices) {
    vertices = getTransformedVertices(vertices);
    return getExents_DoNotApplyTransform(vertices);
  }


  public PVector[] getTransformedVertices(PVector[] vertices) {
    int numVerts = vertices.length;
    PVector[] transformedVerts = new PVector[numVerts];
    for (int n = 0; n < numVerts; n++) {
      transformedVerts[n] = transform(vertices[n]);
    }
    return transformedVerts;
  }

  public PVector midPoint(PVector p1, PVector p2) {
    PVector copyP1 = p1.copy();
    return copyP1.lerp(p2, 0.5f);
  }
}




//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// SimSphere
//

class SimSphere extends SimTransform{

  private float radius = 1;

  public int levelOfDetail = 10;
  SimSphereGraphic drawSphere; 

  public SimSphere() {
    init( vec(0,0,0),  1);
  }

  public SimSphere(float rad) {

    init( vec(0,0,0),  rad);
    
  }

  public SimSphere(PVector cen, float rad) {
    init( cen,  rad);
    
  }
  
  
  
  
  public void init(PVector cen, float rad){
     radius = rad;
     origin = cen.copy();
     drawSphere = new SimSphereGraphic(this, levelOfDetail);
  }
  
  public void setLevelOfDetail(int lod){
    if(lod < 6) {
      lod = 6;
      println("Sphere level of detail cannot be below 6");
    }
    levelOfDetail = lod;
    init(origin, radius);
  }

  public PVector getCentre() {
    return getOrigin();
  }

  public void setCentre(PVector c) {
    this.origin = c;
  }

  public void setRadius(float r) {
    this.radius = r;
  }

  public float getRadius() {
    float tradius = this.radius;
    tradius *= this.scale;
    return tradius;
  }

  // You set centre and radius by using setTransformAbs()


  public boolean isPointInside(PVector p) {
    PVector transCen = getCentre();
    float transRad = getRadius();
    float distP_Cen = transCen.dist(p);
    if (distP_Cen < transRad) return true;
    return false;
  }

  public boolean intersectsSphere(SimSphere otherSphere) {
    PVector otherCen = otherSphere.getCentre();
    PVector thisCen = this.getCentre();
    float otherRadius = otherSphere.getRadius();
    float thisRadius = this.getRadius();
    if ( thisCen.dist(otherCen) < thisRadius+otherRadius ) {
      swapColliderIDs(otherSphere);  
      return true;
    }
    return false;
  }
  
  
  public boolean collidesWith(SimTransform other){
    if(other == this) return false;
    
    String otherClass = getClassName(other);
    //println("collidesWith between this ", getClassName(this), " and " , otherClass);
    switch(otherClass) {
      case "SimSphere": 
          return intersectsSphere((SimSphere) other);
      case "SimBox": 
          return ((SimBox)other).intersectsSphere(this);
      case "SimSurfaceMesh": 
          return ((SimSurfaceMesh)other).intersectsSphere(this);
      case "SimModel": 
          SimTransform boundingGeom  = ((SimModel)other).getPreferredBoundingVolume();
          return boundingGeom.collidesWith(this);
    }
    
    return false;
  }

  public boolean calcRayIntersection(SimRay ray) {
    ray.isIntersection = false;
    PVector sphereCen = this.getCentre();
    //println("ray orig ,dir:", this.origin.x,  this.origin.y,  this.origin.z,"  " ,direction.x,  direction.y,  direction.z);
    //println("sphere centre:", sphereCen.x,  sphereCen.y,  sphereCen.z);
    float sphereRad = this.getRadius();
    PVector sphereCenToRayOrigin = PVector.sub(ray.origin, sphereCen); //m
    float b = PVector.dot(sphereCenToRayOrigin, ray.direction);
    float c = PVector.dot(sphereCenToRayOrigin, sphereCenToRayOrigin) - (sphereRad*sphereRad);

    if (c > 0 && b > 0) return false;
    // goes on to calculate the actual interetxection now
    float discr = b*b - c;

    // a negative discriminant means sphere behind ray origin
    if (discr < 0) return false;

    // ray now found to interesect
    float t = -b - sqrt(discr);

    // if t is negative then ray origin inside sphere, clamp t to zero
    if (t < 0) { 
      t = 0;
      
    }
    

    PVector dirMult = PVector.mult(ray.direction, t);
    ray.intersectionPoint = PVector.add(ray.origin, dirMult );
    ray.setIntersectionNormal(  PVector.sub(ray.intersectionPoint, sphereCen) );
    ray.isIntersection = true;
    swapColliderIDs(ray);
    return true;
  }


  public void drawMe() {
    
    
    float r = getRadius();
    //println("shpere radius",r);
    
     
      PVector transCen = getCentre();
      
      pushMatrix();
      translate(transCen.x, transCen.y, transCen.z);
      scale(r);
      drawSphere.drawMe();
      popMatrix();
  }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// SimBox, for AABB or OBB boxes
// Ray picking works in all cases
// isPointInside only works for AABBs
// intersects does not work yet

class SimBox extends SimTransform{
  // Bounding Box SimObject
  // It can only be initially defined in the major axis, but after that can be rotated
  // It can be used as an Axis Aligned Bounding Box
  // It can be be rotated, but used to estimate geometric collisions/intersectons
  PVector minCorner;
  PVector maxCorner;

  // index of top, and bottom vertices
  int
    T1 = 0, 
    T2 = 1, 
    T3 = 2, 
    T4 = 3, 
    B1 = 4, 
    B2 = 5, 
    B3 = 6, 
    B4 = 7;


  PVector[] vertices;


  public SimBox() {
    PVector c1 = new PVector(-1, -1, -1);
    PVector c2 = new PVector(1, 1, 1);
    setExtents(c1, c2);
  }

  public SimBox(PVector c1, PVector c2) {
    setExtents(c1, c2);
  }



  public void setExtents(PVector c1, PVector c2) {
    vertices = new PVector[8];
    // sorts the data into min x,y,z, and max x,y,z
    // does not yet catch "illegal" boxes (zero width etc)
    setIdentityTransform();
    float minx = min(c1.x, c2.x);
    float miny = min(c1.y, c2.y);
    float minz = min(c1.z, c2.z);

    float maxx = max(c1.x, c2.x);
    float maxy = max(c1.y, c2.y);
    float maxz = max(c1.z, c2.z);

    minCorner = new PVector(minx, miny, minz);
    maxCorner = new PVector(maxx, maxy, maxz);
    createVertices();
  }

  public int getNumFacets() { 
    return 6;
  }

  public PVector[] getExtents() {
    //if (isRotated()) {
      // if rotated, need to find new extents
      return getExtents(vertices);
    // }

    

    //PVector[] extents = new PVector[4];
    //extents[0] = transform(minCorner);
   // extents[1] = transform(maxCorner);
    //extents[2] = getCentre();
    //extents[3] = extents[1]; // max and min corner will be the same distance from centre
    //return extents;
  }

  private void createVertices() {
    //top corner
    float tx = minCorner.x;
    float ty = minCorner.y;
    float tz = minCorner.z;

    float bx = maxCorner.x;
    float by = maxCorner.y;
    float bz = maxCorner.z;
    // top face corners
    vertices[T1] = new PVector(tx, ty, tz);
    vertices[T2] = new PVector(tx, ty, bz);
    vertices[T3] = new PVector(bx, ty, bz);
    vertices[T4] = new PVector(bx, ty, tz);
    // bottom face corners
    vertices[B1] = new PVector(tx, by, tz);
    vertices[B2] = new PVector(tx, by, bz);
    vertices[B3] = new PVector(bx, by, bz);
    vertices[B4] = new PVector(bx, by, tz);
  }

  //////////////////////////////////////////////////////////////////////
  // returns the transformed values depending on boolean
  //
  //
  public PVector getCentre() {
    // sould work for both AABB and OBB's
    PVector minCornerTrans = transform(minCorner);
    PVector maxCornerTrans = transform(maxCorner);

    return minCornerTrans.lerp(maxCornerTrans, 0.5f);
  }






  public SimBox getTransformedCopy() {
    // returns a copy of the current bounding box with the transformation "baked in"
    PVector transvertices[] = getTransformedVertices(vertices);
    SimBox copyOut = new SimBox();
    copyOut.vertices = transvertices;
    PVector exts[] = copyOut.getExtents();
    copyOut.minCorner = exts[0];
    copyOut.maxCorner = exts[1];
    return copyOut;
  }


  public SimFacet getFacet(int num) {
    // returns the transformed facet
    // 0 = top, 1 = front, 2 = left, 3 = right, 4 = back, 5 = bottom 
    int v1, v2, v3, v4;

    //forward face
    // initialise them to this as default
    v1 = T1;
    v2 = T4;
    v3 = B4;
    v4 = B1;

    // top
    if (num == 0) {
      v1 = T1;
      v2 = T2;
      v3 = T3;
      v4 = T4;
    }

    //lhs face 
    if (num == 2) {
      v1 = T1;
      v2 = B1;
      v3 = B2;
      v4 = T2;
    }

    //rhs face 
    if (num == 3) {
      v1 = T4;
      v2 = T3;
      v3 = B3;
      v4 = B4;
    }

    //back face 
    if (num == 4) {
      v1 = T2;
      v2 = B2;
      v3 = B3;
      v4 = T3;
    }

    //bottom face 
    if (num == 5) {
      v1 = B1;
      v2 = B4;
      v3 = B3;
      v4 = B2;
    }

    PVector p1 = transform(vertices[v1]);
    PVector p2 = transform(vertices[v2]);
    PVector p3 = transform(vertices[v3]);
    PVector p4 = transform(vertices[v4]);

    return new SimFacet(p1, p2, p3, p4);
  }

  /////////////////////////////////////////////////////////////////////
  // intersection/collision methods
  //
  public boolean isPointInside(PVector p) {
    
    if( isRotated() ){
      println("non axis aligned BB point intersection not implemented yet");
      return false;
    }
      
      // is AABB
      PVector minCornerTrans = transform(minCorner);
      PVector maxCornerTrans = transform(maxCorner);

      if (  isBetweenInc(p.x, maxCornerTrans.x , minCornerTrans.x)   &&
            isBetweenInc(p.y, maxCornerTrans.y , minCornerTrans.y)   &&
            isBetweenInc(p.z, maxCornerTrans.z , minCornerTrans.z)  ) return true;
            
      return false;
  }
  
  
  
  public boolean collidesWith(SimTransform other){
    if(other == this) return false;
    String otherClass = getClassName(other);
    
    switch(otherClass) {
      case "SimSphere": 
          return  intersectsSphere((SimSphere)other);
      case "SimBox": 
          return  intersectsBox((SimBox)other);
      case "SimSurfaceMesh": 
          return  ((SimSurfaceMesh)other).intersectsBox(this);
      case "SimModel": 
          SimTransform boundingGeom  = ((SimModel)other).getPreferredBoundingVolume();
          return boundingGeom.collidesWith(this);
    }
    //println("collidesWith between this ", getClassName(this), " and " , otherClass, " false");
    return false;
  }

  public boolean calcRayIntersection(SimRay sr) {
    boolean intersectionFound = false;

    sr.clearIntersectingTriangles();
    for (int i = 0; i < 6; i++) {

      SimFacet f = getFacet(i);
      SimTriangle t1 = f.tri1;
      SimTriangle t2 = f.tri2;

      if ( sr.addIntersectingTriangle(t1) ) intersectionFound = true;
      if ( sr.addIntersectingTriangle(t2) ) intersectionFound = true;
    }

    if (intersectionFound) {
      sr.getNearestTriangleIntersectionPoint();
      sr.swapColliderIDs(this);
      //println("camera", getCameraPosition()," box hit ",sr.intersectionPoint);
    }
    return intersectionFound;
  } 




  public boolean intersectsSphere(SimSphere sphere) {
    // Thanks to Jim Arvo in Graphics Gems 2   
    if ( isRotated() == false ) {
      PVector[] exts = getExtents();
      PVector bmin = exts[0];
      PVector bmax = exts[1];
      PVector c = sphere.getCentre();
      float r = sphere.getRadius();
      float r2 = r * r;
      float dmin = 0;

      if ( c.x < bmin.x ) {
        dmin += sqr( c.x - bmin.x );
      } else {
        if ( c.x > bmax.x ) {
          dmin += sqr( c.x - bmax.x );
        }
      }

      if ( c.y < bmin.y ) {
        dmin += sqr( c.y - bmin.y );
      } else {
        if ( c.y > bmax.y ) {
          dmin += sqr( c.y - bmax.y );
        }
      }

      if ( c.z < bmin.z ) {
        dmin += sqr( c.z - bmin.z );
      } else {
        if ( c.z > bmax.z ) {
          dmin += sqr( c.z - bmax.z );
        }
      }

      boolean intersects = dmin <= r2;
      if (intersects) swapColliderIDs(sphere);  
      return intersects;
    }

    println("SimBox::intersectsSphere not implemented for non AABB's");
    return false;
  }



  public boolean intersectsBox(SimBox otherBox) {
    // tbd

    if ( isRotated() == false || otherBox.isRotated()==false) {
      // is AABB
      PVector[] thisExts = getExtents();
      PVector[] otherExts = otherBox.getExtents();
      int MIN = 0;
      int MAX = 1;

      boolean intersects =  (thisExts[MIN].x < otherExts[MAX].x) && (thisExts[MAX].x > otherExts[MIN].x) &&
        (thisExts[MIN].y < otherExts[MAX].y) && (thisExts[MAX].y > otherExts[MIN].y) &&
        (thisExts[MIN].z < otherExts[MAX].z) && (thisExts[MAX].z > otherExts[MIN].z);

      if (intersects) swapColliderIDs(otherBox);  
      return intersects;
    }



    println("Rotated box intersection not implemented yet, use rays");
    return false;
  }

  // draws the transformed shape
  public void drawMe() {
    //topface
    beginShape();
    drawTransformedVertex(vertices[T1]);
    drawTransformedVertex(vertices[T2]);
    drawTransformedVertex(vertices[T3]);
    drawTransformedVertex(vertices[T4]);
    endShape(CLOSE);


    //forward face
    beginShape();
    drawTransformedVertex(vertices[T1]);
    drawTransformedVertex(vertices[T4]);
    drawTransformedVertex(vertices[B4]);
    drawTransformedVertex(vertices[B1]);
    endShape(CLOSE);


    //lhs face 
    beginShape();
    drawTransformedVertex(vertices[T1]);
    drawTransformedVertex(vertices[B1]);
    drawTransformedVertex(vertices[B2]);
    drawTransformedVertex(vertices[T2]);
    endShape(CLOSE);


    //rhs face 
    beginShape();
    drawTransformedVertex(vertices[T4]);
    drawTransformedVertex(vertices[T3]);
    drawTransformedVertex(vertices[B3]);
    drawTransformedVertex(vertices[B4]);
    endShape(CLOSE);


    //back face 
    beginShape();
    drawTransformedVertex(vertices[T2]);
    drawTransformedVertex(vertices[B2]);
    drawTransformedVertex(vertices[B3]);
    drawTransformedVertex(vertices[T3]);
    endShape(CLOSE);

    //bottom face 
    beginShape();
    drawTransformedVertex(vertices[B1]);
    drawTransformedVertex(vertices[B4]);
    drawTransformedVertex(vertices[B3]);
    drawTransformedVertex(vertices[B2]);
    endShape(CLOSE);
  }
}



//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This is a shape initialsed with a PShape. It keeps this copy in cardinalModel, and you can set the shapes
// transformation usnig the standard setTrasnformAbs/Rel.
// The shape is then drawn with these transforms.
// When you need to get the geometry of the shape, using getBoundingBox or getBoundingSphere, getCentre or getVertices
// it temporality creates a falttedned shape from the original model (whihc will then have the actual 
// transformed vertices)
// 
// array of vertices.
// It can be set by copying in a PShape. This will then be "flattened" (children and transforms removed)
// texture and material will also be lost

class SimModel extends SimTransform{

  // stores the cardinal model
  private PShape cardinalModel;
  private PVector[] rawvertices;
  
  // cardinal bounding volumes
  private SimSphere boundingSphere = new SimSphere();
  private SimBox boundingBox = new SimBox();

  private String preferredBoundingVolume;
  public int boundingVolumeTransparency = 100;
  public boolean showBoundingVolume = true;

  public SimModel() {
  }
  
  public SimModel(String filename){
    PShape mod = loadShape(filename);
    if(mod == null){
      println("SimModel: cannot load file name ", filename);
      return;
    }
    setWithPShape(mod);
  }

  public void setWithPShape(PShape shapeIn) {
    cardinalModel = shapeIn;
    calculateBoundingGeometry();
  }

  public void calculateBoundingGeometry() {
    // calculates the boundig sphere of thr cardinal geometry
    // the transformed sphere is returned by getBoundingSphere() 
    rawvertices = getRawVertices();
    PVector[] extents = getExtents(rawvertices);
    //println("extents are ", extents[0],extents[1]);
    PVector centrePoint = extents[2];
    PVector furthestVerticesFromCentre = extents[3];
    float radius = furthestVerticesFromCentre.dist(centrePoint);
    boundingBox = new SimBox(extents[0], extents[1]);
    
    
    boundingSphere = new SimSphere(centrePoint, radius);
 
    preferredBoundingVolume = "box";
  }
  
  public PVector[] getExtents(){
    return boundingBox.getExtents();
  }
  
  public void setID(String i){
    // this overrides the simtransform setID method, to give the bounding
    // shapes the same id
    id = i;
    boundingBox.setID(id + "_boundingBox");
    boundingSphere.setID(id + "_boundingSphere");
  }
  
  public void setPreferredBoundingVolume(String BOXorSPHERE){
    String s = BOXorSPHERE.toLowerCase();
    if(s.equals("box")) preferredBoundingVolume = "box";
    if(s.equals("sphere")) preferredBoundingVolume = "sphere";
  }
  
  public SimTransform getPreferredBoundingVolume(){
    if(preferredBoundingVolume.equals("box")) {return getBoundingBox();}
    else {  return getBoundingSphere(); }
  }
  
  public void showBoundingVolume(boolean show){
    showBoundingVolume = show;
  }
  
  public boolean calcRayIntersection(SimRay sr){ 
    SimTransform obj = null;
    if(preferredBoundingVolume.equals("sphere")) { obj = getBoundingSphere();}
    else { obj = getBoundingBox();}
    
    return obj.calcRayIntersection(sr);
  }

  public boolean collidesWith(SimTransform other){
    
    if(other == this) return false;
    
    SimTransform thisCollidingShape = getPreferredBoundingVolume();
    
    return thisCollidingShape.collidesWith(other);
  }
  
  

  public PVector[] getRawVertices() {
    PShape flatmodel = cardinalModel.getTessellation(); 

    int total = flatmodel.getVertexCount();
    PVector[] vertices = new PVector[total];
    for (int j = 0; j < total; j++) {
      vertices[j] = flatmodel.getVertex(j);
    }
    return vertices;
  }

  public PVector[] getTransformedVertices(){
    PVector[] rawVertices = getRawVertices();
    return getTransformedVertices(rawVertices);
  }


  public SimSphere getBoundingSphere() {
    boundingSphere.setTransformAbs( this.scale, this.rotateX, this.rotateY, this.rotateZ, this.translate);
    boundingSphere.setID( getID() );
    return boundingSphere;
  }

  public SimBox getBoundingBox() {
    boundingBox.setTransformAbs( this.scale, this.rotateX, this.rotateY, this.rotateZ, this.translate);
    boundingBox.setID( getID() );
    return boundingBox;
  }
  
  
  
  public SimBox getAABB(){
    PVector[] transformedVertices = getTransformedVertices();
    PVector[] extents = getExents_DoNotApplyTransform(transformedVertices);
    //println("AABB extents are ", extents[0],extents[1]);
    return new SimBox(extents[0], extents[1]);

  }
  
  

  private void transformOriginalForDrawing() {
    cardinalModel.resetMatrix();
    cardinalModel.scale(this.scale);
    cardinalModel.rotateX(this.rotateX);
    cardinalModel.rotateY(this.rotateY);
    cardinalModel.rotate(this.rotateZ, 0, 0, 1); // fix for bug

    cardinalModel.translate(this.translate.x, this.translate.y, this.translate.z);

    // bounding shapes
  }

  public void drawMe() {

    transformOriginalForDrawing();

    shape(cardinalModel);
    cardinalModel.resetMatrix();
    
    if(showBoundingVolume) drawBoundingVolume();
  }
  
  public void drawBoundingVolume(){
    if(preferredBoundingVolume.equals("sphere")) drawBoundingSphere();
    if(preferredBoundingVolume.equals("box")) drawBoundingBox();
  }

  public void drawBoundingSphere() {
    int cc = g.fillColor;
    pushStyle();
    fill( red(cc),green(cc), blue(cc),boundingVolumeTransparency);
    SimSphere bs = getBoundingSphere();
    bs.drawMe();
    popStyle();
  }

  public void drawBoundingBox() {
    SimBox bb = getBoundingBox();
    int cc = g.fillColor;
    pushStyle();
    fill( red(cc),green(cc), blue(cc),boundingVolumeTransparency);
    bb.drawMe();
    popStyle();
  }
  
  public void drawAABB() {
    SimBox bb = getAABB();
    bb.drawMe();
  }
}


////////////////////////////////////////////////////////////////////////////////////////
// SimRay. a class for defining a ray. It is defined by 2 3d points that are both on the ray
// 
class SimRay extends SimTransform {

  PVector direction = vec(0, 0, -1);
  private PVector intersectionPoint = vec(0, 0, 0);

  // this is the surface normal at the point of intersection
  private PVector intersectionNormal;

  boolean isIntersection = false;
  ArrayList<SimTriangle> intersectingTriangleList = new ArrayList<SimTriangle>();

  public SimRay() {
  }

  public SimRay(PVector v1, PVector v2) {
    origin = v1.copy();
    direction = PVector.sub(v2, v1);
    direction.normalize();
  }

  public void printMe() {
    println("SimRay: origin - ", this.origin, " direction - ", this.direction);
  }
  
  
  
  
  public void drawMe(){
    PVector farPoint = PVector.add(origin, direction);
    farPoint.mult(10000);
    line(origin.x,origin.y,origin.z, farPoint.x,farPoint.y,farPoint.z); 
  }

  //////////////////////////////////////////////////////////////////////////////
  // once an intersection calculation has been made, the intersection can be queried
  //

  public boolean isIntersection() {
    return isIntersection;
  }
  
  public boolean collidesWith(SimTransform otherObject){
    return calcIntersection(otherObject);
  }
  
  // this is here to satify the abtract class SimTransform methods
  // in this universe, a sim ray can never intersect another sim ray
  public boolean calcRayIntersection(SimRay ray){ return false;}
  
  public int getNumIntersections(){
    // this works only with triangulated shapes (so doesnot work with spheres)
    return intersectingTriangleList.size();
  }

  public PVector getIntersectionPoint() {
    // this returns the intersection point nearest to the Ray's origin
    return this.intersectionPoint;
  }

  public PVector getIntersectionNormal() {
    return this.intersectionNormal;
  }
  
  public void setIntersectionNormal(PVector n){
    this.intersectionNormal = n.copy();
    this.intersectionNormal.normalize();
  }



  public SimRay copy() {
    SimRay sr =  new SimRay();
    sr.origin = this.origin.copy();
    sr.direction = this.direction.copy();

    return sr;
  }

  public PVector getPointAtDistance(float d) {
    // returns the point on the ray at distance d from the origin
    PVector p1 = PVector.mult(direction, d);
    return PVector.add(p1, origin);
  }


  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // SimRay intersection calculations
  // Any shape that can intersect with a ray, must have the calcRayIntersection() method implemented
  //
  //
  public boolean calcIntersection(SimTransform shape) {
    return shape.calcRayIntersection(this);
  }



  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // SimRay multiple triangle intersection calculations
  // would like these to be private, but have to be public
  //
  //
  public boolean addIntersectingTriangle(SimTriangle t) {
    if ( t.calcRayIntersection(this) ) {
      intersectingTriangleList.add(t);
      return true;
    }
    return false;
  }

  public void clearIntersectingTriangles() {
    intersectingTriangleList.clear();
  }

  public PVector getNearestTriangleIntersectionPoint() {
    PVector nearestIntersectionPoint = vec(0, 0, 0);
    float nearestIntersectionDistance = 10000000000.0f;
    PVector nearestSurfaceNormal = vec(0, 0, 0);
    for (SimTriangle t : intersectingTriangleList) {

      if ( t.calcRayIntersection(this)) {
        PVector rayIntersectionPoint = getIntersectionPoint();
        PVector rayOrigin = getOrigin();
        float thisPointDistToRayOrigin = PVector.dist(rayIntersectionPoint, rayOrigin);
        if (thisPointDistToRayOrigin < nearestIntersectionDistance) {
          nearestIntersectionDistance = thisPointDistToRayOrigin;
          nearestIntersectionPoint = rayIntersectionPoint;
          nearestSurfaceNormal = t.surfaceNormal();
        }//end if
      }//end if
    }// end for
    this.intersectionPoint = nearestIntersectionPoint;
    this.setIntersectionNormal(nearestSurfaceNormal);
    return nearestIntersectionPoint;
  }// end method
}// end ray class
////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////
// SimSurfaceMesh
// A xdim by zdim mesh of vertices, whihc forms a surface
// Each of these vertices can be given a Y height, so as to form a landscape
//
//
// The mesh can be transformed using setTransformAbs(...) or setTransformRel(....)
// afterwhich you need to call applyTransform() to fix it permanently in the mesh vertices.
//


class SimSurfaceMesh  extends SimTransform{

    int numFacetsX, numFacetsZ;

    PVector[] meshVertices;
    public int numTriangles = 0;
    // mesh coordinates are stored in this array. It is made at the start
    
    //SimRay pick information
    public int rayIntersectionTriangleNum; 
    public PVector rayIntersectionPoint;
    
    PImage textureMap;
    
    public SimSurfaceMesh(int numInX, int numInZ, float scale)
    {
        // numInX and Z represent the number of Facets generated.
        // The number of triangles is (number of Facets)*2
        
        // the number of vertices to make this is (numFacetsX+1) * (numFacetsY+1) 
        numFacetsX = numInX;
        numFacetsZ = numInZ;
        //meshVertices = new ArrayList<PVector>();
        meshVertices = new PVector[(numFacetsX+1)*(numFacetsZ+1)];
        for(int z = 0; z < numFacetsZ+1; z++)
        {
            for (int x = 0; x < numFacetsX+1; x++)
            {
                float xf = x * scale;
                float yf = 0.0f;
                float zf = z * scale;
                setMeshVertex(x,z,  new PVector(xf, yf, zf) );
            }
            
        }

    }
    
    
    public void setHeightsFromImage(PImage im, float maxAltitude){
    
      int numInX = getNumVerticesX();
      int numInZ = getNumVerticesZ();
      int imWidth = im.width;
      int imHeight = im.height;
      
      for(int z = 0; z < numInZ; z++)
          {
              for (int x = 0; x < numInX; x++)
              {
                 int imx = (int) map(x,0,numInX,0,imWidth);
                 int imy = (int) map(z,0,numInZ,0,imHeight);
                 int col = im.get(imx,imy);
                 float hght = map(red(col),0,255,0,maxAltitude);
                 setVertexY(x,z,hght );
              }
              
          }
    
  }
    
    
    public void setTextureMap(PImage t){
      textureMap = t.copy();
    }
    
    
    // this permanently applies the transform
    public void bakeInTransform(){
      meshVertices = getTransformedVertices(meshVertices);
      setTransformAbs(1.0f,0.0f,0.0f,0.0f, vec(0,0,0));
    }
    

    public boolean intersectsSphere(SimSphere sphere){
      for(PVector thisVertex: meshVertices){
        PVector transformedVertex = transform(thisVertex);
        if( sphere.isPointInside(transformedVertex) ){
          swapColliderIDs(sphere);
          return true;
        }
      }
     return false; 
    }
    
     public boolean intersectsBox(SimBox box){
      for(PVector thisVertex: meshVertices){
        PVector transformedVertex = transform(thisVertex);
        if( box.isPointInside(transformedVertex) ) {
          swapColliderIDs(box);
          return true;
        }
      }
     return false; 
    }
    
    public void drawMe(){
      if ( textureMap != null) {
      drawMe_Texture();
      return;
      }
      int numFacets = getNumFacets();
      beginShape(TRIANGLES);
        
        // Center point
        
        for (int i = 0; i < numFacets; i++) {
          SimFacet f = getFacet(i);
          SimTriangle t1 = f.tri1;
          SimTriangle t2 = f.tri2;
          // draws ok
          drawTransformedVertex(t1.p1);
          drawTransformedVertex(t1.p2);
          drawTransformedVertex(t1.p3);
          // doenst draw
          drawTransformedVertex(t2.p1);
          drawTransformedVertex(t2.p2);
          drawTransformedVertex(t2.p3);
        }
        endShape();
    }
    
    public void drawMe_Texture() {
      int numFacets = getNumFacets();
      beginShape(TRIANGLES);
      texture(textureMap);
      //g3d.blendMode(REPLACE); 
      for (int i = 0; i < numFacets; i++) {
       
        SimFacet f = getFacet(i);
        SimTriangle t1 = f.tri1;
        SimTriangle t2 = f.tri2;
   
        drawTransformedVertex_Texture(t1.p1);
        drawTransformedVertex_Texture(t1.p2);
        drawTransformedVertex_Texture(t1.p3);
  
        drawTransformedVertex_Texture(t2.p1);
        drawTransformedVertex_Texture(t2.p2);
        drawTransformedVertex_Texture(t2.p3);
        }
      
      endShape();
  }
  
  public void  drawTransformedVertex_Texture(PVector vertex) {
    PVector transformedVector = transform(vertex);
    PVector uv = getTextureUV(vertex);
    vertex(transformedVector.x, transformedVector.y, transformedVector.z, uv.x, uv.y);
  }
  
  public PVector getTextureUV(PVector vertex){
    int w = textureMap.width;
    int h = textureMap.height;
    PVector minVertex = getMeshVertex(0, 0);
    PVector maxVertex = getMeshVertex(numFacetsX, numFacetsZ);
    float u = map(vertex.x, minVertex.x, maxVertex.x, 0, w-1);
    float v = map(vertex.z, minVertex.z, maxVertex.z, 0, h-1);
    return new PVector(u,v);
  }
  
  
  public boolean collidesWith(SimTransform other){
    if(other == this) return false;
    String otherClass = getClassName(other);
    //println("collidesWith between this ", getClassName(this), " and " , otherClass);
    switch(otherClass) {
      case "SimSphere":
        return intersectsSphere((SimSphere)other);
      case "SimBox": 
        return intersectsBox((SimBox)other);
      case "SimSurfaceMesh": 
        println("SimSurfaceMesh collides with sim surface mesh implemented - use rays");
        break;
      case "SimModel": 
        SimTransform boundingGeom  = ((SimModel)other).getPreferredBoundingVolume();
        return boundingGeom.collidesWith(this);
    }
    
    return false;
  }
  
  
   
  public boolean calcRayIntersection(SimRay sr){
    boolean intersectionFound = false;
    
    int numFacets = getNumFacets();
    sr.clearIntersectingTriangles();
    for (int i = 0; i < numFacets; i++) {
          
          SimFacet f = getTransformedFacet(i);
          SimTriangle t1 = f.tri1;
          SimTriangle t2 = f.tri2;

          if( sr.addIntersectingTriangle(t1) ) intersectionFound = true;
          if( sr.addIntersectingTriangle(t2) ) intersectionFound = true; 
        }
       
    if(intersectionFound){
      sr.getNearestTriangleIntersectionPoint();
      sr.swapColliderIDs(this);
      //println("camera", getCameraPosition(),"grid hit",sr.intersectionPoint);
    }
    return intersectionFound;
  } 
  

   //////////////////////////////////////////////// 
    public void setVertexY(int vertexX, int vertexZ, float y){
      // there are meshSizeX+1, meshSizeY+1 vertices in this messh
      int vertexGridWidth = numFacetsX + 1;
      int index =  vertexZ * vertexGridWidth + vertexX;
      PVector p = meshVertices[index];
      p.y = y;
      meshVertices[index] = p;
    }
    
    public PVector getMeshVertex(int vertexX, int vertexZ){
      int vertexGridWidth = numFacetsX + 1;
      int index =  vertexZ * vertexGridWidth + vertexX;
      return meshVertices[index];
    }
    
    public void setMeshVertex(int vertexX, int vertexZ, PVector v){
      int vertexGridWidth = numFacetsX + 1;
      int index =  vertexZ * vertexGridWidth + vertexX;
      meshVertices[index] = v;
    }
    
    /////////////////////////////////////////////
    // private below here

    public int getNumFacets(){
      return (numFacetsX)* (numFacetsZ); 
    }
    
    public int getNumTriangles(){
        return (numFacetsX)* (numFacetsZ)*2;
    }
    
    private int getNumVerticesX(){ return  numFacetsX+1;}
    private int getNumVerticesZ(){ return  numFacetsZ+1;}

    public SimFacet getTransformedFacet(int index){
      SimFacet facet = getFacet( index);
      PVector[] verts = facet.getVertices();
      PVector[] transformedVerts = new PVector[4];
      for(int n = 0; n < 4; n++) transformedVerts[n] = transform(verts[n]);
      return new SimFacet(transformedVerts[0],transformedVerts[1],transformedVerts[2],transformedVerts[3]);
    }
    
    public SimFacet getFacet(int index){
        
        //the vertices under consideration
        // A B
        // C D
        // as indices into the meshVertices array
        int vertextGridWidth = numFacetsX+1;
        int rowNum = (int)(index/numFacetsX);
        int A = index + rowNum;
        int B = A + 1;
        int C = A + vertextGridWidth;
        int D = C + 1;

        //println("index ", index, "row ", rowNum," vertices nums ", A,B,C,D);
        SimFacet facet = new SimFacet();
        // triangle 1
        facet.tri1.p1 = meshVertices[D];
        facet.tri1.p2 = meshVertices[B];
        facet.tri1.p3 = meshVertices[A];
 
        // triangle 2
        facet.tri2.p1  = meshVertices[D];
        facet.tri2.p2  = meshVertices[A];
        facet.tri2.p3  = meshVertices[C];

        return facet;

      
    }

    public SimFacet getFacet(int x, int z)
    {
        int vertexGridWidth = numFacetsX + 1;
        int index =  z * vertexGridWidth + x;
        
        return getFacet(index); 
    }
    
  
  

}// end SimSurfaceMesh class





////////////////////////////////////////////////////////////////////////////
// SimTriangle
// simple containter for a 2d or 3d triange
//

class SimTriangle{
  public PVector p1,p2,p3;
  public SimTriangle(PVector p1, PVector p2, PVector p3){
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
  }
  
  public SimTriangle(){
    this.p1 = new PVector(0,0,0);
    this.p2 = new PVector(0,0,0);
    this.p3 = new PVector(0,0,0);
  }
  
  public void flip(){
    // flips the direction from CW to CCW or visa-versa
    PVector oldP2 = this.p2.copy();
    PVector oldP3 = this.p3.copy();
    this.p2 = oldP3;
    this.p3 = oldP2;
    // p1 stays the same
  }
  
  public void printMe(){
    println("triange:",p1,p2,p3);
    
  }
  
   public void drawMe(){
      beginShape(TRIANGLE);
      vertex(this.p1.x,this.p1.y,this.p1.z);
      vertex(this.p2.x,this.p2.y,this.p2.z);
      vertex(this.p3.x,this.p3.y,this.p3.z);
      endShape(CLOSE);
    }
    

  public PVector surfaceNormal(){ 
    PVector edgep1p2 = PVector.sub(p2,p1);
    PVector edgep1p3 = PVector.sub(p3,p1);
    PVector cross = edgep1p2.cross(edgep1p3);
    cross.y *= -1;
    return cross;
  }
  
  /*
  boolean isBackFacing(){
    PVector sn = surfaceNormal();
    PVector cameraPos = getCameraPosition();
    if( sn.dot( cameraPos.sub(p1) ) > 0.0 ) return true;
    return false;
  }
  */
  
  
  public boolean calcRayIntersection(SimRay ray) 
        {
        
        
        //MOLLER_TRUMBORE algorithm
        ray.intersectionPoint = null;
        
        // make local copies so we don't change anything ouside the function
        PVector dir = ray.direction.copy();
        PVector orig = ray.origin.copy();
        PVector v0 = this.p1.copy();
        PVector v1 = this.p2.copy();
        PVector v2 = this.p3.copy();
        
        
        PVector edge_v0v1 = v1.sub(v0);
        PVector edge_v0v2 = v2.sub(v0);
        PVector pvec = dir.cross(edge_v0v2);
        
        float det = edge_v0v1.dot(pvec);
        
        if( nearZero(det) ){
          // ray is parallel with triangle plane
          // this ignores the direction of triangle winding
          return false;
        }
        
        float invDet = 1.0f/det;
        PVector tvec = PVector.sub(orig,v0);
        float u = tvec.dot(pvec) * invDet;
        if( u < 0 || u > 1) {
          return false;
        }
        
        PVector qvec = tvec.cross(edge_v0v1);
        float v = dir.dot(qvec) * invDet;
        if(v < 0 || u + v > 1){
          return false;
        }
        
        float t = edge_v0v2.dot(qvec) * invDet;
        if(t < EPSILON){
          // line intersection, not ray intersection... 
          // to avoid hitting a point behind the camera
          return false;
        }
        PVector addToOrigin = PVector.mult(dir,t);
        ray.intersectionPoint = PVector.add(orig,addToOrigin);
        
       // if(isBackFacing()){
       //   //println("is back facing");
       //   //return false;
       // }
        
        
        return true;
        
    }
}





////////////////////////////////////////////////////////////////////////////
// SimFacet
// Two triangles ake a facet
//
class SimFacet{
  public SimTriangle tri1;
  public SimTriangle tri2;
  
  public SimFacet(){
    tri1 = new SimTriangle();
    tri2 = new SimTriangle();
  }
  
  public SimFacet(PVector p1, PVector p2, PVector p3, PVector p4){
    setVertices(p1,p2,p3,p4);
  }
    
  public void setVertices( PVector p1, PVector p2, PVector p3, PVector p4){ 
    // give 4 vertices of a facet, in either winding, create a correct 2-triangle facet
    // currently cannot handle "butterfly" quads
   tri1 = new SimTriangle(p1,p2,p3);
   tri2 = new SimTriangle(p1,p3,p4); 
  }
  
  public PVector[] getVertices(){
    PVector[] verts = new PVector[4];
    verts[0] = tri1.p1.copy();//p1
    verts[1] = tri1.p2.copy();//p2
    verts[2] = tri1.p3.copy();//p3
    verts[3] = tri2.p3.copy();//p4
    return verts;
  }
}
// SimpleUI_Classes version 5.0
// Started Dec 12th 2018
// This update July 2020
// Simon Schofield
// introduces image icons for buttons


//////////////////////////////////////////////////////////////////
// SimpleUI() is the only class you have to use in your 
// application to build and use the UI. 
// With it you can add buttons, sliders, menus and text boxes
// You can use a file dialogue to load and save files
//
//
// 
//
// Once a mouse event has been received by a UI item (button, menu etc) it calls a function called
// simpleUICallback(...) which you have to include in the 
// main part of the project (below setup() and draw() etc.)
//
// Also, you need to call uiManager.drawMe() in the main draw() function
//


public class SimpleUI{
  
    UIRect canvasRect;
    
    ArrayList<Widget> widgetList = new ArrayList<Widget>();
    
    String UIManagerName;
    
    UIRect backgroundRect = null;
    int backgroundRectColor; 

    // these are for capturing user events
    boolean pmousePressed = false;
    boolean pkeyPressed = false;
    String fileDialogPrompt = "";

    public SimpleUI(){
          UIManagerName = "";
          
      }
      
    public SimpleUI(String uiname){
          UIManagerName = uiname;
          
      }
      
   
   ////////////////////////////////////////////////////////////////////////////
   // file dialogue
   //
   
    public void openFileLoadDialog(String prompt) {
      fileDialogPrompt = prompt;
      selectInput(prompt, "fileLoadCallback", null, this);
    }
     
    public void fileLoadCallback(File selection) {
      
      // cancelled
      if(selection == null){
      return;
      }
      
     
      // is directory not file
      if (selection.isDirectory()){
      return;
      }

      UIEventData uied = new UIEventData(UIManagerName, "fileLoadDialog" , "fileLoadDialog", "mouseReleased", mouseX, mouseY);
      uied.fileSelection = selection.getPath();
      uied.fileDialogPrompt = this.fileDialogPrompt;
      handleUIEvent( uied);
    }
    
    
    
    public void openFileSaveDialog(String prompt) {
      fileDialogPrompt = prompt;
      selectOutput(prompt, "fileSaveCallback", null, this);
    }
     
    public void fileSaveCallback(File selection) {
      
      // cancelled
      if(selection == null){
      return;
      }
      
      String path = selection.getPath();
      println(path);

      UIEventData uied = new UIEventData(UIManagerName, "fileSaveDialog" , "fileSaveDialog", "mouseReleased", mouseX, mouseY);
      uied.fileSelection = selection.getPath();
      uied.fileDialogPrompt = this.fileDialogPrompt;
      handleUIEvent(uied);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // canvas creation
    //
    public void addCanvas(int x, int y, int w, int h){
      
      canvasRect = new UIRect(x,y,x+w,y+h);
    }
    
    public void checkForCanvasEvent(String mouseEventType, int x, int y){
       if(canvasRect==null) return;
       if(   canvasRect.isPointInside(x,y)) {
         UIEventData uied = new UIEventData(UIManagerName, "canvas" , "canvas", mouseEventType,x,y);
         handleUIEvent(uied);
       }

    }
    
    public void drawCanvas(){
      if(canvasRect==null) return;
      pushStyle();
      noFill();
      stroke(0,0,0);
      strokeWeight(1);
      rect(canvasRect.left, canvasRect.top, canvasRect.getWidth(), canvasRect.getHeight());
      popStyle();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // widget creation
    //
    
    public boolean widgetNameAlreadyExists(String label, String  uiComponentType){
      
      for(Widget w: widgetList){
       if(w.UILabel.equals(label) && w.UIComponentType.equals(uiComponentType) ) {
         println("SimpleUI: that label name - ", label, " - already exists for widget of type ", uiComponentType);
         return true;
       }
      }
      return false;
    }
    
    // button creation
    public ButtonBaseClass addPlainButton(String label, int x, int y){
      if(widgetNameAlreadyExists(label,"ButtonBaseClass")) return null;
      ButtonBaseClass b = new ButtonBaseClass(UIManagerName,x,y,label);
      
      widgetList.add(b);
      return b;
    }
    
    public ButtonBaseClass addToggleButton(String label, int x, int y){
      if(widgetNameAlreadyExists(label,"ButtonBaseClass")) return null;
      ButtonBaseClass b = new ToggleButton(UIManagerName,x,y,label);
      
      widgetList.add(b);
      return b;
    }
    
    public ButtonBaseClass addToggleButton(String label, int x, int y, boolean initialState){
      if(widgetNameAlreadyExists(label,"ButtonBaseClass")) return null;
      ButtonBaseClass b = new ToggleButton(UIManagerName,x,y,label);
      
      b.selected = initialState;
      widgetList.add(b);
      return b;
    }
    
    public ButtonBaseClass addRadioButton(String label, int x, int y, String groupID){
      if(widgetNameAlreadyExists(label,"ButtonBaseClass")) return null;
      ButtonBaseClass b = new RadioButton(UIManagerName,x,y,label,groupID, this);
      
      widgetList.add(b);
      return b;
    }
    
    // label creation
    public SimpleLabel addLabel(String label, int x, int y,String txt){
      if(widgetNameAlreadyExists(label,"SimpleLabel")) return null;
      SimpleLabel sl = new SimpleLabel(UIManagerName,label,x,y,txt);
      
      widgetList.add(sl);
      return sl;
    }
  
    // menu creation
    public Menu addMenu(String label, int x, int y, String[] menuItems){
      if(widgetNameAlreadyExists(label,"Menu")) return null;
      Menu m = new Menu(UIManagerName,label,x,y,menuItems, this);
      
      widgetList.add(m);
      return m;
      }
    
    // slider creation
    public Slider addSlider(String label, int x, int y){
      if(widgetNameAlreadyExists(label,"Slider")) return null;
      Slider s = new Slider(UIManagerName,label,x,y);
      
      widgetList.add(s);
      return s;
    }
    

    // text input box creation
    public TextInputBox addTextInputBox(String label, int x, int y){
      int maxNumChars = 14;
      if(widgetNameAlreadyExists(label,"TextInputBox")) return null;
      TextInputBox tib = new TextInputBox(UIManagerName,label,x,y,maxNumChars);
      widgetList.add(tib);
      return tib;
    }
    
    public TextInputBox addTextInputBox(String label, int x, int y, String content){
      if(widgetNameAlreadyExists(label,"TextInputBox")) return null;
      TextInputBox tib = addTextInputBox( label,  x,  y);
      
      tib.setText(content);
      return tib;
    }
    


    public void removeWidget(String uilabel){
      Widget w = getWidget(uilabel);
      if(w == null) return;
      widgetList.remove(w);
    }
    


    // getting widget data by lable
    //
    public Widget getWidget(String uilabel){
      for(Widget w: widgetList){
       if(w.UILabel.equals(uilabel)) return w;
      }
      println(" getWidgetByLabel: cannot find widget with label ",uilabel);
      return null;
    }
    
    
    // get toggle state
    public boolean getToggleButtonState(String uilabel){
      Widget w = getWidget(uilabel);
      if( w.UIComponentType.equals("ToggleButton") || w.UIComponentType.equals("RadioButton")) return w.selected;
      println(" getToggleButtonState: cannot find widget with label ",uilabel);
      return false;
    }
   
    // get selected radio button in a group - returns the label name
    public String getRadioGroupSelected(String groupName){
       for(Widget w: widgetList){
        if( w.UIComponentType.equals("RadioButton")){
          if( ((RadioButton)w).radioGroupName.equals(groupName) && w.selected) return w.UILabel;
        }
    }
    return "";
    }
    
    
    
    public float getSliderValue(String uilabel){
      Widget w = getWidget(uilabel);
      if( w.UIComponentType.equals("Slider") ) return ((Slider)w).getSliderValue();
      return 0;
    }
    
    public void setSliderValue(String uilabel, float v){
      Widget w = getWidget(uilabel);
      if( w.UIComponentType.equals("Slider") )  ((Slider)w).setSliderValue(v);
      
    }
    
    
    
    
    
    
    public String getText(String uilabel){
      Widget w = getWidget(uilabel);
     
      if(w.UIComponentType.equals("TextInputBox")){
         return ((TextInputBox)w).getText();
      }
      
      if(w.UIComponentType.equals("SimpleLabel")){
         return ((SimpleLabel)w).getText();
      }
      return "";
    }
  
    public void setText(String uilabel, String content){
      Widget w = getWidget(uilabel);
      if(w.UIComponentType.equals("TextInputBox")){
            ((TextInputBox)w).setText(content); }
      if(w.UIComponentType.equals("SimpleLabel")){
            ((SimpleLabel)w).setText(content); }
      
    }
    
    
    
    // setting a background Color region for the UI. This is drawn first.
    // to do: this should also set an offset for subsequent placement of the buttons
    
    public void setBackgroundRect(int left, int top, int right, int bottom, int r, int g, int b){
      backgroundRect = new UIRect(left,top,right,bottom);
      backgroundRectColor = color(r,g,b);
    }
    
    public void setRadioButtonOff(String groupName){
      for(Widget w: widgetList){
        if( w.UIComponentType.equals("RadioButton")){
           if( ((RadioButton)w).radioGroupName.equals(groupName))  w.selected = false;
        }
      }
    }
    
    public void setMenusOff(){
      for(Widget w: widgetList){
        if( w.UIComponentType.equals("Menu")){
          ((Menu)w).visible = false;
        }
      }
      
    }
    
    
    // this is an alternative to using the seperate event handlers provided by Processing
    // It therefor easier to use, but more sluggish in response
    public void checkForUserInputEvents(){
      // this gets called in the drawMe() method, instead of having to link up
      // to the native mousePressed() etc. methods
      
       if( pmousePressed == false  && mousePressed){
          handleMouseEvent("mousePressed", mouseX, mouseY);
        }
 
      if( pmousePressed == true  && mousePressed == false){
         handleMouseEvent("mouseReleased", mouseX, mouseY);
        }
 
       if( (pmouseX != mouseX || pmouseY != mouseY) && mousePressed ==false){
         handleMouseEvent("mouseMoved", mouseX, mouseY);
       }
       if( (pmouseX != mouseX || pmouseY != mouseY) && mousePressed){
         handleMouseEvent("mouseDragged", mouseX, mouseY);
       }
       
       
       if( pkeyPressed == false && keyPressed == true){
         handleKeyEvent(key, keyCode, "pressed");
       }
       
       if( pkeyPressed == true && keyPressed == false){
        handleKeyEvent(key, keyCode, "released");
       }
       
      pmousePressed = mousePressed;
      pkeyPressed = keyPressed;
    }
      
      

    
    
    public void handleMouseEvent(String mouseEventType, int x, int y){
      checkForCanvasEvent(mouseEventType,x,y);
      Widget widgetActing = null;
      
      for(Widget w: widgetList){
        boolean eventAbsorbed = w.handleMouseEvent(mouseEventType,x,y);
        
        if(eventAbsorbed) { widgetActing = w; }
        if(eventAbsorbed) break;
      }
      if(widgetActing == null) return;
      widgetActing.doEventAction(mouseEventType,x,y);
    }
    
    
    
    public void handleKeyEvent(char k, int kcode, String keyEventType){
      for(Widget w: widgetList){
         w.handleKeyEvent( k,  kcode,  keyEventType);
      }
    }
    
    
    public void update(){
      checkForUserInputEvents();
      
      if( backgroundRect != null ){
        pushStyle();
        fill(backgroundRectColor);
        rect(backgroundRect.left,backgroundRect.top,backgroundRect.getWidth(), backgroundRect.getHeight());
        popStyle();
      }
      
      drawCanvas();
      for(Widget w: widgetList){
         w.drawMe();
      }
      
    }
    
    public void clearAll(){
      widgetList = new ArrayList<Widget>();
    }
    
   

  }// end of SimpleUIManager
  
  
  
//////////////////////////////////////////////////////////////////
// UIEventData
// when a UI component calls the simpleUICallback() function, it passes this object back
// which contains EVERY CONCEIVABLE bit of extra information about the event that you could imagine
//
public class UIEventData{
  // set by the constructor
  public String callingUIManager; // this is the name of the UIManager, because you might have more than one
  public String uiComponentType; // this is the type of widet e.g. ButtonBaseClass, ToggleButton, Slider - it is identical to the class name
  public String uiLabel; // this is the unique shown label for each widget, and is used to idetify the calling widget
  public String mouseEventType;
  public int mousex; // this is the x location of the recieved mouse event, in window space
  public int mousey;
  
  // extra stuff, which is specific to particular widgets
  public boolean toggleSelectState = false;
  public String radioGroupName = "";
  public String menuItem = "";
  public float sliderValue = 0.0f;
  public String fileDialogPrompt = "";
  public String fileSelection = "";
  
  // key press and text content information for text widgets
  public char keyPress;
  public String textContent;
  
   public UIEventData(){
   }
   
   
   
   public UIEventData(String uiname, String thingType, String label, String mouseEvent, int x, int y){
     initialise(uiname, thingType, label, mouseEvent, x,y);
     
   }
   
   public void initialise(String uiname, String thingType, String label, String mouseEvent, int x, int y){
     
     callingUIManager = uiname;
     uiComponentType = thingType;
     uiLabel = label;
     mouseEventType = mouseEvent;
     mousex = x;
     mousey = y;
     
   }
   
   public boolean eventIsFromWidget(String lab){
     if( uiLabel.equals( lab )) return true;
     if( menuItem.equals(lab) ) return true;
     return false;
     
   }
   
   public void print(int verbosity){
     if(verbosity != 3 && this.mouseEventType.equals("mouseMoved")) return;
     
     
     if(verbosity == 0) return;
     
     if(verbosity >= 1){
       println("UIEventData:" + this.uiComponentType + " " + this.uiLabel);
       
       if( this.uiComponentType.equals("canvas")){
         println("mouse event:" + this.mouseEventType + " at (" + this.mousex +"," + this.mousey + ")");
       }
       
     }
     
     if(verbosity >= 2){
         println("toggleSelectState " + this.toggleSelectState);
         println("radioGroupName " + this.radioGroupName);
         println("sliderValue " + this.sliderValue);
         println("menuItem " + this.menuItem);
         println("keyPress " + keyPress);
         println("textContent " + textContent);
         println("fileDialogPrompt " + this.fileDialogPrompt);
         println("fileSelection " + this.fileSelection);
     }
     
     if(verbosity == 3 ){
         if(this.mouseEventType.equals("mouseMoved")) {
         println("mouseMove at (" + this.mousex +"," + this.mousey + ")");
         }
     }
     
     println(" ");
   }
  
}





//////////////////////////////////////////////////////////////////
// Everything below here is stuff wrapped up by the UImanager class
// so you don't need to to look at it, or use it directly. But you can if you
// want to!
// 





//////////////////////////////////////////////////////////////////
// Base class to all components
class Widget{
  
  // Color for overall application
  int SimpleUIAppBackgroundColor = color(240,240,240);// the light neutralgrey of the overall application surrounds
  
  // Color for UI components
  int SimpleUIBackgroundRectColor = color(230,230,240); // slightly purpley background rect Color for alternative UI's
  int SimpleUIWidgetFillColor = color(200,200,200);// darker grey for butttons
  int SimpleUIWidgetRolloverColor = color(215,215,215);// slightly lighter rollover Color
  int SimpleUITextColor = color(0,0,0);


  // should any widgets need to "talk" to other widgets (RadioButtons, Menus)
  SimpleUI parentManager = null; 
  
  // Because you can have more than one UIManager in a system, 
  // e.g. a seperate one for popups, or tool modes
  String UIManagerName;
  
  // this should be the best way to identify a widget, so make sure
  // that all UILabels are unique
  String UILabel;
  boolean displayLabel  = true;
  
  // type of component e.g. "UIButton", should be absolutely same as class name
  public String UIComponentType = "WidgetBaseClass";
  
  // location and size of widget
  int widgetWidth, widgetHeight;
  int locX, locY;
  public UIRect bounds;
  
  // needed by most, but not all widgets
  boolean rollover = false;
  
  // needed by some widgets but not all
  boolean selected = false;
  
  public Widget(String uiname){
    
    UIManagerName = uiname;
  }
  
  public Widget(String uiname, String uilabel, int x, int y, int w, int h){
    
    UIManagerName = uiname;
    UILabel = uilabel;
    setBounds(x, y, w, h);
  }
  
  // virtual functions
  // 
  public void setBounds(int x, int y, int w, int h){
    locX = x;
    locY = y;
    widgetWidth = w;
    widgetHeight = h;
    bounds = new UIRect(x,y,x+w,y+h);
  }
  
  public boolean isInMe(int x, int y){
    if(   bounds.isPointInside(x,y)) return true;
   return false;
  }
  
  public void setParentManager(SimpleUI manager){
    parentManager = manager;
  }
  
  public void setWidgetDims(int w, int h){
    setBounds(locX,locY,w, h);
  }
  
  
  
  // "virtual" functions here
  //
  public void drawMe(){}
  
  public boolean handleMouseEvent(String mouseEventType, int x, int y){ return false;}
  
  public void doEventAction(String mouseEventType, int x, int y){
    UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x,y);
    handleUIEvent(uied);
  }
  
  
  public void handleKeyEvent(char k, int kcode, String keyEventType){}
  
  public void setSelected(boolean s){
    selected = s;
  }

}


//////////////////////////////////////////////////////////////////
// Simple Label widget - uneditable text
// It displays label:text, where text is changeable in the widget's lifetime, but label is not

class SimpleLabel extends Widget{
  
  int textPad = 5;
  String text;
  int textSize = 12;
  
  
  public SimpleLabel(String uiname, String uilable, int x, int y,  String txt){
    super(uiname, uilable,x,y,100,30);
    UIComponentType = "SimpleLabel";
    this.text = txt;
    
  }
  
  public void drawMe(){
    pushStyle();
    stroke(100,100,100);
    strokeWeight(1);
    fill(SimpleUIBackgroundRectColor);
    rect(locX, locY, widgetWidth, widgetHeight);
   
    String seperator = ":";
    if(this.text.equals("")) seperator = " ";
    String displayString;
    
    if(displayLabel) { 
      
      displayString = this.UILabel + seperator + this.text;
    
    } else {
      
      displayString = this.text;
    }
    
    
        
    if( displayString.length() < 20) {
      textSize = 9;} 
      else { textSize = 9; }
    fill(SimpleUITextColor);  
    textSize(textSize);
    strokeWeight(1);
    text(displayString, locX+textPad, locY+textPad, widgetWidth, widgetHeight);
    popStyle();
  }
  
  public void setText(String txt){
    this.text = txt;
  }
  
  public String getText(){
    return this.text;
  }
  
  
}


//////////////////////////////////////////////////////////////////
// Base button class, functions as a simple button, and is the base class for
// toggle and radio buttons
class ButtonBaseClass extends Widget{

  int textPad = 5;
  int textSize = 12;

  PImage icon = null;

  public ButtonBaseClass(String uiname, int x, int y, String uilable){
    super(uiname, uilable,x,y,70,30);

    UIComponentType = "ButtonBaseClass";
  }
  
  public void setIcon(PImage iconImg){
    icon = iconImg.copy();
    icon.resize(widgetWidth-2, widgetHeight-2);
  }
  
  
  public void setButtonDims(int w, int h){
    setBounds(locX,locY,w, h);
  }
  
  public boolean handleMouseEvent(String mouseEventType, int x, int y){
    if( isInMe(x,y) /*&& (mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))*/){
      rollover = true;
      
    } else { rollover = false;}
    
    if( isInMe(x,y) && mouseEventType.equals("mouseReleased")){
      return true;
    }
    return false;
  }
  
  
  
  public void drawMe(){
    pushStyle();
    stroke(0,0,0);
    strokeWeight(1);
    if(rollover){
      fill(SimpleUIWidgetRolloverColor);}
    else{
      fill(SimpleUIWidgetFillColor);
    }
    
    rect(locX, locY, widgetWidth, widgetHeight);
    fill(SimpleUITextColor);
    if( this.UILabel.length() < 10) {
      textSize = 12;} 
      else { textSize = 9; }
      
    textSize(textSize);
    strokeWeight(1);
    text(this.UILabel, locX+textPad, locY+textPad, widgetWidth, widgetHeight);
    popStyle();
    
    drawIconImage();
  }
  
  public void drawIconImage(){
    if(icon == null) return;
    
    pushStyle();
    tint(127, 127, 127, 255);
    if(rollover) tint(200, 200, 200, 255);
    if(rollover && mousePressed) tint(255, 255, 255, 255);
    if(selected) tint(255, 255, 255, 255);
    image(icon, locX+1, locY+1, widgetWidth-2, widgetHeight-2);
    
    popStyle();
  }

}

//////////////////////////////////////////////////////////////////
// ToggleButton

class ToggleButton extends ButtonBaseClass{
  
  
  
  public ToggleButton(String uiname, int x, int y, String labelString){
    super(uiname,x,y,labelString);
    
    UIComponentType = "ToggleButton";
  }
  
  public boolean handleMouseEvent(String mouseEventType, int x, int y){
    if( isInMe(x,y) && (mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))){
      rollover = true;
    } else { rollover = false; }
    
    if( isInMe(x,y) && mouseEventType.equals("mouseReleased")){
      return true;
    }
    return false;
  }
  
  public void doEventAction(String mouseEventType, int x, int y){
      swapSelectedState();
      UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x,y);
      uied.toggleSelectState = selected;
      handleUIEvent(uied);
  }
  
  public void swapSelectedState(){
    selected = !selected;
  }
  
  public void drawMe(){
    pushStyle();
    stroke(0,0,0);
    if(rollover){
      fill(SimpleUIWidgetRolloverColor);}
    else{
      fill(SimpleUIWidgetFillColor);   
    }
    
    if(selected){
     strokeWeight(2);
     rect(locX+1, locY+1, widgetWidth-2, widgetHeight-2);
     } else {
     strokeWeight(1);
     rect(locX, locY, widgetWidth, widgetHeight);  
     }
   
      
      
    
    
    stroke(0,0,0);
    strokeWeight(1);
    fill(SimpleUITextColor);
    textSize(textSize);
    strokeWeight(1);
    text(this.UILabel, locX+textPad, locY+textPad, widgetWidth, widgetHeight);
    popStyle();
  }
  
  
  
}

//////////////////////////////////////////////////////////////////
// RadioButton

class RadioButton extends ToggleButton{
  
  
  // these have to be part of the base class as is accessed by manager
  public String radioGroupName = "";
  
  public RadioButton(String uiname,int x, int y, String labelString, String groupName,SimpleUI manager){
    super(uiname,x,y,labelString);
    radioGroupName = groupName;
    UIComponentType = "RadioButton";
    parentManager = manager;
  }
  
  
  public boolean handleMouseEvent(String mouseEventType, int x, int y){
    if( isInMe(x,y) && (mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))){
      rollover = true;
    } else { rollover = false; }
    
    if( isInMe(x,y) && mouseEventType.equals("mouseReleased")){
       return true;
    }
    
    return false;
    
  }
  
  
   public void doEventAction(String mouseEventType, int x, int y){ 
      parentManager.setRadioButtonOff(this.radioGroupName);
      selected = true;
      UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x,y);
      uied.toggleSelectState = selected;
      uied.radioGroupName  = this.radioGroupName;
      handleUIEvent(uied);
  }
  
  public void turnOff(String groupName){
    if(groupName.equals(radioGroupName)){
      selected = false;
    }
    
  }
  
}



/////////////////////////////////////////////////////////////////////////////
// menu stuff
//
//

/////////////////////////////////////////////////////////////////////////////
// the menu class
//
class Menu extends Widget{
  
  
  int textPad = 5;
  //String title;
  int textSize = 12;

  int numItems = 0;
  SimpleUI parentManager;
  public boolean visible = false;
  
  
  ArrayList<String> itemList = new ArrayList<String>();
  
  
  
  public Menu(String uiname, String uilabel, int x, int y, String[] menuItems, SimpleUI manager)
    {
    super(uiname,uilabel,x,y,100,20);
    parentManager = manager;
    UIComponentType = "Menu";
    
    for(String s: menuItems){
      itemList.add(s);
      numItems++;
    }
    }
    
  

  public void drawMe(){
    //println("drawing menu " + title);
    drawTitle();
    if( visible ){
     drawItems();
    } 
    
  }
  
  public void drawTitle(){
    pushStyle();
    strokeWeight(1);
    stroke(0,0,0);
    if(rollover){
      fill(SimpleUIWidgetRolloverColor);}
    else{
      fill(SimpleUIWidgetFillColor);
    }
     
    rect(locX, locY, widgetWidth,widgetHeight);
    fill(SimpleUITextColor);
    textSize(textSize);
    text(this.UILabel, locX+textPad, locY+3, widgetWidth,widgetHeight);
    popStyle();
  }
  
  
  public void drawItems(){
    pushStyle();
    strokeWeight(1);
    if(rollover){
      fill(SimpleUIWidgetRolloverColor);}
    else{
      fill(SimpleUIWidgetFillColor);
    }
    
    
    
    int thisY = locY + widgetHeight;
    rect(locX, thisY, widgetWidth, (widgetHeight*numItems));
    
    if(isInItems(mouseX,mouseY)){
      hiliteItem(mouseY);
    }
    
    fill(SimpleUITextColor);
    
    textSize(textSize);
    
    for(String s : itemList){
      
      if(s.length() > 14)
        {textSize(textSize-1);}
      else {textSize(textSize);}
      
      
      text(s, locX+textPad, thisY, widgetWidth, widgetHeight);
      thisY += widgetHeight;
    }
   popStyle();
  }
  
  
 public void hiliteItem(int y){
   pushStyle();
   int topOfItems =this.locY + widgetHeight;
   float distDown = y - topOfItems;
   int itemNum = (int) distDown/widgetHeight;
   fill(230,210,210);
   rect(locX, topOfItems + itemNum*widgetHeight, widgetWidth, widgetHeight);
   popStyle();
 }
  
 public boolean handleMouseEvent(String mouseEventType, int x, int y){
    rollover = false;
    
    //println("here1 " + mouseEventType);
    if(isInMe(x,y)==false) {
      visible = false;
      return false;
    }
    if( isInMe(x,y)){
      rollover = false;
    }
    
    //println("here2 " + mouseEventType);
    if(mouseEventType.equals("mousePressed") && visible == false){
      //println("mouseclick in title of " + title);
      parentManager.setMenusOff();
      visible = true;
      rollover = true;
      return false;
    }
    if(mouseEventType.equals("mousePressed") && isInItems(x,y)){
      parentManager.setMenusOff();
      return true;
    }
    return false;
  }
  
   public void doEventAction(String mouseEventType, int x, int y){ 
      println("menu event ", UIComponentType, UILabel, mouseEventType, x,y);
      String pickedItem = getItem(y);
      
      UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x,y);
      uied.menuItem = pickedItem;
      
      handleUIEvent(uied);
      
      
  }
  
 public String getItem(int y){
   int topOfItems =this.locY + widgetHeight;
   float distDown = y - topOfItems;
   int itemNum = (int) distDown/widgetHeight;
   //println("picked item number " + itemNum);
   return itemList.get(itemNum); //<>//
 }
  
 public boolean isInMe(int x, int y){
   if(isInTitle(x,y)){
     //println("mouse in title of " + title);
     return true;
   }
   if(isInItems(x,y)){
     return true;
   }
   return false;
 }
 
 public boolean isInTitle(int x, int y){
   if(x >= this.locX   && x < this.locX+this.widgetWidth &&
      y >= this.locY && y < this.locY+this.widgetHeight) return true;
   return false;
   
 }
 
 
 public boolean isInItems(int x, int y){
   if(visible == false) return false;
   if(x >= this.locX   && x < this.locX+this.widgetWidth &&
      y >= this.locY+this.widgetHeight && y < this.locY+(this.widgetHeight*(this.numItems+1))) return true;
      
   
   return false;
 }
  
  
  
  
}// end of menu class

/////////////////////////////////////////////////////////////////////////////
// Slider class stuff

/////////////////////////////////////////////////////////////////////////////
// Slider Class
//
// calls back with value on  both release and drag

class Slider extends Widget{

  boolean showValue = true;
  public float currentValue  = 0.0f;
  boolean mouseEntered = false;
  int textPad = 5;
  int textSize = 12;
  boolean rollover = false;
  
  public String HANDLETYPE = "ROUND";
  
  public Slider(String uiname, String label, int x, int y){
    super(uiname,label,x,y,102,30); 
    UIComponentType = "Slider";
  }
  
  public void showValue(boolean sv){
    showValue = sv;
  }
  
  public boolean handleMouseEvent(String mouseEventType, int x, int y){
    PVector p = new PVector(x,y);
    
    if( mouseLeave(p) ){
      return false;
      //println("mouse left sider");
    }
    
    if( bounds.isPointInside(p) == false){
      mouseEntered = false;
      return false; }
    
    
    
    if( (mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))){
      rollover = true;
    } else { rollover = false; }
    
    
    if(  mouseEventType.equals("mousePressed") /*|| mouseEventType.equals("mouseReleased") || mouseEventType.equals("mouseDragged")*/ ){
      mouseEntered = true;
      return true;
    }
    
    if( mouseEventType.equals("mouseDragged") && mouseEntered) return true;
    
    return false;
    
  }
  
  public void doEventAction(String mouseEventType, int x, int y){ 
      float val = getSliderValueAtMousePos(x);
      //println("slider val",val);
      setSliderValue(val);
      UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x,y);
      uied.sliderValue = val;
      handleUIEvent(uied);
  }
  
  public float getSliderValueAtMousePos(int pos){
    float val = map(pos, bounds.left, bounds.right, 0,1);
    return val;
  }
  
  public float getSliderValue(){
    return currentValue;
  }
  
  public void setSliderValue(float val){
   currentValue =  constrain(val,0,1);
  }
  
  public boolean mouseLeave(PVector p){
     // is only true, if the mouse has been in the widget, has been depressed
    if( mouseEntered && bounds.isPointInside(p)== false) {
      mouseEntered = false;
      return true; }
      
    return false;
  }
  
  public void drawMe(){
    pushStyle();
    stroke(0,0,0);
    strokeWeight(1);
    if(rollover){
      fill(SimpleUIWidgetRolloverColor);}
    else{
      fill(SimpleUIWidgetFillColor);
    }
    rect(bounds.left, bounds.top,  bounds.getWidth(), bounds.getHeight());
    fill(SimpleUITextColor);
    textSize(textSize);
    text(this.UILabel, bounds.left+textPad, bounds.top+26);
    int sliderHandleLocX = (int) map(currentValue,0,1,bounds.left, bounds.right);
    sliderHandleLocX = (int)constrain(sliderHandleLocX, bounds.left+10, bounds.right-10 );
    stroke(127);
    float lineHeight = bounds.top+ (bounds.getHeight()/2.0f) - 5;
    line(bounds.left+5, lineHeight,  bounds.left+bounds.getWidth()-5, lineHeight);
    stroke(0);
    drawSliderHandle(sliderHandleLocX);
    popStyle();
    
    if(showValue) drawValue();
  }
  
  public void drawValue(){
    pushStyle();
    textSize(10);
    fill(255,0,0);
    
    String currentValueString = nf(currentValue,1,4);
    
    text(currentValueString, bounds.right-60, bounds.top + 10);
    popStyle();
  }
  
  public void drawSliderHandle(int loc){
    pushStyle();
    stroke(0,0,0);
    fill(255,255,255,100);
    if(HANDLETYPE.equals("ROUND")) {
      //if(this.label =="tone"){
      //  println("drawing slider" + this.label, loc, bounds.top + 10);
      //  
      //}
      
     ellipse(loc, bounds.top + 10, 10,10);
    }
    if(HANDLETYPE.equals("UPARROW")) {
      triangle(loc-4, bounds.top + 15, loc,bounds.top - 2, loc+4, bounds.top + 15);
    }
    if(HANDLETYPE.equals("DOWNARROW")){
      triangle(loc-4, bounds.top + 5, loc,bounds.bottom + 2, loc+4, bounds.top + 5);
    }
    popStyle();
  }
  
}

////////////////////////////////////////////////////////////////////////////////
// self contained simple txt ox input
// simpleUICallback is called after every character insertion/deletion, enabling immediate udate of the system
//
class TextInputBox extends Widget{
  String contents = "";
  int maxNumChars = 14;
  
  boolean rollover;
  
  int textBoxBackground = color(235,235,255);
  
  public TextInputBox(String uiname, String uilabel, int x, int y,  int maxNumChars){
    super(uiname,uilabel,x,y,100,30);
    UIComponentType = "TextInputBox";
    this.maxNumChars = maxNumChars;
    
    rollover = false;
    
  }

  
  public boolean handleMouseEvent(String mouseEventType, int x, int y){
    // can only type into an input box if the mouse is hovering over
    // this way we avoid sending text input to multiple widgets
    PVector mousePos = new PVector (x,y);
    rollover = bounds.isPointInside(mousePos);
   // if(rollover) println("text box rollover", UILabel, " ", rollover);
    return rollover;  
  }
  
  public void handleKeyEvent(char k, int kcode, String keyEventType){
    rollover = bounds.isPointInside(mouseX, mouseY);
    if(keyEventType.equals("released")) return;
    if(rollover == false) return;

    UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, "textInputEvent", 0,0);
    uied.keyPress = k;
   


    
        
    if( isValidCharacter(k) ){
        addCharacter(k);   
    }
    
    if(k == BACKSPACE){
        deleteCharacter();
    }
    
     handleUIEvent(uied);
  }
  
  public void addCharacter(char k){
    if( contents.length() < this.maxNumChars){
      contents=contents+k;
      
    }
    
  }
  
  public void deleteCharacter(){
    int l = contents.length();
    if(l == 0) return; // string already empty
    if(l == 1) {contents = ""; }// delete the final character
    String cpy  = contents.substring(0, l-1);
    contents = cpy;
    
  }
  
  public boolean isValidCharacter(char k){
    if(k == BACKSPACE) return false;
    return true;
    
  }

  public String getText(){
    return contents;
  }
  
  public void setText(String s){
    contents = s;
  }

  public void drawMe(){
    pushStyle();
      stroke(0,0,0);
      fill(textBoxBackground);
      strokeWeight(1);
      
      if(rollover){stroke(255,0,0);fill(SimpleUIWidgetRolloverColor);}
      

      rect(locX, locY, widgetWidth, widgetHeight);
      stroke(0,0,0);
      fill(SimpleUITextColor);
      
      int textPadX = 5;
      int textPadY = 20;
      fill(SimpleUITextColor);  
      textSize(12);
      strokeWeight(1);
      text(contents, locX + textPadX, locY + textPadY);
      //text(UILabel, locX + textPadX + 50, locY + textPadY);
      
      if(displayLabel) text(UILabel, locX + widgetWidth + textPadX, locY + textPadY);
    popStyle();
  }
}  



/////////////////////////////////////////////////////////////////
// simple rectangle class especially for this UI stuff
//

class UIRect{
  
  float left,top,right,bottom;
  public UIRect(){
    
  }

  public UIRect(PVector upperleft, PVector lowerright){
    setRect(upperleft.x,upperleft.y,lowerright.x,lowerright.y);
  }
  
  public UIRect(float x1, float y1, float x2, float y2){
    setRect(x1,y1,x2,y2);
  }
  
  public void setRect(UIRect other){
    setRect(other.left, other.top, other.right, other.bottom);
  }
  
  public UIRect copy(){
    return new UIRect(left, top, right, bottom);
  }
  
  public void setRect(float x1, float y1, float x2, float y2){
    this.left = min(x1,x2);
    this.top = min(y1,y2);
    this.right = max(x1,x2);
    this.bottom = max(y1,y2);
  }
  
  
  public boolean equals(UIRect other){
    if(left == other.left && top == other.top && 
       right == other.right && bottom == other.bottom) return true;
    return false;
  }
  
  public PVector getCentre(){
    float cx = this.left + (this.right - this.left)/2.0f;
    float cy = this.top + (this.bottom - this.top)/2.0f;
    return new PVector(cx,cy);
  }
  
  public boolean isPointInside(PVector p){
    // inclusive of the boundries
    if(   this.isBetweenInc(p.x, this.left, this.right) && this.isBetweenInc(p.y, this.top, this.bottom) ) return true;
    return false;
  }
  
  public boolean isPointInside(float x, float y){
    PVector v = new PVector(x,y);
    return isPointInside(v);
  }
  
  public float getWidth(){
    return (this.right - this.left);
  }
  
  public float getHeight(){
    return (this.bottom - this.top);
  }
  
  public PVector getTopLeft(){
    return new PVector(left,top);
  }
  
  public PVector getBottomRight(){
    return new PVector(right,bottom);
  }
  
  public boolean isBetweenInc(float v, float lo, float hi){
  if(v >= lo && v <= hi) return true;
  return false;
 }

}// end UIRect class
////////////////////////////////////////////
// Useful timer class. Returns time in 
// floating point seconds

class Timer{
  
  int startMillis = 0;
  float lastNow;
  public Timer(){
    start();
  }
  
  // call this to reset the timer
  public void start(){
    startMillis = millis();
    
    lastNow = 0;
  }
  
  // returns the elapsed time since you last called this function
  // or since start() if it's the first time called
  public float getElapsedTime(){
    float now =  getTimeSinceStart();
    float elapsedTime = now - lastNow;
    lastNow = now;
    return elapsedTime;
    
  }
  
  // call this to get the time since you called start() or 
  // instantiated the object
  public float getTimeSinceStart(){
    return ((millis()-startMillis)/1000.0f);
  }
  
  
}
  public void settings() {  size(1280, 720, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "GraphicsForGamesProject" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
