package com.lgc.gitlabtool.git.services;

import java.util.Collection;

import com.lgc.gitlabtool.git.entities.Project;
import com.lgc.gitlabtool.git.project.nature.projecttype.ProjectType;
import com.lgc.gitlabtool.git.project.nature.projecttype.UnknownProjectType;

/**
 * Service for working with a type of projects
 *
 * @author Lyudmila Lyska
 */
public interface ProjectTypeService extends Service {
    static final ProjectType UNKNOWN_TYPE = new UnknownProjectType();

    /**
     * Defines type for project and returns it.
     *
     * @param project a cloned project
     * @return project type
     */
    ProjectType getProjectType(Project project);

    /**
     * Gets ProjectType by id.
     *
     * @param idType type id
     * @return project type
     */
    ProjectType getTypeById(String idType);

    /**
     * Gets collection of all existing types. Collection has types' names(ids).
     *
     * @return collection of types' names
     */
    Collection<String> getAllIdTypes();
}
