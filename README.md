JUnit Params : A Parameterized approach to Testing using JUnit
------------------------------------------------------------------------------------------------------

An extension of JUnit to perform Data Driven Testing using annotations.

Introduction:
-------------
This project is built as an extension of Junit, and has taken the approach of providing test data to the test classes/methods at the whole new level. 
to how the test data is provided to the test method by the JUnit Runner.

Before describing the changes proposed in this repository, let us walk through what JUnit provides us for performing Data Driven Testing.
JUnit, in its experimental package provides us two options:

1) Parameterized Runner, in which we provide test data to the test method with @Parameters annotation

2) Theories Runner, in which we provide test data to the test method using either @DataPpoint(s) annotations 
or by using @ParametersSuppliedBy and DataSupplier extension.

Both of the above approach requires the user to write boilerplate code in their test classes. Even though the data now resides outside the test case,
it still is coupled with the test class. Finally, the ease of use that JUnit has been synonymous with for so long appears to be missing in the above experimental Runners.

You can find the detailed examples of Parameterized Runner and its limitations here:
http://www.kumaranuj.com/2012/08/junits-parameterized-runner-and-data.html

and for Theories runner here :
http://www.kumaranuj.com/2012/08/junit-theories-and-data-driven-testing.html

All this and more, inspired me to write a test framework that is simple to use, is flexible in its approach and can be extended by the user in a consistent manner.
Finally I wanted to bring back the same ease of use to the testing world, like we had few years ago(annotate methods with @Test and relax).

What this code base consists of:
---------------------------------

This code base consists of :

1) A customized JUnit Runner(extending BlockJUnit4Runner) that provides the test data in a consistent and user controlled manner. It is called ParamRunner. 
This Runner works on our favorite annotation @Test from JUnit and also supports passing parameters to the test method. And this is not its only selling point.

2) A Data Loading Strategy consisting of interface Loader and classes LoaderFactory and CSVDataLoader and an Enum FileType. 
CSVDataLoader is an implementation of Loader interface and provides a mechanism to load test data from a CSV file.
LoaderFactory is a Factory class that is responsible for returning the right type of Loader based on the FileType.

3) Param annotation that is an extension of ParametersSuppliedBy annotation and provides a lot of useful features to its user. Some of them include:
 + A mechanism to provide custom Objects to the test method. For eg. if a test method requires a user defined object LibraryId, then the Param annotation 
 can automatically convert the string data(provided in the CSV file) to the LibraryId Object.This is based on Java RegistryEditorsSupport. In case the standard PropertyEditor find mechanism does not apply to your project, 
 you can always register your own custom editors in your test class and the Framework will take care of the rest. For example look in the test package at LibraryId and LibraryIdEditor.
 
 + Another way to provide custom objects to the test method is by using ConverterManager and AbstractConverter. 
 A user can provide its own implementation of converting a Map (containing the key value pair) to an object that is expected by the test method and the extension framework will take care of the rest.
 See <B>CASE 4 </B>below 

4) TestData annotation to be used by the user in the test to provide information about the test data like:
   + The list of files from which to load the input test data. This is a MANDATORY field whose type is a String[]</li>
   
   + The type of File that contains the data, identified by FileType.</li>
 
    
   Currently the framework only support CSV file Type and in case the Framework does not support the specified file Type the test execution will fail.
   For the moment the annotation can only be applied at method level. 
   
5)DataContext class that contains thread-local variables that stores test data as well as the name of the currently executing test method.


Some Examples of using the junit theories extension
---------------------------------------------------
<B>CASE 1</B>: Provides input test data in the form of CSV file at the class level, that is used by the test methods.

    @RunWith(ParamRunner.class)
    @TestData(filePaths = { "getItemsData.csv" }, fileType = FileType.CSV)
    public class TestConditionsSupportedByParamRunner {


    /**
     * A Simple test that uses data provided by TestData annotation present at the Class level
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    public void testGetItems(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
<B>CASE 2</B>: User provides input test data in the form of CSV file at the method level only.

    @RunWith(ParamRunner.class)
    public class TestConditionsSupportedByParamRunner {


    /**
     * A Simple test that uses data provided by TestData annotation present at the Method level
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    @TestData(filePaths = { "getItemsData.csv" }, fileType = FileType.CSV)
    public void testGetItems(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
<B>CASE 3</B>: User provides input test data in the form of CSV file at the Class level as well as method level. In this case method level test data takes priority over class level test data.

    @RunWith(ParamRunner.class)
    @TestData(filePaths = { "getItemsData.csv" }, fileType = FileType.CSV)
    public class TestConditionsSupportedByParamRunner {


    /**
     * A Simple test that uses data provided by TestData annotation present at the Method level
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    @TestData(filePaths = { "getCustomData.csv" }, fileType = FileType.CSV)
    public void testGetItems(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }

<B>CASE 4</B>: User can also provide its custom Data Loader both at the Class level as well as at the method level. The below example shows using CustomLoader at the method level :

    @RunWith(ParamRunner.class)
    @TestData(filePaths = { "getItemsData.csv" }, fileType = FileType.CSV)
    public class TestConditionsSupportedByParamRunner {


    /**
     * A Simple test that uses data provided by TestData annotation present at the Method level
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    @TestData(filePaths = { "getCustomData.csv" }, fileType = FileType.CUSTOM)
    @CustomLoader(loader = MyDataLoader.class)
    public void testGetItems(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
<B>CASE 5</B>: User can also use their custom defined Objects as parameters in the test case. In this case LibraryId and ItenmId will be resolved using RegsitryEditorSupport of java:

    @RunWith(ParamRunner.class)
    @TestData(filePaths = { "getItemsData.csv" }, fileType = FileType.CSV)
    public class TestConditionsSupportedByParamRunner {


    /**
     * A Simple test that uses data provided by TestData annotation present at the Method level
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    @TestData(filePaths = { "getCustomData.csv" }, fileType = FileType.CUSTOM)
    @CustomLoader(loader = MyDataLoader.class)
    public void testGetItems(@Param()
    LibraryId id , @Param(name="itemid") ItemId itemId) {
        System.out.println("library Id : " + id.getValue() + " and item type : "
            + itemId.getValue());
            # Param annotation tells the framework that the parameter's value should be provided by the framework.
               It can also take an optional name attribute which gives more control over the data to the user.

    }
    
    
<B>CASE 6</B>: User can also use their custom defined objects when RegistryEditor support is not enough. The user simply has to either extend AbstractConverter class or implement the Converter interface and register it with the framework using ConverterManager class.

    @Test
    @TestData(filePaths = { "getItemsData.csv" })
    public void testConverter(@Param() Item item){
        Assert.assertNotNull(item);
        System.out.println(item.getDescription() + item.getItemId() + item.getItemType());
        
    }
    
    And the framework supports much much more. Look at the Getting Started Guide for more information.
Conclusion
-----------
This extension to JUnit focuses on bringing back the simplicity back to JUnit in JUnit way.
This extension also focuses mainly on performing Data Driven Testing within your system with ease and at the same time giving Flexibility and Extensibility to the user to use their custom behavior.
This extension is meant for people who want to write Test cases once and then reuse them again and again.
A single test can act both as a Unit Test and an integration test. Nothing in the test case should or will change. Only the test data and the tesSubject will change. This saves a lot of developers time and in turn of the project.
