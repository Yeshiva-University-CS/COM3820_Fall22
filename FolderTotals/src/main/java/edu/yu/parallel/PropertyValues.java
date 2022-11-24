package edu.yu.parallel;

public interface PropertyValues {

    /***
     *
     * @return The number of total files
     */
    int getFileCount();

    /***
     *
     * @return The number of total bytes
     */
    long getByteCount();

    /***
     *
     * @return The number of total subfolders
     */
    int getFolderCount();
}
