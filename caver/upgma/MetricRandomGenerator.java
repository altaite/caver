package upgma;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetricRandomGenerator {

    public static float dist(float[] point1, float[] point2) {
        return (float) Math.sqrt((point1[0] - point2[0])
                * (point1[0] - point2[0]) + (point1[1] - point2[1])
                * (point1[1] - point2[1]));
    }

    public static void main(String[] args) {

        int size = 100000;
        float points[][] = new float[size][2];

        for (int i = 0; i < size; i++) {
            points[i][0] = (float) Math.random() * 1000;
            points[i][1] = (float) Math.random() * 1000;
        }

        try {
            String outfile = "matrix.edges.bin";
            DataOutputStream stream = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(outfile)));

            for (int i = 0; i < size; i++) {
                if (i % 1000 == 0) {
                    System.out.println("i: " + i + " of " + size);
                }
                for (int j = i + 1; j < size; j++) {
                    float dist = dist(points[i], points[j]);

                    stream.writeInt(i);
                    stream.writeInt(j);
                    stream.writeFloat(dist);


                }
            }
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger(MetricRandomGenerator.class.getName()).log(
                    Level.SEVERE, null, ex);
        }









    }
}
