import javax.swing.JFrame; //Window 19 columns x 21 rows. 32x32 pixel tile for each tile


public class App {
  public static void main(String [] args) throws Exception {
    int rowCount = 21;
    int colCount = 19;
    int tileSize = 32;
    int boardWidth = colCount * tileSize;
    int boardHeight = rowCount * tileSize;

    // Window name, window visibility, opening location in center, close on X button
    JFrame frame = new JFrame("Pac Man Java Edition");
    frame.setSize(boardWidth, boardHeight);
    frame.setLocationRelativeTo(null);
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    PacMan game = new PacMan(); //Overlayed over frame
    frame.add(game);
    frame.pack();
    frame.setVisible(true);
    game.requestFocus();

  }
  
}