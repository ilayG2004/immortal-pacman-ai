import java.util.Objects;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Random;
import javax.swing.*;


public class AstarAgent {

  public int manhattanDistance(int x1, int y1, int x2, int y2) {
    return Math.abs(x1-x2) + Math.abs(y1-y2);
  }

  // Grabs nearest pellet to pacman. This the goal vertex of A*
  public Node greedyClosestPellet(Block pacman) {
    int closestPelletDist = Integer.MAX_VALUE;
    Node closestPelletCoords = new Node();
    int pacX = pacman.x/32;
    int pacY = pacman.y/32;

    for (Block pellet : PacMan.pellets) {
      int pX = pellet.x/32;
      int pY = pellet.y/32;
      int dist = manhattanDistance(pacX, pacY, pX, pY);
      if (dist < closestPelletDist) {
        closestPelletDist = dist;
        closestPelletCoords.setCoords(pX, pY);
        System.out.println(pX + " " + pY);
      }
    }
    //System.out.println(closestPelletCoords.x +  " " + closestPelletCoords.y);
    return closestPelletCoords;
  }

  public ArrayList<Node> getNeighbors(Node current) {
    int x = current.x;
    int y = current.y;

    ArrayList<Node> results = new ArrayList<>();

    // 4 directions. We will never be moving diagonally
    int[][] cardinalDirections = {{0,1}, 
                                  {0,-1}, 
                                  {1, 0}, 
                                  {-1,0}};
    for (int i =0; i < cardinalDirections[0].length; i++) {
      int nx = cardinalDirections[i][0];
      int ny = cardinalDirections[i][1];
      boolean blocked = false;

      // If it's a tile blocked by a wall. Do not include. Otherwise it's fine
      for (Block wall : PacMan.walls) {
        if (wall.x == nx && wall.y == ny) {
          blocked = true;
          break;
        }
      }

      if (blocked != true) {
        results.add(new Node(nx, ny));
      } else {blocked = false; continue; }

    }
    return results;
  }
  public float evaluateRisk(Block pacman, Node neighbor, Node goal) {
    float cost = 0;
    for (Block ghost : PacMan.ghosts.values()) {
      int dist = manhattanDistance((ghost.x/32), (ghost.y/32), neighbor.x, neighbor.y);
      cost += (dist*1.2);
    }
    int dist_to_pellet = manhattanDistance(goal.x, goal.y, neighbor.x, neighbor.y);
    int curr_pacman_to_pellet = manhattanDistance((pacman.x/32), (pacman.y/32), goal.x, goal.y);
    if (dist_to_pellet < curr_pacman_to_pellet) {
      cost -= (dist_to_pellet * 1.2);
    }
    return cost;
  }

  public HashMap<Node, Character> reconstructPath(Node goal, HashMap<Node, Node> parents) {
    HashMap<Node, Character> results = new HashMap<>();

    Node pointer = goal;
    while (parents.get(pointer) != null) {
      // The previous node
      Node temp = parents.get(pointer);
      // if the node where we are coming from is less than the destination then we must be moving right
      if (temp.x < pointer.x) {
        results.put(temp, 'R');
      } else if (temp.x > pointer.x) {
        results.put(temp, 'L');
      } else if (temp.y > pointer.y) {
        results.put(temp, 'D');
      } else {
        results.put(temp, 'U');
      }
      pointer = temp;
    }

    return results;
  }

  public HashMap<Node, Character> Astar(Block pacman, Node goal)  {
    //F = total cost of node
    //G = distance between current node & start
    //H is hueristic - estimated distance from current node to end node

    PriorityQueue<Node> openSet = new PriorityQueue<>();
    HashSet<Node> closedSet = new HashSet<>();
    HashMap<Node, Float> totalCost = new HashMap<>();
    HashMap<Node, Node> parents = new HashMap<>();

    Node src = new Node((pacman.x/32), (pacman.y/32));
    openSet.add(src);
    totalCost.put(src, 0f);
    parents.put(src, null);

    while (!openSet.isEmpty()) {
      Node current = openSet.poll();

      
      if (current.x == goal.x && current.y == goal.y) {
        // Iterate up parents of the current node which is our goal
        return reconstructPath(current, parents); 
      }

      closedSet.add(current);

      for (Node neighbor : getNeighbors(current)) {
        // Skip already evaluated nodes
        if (closedSet.contains(neighbor)) {
          continue;
        }
        float edgeCost = manhattanDistance(current.x, current.y, neighbor.x, neighbor.y);

        // G
        float tenativeCost = current.gCost + edgeCost; // Cost to our current node + the manhattan distance from our current node, to this reachable node
        // H
        float hueristicCost = evaluateRisk(pacman, neighbor, goal);

        // Check if this is a better path
        if (!openSet.contains(neighbor) || tenativeCost < totalCost.getOrDefault(neighbor, Float.MAX_VALUE)) {
          neighbor.setCosts(tenativeCost, hueristicCost);
          totalCost.put(neighbor, tenativeCost);
          parents.put(neighbor, current);

          if (!openSet.contains(neighbor)) {
              openSet.add(neighbor);
          }
        }
      }

    }
    return null;
  }
}