package com.lgc.solutiontool.git.entities;

import com.lgc.solutiontool.git.jgit.BranchType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Class keeps data about branch.
 *
 * @author Pavlo Pidhorniy
 */
public class Branch {

    /**
     * The name of the branch
     **/
    private String branchName;

    /**
     * The type of the branch
     **/
    private BranchType branchType;

    /**
     * Constructor to create an instance of the class.
     *
     * @param name the name of the branch
     * @param type the type of the branch
     */
    public Branch(String name, BranchType type) {
        setBranchName(name);
        setBranchType(type);
    }

    /**
     * Gets name of branch
     *
     * @return status
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Gets type of branch
     *
     * @return status
     */
    public BranchType getBranchType() {
        return branchType;
    }

    /**
     * Sets type of branch
     *
     * @param bType type of branch
     */
    private void setBranchType(BranchType bType) {
        if (bType == null) {
            throw new IllegalArgumentException("ERROR: Incorrect data. Value is null.");
        }
        branchType = bType;
    }

    /**
     * Sets name of branch
     *
     * @param bName name of branch
     */
    private void setBranchName(String bName) {
        if (bName == null || bName.isEmpty()) {
            throw new IllegalArgumentException("ERROR: Incorrect data. Value is null or empty.");
        }
        branchName = bName;
    }

    /* (non-Javadoc)
     * @see java.lang.hashCode();
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(branchName).
                toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.equals();
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Branch))
            return false;
        if (obj == this)
            return true;

        Branch anotherBranch = (Branch) obj;
        return new EqualsBuilder().
                append(branchName, anotherBranch.branchName).
                isEquals();
    }
}