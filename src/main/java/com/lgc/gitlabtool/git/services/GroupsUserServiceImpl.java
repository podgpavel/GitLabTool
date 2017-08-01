package com.lgc.gitlabtool.git.services;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.lgc.gitlabtool.git.connections.RESTConnector;
import com.lgc.gitlabtool.git.connections.token.CurrentUser;
import com.lgc.gitlabtool.git.entities.Group;
import com.lgc.gitlabtool.git.entities.Project;
import com.lgc.gitlabtool.git.entities.User;
import com.lgc.gitlabtool.git.jgit.JGit;
import com.lgc.gitlabtool.git.listeners.stateListeners.ApplicationState;
import com.lgc.gitlabtool.git.ui.javafx.listeners.OperationProgressListener;
import com.lgc.gitlabtool.git.util.JSONParser;
import com.lgc.gitlabtool.git.util.PathUtilities;

public class GroupsUserServiceImpl implements GroupsUserService {

    private static final Logger logger = LogManager.getLogger(GroupsUserServiceImpl.class);

    private RESTConnector _connector;

    private static String privateTokenKey;
    private static String privateTokenValue;

    private static final String GROUP_ALREADY_LOADED_MESSAGE = "The group with this path is already loaded.";
    private static final String GROUP_DOESNT_EXIST_MESSAGE = "This group does not exist.";
    private static final String ERROR_GETTING_GROUP_MESSAGE = "Error getting group from GitLab.";
    private static final String INCCORECT_DATA_MESSAGE = "ERROR: Incorrect data.";

    private static ClonedGroupsService _clonedGroupsService;
    private static ProjectService _projectService;
    private static StateService _stateService;

    public GroupsUserServiceImpl(RESTConnector connector,
                                 ClonedGroupsService clonedGroupsService,
                                 ProjectService projectService,
                                 StateService stateService) {
        setConnector(connector);
        setClonedGroupsService(clonedGroupsService);
        setProjectService(projectService);
        setStateService(stateService);
    }

    @Override
    public Object getGroups(User user) {
        privateTokenValue = CurrentUser.getInstance().getPrivateTokenValue();
        privateTokenKey = CurrentUser.getInstance().getPrivateTokenKey();
        if (privateTokenValue != null) {
            HashMap<String, String> header = new HashMap<>();
            header.put(privateTokenKey, privateTokenValue);
            Object userProjects = getConnector().sendGet("/groups", null, header).getBody();
            return JSONParser.parseToCollectionObjects(userProjects, new TypeToken<List<Group>>() {
            }.getType());
        }

        return null;
    }

    private void cloneGroup(Group group, String destinationPath, OperationProgressListener progressListener) {
        String groupPath = destinationPath + File.separator + group.getName();
        Collection<Project> projects = _projectService.getProjects(group);
        if (projects == null) {
            String errorMessage = "Error getting project from the GitLab";
            progressListener.onError(errorMessage);
            progressListener.onFinish((Object)null);
            return;
        }
        boolean resultCreation = PathUtilities.createPath(Paths.get(groupPath), true);
        if (projects.isEmpty()) {
            String message = resultCreation ? "Group successfuly created!" : "Failed creation of group";
            if (resultCreation) {
                progressListener.onSuccess(null, 1, message);
            } else {
                progressListener.onError(1, message);
            }
            progressListener.onFinish((Object)null);
        } else {
            JGit.getInstance().clone(projects, groupPath, progressListener);
        }
        group.setClonedStatus(true);
        group.setPathToClonedGroup(destinationPath + File.separator + group.getName());
        _clonedGroupsService.addGroups(Arrays.asList(group));
    }

    @Override
    public Group getGroupById(int idGroup) {
        privateTokenValue = CurrentUser.getInstance().getPrivateTokenValue();
        privateTokenKey = CurrentUser.getInstance().getPrivateTokenKey();
        if (privateTokenValue != null) {
            String sendString = "/groups/" + idGroup;
            HashMap<String, String> header = new HashMap<>();
            header.put(privateTokenKey, privateTokenValue);

            Object uparsedGroup = getConnector().sendGet(sendString, null, header).getBody();
            return JSONParser.parseToObject(uparsedGroup, Group.class);
        }
        return null;
    }

    @Override
    public void cloneGroups(List<Group> groups, String destinationPath, OperationProgressListener progressListener) {
        if (groups == null || destinationPath == null) {
            throw new IllegalArgumentException("Invalid parameters.");
        }
        // we must call StateService::stateOFF for this state in the ProgressListener::onFinish method
        _stateService.stateON(ApplicationState.CLONE);

        Path path = Paths.get(destinationPath);
        if (!PathUtilities.isExistsAndDirectory(path)) {
            String errorMessage = path.toAbsolutePath() + " path is not exist or it is not a directory.";
            logger.error(errorMessage);
            progressListener.onError(null, errorMessage);
            progressListener.onFinish(null, false);
            return;
        }
        groups.forEach((group) -> cloneGroup(group, destinationPath, progressListener));
    }

