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