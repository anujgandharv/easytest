
package org.junit.params.example;

import org.junit.params.example.editors.LibraryIdEditor;

import org.junit.params.loader.FileType;

import org.junit.params.converter.ConverterManager;

import org.junit.params.annotation.CustomLoader;
import org.junit.params.annotation.Param;
import org.junit.params.annotation.TestData;

import org.junit.params.ParamRunner;

import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * 
 * An example test class that tries to list different scenarios of using {@link ParamRunner} and its supports annotations and classes.
 * We are loading the test data at the class level, but the user can override the data at the method level as well.
 *
 */
@RunWith(ParamRunner.class)
@TestData(filePaths = { "getItemsData.csv" }, fileType = FileType.CSV)
public class TestConditionsSupportedByParamRunner {
    
    /**
     * A Junit annotated({@link Rule}) expected Exception rule that gives us the ability to specify 
     * what exception is expected to be be thrown by the test case.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    /**
     * Example showing the usage of propertyEditors for getting custom object.
     */
    @BeforeClass
    public static void setUp(){
        PropertyEditorManager.registerEditor(LibraryId.class, LibraryIdEditor.class);
        ConverterManager.registerConverter(ItemConverter.class);
    }

    /**
     * Test case that uses {@link TestData} annotation with both the available attributes : filepaths and fileTye.
     * The CSV file in this case is present in the src/test/resources folder
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    public void testGetItems(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
    /**
     * Test case that uses {@link TestData} annotation with the available attribute : filepaths only.
     * The CSV file in this case is present in the src/test/resources folder
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    public void testGetItemsWithoutFileType(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
    /**
     * Test case that uses {@link TestData} annotation with the available attribute : filepaths only.
     * The CSV file in this case is present in the src/test/resources folder
     * @param id a strongly typed, user defined Object that has its editor present in the same package as the Object.
     * So in this case LibraryId and LibraryIdEditor are present in the same package : org.example
     * And same is the case for ItemId and ItemIdEditor.
     * @param itemId same as above
     * 
     */
    @Test
    public void testGetItemsWithoutFileTypeWithStrongParameters(@Param()
    LibraryId id , @Param(name="itemid") ItemId itemId) {
        System.out.println("library Id : " + id + " and itemId :" + itemId);

    }
    
    @Test
    public void testGetItemsWithoutFileTypeWithStrongParameters(){
        thrown.expect(RuntimeException.class);
        throw new RuntimeException("ExceptionTest"); 

    }
    
    /**
     * Test case showing the use of {@link CustomLoader} annotation.
     * This example can also be used as a test to using PropertyEditors 
     * that are registered by the test class itself.
     * @param inputData
     */
    @Test
    @TestData(filePaths = { "getItemsDataCustom.csv" }, fileType = FileType.CUSTOM)
    @CustomLoader(loader = CustomCSVDataLoader.class)
    public void testGetItemsWithCustomLoader(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
    /**
     * Test case that does not use {@link TestData} annotation to get the test data. Instead it provides its own DatSupplier
     * @param inputData  a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    public void testGetItemsWithoutTestData(@ParametersSuppliedBy(GetItemsDataSupplier.class)HashMap<String, Object> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
    /**
     * Test case that uses {@link ItemConverter} to convert from a Hashmap to an Item object instance.
     * @param item an instance of Item object that is automatically converted from a map to an Item instance.
     */
    @Test
    public void testConverter(@Param() Item item){
        Assert.assertNotNull(item);
        System.out.println(item.getDescription() + item.getItemId() + item.getItemType());
        
    }
    
    /**
     * 
     * A static {@link ParameterSupplier} class for providing data to testGetItemsWithoutTestData method
     *
     */
    public static class GetItemsDataSupplier extends ParameterSupplier {
        
                @Override
                public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
                    List<PotentialAssignment> list = new ArrayList<PotentialAssignment>();
                    HashMap<String, Object> inputData = new HashMap<String, Object>();
                    inputData.put("LibraryId", new LibraryId(1L));
                    inputData.put("itemType", "ebook");
                    inputData.put("searchText", new String[]{"potter" , "poppins" , "superman"});
                    list.add(PotentialAssignment.forValue("", inputData));
                    HashMap<String, Object> inputData1 = new HashMap<String, Object>();
                    inputData1.put("LibraryId", new LibraryId(1L));
                    inputData1.put("itemType", "book");
                    inputData1.put("searchText", new String[]{"spiderman"});
                    list.add(PotentialAssignment.forValue("", inputData1));
                    return list;
                }
                
            }
        
    

}
