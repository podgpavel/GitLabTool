package com.lgc.gitlabtool.git.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lgc.gitlabtool.git.entities.Project;
import com.lgc.gitlabtool.git.project.nature.projecttype.DSGProjectType;
import com.lgc.gitlabtool.git.project.nature.projecttype.ProjectType;

/**
 * Service for working with a type of projects
 *
 * @author Lyudmila Lyska
 */
public class ProjectTypeServiceImpl implements ProjectTypeService {
    private final Set<ProjectType> _types;

    public ProjectTypeServiceImpl() {
        _types = new HashSet<>();
        initProjectTypes();
    }

    @Override
    public ProjectType getProjectType(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Invalid data. Project is null.");
        }
        String path = project.getPath();
        if (path != null) {
            for (ProjectType projectType : _types) {
                if (projectType.isProjectCorrespondsType(path)) {
                    return projectType;
                }
            }
        }
        return UNKNOWN_TYPE;
    }

    @Override
    public ProjectType getTypeById(String idType) {
        if (idType != null) {
            for (ProjectType projectType : _types) {
                if (projectType.getId().equals(idType)) {
                    return projectType;
                }
            }
        }
        return UNKNOWN_TYPE;
    }

    protected void initProjectTypes() {
        _types.add(new DSGProjectType());
    }

    protected Set<ProjectType> getSetProjectTypes() {
        return _types;
    }

    @Override
    public Collection<String> getAllIdTypes() {
        List<String> ids = new ArrayList<>();
        _types.forEach(type -> ids.add(type.getId()));
        ids.add(UNKNOWN_TYPE.getId());
        return Collections.unmodifiableList(ids);
    }
}
