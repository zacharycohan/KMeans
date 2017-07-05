
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author Zachary Cohan
 */
public class KMeans {

    
    static ArrayList<Datum> data = new ArrayList<>();//initial data
    static ArrayList<Datum> group1 = new ArrayList<>();//the group that goes with the first centroid
    static ArrayList<Datum> group2 = new ArrayList<>();//the group that goes with the second centroid
    static ArrayList<double[]> oldCentroids;//locations of the old centroids
    static double[] centroid1;//current first centroid
    static double[] centroid2;//current second centroid
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        File vd = new File("voting-data.tsv");
        Scanner in = null;
        String[] datumString;
        Datum datum;
        try {
            in = new Scanner(vd);
        } catch (FileNotFoundException ex) {
            System.out.println("The file was not found");
        }
        
        //parse through the voting data and create the centroids
        while (in.hasNext()) {
            datumString = in.nextLine().split("\t");
            datum = new Datum(datumString[0], datumString[1], datumString[2]);
            data.add(datum);
        }
        
        //finds the initial representatives to be used as centroids
        Datum[] init = KMeans.getInitalCentroids();
        System.out.println(init[0].name + " and "+init[1].name+ " chosen as initial centroids");
        
        //create the initial centroids
        centroid1 = new double[data.get(0).votes.length()];
        centroid2 = new double[data.get(0).votes.length()];
        for(int i = 0;i<centroid1.length;i++)
        {
            centroid1[i] = init[0].getVote(i);
            centroid2[i] = init[1].getVote(i);            
        }
        
        //goes through the data, assigning groups and readjusting them while the centroids are not in their final location
        do{
            assignGroups();
            adjustCentroids(); 
        }while(!centroidsEqual());
        
        
        //the following lines and loops calculate the percentage of democrats
        //and republicans in each group
        double demInOne = 0;
        double repInOne = 0;
        double demInTwo = 0;
        double repInTwo = 0;
        for(Datum d : group1)
        {
            if(d.classification.equals("D"))demInOne++;
            else repInOne++;
        }
        for(Datum d : group2)
        {
            if(d.classification.equals("D"))demInTwo++;
            else repInTwo++;
        }
        double pdemInOne = round(demInOne/(double)group1.size()*100,2);
        double prepInOne = round(repInOne/(double)group1.size()*100,2);
        double pdemInTwo = round(demInTwo/(double)group2.size()*100,2);
        double prepInTwo = round(repInTwo/(double)group2.size()*100,2);
        
        //print out the final numbers
        System.out.println("Final groups:");
        System.out.println("Group 1 ("+group1.size()+" reps):  "+pdemInOne+"% Dem, "+prepInOne+"% GOP");
        System.out.println("Group 2 ("+group2.size()+" reps):  "+pdemInTwo+"% Dem, "+prepInTwo+"% GOP");

    }
    
    /**
     * Determines the distance between a particular datum and a centroid using the distance formula
     * @param a the datum that will be used
     * @param b the centroid that will be used
     * @return the distance between them
     */
    public static double getDistance(Datum a,double[] b) {

        double total = 0;

        for(int i = 0;i<10;i++)
        {
            double d = (double)(a.getVote(i))-b[i];
            total += Math.pow(d, 2);
        }

        return Math.sqrt(total);
    }    

    /**
     * Determines the distance between two data using the distance formula
     * @param a the first datum
     * @param b the second datum
     * @return the distance between them
     */
    public static double getDistance(Datum a, Datum b) {

        double total = 0;

        for(int i = 0;i<10;i++)
        {
            int d = a.getVote(i)-b.getVote(i);
            total += Math.pow((double)d, 2);
        }

        return Math.sqrt(total);
    }
    
    
    /**
     * finds the two data that are farthest apart from each other
     * @return the distance between the two data
     */
    public static Datum[] getInitalCentroids()
    {
        //bfs means "best so far", gets initialized to -1 to gaurantee that the method works when no data has been checked
        double bsf = -1;
        
        
        Datum[] centroids = new Datum[2];
        //this loop goes through every combination of a pair of data
        //and determines the two that are the farthest apart
        for(int i = 0;i<data.size()-1;i++)
        {
            for(int j = i+1;j<data.size();j++)
            {
                double attempt = KMeans.getDistance(data.get(i), data.get(j));
                if(attempt > bsf){
                    bsf = attempt;
                    centroids[0] = data.get(i);
                    centroids[1] = data.get(j);
                }
            }
        }
        //returns an array of length 2, containing the two data furthest apart
        return centroids;    
    }
    
    /**
     * takes the average location of all of the data in a group and 
     * assigns the centroids accordingly.
     * the previous values of the centroids are recorded so that the algorithm knows when to stop
     * (algorithm stops when the centroids move less that a thousandth for each of the 10 dimensions)
     */
    public static void adjustCentroids()
    {
        ArrayList<double[]> arr = new ArrayList<>();
        double[] a = new double[centroid1.length];
        double[] b = new double[centroid2.length];
        
        //following two loops add up all of the values for each dimension for every piece of data in the groups
        for(Datum d : group1)
        {
            for(int i = 0;i<d.votes.length();i++)
            {
                a[i] += d.getVote(i);
            }
        }
        for(Datum d : group2)
        {
            for(int i = 0;i<d.votes.length();i++)
            {
                b[i] += d.getVote(i);
            }
        }
        
        
        //the following two loops divide each dimension by the group size to determin the average
        for(int i = 0;i<a.length;i++)
        {
            a[i] = round(a[i]/(double)group1.size(),3);
        }
        
        for(int i = 0;i<b.length;i++)
        {
            b[i] = round(b[i]/(double)group2.size(),3);
        }
        
        //following four lines keep track of the old centroid locations so the algorithm knows when to stop
        if(oldCentroids != null)oldCentroids.clear();
        else oldCentroids = new ArrayList<>();
        oldCentroids.add(centroid1);
        oldCentroids.add(centroid2);

        centroid1 = a;
        centroid2 = b;        
    }
    
    /**
     * the old groups get cleared and then reassigned based on the new centroid locations
     */
    public static void assignGroups(){

        
        group1.clear();
        group2.clear();
        
        for(Datum d : data){
            if(getDistance(d,centroid2)>getDistance(d,centroid1))
                group2.add(d);
            else group1.add(d);
        }
    }
    
    /**
     * determines if the centroids are in the same location as they previously were
     * @return whether the old centroids have remained in the same location(measures to the thousandth)
     */
    public static boolean centroidsEqual()
    {

        int count = 0;
        
        for(double d : centroid1)
        {
            if(d == oldCentroids.get(1)[count]);
            else return false;
            count++;
        }
        count = 0;
        for(double d : centroid2)
        {
            if(d == oldCentroids.get(0)[count]);
            else return false;
            count++;
        }
        return true;
    }

    //THIS CODE TAKEN FROM STACK OVERFLOW, IT JUST ROUNDS THE VALUE OF A DOUBLE
        public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    

}
