package edu.yu.parallel;

import java.util.concurrent.Future;

public class MyFolderService implements FolderService {
    public MyFolderService(String rootFolder) {
    }

    @Override
    public PropertyValues getPropertyValuesSequential() {
        return null;
    }

    @Override
    public Future<PropertyValues> getPropertyValuesParallel() {
        return null;
    }
}
