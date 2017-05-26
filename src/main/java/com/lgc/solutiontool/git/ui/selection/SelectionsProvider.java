package com.lgc.solutiontool.git.ui.selection;

import com.lgc.solutiontool.git.entities.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider which store listView elements by identifier
 * <p>
 * Should be useful for sending data between some ui controllers.
 * Note: use also {@link ListViewKey}
 *
 * @author Pavlo Pidhorniy
 */
public class SelectionsProvider {

    private static SelectionsProvider _instance;

    private final Map<String, List> _listItemsMap;

    private SelectionsProvider() {
        _listItemsMap = new HashMap<>();
        _listItemsMap.put(ListViewKey.MAIN_WINDOW_PROJECTS.getKey(), new ArrayList<Project>());

    }

    /**
     * Gets instance's the class
     *
     * @return instance
     */
    public static SelectionsProvider getInstance() {
        if (_instance == null) {
            _instance = new SelectionsProvider();
        }
        return _instance;
    }

    /**
     * Gets list of items by id
     *
     * @return list of items
     */
    public List getSelectionItems(String listViewId) {
        if (listViewId == null || listViewId.isEmpty()) {
            throw new IllegalArgumentException("ERROR: Incorrect data. Value is null or empty.");
        }
        return _listItemsMap.get(listViewId);
    }

    /**
     * Add list with identifier
     *
     * @param listViewId identifier
     * @param items      items for adding
     */
    public void setSelectionItems(String listViewId, List items) {
        if (listViewId == null || listViewId.isEmpty() || items == null) {
            throw new IllegalArgumentException("ERROR: Incorrect data. Value is null or empty.");
        }
        if (_listItemsMap.containsKey(listViewId)) {
            _listItemsMap.get(listViewId).clear();
            _listItemsMap.get(listViewId).addAll(items);
        } else {
            _listItemsMap.put(listViewId, items);
        }
    }
}