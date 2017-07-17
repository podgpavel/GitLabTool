package com.lgc.gitlabtool.git.services;

import java.util.List;
import java.util.Map;

import com.lgc.gitlabtool.git.entities.Branch;
import com.lgc.gitlabtool.git.entities.Project;
import com.lgc.gitlabtool.git.jgit.BranchType;
import com.lgc.gitlabtool.git.jgit.JGitStatus;

/**
 * Service for working with Git features.
 *
 * @author Pavlo Pidhorniy
 */
public interface GitService {

    /**
     * Checks that project has selected branches
     *
     * @param project  project for checking
     * @param branches branches that need to be checked
     * @param isCommon if true - checking will occur for all selected branches, if false - for at least one of them.
     * @return true if project contains selected branches, false if does not contains
     */
    boolean containsBranches(Project project, List<Branch> branches, boolean isCommon);

    /**
     * Switches projects to selected branch
     *
     * @param projects projects that need to be switched
     * @param branch selected branch
     * @return map with projects and theirs statuses of switching
     */
    Map<Project, JGitStatus> switchTo(List<Project> projects, Branch branch);

    /**
     * Switches projects to selected branch
     *
     * @param projects projects that need to be switched
     * @param branchName name of the branch
     * @param isRemote <code>true</code> if the branch has {@link BranchType#REMOTE}
     * @return map with projects and theirs statuses of switching
     */
    Map<Project, JGitStatus> switchTo(List<Project> projects, String branchName, boolean isRemote);

    /**
     * Gets projects that have uncommited changes
     *
     * @param projects projects that need to be checked
     * @return list of projects that has uncommited changes
     */
    List<Project> getProjectsWithChanges(List<Project> projects);

    /**
     * Discard uncommited changes
     *
     * @param projects projects that need to be resets
     * @return list of projects that and their discard statuses
     */
    Map<Project, JGitStatus> discardChanges(List<Project> projects);

    /**
     * Commit changes to selectedProjects
     *
     * @param projects          projects that contains changes
     * @param commitMessage     message for commit
     * @param isPushImmediately if true - make push operation after commiting, if false - make commit without pushing
     * @param progressListener Listener for obtaining data on the process of performing the operation.
     */
    Map<Project, JGitStatus> commitChanges(List<Project> projects, String commitMessage, boolean isPushImmediately,
                       ProgressListener progressListener);

    /**
     * Creates new branch
     *
     * @param projects     the projects that needs new branch
     * @param branchName   new branch name
     * @param force        if <code>true</code> and the branch with the given name
     *                     already exists, the start-point of an existing branch will be
     *                     set to a new start-point; if false, the existing branch will
     *                     not be changed
     * @return map with projects and theirs statuses of branch creating
     */
    Map<Project, JGitStatus> createBranch(List<Project> projects, String branchName, boolean force);

}
