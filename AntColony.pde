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
    float minWayHomeIntensity = 717.16; //1000 - 282.84 //In the corner of the map

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
    
    void update() {
        
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
    
    void display() {

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
                color colour = color(255,255,0, getAlphaFromIntensity(pheromone.intensity,true));
                fill(colour);
                pheromone.drawMe();
            }
            // pheromone.update();
        }
        
        for (Pheromone pheromone : foundFoodPheromones) {
            if (drawFoodFoundPheromones) {
                color colour = color(0,255,255, getAlphaFromIntensity(pheromone.intensity,false));
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
    
    void spawnAnts() {
        for (int i = 0; i < numberOfAnts; ++i) {
            Ant ant = new Ant(this,simObjectManagerRef);
            simObjectManager.addSimObject(ant.model,"ant_" + i);
            ants.add(ant);
            noneFoodObjs++;
        }
    }
    
    void updateFoodList() {
        for (Ant ant : ants) {
            ant.updateFoodList();
        }
    }

    float getAlphaFromIntensity(float _intensity,boolean wayHomePheromone){
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
    
    PVector removeCollidingObjectWithTag(SimTransform _object, String _tag) {
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
    
    void makeSomeFood(SimRay simRay, int foodIndex) {
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
    
    void drawTheFood() {
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