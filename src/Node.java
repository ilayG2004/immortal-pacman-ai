public class Node implements Comparable<Node>{
  int x;
  int y;
  float gCost;
  float fCost;

  public Node() {
    this.x = 0;
    this.y = 0;
    this.fCost = 0;
    this.gCost = 0;
  }

  public Node(int x, int y) {
    this.x = x;
    this.y = y;
    this.fCost = 0;
    this.gCost = 0;
  }

  public Node(int x, int y, float fCost, float gCost) {
    this.x = x;
    this.y = y;
    this.fCost = fCost;
    this.gCost = gCost;
  }

  public void setCoords(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void setCosts(float g, float f) {
    this.gCost = g;
    this.fCost = g+f;
  }

  @Override
  public int compareTo(Node other) {
      return Float.compare(this.fCost, other.fCost);
  }
}