    @Override
    public Group importGroup(String groupPath) {
        if (groupPath == null || groupPath.isEmpty()) {
            throw new IllegalArgumentException(INCCORECT_DATA_MESSAGE);
        }
        Path path = Paths.get(groupPath);
        if (!PathUtilities.isExistsAndDirectory(path)) {
            return null;
        }
        return importGroup(path);
    }

    private RESTConnector getConnector() {
        return _connector;
    }

    private void setConnector(RESTConnector connector) {
        _connector = connector;
    }

    private void setClonedGroupsService(ClonedGroupsService clonedGroupsService) {
        if (clonedGroupsService != null) {
            _clonedGroupsService = clonedGroupsService;
        }
    }

    private void setProjectService(ProjectService projectService) {
        if (projectService != null) {
            _projectService = projectService;
        }
    }

    private void setStateService(StateService stateService) {
        if (stateService != null) {
            _stateService = stateService;
        }
    }

    private Group importGroup(Path groupPath) {
        String nameGroup = groupPath.getName(groupPath.getNameCount() - 1).toString();
        if (checkGroupIsLoaded(groupPath.toAbsolutePath().toString())) {
            logger.debug(GROUP_ALREADY_LOADED_MESSAGE);
            return null;
        }
        Optional<Group> optFoundGroup = getGroupByName(nameGroup);
        if (!optFoundGroup.isPresent()) {
            logger.debug(GROUP_DOESNT_EXIST_MESSAGE);
            return null;
        }
        Group foundGroup = getGroupById(optFoundGroup.get().getId());
        if (foundGroup == null) {
            logger.debug(ERROR_GETTING_GROUP_MESSAGE);
            return null;
        }
        foundGroup.setPathToClonedGroup(groupPath.toString());
        foundGroup.setClonedStatus(true);
        _clonedGroupsService.addGroups(Arrays.asList(foundGroup));
        return foundGroup;
    }

    @Override
    public Map<Boolean, String> removeGroup(Group removeGroup, boolean isRemoveFromLocalDisk) {
        Map<Boolean, String> result = new HashMap<>();
        logger.info("Deleting group is started ...");
        boolean isRemoved = _clonedGroupsService.removeGroups(Arrays.asList(removeGroup));
        if (!isRemoved) {
            logger.warn("Failed deleting of group from the workspace.");
            result.put(false, "Failed deleting of group from the workspace.");
            return result;
        }
        if (isRemoveFromLocalDisk) {
            logger.info("Deleting group from local disk is started ...");
            return deleteGroupFromDirectory(removeGroup.getPathToClonedGroup());
        }
        logger.info("Successful deleting of group from the workspace.");
        result.put(isRemoved, "Successful deleting of group from the workspace.");
        return result;
    }

    private Map<Boolean, String> deleteGroupFromDirectory(String pathToClonedGroup) {
        Map<Boolean, String> result = new HashMap<>();
        if (pathToClonedGroup == null) {
            logger.warn("Error removing. The path to the cloned group is not specified.");
            result.put(false, "Error removing. The path to the cloned group is not specified.");
            return result;
        }
        Path path = Paths.get(pathToClonedGroup);
        if (!PathUtilities.isExistsAndDirectory(path)) {
            logger.warn("Error removing. The specified path does not exist or is not a directory.");
            result.put(false, "Error removing. The specified path does not exist or is not a directory.");
            return result;
        }

        boolean deleteResult = PathUtilities.deletePath(path);
        if (deleteResult) {
            String successMessage = "The group was successfully deleted from " + path.toString();
            logger.info(successMessage);
            result.put(true, successMessage);
            return result;
        } else {
            String errorMessage = "Error removing folder " + path.toString();
            result.put(false, errorMessage);
            logger.error(errorMessage);
        }
        return result;
    }

    private Optional<Group> getGroupByName(String nameGroup) {
        List<Group> groups = (List<Group>) getGroups(CurrentUser.getInstance().getCurrentUser());
        if (groups == null || groups.isEmpty()) {
            return Optional.empty();
        }
        return findGroupByName(groups, nameGroup);
    }

    private Optional<Group> findGroupByName(Collection<Group> groups, String nameGroup) {
        return groups.stream().filter(group -> group.getName().equals(nameGroup)).findFirst();
    }

    private Optional<Group> findGroupByPath(Collection<Group> groups, String groupPath) {
        return groups.stream().filter(group -> group.getPathToClonedGroup().equals(groupPath)).findFirst();
    }

    private boolean checkGroupIsLoaded(String localPathGroup) {
        List<Group> loadedGroups = getLoadedGroups();
        return loadedGroups == null ? false : findGroupByPath(loadedGroups, localPathGroup).isPresent();
    }

    private List<Group> getLoadedGroups() {
        return _clonedGroupsService.loadClonedGroups();
    }
}