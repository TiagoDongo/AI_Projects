# search.py
# ---------
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


"""
In search.py, you will implement generic search algorithms which are called by
Pacman agents (in searchAgents.py).
"""

import util

class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """
        Returns the start state for the search problem.
        """
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
          state: Search state

        For a given state, this should return a list of triples, (successor,
        action, stepCost), where 'successor' is a successor to the current
        state, 'action' is the action required to get there, and 'stepCost' is
        the incremental cost of expanding to that successor.
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()


def tinyMazeSearch(problem):
    """
    Returns a sequence of moves that solves tinyMaze.  For any other maze, the
    sequence of moves will be incorrect, so only use this for tinyMaze.
    """
    from game import Directions
    s = Directions.SOUTH
    w = Directions.WEST
    return  [s, s, w, s, w, w, s, w]

def depthFirstSearch(problem: SearchProblem):
    """
    Search the deepest nodes in the search tree first.

    Your search algorithm needs to return a list of actions that reaches the
    goal. Make sure to implement a graph search algorithm.

    To get started, you might want to try some of these simple commands to
    understand the search problem that is being passed in:

    print("Start:", problem.getStartState())
    print("Is the start a goal?", problem.isGoalState(problem.getStartState()))
    print("Start's successors:", problem.getSuccessors(problem.getStartState()))
    """
    "*** YOUR CODE HERE ***"
    
    pilha = util.Stack() #cria uma pilha
    #adiciona o estado inicial e um caminho vazio
    pilha.push((problem.getStartState(), []))
    #variavel que cria um conjunto para armazenar os estados ja visitados
    estadoJaVisitado = set() 
    
    #loop que percorre enquanto hover estados na pilha
    while not pilha.isEmpty():
        #remove o ultimo estado inserido
        state, path = pilha.pop()
        
        #se o estado atual for o objetivo,então retorna o caminho atual como solução
        if problem.isGoalState(state):
            return path

        for nextState, action, cost in problem.getSuccessors(state):
            #verifica se o proximo estado ainda não visitado
            if nextState not in estadoJaVisitado:
                #marca o estado atual como visitado
                estadoJaVisitado.add(state)
                #adiciona o proximo extado na pilha + ação que lava para proximo estado
                pilha.push((nextState, path + [action]))
    
    
    util.raiseNotDefined()
    


def breadthFirstSearch(problem: SearchProblem):
    """Search the shallowest nodes in the search tree first."""
    "*** YOUR CODE HERE ***"
    #cia uma fila
    fila = util.Queue()
    #conjunto para armazenar os estados ja visitados
    visitado = set()
    #adiciona o estado inicial a fila
    fila.push((problem.getStartState(), []))

    while not fila.isEmpty():
        #retira o primeiro elemento da fila
        state, path = fila.pop()
        
        #verifica se o estado atual é o objetivo
        #se for, retorna o caminho percorrido ate ele
        if problem.isGoalState(state):
            return path
        
        #verifoca se o estado ja foi visitado
        if state not in visitado:
            #se não, marca o estado como visitado
            visitado.add(state)
            #para cada sucessor do estado atual ele espande para os outros estados
            for nextState, action, cost in problem.getSuccessors(state):
                #verifica se o setado sucessor ja foi visitado
                if nextState not in visitado:
                    #se não adiciona o estado sucessor, o caminho + a ção que foi tomada para chegar ate ele
                    fila.push((nextState, path+[action]))

    util.raiseNotDefined()

def uniformCostSearch(problem: SearchProblem):
    """Search the node of least total cost first."""
    "*** YOUR CODE HERE ***"
    priorityQueue = util.PriorityQueue()
    start = problem.getStartState()

    priorityQueue.push((start,[],0),0)
    visited = set()

    while not priorityQueue.isEmpty():
        state,path,cost = priorityQueue.pop()

        if problem.isGoalState(state):
            return path

        if state not in visited:
            visited.add(state)
            for next_state,action,stateCost in problem.getSuccessors(state):
                totalCost = cost+stateCost
                priorityQueue.push((next_state,path+[action],totalCost), totalCost)
    
    util.raiseNotDefined()

def nullHeuristic(state, problem=None):
    """
    A heuristic function estimates the cost from the current state to the nearest
    goal in the provided SearchProblem.  This heuristic is trivial.
    """
    return 0

def aStarSearch(problem: SearchProblem, heuristic=nullHeuristic):
    """Search the node that has the lowest combined cost and heuristic first."""
    "*** YOUR CODE HERE ***"
    #cria uma fila prioritatia para armazenar os nós a serem explorados
    filaPrioritaria = util.PriorityQueue()
    #obtem o estado inicial do problema
    start = problem.getStartState()
    # Inicializa o custo total do caminho até o nó inicial
    custoTotal = 0
    #cria o nó inicial com: estado, caminho e custo
    node = (start, [], custoTotal)

    #adiciona o nó inicial a fila com prioridade 0
    filaPrioritaria.push(node, 0)
    #conjunto que armazena os estados ja visitados
    visitados = set()

    # Enquanto houver nós na fila prioritária
    while not filaPrioritaria.isEmpty():
        # Remove o nó com menor custo total da fila 
        state, path, actualCost = filaPrioritaria.pop()

        #verifica se o estado atual é o goalState
        if problem.isGoalState(state):
            # Retorna o caminho até o goalState
            return path

        # Se o estado ainda não foi visitado
        if state not in visitados:
            # Marca o estado como visitado
            visitados.add(state)

            #explora os estados sucessores possiveis
            for nextstate, action, stateCost in problem.getSuccessors(state):
                #calcula o novo custo total para o estado sucessor
                newCost = actualCost+stateCost

                #calcula a prioridade como a soma do custo total e da heurística
                priority = newCost + heuristic(nextstate, problem)

                # Adiciona o estado sucessor à fila prioritária
                filaPrioritaria.push((nextstate, path+[action], newCost), priority)

    util.raiseNotDefined()


# Abbreviations
bfs = breadthFirstSearch
dfs = depthFirstSearch
astar = aStarSearch
ucs = uniformCostSearch
