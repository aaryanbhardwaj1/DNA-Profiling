package forensic;

/**
 * This class represents a forensic analysis system that manages DNA data using
 * BSTs.
 * Contains methods to create, read, update, delete, and flag profiles.
 */
public class ForensicAnalysis {

    private TreeNode treeRoot;            // BST's root
    private String firstUnknownSequence;
    private String secondUnknownSequence;

    public ForensicAnalysis () {
        treeRoot = null;
        firstUnknownSequence = null;
        secondUnknownSequence = null;
    }

    /**
     * Builds a simplified forensic analysis database as a BST and populates unknown sequences.
     * The input file is formatted as follows:
     * 1. one line containing the number of people in the database, say p
     * 2. one line containing first unknown sequence
     * 3. one line containing second unknown sequence
     * 2. for each person (p), this method:
     * - reads the person's name
     * - calls buildSingleProfile to return a single profile.
     * - calls insertPerson on the profile built to insert into BST.
     *      Use the BST insertion algorithm from class to insert.
     * 
     * @param filename the name of the file to read from
     */
    public void buildTree(String filename) {
        StdIn.setFile(filename);

        // Reads unknown sequences
        String sequence1 = StdIn.readLine();
        firstUnknownSequence = sequence1;
        String sequence2 = StdIn.readLine();
        secondUnknownSequence = sequence2;
        
        int numberOfPeople = Integer.parseInt(StdIn.readLine()); 

        for (int i = 0; i < numberOfPeople; i++) {
            // Reads name, count of STRs
            String fname = StdIn.readString();
            String lname = StdIn.readString();
            String fullName = lname + ", " + fname;
            // Calls buildSingleProfile to create
            Profile profileToAdd = createSingleProfile();
            // Calls insertPerson on that profile: inserts a key-value pair (name, profile)
            insertPerson(fullName, profileToAdd);
        }
    }

    /** 
     * Reads ONE profile from input file and returns a new Profile.
    */
    public Profile createSingleProfile() {

        int numStrs = StdIn.readInt();
        STR[] arr = new STR[numStrs];
        
        for(int i = 0; i < arr.length; i++){
            String strName = StdIn.readString();
            int strOccurences = StdIn.readInt();
            STR newStr = new STR(strName);
            newStr.setStrString(strName);
            newStr.setOccurrences(strOccurences);
            arr[i] = newStr;
        }

        Profile newUserProfile = new Profile(arr);

        return newUserProfile;
    }

    /**
     * Inserts a node with a new (key, value) pair into
     * the binary search tree rooted at treeRoot.
     * 
     * Names are the keys, Profiles are the values.
     * 
     * @param newProfile the profile to be inserted
     */
    public void insertPerson(String name, Profile newProfile) {
        
        TreeNode rootNode = treeRoot;
        TreeNode prevNode = null;

        TreeNode insertNode = new TreeNode();
        insertNode.setName(name);
        insertNode.setProfile(newProfile);
        int cmp = 0;
    
        while (rootNode != null) {
            cmp = name.compareTo(rootNode.getName());
            prevNode = rootNode;

            if (cmp < 0) { //Traverse left side if it is less
                rootNode = rootNode.getLeft();}
            else if (cmp > 0){ //Traverse right side if it is greater
                rootNode = rootNode.getRight();}
        }

        if (prevNode == null) { //If the rootnode is null
            treeRoot = insertNode;
        } else if (cmp < 0) { //set the prevnode to left node
            prevNode.setLeft(insertNode);
        } else { //set the prevnode to the right node
            prevNode.setRight(insertNode);
        }
    }

    /**
     * Finds the number of profiles in the BST whose interest status matches
     * isOfInterest.
     *
     * @param isOfInterest the search mode: whether we are searching for unmarked or
     *                     marked profiles. true if yes, false otherwise
     * @return the number of profiles according to the search mode marked
     */
    public int getMatchingProfileCount(boolean isOfInterest) {

        TreeNode rootNode = treeRoot;
        Queue<TreeNode> newQueue = new Queue<TreeNode>();

        int num = 0;
        int count = correlatedProfileTraversal(rootNode, newQueue, isOfInterest, num);
        
        return count;
    }

    private int correlatedProfileTraversal(TreeNode n, Queue < TreeNode > q, boolean status, int num) {

        if(n == null){
            return num; //After all profiles have been traversed, return the number
        } else {
            if(n.getProfile().getMarkedStatus() == status){
                num += 1;
            }
        }

        num = correlatedProfileTraversal(n.getLeft(), q, status, num);
        num = correlatedProfileTraversal(n.getRight(), q, status, num);
        return num;
    }
    /**
     * Helper method that counts the # of STR occurrences in a sequence.
     * Provided method
     * 
     * @param sequence the sequence to search
     * @param STR      the STR to count occurrences of
     * @return the number of times STR appears in sequence
     */
    private int numberOfOccurrences(String sequence, String STR) {
                
        int repeats = 0;
        // STRs can't be greater than a sequence
        if (STR.length() > sequence.length())
            return 0;
        
            // indexOf returns the first index of STR in sequence, -1 if not found
        int lastOccurrence = sequence.indexOf(STR);
        
        while (lastOccurrence != -1) {
            repeats++;
            // Move start index beyond the last found occurrence
            lastOccurrence = sequence.indexOf(STR, lastOccurrence + STR.length());
        }
        return repeats;
    }

