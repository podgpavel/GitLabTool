package com.lgc.solutiontool.git.services;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.lgc.solutiontool.git.entities.Branch;
import com.lgc.solutiontool.git.entities.Project;
import com.lgc.solutiontool.git.jgit.JGitStatus;

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
     * Gets projects that have uncommited changes
     *
     * @param projects projects that need to be checked
     * @return list of projects that has uncommited changes
     */
    List<Project> getProjectsWithChanges(List<Project> projects);

    /**
     * Commit changes to selectedProjects
     *
     * @param projects          projects that contains changes
     * @param commitMessage     message for commit
     * @param isPushImmediately if true - make push operation after commiting, if false - make commit without pushing
     * @param onSuccess      method for tracking the success progress of cloning,
     *                       where <Integer> is a percentage of progress.
     * @param onError        method for tracking the errors during cloning,
     *                       where <Integer> is a percentage of progress, <String> error message.
     */
    void commitChanges(List<Project> projects, String commitMessage, boolean isPushImmediately,
                       Consumer<Integer> onSuccess, BiConsumer<Integer, String> onError);
}
