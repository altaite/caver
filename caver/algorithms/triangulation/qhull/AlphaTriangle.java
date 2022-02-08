 package algorithms.triangulation.qhull;
 
 public class AlphaTriangle
 {
   public double minU;
   public double maxU;
   public int lastSeen;
 
   public AlphaTriangle(double min, double max)
   {
     this.minU = min;
     this.maxU = max;
     this.lastSeen = 0;
   }
 }
