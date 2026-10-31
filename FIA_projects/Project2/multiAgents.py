# multiAgents.py
# --------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


import math
from util import manhattanDistance
from game import Directions
import random, util

from game import Agent
from pacman import GameState

class ReflexAgent(Agent):
    """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
    """


    def getAction(self, gameState: GameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {NORTH, SOUTH, WEST, EAST, STOP}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState: GameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

        "*** YOUR CODE HERE ***"
        for i,ghostState in enumerate(newGhostStates):
            dist_from_ghost = manhattanDistance(newPos,ghostState.getPosition())
            if dist_from_ghost <= 1 and newScaredTimes[i] == 0:
                    return -math.inf
        
        food_list = newFood.asList()
        food_value = 0
        
        if food_list:
            distance_from_food = [manhattanDistance(newPos,food)for food in food_list]
            food_value = 10/min(distance_from_food)
        else:
            food_value = 0
            
        return successorGameState.getScore()+food_value

def scoreEvaluationFunction(currentGameState: GameState):
    """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
    """
    Your minimax agent (question 2)
    """

    def getAction(self, gameState: GameState):
        """
        Returns the minimax action from the current gameState using self.depth
        and self.evaluationFunction.

        Here are some method calls that might be useful when implementing minimax.

        gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

        gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

        gameState.getNumAgents():
        Returns the total number of agents in the game

        gameState.isWin():
        Returns whether or not the game state is a winning state

        gameState.isLose():
        Returns whether or not the game state is a losing state
        """
        "*** YOUR CODE HERE ***"
        #função que calcula o valor minimax de um estado
        #state - estado atual do jogo
        #depth - profundidade
        #agentIndex - indica qual agente esta jogando( Pacman - agentIndex[0], ghosts  - agentIndex[>=1])
        def value(state, depth, agentIndex):
            #verifica se o jogo ja acabou(ganhau ou perdeu) ou se atingiu a profundidade maxima
            if state.isWin() or state.isLose() or depth == self.depth:
                #se sim retorna a avaliação do estado atual
                return self.evaluationFunction(state)
            if agentIndex == 0: #verifica se é o pacman que esta jogando
                #se sim maximiza o valor do pacman
                return maxValue(state, depth)
            else:#caso não for o pacman
                #minimiza o valor dos fantasmas
                return minValue(state, depth, agentIndex)

        #calcula o valor maximo que o pacman pode obter a partir do estado atual
        def maxValue(state, depth):
            #inicializa o valor maximo como menos infinito
            v = float('-inf')
            #guarda todos as ações legais do Pacman no estado atual
            actions = state.getLegalActions(0)
            #verifica se não tem ações possiveis (estado sem saida)
            if not actions:
                #se sim retorna a avaliação do estado atual
                return self.evaluationFunction(state)
            #para cada ação possivel do Pacman
            for action in actions:
                #gera estado sucessor apos a ação
                successor = state.generateSuccessor(0, action)
                #calcula o valor maximo considerando o proximo agente
                v = max(v, value(successor, depth, 1))  # próximo agente = fantasma 1
            #retorna o maior valor encontrado entre todas as ações
            return v

        #calcula o valor minimo que o pacman pode obter a partir do estado atual
        def minValue(state, depth, agentIndex):
            #inicializa o valor minimo como mais-infinito
            v = float('inf')
            #guarda todos as ações legais do Pacman no estado atual
            actions = state.getLegalActions(agentIndex)
            #verifica se não tem ações possiveis (estado sem saida)
            if not actions:
                #se sim retorna a avaliação do estado atual
                return self.evaluationFunction(state)
            #guarda o numero total de agentes (Pacman + Fantasmas)
            numAgents = state.getNumAgents()
            #para cada ação possivel do Pacman
            for action in actions:
                #gera estado sucessor apos a ação
                successor = state.generateSuccessor(agentIndex, action)
                #verifica se é o ultimo fantasma da rodada
                if agentIndex == numAgents - 1:
                    #se sim, o proximo agente é o Pacman, a profundidade aumenta e minimiza o valor dos fantasmas
                    v = min(v, value(successor, depth + 1, 0))
                else: #se não for o ultimo fantasma
                    #o proximo agente é o proximo fantasma, a profundidade não muda e minimiza o valor do proximo fantasma
                    v = min(v, value(successor, depth, agentIndex + 1))
            #retorna o menor valor encontrado entre todas as ações
            return v

        bestAction = None #variavel para gurdar a melhor ação
        bestValue = float('-inf') # melhor valor até o momento
        actions = gameState.getLegalActions(0) #guarda todos as ações legais do Pacman no estado atual

        #para cada ação possivel do pacman
        for action in actions:
            #gera estado sucessor apos a ação
            successor = gameState.generateSuccessor(0, action)
            #calcula o minimax desse sucessor, considerando que o proximo agente é o fantasma 1
            score = value(successor, 0, 1)
            #verifica se o valor calculado é melhor que o melhor valor até o momento
            if score > bestValue:
                #atualiza o melhor valor
                bestValue = score
                #atualiza a melhor ação correspondente
                bestAction = action
        #retorna a ação que produz o melhor malor
        return bestAction

class AlphaBetaAgent(MultiAgentSearchAgent):
    """
    Your minimax agent with alpha-beta pruning (question 3)
    """
    
    def getAction(self, gameState: GameState):
        """
        Returns the minimax action using self.depth and self.evaluationFunction
        """
        PACMAN = 0
        numAgent = gameState.getNumAgents()
        "*** YOUR CODE HERE ***"
        def value(state, depth_remaining, agentIndex, alpha, beta):
            if state.isWin() or state.isLose() or depth_remaining == self.depth:
                return self.evaluationFunction(state)
            
            if agentIndex == 0:
                return max_value(state, depth_remaining, alpha, beta)
            else:
                return min_value(state, agentIndex, depth_remaining, alpha, beta)
            
        def max_value(state, depth_remaining, alpha, beta):
            v = float('-inf')
            actions = state.getLegalActions(PACMAN)
            if not actions:
                return self.evaluationFunction(state)
            for action in actions:
                successor = state.generateSuccessor(PACMAN, action)
                v = max(v, value(successor, depth_remaining, 1,alpha, beta))
                if v > beta:
                    return v
                alpha = max(alpha, v)
            return v
        
        def min_value(state, agentIndex, depth_remaining, alpha, beta):
            v = float('inf')
            actions = state.getLegalActions(agentIndex)
            if not actions:
                return self.evaluationFunction(state)
            
            for action in actions:
                successor = state.generateSuccessor(agentIndex, action)
                if agentIndex == numAgent-1:
                    newDepth = depth_remaining+1
                    nextAgent = PACMAN
                else:
                    newDepth = depth_remaining
                    nextAgent = agentIndex+1

                v = min(v, value(successor, newDepth, nextAgent, alpha, beta))

                if v < alpha:
                    return v
                beta = min(beta, v)
            return v
        

        alpha = float('-inf')    
        beta = float('inf')    
        bestAction = None
        bestValue = float('-inf')
        actions = gameState.getLegalActions(PACMAN)

        for action in actions:
            successor = gameState.generateSuccessor(PACMAN, action)
            v = value(successor, 0, 1, alpha, beta)
            if v > bestValue:
                bestValue = v
                bestAction = action
            alpha = max(alpha, bestValue)
        return bestAction

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """

    def getAction(self, gameState: GameState):
        """
        Returns the expectimax action using self.depth and self.evaluationFunction

        All ghosts should be modeled as choosing uniformly at random from their
        legal moves.
        """
        "*** YOUR CODE HERE ***"
        def expHelper(gameState, deepness, agent):
            if agent >= gameState.getNumAgents():
                agent = 0
                deepness += 1
            if (deepness==self.depth or gameState.isWin() or gameState.isLose()):
                return self.evaluationFunction(gameState)
            elif (agent == 0):
                return maxFinder(gameState, deepness, agent)
            else:
                return expFinder(gameState, deepness, agent)

        def maxFinder(gameState, deepness, agent):
            output = ["meow", -float("inf")]
            pacActions = gameState.getLegalActions(agent)

            if not pacActions:
                return self.evaluationFunction(gameState)

            for action in pacActions:
                currState = gameState.generateSuccessor(agent, action)
                currValue = expHelper(currState, deepness, agent+1)
                if type(currValue) is list:
                    testVal = currValue[1]
                else:
                    testVal = currValue
                if testVal > output[1]:
                    output = [action, testVal]
            return output

        def expFinder(gameState, deepness, agent):
            output = ["meow", 0]
            ghostActions = gameState.getLegalActions(agent)

            if not ghostActions:
                return self.evaluationFunction(gameState)

            probability = 1.0/len(ghostActions)

            for action in ghostActions:
                currState = gameState.generateSuccessor(agent, action)
                currValue = expHelper(currState, deepness, agent+1)
                if type(currValue) is list:
                    val = currValue[1]
                else:
                    val = currValue
                output[0] = action
                output[1] += val * probability
            return output

        outputList = expHelper(gameState, 0, 0)
        return outputList[0]
        
        util.raiseNotDefined()

def betterEvaluationFunction(currentGameState: GameState):
    """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: <write something here so we know what you did>
    """
    "*** YOUR CODE HERE ***"
    #posição atual do Pacman
    pacmanPosition = currentGameState.getPacmanPosition()
    #grid das comida
    foodGrid = currentGameState.getFood()
    #estados dos fantasmas (posição + scaredTimer)
    ghostStates = currentGameState.getGhostStates()
    #capsulas restantes
    capsules = currentGameState.getCapsules()
    #score base dado pelo proprio ambiente
    score = currentGameState.getScore()

    #lista com as posições de toda a comida
    food_list = foodGrid.asList()
    food_value = 0.0

    #para cada comida na lista
    for food_pos in food_list:
        #calcula a distancia do pacman ate a comuda
        dist = manhattanDistance(pacmanPosition, food_pos)
        #atribui um valor baseado na distancia (quanto mais perto maior o valor)
        food_value += 1.0 / (dist + 1)

    
    capsule_value = 0.0
    #para cada capsula na lista
    for cap in capsules:
        #calcula a distancia do pacman ate a capsula
        dist = manhattanDistance(pacmanPosition, cap)
        #atribui um valor baseado na distancia (quanto mais perto maior o valor)
        capsule_value += 2.0 / (dist + 1)

    dangerPenalty = 0.0
    scaredBonus = 0.0

    #para cada fantasma no estado atual
    for ghost in ghostStates:
        #posição do fantasma
        ghostPosition = ghost.getPosition()
        #calcula a distancia do pacman ate o fantasma
        dist = manhattanDistance(pacmanPosition, ghostPosition)

        #se o fantasma estiver assustado
        if ghost.scaredTimer > 0:
            #aproxima-se ao fantasma vale a pena (comer fantasma da +200pts)
            scaredBonus += 3.0 / (dist + 1)
        #caso não
        else:
            #se a distancia entre pacman e fantasma for menor que 2
            if dist < 2:
                #aumenta a penalidade de perigo
                dangerPenalty += 10.0
            else:
                #atribui uma penalidade baseada na distancia (quanto mais perto maior a penalidade)
                dangerPenalty += 1.0 / (dist + 1)

    finalScore = score + food_value + capsule_value + scaredBonus - dangerPenalty
    return  finalScore
    #util.raiseNotDefined()

# Abbreviation
better = betterEvaluationFunction
