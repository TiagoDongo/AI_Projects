package s3.ai;


import java.util.List;
import s3.base.S3;
import s3.entities.S3PhysicalEntity;
import s3.util.Pair;
import java.util.PriorityQueue;
import java.util.LinkedList;
import s3.entities.WOGrass;


public class AStar {
	private final double startX;
	private final double startY;
	private final double goalX;
	private final double goalY;
	private S3PhysicalEntity entity;
	private S3 game;

	
	
	public static int pathDistance(double start_x, double start_y, double goal_x, double goal_y,
			S3PhysicalEntity i_entity, S3 the_game) {
		AStar a = new AStar(start_x,start_y,goal_x,goal_y,i_entity,the_game);
		List<Pair<Double, Double>> path = a.computePath();
		if (path!=null) return path.size();
		return -1;
	}

	public AStar(double start_x, double start_y, double goal_x, double goal_y,
		S3PhysicalEntity i_entity, S3 the_game) {
		// ...
		this.startX = start_x;
		this.startY = start_y;
		this.goalX = goal_x;
		this.goalY = goal_y;
		this.game = the_game;
		this.entity = i_entity;
	}

	public List<Pair<Double, Double>> computePath() {

		// Posição inicial da unidade
		int startUnitPositionX = (int) startX;
		int startUnitPositionY = (int) startY;

		// Posição do objetivo
		int goalPositionX = (int) goalX;
		int goalPositionY = (int) goalY;

		// Dimensões do mapa
		int mapWidth = game.getMap().getWidth();
		int mapHeight = game.getMap().getHeight();

		// Número de passos desde o início
		int[][] numStepsFromStart = new int[mapWidth][mapHeight];

		// Posições anteriores da unidade
		int[][] previousPositionX = new int[mapWidth][mapHeight];
		int[][] previousPositionY = new int[mapWidth][mapHeight];

		boolean[][] alreadyProcessed = new boolean[mapWidth][mapHeight];

		// Inicialização dos custos
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				numStepsFromStart[x][y] = Integer.MAX_VALUE;
			}
		}

		// Lista de posições à espera de serem analisadas
		PriorityQueue<int[]> waitingPosition =
			new PriorityQueue<>((positionA, positionB) -> Integer.compare(positionA[2], positionB[2]));

		// Posição inicial
		numStepsFromStart[startUnitPositionX][startUnitPositionY] = 0;

		waitingPosition.add(new int[]{
				startUnitPositionX,
				startUnitPositionY,
				heuristic(startUnitPositionX, startUnitPositionY,goalPositionX, goalPositionY)
		});

		// Quatro direções possíveis
		int[] stepX = {1, -1, 0, 0};
		int[] stepY = {0, 0, 1, -1};

		// Ciclo principal
		while (!waitingPosition.isEmpty()) {

			int[] current = waitingPosition.poll();

			int currentX = current[0];
			int currentY = current[1];

			if (alreadyProcessed[currentX][currentY]) {
				continue;
			}

			// Verifica se chegou ao objetivo
			if (currentX == goalPositionX && currentY == goalPositionY) {

				LinkedList<Pair<Double, Double>> path = new LinkedList<>();

				int pathX = goalPositionX;
				int pathY = goalPositionY;

				// Reconstrução do caminho
				while (pathX != startUnitPositionX || pathY != startUnitPositionY) {

					path.addFirst(new Pair<Double, Double>((double) pathX,(double) pathY));

					int previousX = previousPositionX[pathX][pathY];
					int previousY = previousPositionY[pathX][pathY];

					pathX = previousX;
					pathY = previousY;
				}

				return path;
			}

			alreadyProcessed[currentX][currentY] = true;

			// Exploração das células vizinhas
			for (int direction = 0; direction < 4; direction++) {

				int neighborX = currentX + stepX[direction];
				int neighborY = currentY + stepY[direction];

				// Verifica os limites do mapa
				if (neighborX < 0 || neighborX >= mapWidth ||
						neighborY < 0 || neighborY >= mapHeight) {
					continue;
				}

				// Verifica se a célula está livre
				if (!(game.getEntity(neighborX, neighborY) instanceof WOGrass)) {
					continue;
				}

				if (alreadyProcessed[neighborX][neighborY]) {
					continue;
				}

				// Calcula o novo custo
				int newCost = numStepsFromStart[currentX][currentY] + 1;

				if (newCost < numStepsFromStart[neighborX][neighborY]) {
					numStepsFromStart[neighborX][neighborY] = newCost;

					previousPositionX[neighborX][neighborY] = currentX;
					previousPositionY[neighborX][neighborY] = currentY;

					waitingPosition.add(new int[]{
						neighborX,
						neighborY,
						newCost + heuristic(neighborX, neighborY,goalPositionX, goalPositionY)
					});
				}
			}
		}

		return null;
	}

	private int heuristic(int x, int y, int goalCellX, int goalCellY) {
		return Math.abs(x - goalCellX) + Math.abs(y - goalCellY);
	}

}
