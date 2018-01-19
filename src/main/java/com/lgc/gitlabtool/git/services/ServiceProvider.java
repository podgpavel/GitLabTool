package com.lgc.gitlabtool.git.services;

import java.util.HashMap;
import java.util.Map;

import com.lgc.gitlabtool.git.connections.RESTConnector;
import com.lgc.gitlabtool.git.connections.RESTConnectorFactory;
import com.lgc.gitlabtool.git.jgit.ChangedFilesUtils;
import com.lgc.gitlabtool.git.jgit.JGit;

public class ServiceProvider {

    private static ServiceProvider _instance;

    private final Map<Class<? extends Service>, Service> _services;

    public static ServiceProvider getInstance() {
        if (_instance == null) {
            _instance = new ServiceProvider();
        }
        return _instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<? extends Service> service) {
        return (T) _services.get(service);
    }

    private ServiceProvider() {
        BackgroundService backgroundService = new BackgroundServiceImpl();
        JGit jGit = new JGit(backgroundService);
        RESTConnector restConnector = RESTConnectorFactory.getInstance().getRESTConnector();
        LoginService loginService = new LoginServiceImpl(restConnector, backgroundService);
        StorageService storageService = new StorageServiceImpl();
        ProjectTypeService projectTypeService = new ProjectTypeServiceImpl();
        StateService stateService = new StateServiceImpl();
        GitService gitService = new GitServiceImpl(stateService, jGit, new ChangedFilesUtils());
        ConsoleService consoleService = new ConsoleServiceImpl();
        ProjectService projectService = new ProjectServiceImpl(restConnector, projectTypeService,
                stateService, consoleService, gitService, jGit);
        ClonedGroupsService programPropertiesService = new ClonedGroupsServiceImpl(storageService, loginService);
        PomXmlEditService pomXmlEditService = new PomXMLEditServiceImpl();

        _services = new HashMap<>();
        _services.put(LoginService.class, loginService);
        _services.put(ClonedGroupsService.class, programPropertiesService);
        _services.put(GroupsUserService.class, new GroupsUserServiceImpl(restConnector,
                programPropertiesService, projectService, stateService, consoleService, jGit));
        _services.put(ProjectService.class, projectService);
        _services.put(StorageService.class, storageService);
        _services.put(ReplacementService.class, new ReplacementServiceImpl());
        _services.put(PomXMLService.class, new PomXMLServiceImpl(consoleService, stateService, pomXmlEditService));
        _services.put(ProjectTypeService.class, projectTypeService);
        _services.put(NetworkService.class, new NetworkServiceImpl(backgroundService));
        _services.put(GitService.class, gitService);
        _services.put(StateService.class, stateService);
        _services.put(ConsoleService.class, consoleService);
        _services.put(BackgroundService.class, backgroundService);
    }

    public void stop() {
        if (_services != null) {
            _services.values().forEach(Service::dispose);
        }
    }
}