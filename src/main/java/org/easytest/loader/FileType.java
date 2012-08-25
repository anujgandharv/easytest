package org.easytest.loader;

import org.easytest.annotation.CustomLoader;

/**
 * 
 * An Enum identifying the type of File to be used for loading the data.
 * A file type can identify a file to be framework based file(CSV , Excel or XML)
 * or it can identify the file to be user defined file(CUSTOM).
 * Note that a user can provide a file whose fileType is already 
 * supported by the framework and still choose to mark it CUSTOM. In such a scenario, the framework
 * will use the Loader provided by the user to load the test data.
 *
 */
public enum FileType {

    /**
     * Identifies that the type of file is a framework based CSV file. 
     * This file should support the structure as identified in {@link CSVDataLoader}
     */
    CSV ,
    /**
     * Identifies that the type of file is a framework based XML file.
     * This is currently just a place holder and will be supported in future.
     */
    XML,
    /**
     * Identifies that the type of file is a framework based EXCEL file.
     * This is currently just a place holder and will be supported in future.
     */
    EXCEL,
    /**
     * Identifies that the type of file is a user defined custom type. 
     * This option is used in conjunction with {@link CustomLoader} annotation.
     *  
     */
    CUSTOM
}
