package org.easytest.annotation;

import org.easytest.loader.FileType;
import org.easytest.loader.Loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * A method or class level annotation providing users with the ability to specify a custom Loader for their test data files.
 * JUnit supports CSV, EXCEL and XML based data loading. But it may not be sufficient in all the cases.
 * Also JUnit's Data Loading Strategy may not suit every user. In such a case, a user can use this 
 * annotation along with the attribute <B>fileType = {@link FileType#CUSTOM}</B> in {@link TestData} annotation 
 * to supply your own custom Loader.<br>
 * 
 * For eg. this is how you can use it :
 * <code>
 *   @Theory
 *   @TestData(filePaths ={"myDataFile.dat"},fileType=FileType.CUSTOM)<br>
 *   @CustomLoader(loader=MyCustomDataLoader.class)<br>
 *    public void testGetItems(........<br>
 * </code>
 *<br>
 * Note that the custom Loader must implement the {@link Loader} interface and should have a no arg constructor.
 * 
 *  @author Anuj Kumar
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD , ElementType.TYPE})
public @interface CustomLoader {
    
    /** The custom Loader class that will be used by JUnit to load the test data*/
    Class<? extends Loader> loader() ;

}