    private void poFlagProfileTraversal(TreeNode n) {

        if (n == null) { //If root note does not exist, return
            return;
        } else {
            STR[] profileSTRs = n.getProfile().getStrs();

            int commonSTRs = 0; //Number of common strs which will be used to compare
            
            double halfOfSTRs = 0; //Half of the strs
            if (profileSTRs.length % 2 != 0) {
                halfOfSTRs = ((profileSTRs.length / 2) + 1);
            } else {
                halfOfSTRs = profileSTRs.length / 2;
            }

            String combinedSequences = new String();
            String fSequence = firstUnknownSequence;
            String sSequence = secondUnknownSequence;

            combinedSequences = fSequence + sSequence;

            for (int i = 0; i <= profileSTRs.length - 1; i++) { //Check for occurences in the profile

                int nOccs = profileSTRs[i].getOccurrences();
                int sequenceOccs  = numberOfOccurrences(combinedSequences, profileSTRs[i].getStrString());

                if (nOccs == sequenceOccs){
                    commonSTRs++;
                }

                if (commonSTRs >= halfOfSTRs){ //If it is greater, change the status to true
                    n.getProfile().setInterestStatus(true);
                }
            }
        }

        poFlagProfileTraversal(n.getLeft());
        poFlagProfileTraversal(n.getRight());
    }

    /**
     * Traverses the BST at treeRoot to mark profiles if:
     * - For each STR in profile STRs: at least half of STR occurrences match (round
     * UP)
     * - If occurrences THROUGHOUT DNA (first + second sequence combined) matches
     * occurrences, add a match
     */
    public void flagProfilesOfInterest() {

        //Traverses the BST in preorder form and 
        //flags the profiles that have similarities
        //in number of occurences

        poFlagProfileTraversal(treeRoot);
    }

    /**
     * Uses a level-order traversal to populate an array of unmarked Strings representing unmarked people's names.
     * 
     * @return the array of unmarked people
     */
    public String[] getUnmarkedPeople() {

        int falseMarked = getMatchingProfileCount(false);
        String[] falseMarkedArr = new String[falseMarked];

        Queue<TreeNode> newQueue = new Queue<TreeNode>();
        int ptr = 0;
        newQueue.enqueue(treeRoot);

        while (!newQueue.isEmpty()) { 

            TreeNode tmp = newQueue.dequeue();
 
            if (tmp.getLeft() != null) {
                newQueue.enqueue((tmp.getLeft()));
            }

             if (tmp.getRight() != null) {
                newQueue.enqueue(tmp.getRight());
            }

            if (!tmp.getProfile().getMarkedStatus()){
                falseMarkedArr[ptr] = tmp.getName();
                ptr++;
            }
        }

        return falseMarkedArr;
    }

    /**
     * Removes a SINGLE node from the BST rooted at treeRoot, given a full name (Last, First)
     * 
     * If a profile containing fullName doesn't exist, do nothing.
     * 
     * @param fullName the full name of the person to delete
     */
    public void removePerson(String fullName) {

        treeRoot = traversalDeletion(fullName, treeRoot);
    }

    public TreeNode traversalDeletion(String name, TreeNode n) {

        if(n == null){
            return n;
        } else {
            int cmp = name.compareTo(n.getName());

            if (cmp < 0) {
                n.setLeft(traversalDeletion(name, n.getLeft()));
            } else if (cmp > 0) {
                n.setRight(traversalDeletion(name, n.getRight()));
            } else {
                if (n.getLeft() == null) {
                    return n.getRight();
                }

                if (n.getRight() == null) {
                    return n.getLeft();
                }
                    
                TreeNode p = n;
        
                TreeNode nextP = n.getRight();
                while (nextP.getLeft() != null) {
                    p = nextP;
                    nextP = nextP.getLeft();
                }

                if (p != n){
                    p.setLeft(nextP.getRight());
                } else if (p == n) {
                    p.setRight(nextP.getRight());
                }

                n.setName(nextP.getName());
                n.setProfile(nextP.getProfile());
            }
        }
        return n;
    }

    /**
     * Clean up the tree by using previously written methods to remove unmarked
     * profiles.
     */
    public void cleanupTree() {
        String[] getRidOfListYeaaahhhh = getUnmarkedPeople();

        for (int i = 0; i < getRidOfListYeaaahhhh.length; i++){
            removePerson(getRidOfListYeaaahhhh[i]);
        }

    }

    /**
     * Gets the root of the binary search tree.
     *
     * @return The root of the binary search tree.
     */
    public TreeNode getTreeRoot() {
        return treeRoot;
    }

    /**
     * Sets the root of the binary search tree.
     *
     * @param newRoot The new root of the binary search tree.
     */
    public void setTreeRoot(TreeNode newRoot) {
        treeRoot = newRoot;
    }

    /**
     * Gets the first unknown sequence.
     * 
     * @return the first unknown sequence.
     */
    public String getFirstUnknownSequence() {
        return firstUnknownSequence;
    }

    /**
     * Sets the first unknown sequence.
     * 
     * @param newFirst the value to set.
     */
    public void setFirstUnknownSequence(String newFirst) {
        firstUnknownSequence = newFirst;
    }

    /**
     * Gets the second unknown sequence.
     * 
     * @return the second unknown sequence.
     */
    public String getSecondUnknownSequence() {
        return secondUnknownSequence;
    }

    /**
     * Sets the second unknown sequence.
     * 
     * @param newSecond the value to set.
     */
    public void setSecondUnknownSequence(String newSecond) {
        secondUnknownSequence = newSecond;
    }
}