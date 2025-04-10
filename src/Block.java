import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import javax.swing.*;


public class Block {
    public static HashSet<Block> walls;
    int x;
    int y;
    int width;
    int height;
    Image image;
    boolean isPlayer = true;

    int startX;
    int startY;
    char direction = 'L';
    int velocityX = 0;
    int velocityY = 0;
    

    Block(Image image, int x, int y, int width, int height, boolean isPlayer) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startX = x;
        this.startY = y;
        this.isPlayer = isPlayer;
    }

    public void reset() {
        this.x = this.startX;
        this.y = this.startY;
    }

    public void setDirection(char direction) {
        char prevDirection = this.direction;
        this.direction = direction;
        updateVelocity();

        this.x += this.velocityX;
        this.y += this.velocityY;

        //Keeps us from changing directions into a collideable object
        for (Block wall : PacMan.walls) {
        if (collision(this, wall)) {
            this.x -= this.velocityX;
            this.y -= this.velocityY;
            this.direction = prevDirection;
            updateVelocity();
        }
        }
    }
    public void updateVelocity() {
        int moveSpeed;
        if (this.isPlayer) {
        moveSpeed = (32/4);
        } else {
        moveSpeed = (32/8);
        }
        if (this.direction == 'U') {
        this.velocityX = 0;
        this.velocityY = -(moveSpeed); // 8 pixel per frame. 20 frames per second
        if (this.isPlayer) { this.setSprite(new ImageIcon(getClass().getResource("./Player-Sprites/pacmanUp.png")).getImage()); }
        } else if (this.direction == 'D') {
        this.velocityX = 0;
        this.velocityY = (moveSpeed);
        if (this.isPlayer) {this.setSprite(new ImageIcon(getClass().getResource("./Player-Sprites/pacmanDown.png")).getImage());} 
        } else if (this.direction == 'R') {
        this.velocityX = (moveSpeed);
        this.velocityY = 0;
        if (this.isPlayer) {this.setSprite(new ImageIcon(getClass().getResource("./Player-Sprites/pacmanRight.png")).getImage());}
        } else if (this.direction == 'L') {
        this.velocityX = -(moveSpeed);
        this.velocityY = 0;
        if (this.isPlayer) {this.setSprite(new ImageIcon(getClass().getResource("./Player-Sprites/pacmanLeft.png")).getImage());}
        } 

    }
    public void setSprite(Image image) {
        this.image = image;
    }

    public boolean collision(Block a, Block b) {
        return (a.x < (b.x + b.width)) && 
                ((a.x + a.width) > b.x )&&
                (a.y < (b.y + b.height)) &&
                ((a.y + a.height) > b.y);
      }
}
