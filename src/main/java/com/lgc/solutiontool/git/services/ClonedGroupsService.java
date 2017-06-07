package com.lgc.solutiontool.git.services;

import java.util.List;

import com.lgc.solutiontool.git.entities.Group;

/**
 * Service for working with current cloned groups.
 *
 * Using it we can:
 *
 *  - update current list;
 *  - read data from the XML file;
 *  - save data to the XML file;
 *  - add and remove groups from the current list.
 *
 * @author Lyudmila Lyska
 */
public interface ClonedGroupsService {

    /**
     * Gets a list of cloned groups
     *
     * @return list of cloned groups
     */
    List<Group> getClonedGroups();

    /**
     * Adds new cloned groups to the current and updates the XML file.
     */
    void addGroups(List<Group> groups);

    /**
     * Removes new cloned groups to the current list and updates the XML file.
     */
    boolean removeGroups(List<Group> groups);

    /**
     * Loads a list with currently cloned groups from the XML file.
     */
    List<Group> loadClonedGroups();

}