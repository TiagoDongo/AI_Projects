package controllers;

import engine.Car;
import engine.Game;
import engine.GameObject;

public class SeekController extends Controller{
    private GameObject target;

    public SeekController(GameObject target){
        this.target = target;
    }

    @Override
    public void update(Car subject, Game game, double delta_t, double controlVariables[]){
        double dx = target.getX() - subject.getX();
        double dy = target.getY() - subject.getY();
        double angle = Math.atan2(dy, dx);

        double diff = angle - subject.getAngle();
        while (diff > Math.PI){
            diff -= 2 * Math.PI;
        }
        while (diff < -Math.PI){
            diff += 2 * Math.PI;
        }

        controlVariables[VARIABLE_STEERING] = Math.max(-1, Math.min(1, diff*2));
        controlVariables[VARIABLE_THROTTLE]= 1;
        controlVariables[VARIABLE_BRAKE]=0;
    }
}
