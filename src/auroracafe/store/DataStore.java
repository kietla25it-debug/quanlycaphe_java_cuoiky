package auroracafe.store;

import auroracafe.model.AppData;

public interface DataStore {
    AppData load();
    void save(AppData data);
}
