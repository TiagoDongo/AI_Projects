package controllers;

import engine.*;

public class WallAvoidanceController extends Controller{
    //quantos passos se simulam a frente e quantos pixels tem cada um (8 * 18 = 144 px)
    private static final int numSteps = 18;
    private static final double stepDistance = 8;

    //meio-tamanho do retangulo que representa o carro (um pouco maior que o real, para ter margem)
    private static final double carHalfLength = 18;
    private static final double carHalfWidth = 12;

    //o Car roda steer * velocidade / 50 radianos por segundo
    private static final double turnFactor = 50;

    private final GameObject target;

    public WallAvoidanceController(GameObject target) {
        this.target = target;
    }

    @Override
    public void update(Car subject, Game game, double delta_t, double[] controlVariables) {
        //1. Seek: o volante que levava o carro direto ao alvo
        double dx = target.getX() - subject.getX();
        double dy = target.getY() - subject.getY();
        double diff = Math.IEEEremainder(Math.atan2(dy, dx) - subject.getAngle(), 2 * Math.PI);
        double seekSteer = Math.max(-1, Math.min(1, diff * 2));

        //2. volantes que vamos experimentar: primeiro o do Seek, depois os outros
        double[] candidates = { seekSteer, 0, -0.5, 0.5, -1, 1 };

        double steer = seekSteer;
        boolean pathFree = false;
        double bestSteer = seekSteer;
        int bestFreeSteps = -1;

        for (double candidate : candidates) {
            //3. simular o carro a andar com este volante e ver se bate numa parede
            double x = subject.getX();
            double y = subject.getY();
            double angle = subject.getAngle();
            int freeSteps = 0;

            for (int step = 1; step <= numSteps; step++) {
                angle += candidate * stepDistance / turnFactor;
                x += Math.cos(angle) * stepDistance;
                y += Math.sin(angle) * stepDistance;

                RotatedRectangle carBox = new RotatedRectangle(x, y, carHalfLength, carHalfWidth, angle);
                if (game.collision(carBox) instanceof Obstacle) {
                    break;
                }
                freeSteps = step;
            }

            //4. caminho livre: usa este volante (o do Seek, se estiver livre)
            if (freeSteps == numSteps) {
                steer = candidate;
                pathFree = true;
                break;
            }
            if (freeSteps > bestFreeSteps) {
                bestFreeSteps = freeSteps;
                bestSteer = candidate;
            }
        }

        //5. sem caminho livre: o volante que anda mais longe e travar
        if (!pathFree) {
            steer = bestSteer;
        }

        controlVariables[VARIABLE_STEERING] = steer;
        controlVariables[VARIABLE_THROTTLE] = pathFree ? 1 : 0;
        controlVariables[VARIABLE_BRAKE]    = pathFree ? 0 : 1;
    }

}
