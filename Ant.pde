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
    
    void update() {
        if(searching){
            pathToAntTrackBall();
            movingAnt();
        }
        mover.update();   
    }
    
    void display() {
        fill(0,0,0);
        noStroke();
        model.drawMe();
    }
    
    void setAcceleration(PVector acceleration) {
        mover.acceleration = acceleration;
    }
    
    PVector getRandomLocaion() {
        
        x = random(mover.location.x - searchRadius,mover.location.x + searchRadius);
        y = model.getOrigin().y;
        z = random(mover.location.z - searchRadius,mover.location.z + searchRadius);
        
        return new PVector(x,y,z);
    }

    PVector getRandomLocaionWithinLimits(float xUpperLimit,float xLowerLimit, float zUpperLimit, float zLowerLimit) {
        
        x = random(xLowerLimit,xUpperLimit);
        y = model.getOrigin().y;
        z = random(zLowerLimit,zUpperLimit);

        return new PVector(x,y,z);
    }
    
    void setSearchLocation(PVector specificMovingToLocation) {
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
    
    PVector getMoveToLocation() {
        return movingToLocation;        
    }
    
    void movingAnt() {
        PVector twoDHeading = new PVector(mover.acceleration.x,mover.acceleration.z);
        model.setTransformAbs(antSize, 0,twoDHeading.heading() + radians(90) ,PI, vec(mover.location.x,model.getOrigin().y,mover.location.z));
    }
    
    void pathToAntTrackBall() {
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
        desiredLocation.setMag(0.2);
        
        setAcceleration(new PVector(desiredLocation.x, 0,desiredLocation.z));
    }

    void lookForWayHomePheromones(PVector currentLocation){
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

    void lookForFoodFoundPheromones(PVector currentLocation){
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

    void searchForFoodWithinLocation(PVector currentLocation){
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
    Pheromone LeavePheromoneObject(PVector moverLocation, PVector goingToLocation, float scalar){
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

    float getPheromoneScalar(float currentScalar){
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
