package com.librarymanagement.service.services;

import com.librarymanagement.service.LibrarySnapshot;
import com.librarymanagement.service.StorageMode;

public interface StorageService {

    StorageMode getCurrentMode();

    void switchMode(StorageMode mode);

    void persistCurrentIfNeeded();

    LibrarySnapshot buildSnapshot();

    void loadFromTxt();

    void loadFromExcel();
}
