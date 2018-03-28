package Maps;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Random;


public class KMeansClustering {
    static final Double EARTH_RADIUS = 6371.00;
    double minima[] = new double[2], maxima[] = new double[2];
    private FirebaseAuth mAuth;



    KMeansClustering() {
        mAuth = FirebaseAuth.getInstance();
    }

    void FindColMinMax(double[][] items) {
        int n = items[0].length;

        for (int i = 0; i < 2; i++) {
            maxima[i] = -100000000.00;
            minima[i] = 100000000.00;
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < items.length; j++) {

                if (items[j][i] < minima[i]) {
                    minima[i] = items[j][i];

                }
                if (items[j][i] > maxima[i]) {
                    maxima[i] = items[j][i];

                }
            }
        }
    }

    double EuclideanDistance(double[] x, double[] y) {
        double S = 0;
        for (int i = 0; i < x.length; i++) {
            S += Math.pow((x[i] - y[i]), 2);
        }
        return Math.sqrt(S);
    }


    protected static Random random = new Random();

    static double randomInRange(double min, double max) {
        double range = max - min;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + min;
        return shifted; // == (rand.nextDouble() * (max-min)) + min;
    }

    double[][] InitializeMeans(double[][] items, int k, double[] cMin, double[] cMax) {

        int f = items[0].length;
        double[][] means = new double[k][f];
        for (double[] mean : means) {
            for (int i = 0; i < mean.length; i++) {
                mean[i] = randomInRange(cMin[i] + 1, cMax[i] - 1);
            }

        }
        return means;
    }

    double[] UpdateMeans(int n, double[] mean, double[] item) {
        for (int i = 0; i < mean.length; i++) {
            double m = mean[i];
            m = (m * (n - 1) + item[i]) / (float) n;
            mean[i] = m;
        }
        return mean;
    }



    double[][] FindClusters(double[][] means, double[][] items) {

        double[][] clusters = new double[means.length][];
        for (double[] item : items) {
            int index = Classifyy(means, item);//item is a 1d array;

        }
        return clusters;
    }

    int Classifyy(double[][] means, double[] item) {
        double minimum = 100000000.00;
        int index = -1;
        for (int i = 0; i < means.length; i++) {
            double dis = CalculationByDistance(item[0],item[1], means[i][0],means[i][1]);
            if (dis < minimum) {
                minimum = dis;
                index = i;
            }
        }

        return index;

    }

    public double CalculationByDistance(double lat1, double lon1, double lat2, double lon2) {
        double Radius = EARTH_RADIUS;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }


    double[][] CalculateMeans(int k, double[][] items, int maxIterations) {
        FindColMinMax(items);

        double[][] means = InitializeMeans(items, k, minima, maxima);
        int[] clusterSizes = new int[means.length];
        int[] belongsTo = new int[items.length];

        for (int e = 0; e < maxIterations; e++) {
            boolean noChange = true;
            for (int i = 0; i < items.length; i++) {
                double[] item = items[i];
                int index = Classifyy(means, item);
                clusterSizes[index] += 1;

                means[index] = UpdateMeans(clusterSizes[index], means[index], item);

                if (index != belongsTo[i]) {
                    noChange = false;
                }
                belongsTo[i] = index;
            }
            if (noChange) {
                break;
            }

        }
        return means;
    }


}
