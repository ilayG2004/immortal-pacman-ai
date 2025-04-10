import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
  private int rowCount = 21;
  private int colCount = 19;
  private int tileSize = 32;
  private int boardWidth = colCount * tileSize;
  private int boardHeight = rowCount * tileSize;

  private Image wallImage;
  private Image pelletImage;
  private Image blueGhostImage;
  private Image redGhostImage;
  private Image pinkGhostImage;
  private Image orangeGhostImage;

  private Image pacmanUpImage;
  private Image pacmanDownImage;
  private Image pacmanLeftImage;
  private Image pacmanRightImage;

  public static HashSet<Block> walls;
  public static HashSet<Block> pellets;
  public static Hashtable<String, Block> ghosts;
  Block pacman;
  Timer gameloop;
  boolean firstFrame = true;
  int score = 0;
  int lives = 3;
  boolean gameOver = false;
  boolean pathSet = false;

  //Ghosts: b = blue, o = orange, p = pink, r = red
  private String[] tileMap = {
    "XXXXXXXXXXXXXXXXXXX",
    "X        X        X",
    "X XX XXX X XXX XX X",
    "X                 X",
    "X XX X XXXXX X XX X",
    "X    X       X    X",
    "XXXX XXXX XXXX XXXX",
    "OOOX X   X   X XOOO",
    "XXXX X XXrXX X XXXX",
    "O      XbpoX      O",
    "XXXX X XXXXX X XXXX",
    "OOOX X       X XOOO",
    "XXXX X XXXXX X XXXX",
    "X        X        X",
    "X XX XXX X XXX XX X",
    "X  X     P     X  X",
    "XX X X XXXXX X X XX",
    "X    X   X   X    X",
    "X XXXXXX X XXXXXX X",
    "X                 X",
    "XXXXXXXXXXXXXXXXXXX" 
  };


  PacMan() {
    setPreferredSize(new Dimension(boardWidth, boardHeight));
    setBackground(Color.BLACK);
    addKeyListener(this); //Game (Pacman) now has a keylistener
    setFocusable(true);


    // Load image
    wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
    pelletImage = new ImageIcon(getClass().getResource("./powerFood.png")).getImage();

    blueGhostImage = new ImageIcon(getClass().getResource("./Enemy-Sprites/blueGhost.png")).getImage();
    redGhostImage = new ImageIcon(getClass().getResource("./Enemy-Sprites/redGhost.png")).getImage();
    pinkGhostImage = new ImageIcon(getClass().getResource("./Enemy-Sprites/pinkGhost.png")).getImage();
    orangeGhostImage = new ImageIcon(getClass().getResource("./Enemy-Sprites/orangeGhost.png")).getImage();

    pacmanDownImage = new ImageIcon(getClass().getResource("./Player-Sprites/pacmanDown.png")).getImage();
    pacmanUpImage = new ImageIcon(getClass().getResource("./Player-Sprites/pacmanUp.png")).getImage();
    pacmanLeftImage = new ImageIcon(getClass().getResource("./Player-Sprites/pacmanLeft.png")).getImage();
    pacmanRightImage = new ImageIcon(getClass().getResource("./Player-Sprites/pacmanRight.png")).getImage();

    /*Tile map
     * P = pacman
     * empty space = food
     * X = wall
     * O = genuine whitespace
     * r, p, o, b = ghosts
     * [x_val * 32, y_val * 32] = where to draw tile sprite
     */
    /*OBJ Storage
     * Hashset used for looking up pellet, or wall objects
     * Hashtable <name, block> used for looking up specific ghosts. Useful since each ghost has their own unique pathfinding AI
     */
    
    loadMap();
    gameloop = new Timer(50, this); // 20fps (1000/50)
    gameloop.start();
    
  }

  public void loadMap() {
    walls = new HashSet<Block>();
    pellets = new HashSet<Block>();
    ghosts = new Hashtable<String, Block>();

    for (int r = 0; r < rowCount; r++) {
      for (int c = 0; c < colCount; c++) {
        String row = tileMap[r];
        char tileChar = row.charAt(c);

        int x = c*tileSize;
        int y = r*tileSize; // Ex: the tile position of 4,3 = (4*32, 3*32)

        // Wall rendering
        if (tileChar == 'X') {
          Block wall = new Block(wallImage, x, y, tileSize, tileSize, false);
          walls.add(wall);
        // Player rendering
        } else if (tileChar == 'P') {
          Block player = new Block(pacmanLeftImage, x, y, tileSize, tileSize, true);
          pacman = player;
        // Pellet rendering
        } else if (tileChar == ' ') {
          Block pellet = new Block(null, x+14, y+14, 4, 4, false);
          pellets.add(pellet);
        // Ghost rendering
        } else if (tileChar == 'r') {
          Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize, false);
          ghosts.put("Blinky", ghost);
        } else if (tileChar == 'p') {
          Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize, false);
          ghosts.put("Pinky", ghost);
        } else if (tileChar == 'o') {
          Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize, false);
          ghosts.put("Clyde", ghost);
        } else if (tileChar == 'b') {
          Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize, false);
          ghosts.put("Inky", ghost);
        }
      }
    }
  }

  // HANDLES GHOST DIRECTION SETTING
  public void moveGhosts() {
    // Pinky. Always moving in front of Pacman
    Block pinky = ghosts.get("Pinky");
    pinky.setDirection(greedyDirection(pinky));
    // Clyde. Mostly chasing pacman. But sometimes fleeing when Pacman is close
    Block clyde = ghosts.get("Clyde");
    clyde.setDirection(greedyDirection(clyde));

    // Blinky. Directly chasing pacman
    Block blinky = ghosts.get("Blinky");
    blinky.setDirection(greedyDirection(blinky));
    //System.out.println(commands);

    // Inky. Combo of Pacman's and Blinky's position
    Block inky = ghosts.get("Inky");
    inky.setDirection(greedyDirection(inky));
  }

  // Needed so that we can draw to the JPanel compoennt something other than background color
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    renderGraphics(g);
  }


  // RENDER MAP & GUI
  public void renderGraphics(Graphics g) {
    g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

    for (Block obj : walls) {
      g.drawImage(obj.image, obj.x, obj.y, obj.width, obj.height, null);
    }
    for (Block obj : ghosts.values()) {
      g.drawImage(obj.image, obj.x, obj.y, obj.width, obj.height, null);
    }
    g.setColor(Color.white);
    for (Block obj : pellets) {
      g.fillRect(obj.x, obj.y, obj.width, obj.height);
    }

    //score 
    g.setFont(new Font("Arial", Font.PLAIN, 18));
    if (gameOver) {
      g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
    } else {
      g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
    }
  }

  /*
  public void executeInstructions() {
    Block closestPellet = greedyClose
    HashMap<Node, Character> instructions = Astar(pacman, closestPellet);
  }
   */

  public void movePacman() {
    /*
    pathSet = true;
    AstarAgent pathfinder = new AstarAgent();
    Node closestPellet = pathfinder.greedyClosestPellet(pacman);
    System.out.println(closestPellet);
    HashMap<Node, Character> path = pathfinder.Astar(pacman, closestPellet);
    Node currCoord = new Node (pacman.x/32, pacman.y/32);
    while (!currCoord.equals(closestPellet)) {
      pacman.setDirection(path.get(currCoord));
      currCoord = new Node (pacman.x/32, pacman.y/32);
    }
    //pathSet=false;
    */
    pathSet = true;
    AstarAgent pathfinder = new AstarAgent();
    Node closestPellet = pathfinder.greedyClosestPellet(pacman);
    int bestDist = Integer.MAX_VALUE;
    char bestDir = 'H';
    int[][] toMove = {{0,-1}, //U
                    {0, 1}, //D
                    {1, 0}, //R
                    {-1, 0}}; //L
    int ghostX = pacman.x/32;
    int ghostY = pacman.y/32;
    for (char dir : getValidDirections(pacman.direction)) {
      int nx = 0;
      int ny= 0;
      if (dir == 'U') {
          nx = toMove[0][0];
          ny = toMove[0][1];
      } else if (dir == 'D') {
          nx = toMove[1][0];
          ny = toMove[1][1];
      } else if (dir == 'R') {
          nx = toMove[2][0];
          ny = toMove[2][1];
      } else if (dir == 'L') {
          nx = toMove[3][0];
          ny = toMove[3][1];
      }
      if (isValidTile(ghostX+nx, ghostY+ny)) {
        int projectedDistance = manhattanDistance(ghostX, ghostY, closestPellet.x, closestPellet.y);
        //System.out.println(projectedDistance + "  " + closestPellet.x + "  " + closestPellet.y);
        if (projectedDistance < bestDist) {
          bestDist = projectedDistance;
          bestDir = dir;
        }
      } 
    }
    pacman.setDirection(bestDir);
    pathSet = false;
  }
  // MOVEMENT FOR PACMAN & GHOSTS
  public void move() {
    screenWrapCheck(pacman);
    pacman.x += pacman.velocityX;
    pacman.y += pacman.velocityY;
    if (pathSet == false) {
      movePacman();
    }
    
    

    //Checks for collisions while moving
    for (Block wall : walls) {
      if (collision(pacman, wall)) {
        pacman.x -= pacman.velocityX;
        pacman.y -= pacman.velocityY;
        break;
      }
    }


    // GHOST COLLISIONS
    for (Block ghost : ghosts.values()) {
      ghost.x += ghost.velocityX;
      ghost.y += ghost.velocityY;
      if (collision(pacman, ghost)) {
        resetPositions();
      }
      for (Block wall : walls) {
        if (collision(ghost, wall)) {
          ghost.x -= ghost.velocityX;
          ghost.y -= ghost.velocityY;
          break;
        }
      } 
      screenWrapCheck(ghost);
    }

    // Pellet collisions
    Block eatenPellet = null;
    for (Block food : pellets) {
      if (collision(pacman, food)) {
        eatenPellet = food;
        score += 100;
      }
    }
    pellets.remove(eatenPellet);

    if (pellets.isEmpty()) {
      loadMap();
      resetPositions();
    }
  }

  
  public void resetPositions() {
    pacman.reset();
    pacman.velocityX = 0;
    pacman.velocityY = 0;
    lives -= 1;
    if (lives == 0) {gameOver = true;}
    for (Block entity : ghosts.values()) {
      entity.reset();
      entity.velocityX = 0;
      entity.velocityY = 0;
    }
  }

  public void screenWrapCheck(Block entity) {
    if ((entity.x/32 < 0) && (entity.y/32 == 9) ) {
      entity.x = 18*32;
      entity.y = 9*32;
      entity.setDirection('L');
    } else if ((entity.x/32 > 18) && (entity.y/32 == 9)) {
      entity.x = 0;
      entity.y = 9*32;
      entity.setDirection('R');
    }
  }

  public boolean collision(Block a, Block b) {
    return (a.x < (b.x + b.width)) && 
            ((a.x + a.width) > b.x )&&
            (a.y < (b.y + b.height)) &&
            ((a.y + a.height) > b.y);
  }

  @Override
  public void actionPerformed (ActionEvent e) {
    if (gameOver != true) {
      move(); //Update positions of all objects and redraw every frame
      moveGhosts();
    } else {System.out.println(score); System.exit(ABORT); }
    repaint();
    
  }

  @Override
  public void keyTyped (KeyEvent e) {}
  @Override
  public void keyPressed(KeyEvent e) {}
  @Override
  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_UP) {
      pacman.setDirection('U');
    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      pacman.setDirection('D');
    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
      pacman.setDirection('L');
    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
      pacman.setDirection('R');
    }
  }


  /* GHOST AI */
  public boolean isValidTile(int nx, int ny) {
    // Is this new position occupied by a wall?
    for (Block wall : walls) {
        if ((wall.x/32) == nx && (wall.y/32) == ny) {
            return false;
        }
    }
    return true;
  }
  public char[] getValidDirections(char currDir) {
      // Cannot turn completely around. If going up, cannot suddenly go down
      if (currDir == 'U') {
          return new char[] {'L','R', 'U'};
      } else if (currDir == 'D') {
          return new char[] {'L','R', 'D'};
      } else if (currDir == 'L') {
          return new char[] {'U','D', 'L'};
      } else {
          return new char[] {'U','D', 'R'};
      }
  }
  public int[] getPinkyDirections() {
    if (pacman.direction == 'U') {
      return new int[] {0, -4};
    } else if (pacman.direction == 'D') {
      return new int[] {0, 4};
    } else if (pacman.direction == 'L') {
      return new int[] {-4, 0};
    } else {
      return new int[] {4, 0};
    }
  }
  public char oppositeDir(char dir) {
    if (dir == 'U') {
      return 'D';
    } else if (dir == 'D') {
        return 'U';
    } else if (dir == 'R') {
        return 'L';
    } else {
        return 'R';
    }
  }

  public int manhattanDistance(int x1, int y1, int x2, int y2) {
      return Math.abs(x1-x2) + Math.abs(y1-y2);
  }

  // Find direction which takes us closest to pacman
  public char greedyDirection(Block ghost) {
      int bestDist = Integer.MAX_VALUE;
      char bestDir = 'H';
      int[][] toMove = {{0,-1}, //U
                      {0, 1}, //D
                      {1, 0}, //R
                      {-1, 0}}; //L
      int ghostX = ghost.x/32;
      int ghostY = ghost.y/32;
      for (char dir : getValidDirections(ghost.direction)) {
          int nx = 0;
          int ny= 0;
          if (dir == 'U') {
              nx = toMove[0][0];
              ny = toMove[0][1];
          } else if (dir == 'D') {
              nx = toMove[1][0];
              ny = toMove[1][1];
          } else if (dir == 'R') {
              nx = toMove[2][0];
              ny = toMove[2][1];
          } else if (dir == 'L') {
              nx = toMove[3][0];
              ny = toMove[3][1];
          }
          if (isValidTile(ghostX+nx, ghostY+ny)) {
            int projectedDistance = Integer.MAX_VALUE;
            if (ghost.equals(ghosts.get("Blinky"))) {
              projectedDistance = manhattanDistance(ghostX+nx, ghostY+ny, (pacman.x/32), (pacman.y/32));
            } else if (ghost.equals(ghosts.get("Pinky"))) {
              int [] pinkyModifiers = getPinkyDirections();
              projectedDistance = manhattanDistance(ghostX+nx, ghostY+ny, (pacman.x/32) + pinkyModifiers[0], (pacman.y/32) + pinkyModifiers[1]);
            } else if (ghost.equals(ghosts.get("Clyde"))) {
              projectedDistance = manhattanDistance(ghostX+nx, ghostY+ny, (pacman.x/32), (pacman.y/32));
              if (projectedDistance <= 6) {
                return oppositeDir(dir); //Clyde runaway!!
              }
            } else if (ghost.equals(ghosts.get("Inky"))) {
              // Inky's target = (distance Blinky from Pinky's target) * 2
              int blinkyX = (ghosts.get("Blinky").x/32);
              int blinkyY = (ghosts.get("Blinky").y/32);
              int [] pinkyModifiers = getPinkyDirections();
              projectedDistance = manhattanDistance(ghostX+nx, ghostY+ny, (blinkyX - ((pacman.x/32) + pinkyModifiers[0]))*2, (blinkyY-((pacman.y/32) + pinkyModifiers[1]))*2);
            }
            if (projectedDistance < bestDist) {
                bestDist = projectedDistance;
                bestDir = dir;
            } 
          } else {continue;}
      }
      return bestDir;
  }
}
