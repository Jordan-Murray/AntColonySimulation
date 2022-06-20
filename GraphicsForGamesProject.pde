

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

void setup() {
    size(1280, 720, P3D);
    
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
    
    terrain = new SimSurfaceMesh(20, 20, 20.0);
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

void draw() {
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

void mouseDragged() {
    updateTrackball();
}

void mousePressed() {
    updateTrackball();
}

void updateTrackball() { 
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

void printRayIntersectionDetails(String what, SimRay sr) {
    
    PVector intersectionPt = sr.getIntersectionPoint();
    PVector intersectionNormal = sr.getIntersectionNormal();
    int hits = sr.getNumIntersections();
    
    println("That ray hit ", what ,"with " ,  hits , " intersections ");
    println("Intersection at ", intersectionPt);
    println("Surface Normal at ", intersectionNormal);
    println("ID of object hit ", sr.getColliderID());
}

// you MUST have this function declared.. it receives all the user-interface events
void handleUIEvent(UIEventData uied) {
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