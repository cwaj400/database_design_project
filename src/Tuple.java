
public class Tuple {
  private final String x;
  private final String y;

  /**
   *
   * @param x field
   * @param y where, y is value portion
   */
  public Tuple(String x, String y) {
    this.x = x;
    this.y = y;
  }

  public String getX() {
    return x;
  }

  public String getY() {
    return y;
  }

}
