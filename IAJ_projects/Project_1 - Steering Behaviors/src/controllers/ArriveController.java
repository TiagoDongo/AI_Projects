package controllers;

import engine.Car;
import engine.Game;
import engine.GameObject;

public class ArriveController extends Controller{
    private GameObject target;

    private static final double MAX_SPEED =  250;
    private static final double MAX_ACCELERATION =  100;
    private static final double MAX_BRAKING =  250;
    private static final double TARGET_RADIUS =  5;
    private static final double SLOW_RADIUS =  200;
    private static final double TIME_TO_TARGET =  0.1;

    public ArriveController(GameObject target) {
        this.target = target;
    }

    @Override
    public void update(Car subject, Game game, double delta_t, double controlVariables[]){
        double dx = target.getX() - subject.getX();
        double dy = target.getY() - subject.getY();
        double distance = Math.sqrt(dx*dx + dy*dy);
        double angle = Math.atan2(dy, dx);
        double diff = angle - subject.getAngle();

        if(distance < TARGET_RADIUS){
            controlVariables[VARIABLE_STEERING] = 0;
            controlVariables[VARIABLE_THROTTLE] = 0;
            controlVariables[VARIABLE_BRAKE] = 1;
        }

        while (diff > Math.PI){
            diff -= 2 * Math.PI;
        }
        while (diff < -Math.PI){
            diff += 2 * Math.PI;
        }

        controlVariables[VARIABLE_STEERING] = Math.max(-1, Math.min(1, diff*2));

        double targetSpeed;
        if(distance > SLOW_RADIUS){
            targetSpeed = MAX_SPEED;
        }else {
            targetSpeed = MAX_SPEED * distance / SLOW_RADIUS;
        }

        double acceleration = (targetSpeed - subject.getSpeed())/ TIME_TO_TARGET;

        if(acceleration > 0){
            controlVariables[VARIABLE_THROTTLE] = Math.min(1, acceleration / MAX_ACCELERATION);
            controlVariables[VARIABLE_BRAKE] = 0;
        }else {
            controlVariables[VARIABLE_THROTTLE] = 0;
            controlVariables[VARIABLE_BRAKE] = Math.min(1, -acceleration / MAX_ACCELERATION);
        }
    }
}
