/**
 *
 * @author Zachary Cohan
 */
public class Datum {


    protected String name;
    protected String classification;
    protected String votes;

    /**
     *
     * @param name the name of the representative
     * @param classification their political party
     * @param votes how they voted
     */
    public Datum(String name, String classification, String votes) {
        this.votes = votes;
        this.classification = classification;
        this.name = name;
    }

    /**
     * returns an int that says which way a representative voted
     * 
     * @param issue which is they voted on
     * @return how they voted
     */
    public int getVote(int issue) {
        if (issue < 0 || issue >= votes.length()) {
            return -2;
        }
        if (votes.charAt(issue) == '+') {
            return 1;
        } else if (votes.charAt(issue) == '-') {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     *
     * @return the string representation of a datum
     */
    public String toString() {
        return name + " " + classification + " " + votes;
    }

}
