package algorithms.triangulation.qhull;

public abstract interface PickableObject
{
  public abstract void setPosition(float[] paramArrayOfFloat);

  public abstract void updatePosition();

  public abstract void updateColor();

  public abstract float[] getPositionReference();

  public abstract byte[] getColorReference();

  public abstract float[] getPosition();

  public abstract int[] getColorIndices();

  public abstract byte[] getColor();

  public abstract void setColor(byte[] paramArrayOfByte);
}
