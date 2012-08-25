
package org.easytest.annotation;

import org.easytest.loader.FileType;
import org.easytest.loader.Loader;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Annotation responsible for providing information about the test data like :
 * 1) The list of files from which to load the input test data. This is a MANDATORY field whose type is a String[]
 * 2) The type of File that contains the data. 
 * 
 * This annotation can be specified both at the Class level and at the Method level.
 * The following algorithm is applied to load the test data:<br><br>
 * <B>If {@link TestData} is present ONLY at Class level:</B><br>
 *   Load the test data using either the specified {@link CustomLoader} or the inbuilt {@link Loader}<br><br>
 *   <B>If {@link TestData} present ONLY at Method level:</B><br>
 *   Load the test data using either the specified {@link CustomLoader} or the inbuilt {@link Loader}<br><br>
 *   <B>If {@link TestData} present BOTH at Method level and at the Class level:</B><br>
 *   Override the test data at the class level with the data specified at the method level. 
 *   If no data is present initially at the class level, the framework will introduce the new test data and will use the data.
 *   <br><br>
 *   
 * A <B>Note</B> on FileType:
 * <br>Currently we only support CSV file Type and in case the Framework does not support the specified file Type the test execution will fail.
 *
 * For the moment the annotation can only be applied at method level. 
 * An improvement to this is to be able to apply this annotation at the class level.<br><br>
 *
 *<B>The annotation can be used as follows:</B><br>
 *
 *   {@code @Test}<br>
 *   {@code @TestData(filePaths ={"getItemsData.csv" })}<br>
 *   public void testWithStrongParameters(@DataProvider()
 *   LibraryId id , @DataProvider(paramName="itemid") ItemId itemId) <br>{
 *      ....
 *
 *   }
 *   
 *  <br> In case the fileType is not provided, it is defaulted to {@link FileType#CSV}
 *   
 *   @author Anuj Kumar
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD , ElementType.TYPE})
public @interface TestData {

    /** The list of files representing the input test data for the given test method. */
    String[] filePaths();

    /** The type of file that contains the data. Defaults to "csv"*/
    FileType fileType() default FileType.CSV;

}
