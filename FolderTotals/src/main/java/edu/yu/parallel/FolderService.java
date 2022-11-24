package edu.yu.parallel;

import java.util.concurrent.Future;


/***
 *  Extend the FolderService interface to provide functionality
 *  for getting the folder properties for a single root folder
 */
public interface FolderService {

    /***
     *  Calculates the total number of files, file content bytes and the number of folders
     *  contained in the root folder an all if its descendent folders
     *
     *  This method iterates ** SEQUENTIALLY ** through the directory structure
     *
     * @return A PropertyValues result object that contains the correct folder properties
     * for the given folder
     */
    PropertyValues getPropertyValuesSequential();

    /***
     *  Calculates the total number of files, file content bytes and the number of folders
     *  contained in the root folder an all if its descendent folders
     *
     *  This method iterates over the directory structure IN PARALLEL
     *
     * @return immediately with a  PropertyValues Future that can then be used to
     * obtain the PropertyValues result
     */
    Future<PropertyValues> getPropertyValuesParallel();
}
