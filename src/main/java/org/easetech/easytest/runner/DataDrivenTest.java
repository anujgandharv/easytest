package org.easetech.easytest.runner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.cglib.proxy.Enhancer;
import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.annotation.TestSubject;
import org.easetech.easytest.interceptor.TestDataInterceptor;
import org.easetech.easytest.loader.DataConverter;
import org.easetech.easytest.loader.Loader;
import org.easetech.easytest.loader.LoaderFactory;
import org.easetech.easytest.loader.LoaderType;
import org.easetech.easytest.util.DataContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Suite} that encapsulates the {@link EasyTestRunner} in order to provide users with
 * clear indication of which test method is run and what is the input test data that the method is run with.
 * For example, when a user runs the test method with name : <B><I>getTestData</I></B> with the following test data:
 * <ul><li><B>"libraryId=1 and itemId=2"</B></li>
 * <li><B>"libraryId=2456 and itemId=789"</B></li><br><br>
 * 
 * then, {@link DataDrivenTest}, will provide the details of the executing test method in the JUnit supported IDEs like this: 
 * 
 * <ul><li><B><I>getTestData{libraryId=1 ,itemId=2}</I></B></li>
 * <li><B><I>getTestData{libraryId=2456 ,itemId=789}</I></B></li></br></br>
 * 
 * This gives user the clear picture of which test was run with which input test data.
 * 
 * For details on the actual Runner implementation, see {@link EasyTestRunner}
 * 
 * @author Anuj Kumar
 *
 */
public class DataDrivenTest extends Suite {
    
    /**
     * Load the Data for the given class or method.
     * This method will try to find {@link DataLoader} on either the class level or the method level.
     * In case the annotation is found, this method will load the data using the specified loader class 
     * and then save it in the DataContext for further use by the system.
     * @param testClass the class object, if any.
     * @param method current executing method, if any.
     */
    @SuppressWarnings("cast")
    protected void loadData(Class<?> testClass , FrameworkMethod method){
        if(testClass == null && method == null){
            Assert.fail("The framework should provide either the testClass parameter or the method parameter in order to load the test data.");
        }
        //We give priority to Class Loading and then to method loading
        DataLoader testData = null;
        if(testClass != null){
            testData = (DataLoader)testClass.getAnnotation(DataLoader.class);
        }else{
            testData = method.getAnnotation(DataLoader.class);
        }
        if(testData != null){
            String[] dataFiles = testData.filePaths();
            LoaderType loaderType = testData.loaderType();
            Loader dataLoader = null;
            if(LoaderType.CUSTOM.equals(loaderType)){
                PARAM_LOG.info("User specified to use custom Loader. Trying to get the custom loader.");
                if(testData.loader() == null){
                    Assert.fail("Specified the LoaderType as CUSTOM but did not specify loader"+ 
                        " attribute. A loaderType of CUSTOM requires the loader " +
                               "attribute specifying " +
                               "the Custom Loader Class which implements Loader interface.");
                }else{
                    try {
                        Class<? extends Loader> loaderClass = testData.loader();
                        dataLoader = loaderClass.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Exception occured while trying to instantiate a class of type :" + testData.loader(),e);
                    } 
                }
            }else if(dataFiles.length == 0){
                //No files specified, implies user wants to load data with custom loader
                if(testData.loader() == null){
                    Assert.fail("Specified the LoaderType as CUSTOM but did not specify loader"+ 
                        " attribute. A loaderType of CUSTOM requires the loader " +
                               "attribute specifying " +
                               "the Custom Loader Class which implements Loader interface.");
                }else{
                    try {
                        Class<? extends Loader> loaderClass = testData.loader();
                        dataLoader = loaderClass.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Exception occured while trying to instantiate a class of type :" + testData.loader(),e);
                    } 
                }               
            }else{
                //user has specified data files and the data fileType is also not custom.
                dataLoader = LoaderFactory.getLoader(loaderType);
            }
            if(dataLoader == null){
                Assert.fail("The framework currently does not support the specified Loader type. " +
                        "You can provide the custom Loader by choosing LoaderType.CUSTOM in TestData " +
                        "annotation and providing your custom loader using @DataLoader annotation." );
            }else{
                Map<String, List<Map<String, Object>>> data = dataLoader.loadData(dataFiles);
                DataContext.setData(data);
                DataContext.setConvertedData(DataConverter.convert(data));
                
            }
        }
    }

    
    /**
     * An instance of logger associated with the test framework.
     */
    protected static final Logger PARAM_LOG = LoggerFactory.getLogger(EasyTestRunner.class);
    
    /**
     * A {@link BlockJUnit4ClassRunner} Runner implementation that adds support of input parameters as part of the {@link Test} annotation. 
     * This {@link BlockJUnit4ClassRunner} is an interesting mix of the current JUnits support for traditional {@link Test} methods and certain 
     * new things introduced in Theories. For example the Runner supports {@link DataPoint} , {@link DataPoints} and 
     * {@link ParametersSuppliedBy} annotations in the test class.
     * <br><br>
     * 
     * This {@link Runner} implementation supports input data in the same way as is entered by the user.
     * Currently, with Junit's Theories runner implementation, the test input is combined in all possible ways 
     * to generate a permutation and combination of test data.
     * This may not be required in many cases which this extended runner aims to eliminate.
     * 
     * <br><br>
     * <B>
     * A user can specify the test data at the class level as well, using the {@link DataLoader} annotation and override it at the method level. </B><br>
     * This is extremely beneficial in cases, where the user just wants to load the data once and then reuse it for all the test methods.
     * If the user wants, then he can always override the test data at the method level by specifying the {@link DataLoader} annotation at the method level.
     * <br><br>
     * In addition, this runner also introduces a new way for the user to specify the test data using {@link DataLoader} annotation.
     * 
     * <br><br>
     * There is also a {@link Param} annotation that is an extension of {@link ParametersSuppliedBy} annotation to 
     * handle boiler plate tasks on behalf of the user as well as supports additional functionality that eases the life of the user.
     * For eg. it supports Java PropertyEditors to automatically convert a String to the specified Object.
     * It also supports passing a Map to the test method that contains all the available test data key / value pairs for easy consumption by the user.
     *<br><br>
     * In order to use it, simply use the existing {@link Test} annotation along with any or no parameters on the test case 
     * and the runner will take care of supplying any parameters to the test method before running the test cases.
     *
     *@author Anuj Kumar
     */
    private class EasyTestRunner extends BlockJUnit4ClassRunner {
        
        private final String methodName;
        
        List<FrameworkMethod> frameworkMethods;
        
        

        /**
         * @return the methodName
         */
        public String getMethodName() {
            return methodName;
        }

        /**
         * 
         * Construct a new EasyTestRunner
         * @param klass the test class whose test methods needs to be executed
         * @param methodName The name of the method to use for this instance of Runner
         * @throws InitializationError if any error occurs
         */
        public EasyTestRunner(Class<?> klass , String methodName) throws InitializationError {
            super(klass);
            this.methodName = methodName;
            enhanceTestSubject(klass);
        }
        
        /**
         * Try to collect any initialization errors, if any.
         * @param errors
         */
        @Override
        protected void collectInitializationErrors(List<Throwable> errors) {
            super.collectInitializationErrors(errors);
            validateDataPointFields(errors);
        }
        
        /**
         * Override the name of the test. In case of EasyTest, it will be 
         * the name of the test method concatenated with the input test 
         * data that the method will run with.
         * @param method the {@link FrameworkMethod}
         * @return an overridden test method Name
         */
        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s", method.getName());
        }
        
        /**
         * Overridden the compute test method to make it save the method list as class instance, 
         * so that the method does not run multiple times.
         * Also, this method now is responsible for creating multiple {@link FrameworkMethod} instances for a given method with multiple test data.
         * So, if a given test method needs to run three times with three set of input test data, 
         * then this method will actually create three instances of {@link FrameworkMethod}.
         * In order to allow the user to override the default name, {@link FrameworkMethod} is extended with 
         * {@link EasyFrameworkMethod} and {@link EasyFrameworkMethod#setName(String)} method introduced.
         * @return  list of {@link FrameworkMethod}
         */
        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            if(frameworkMethods != null && !frameworkMethods.isEmpty()){
                return frameworkMethods;
            }
            String methodToUse = null;
            if(superMethodName == null && methodName == null){
                Assert.fail("Cannot compute Test Methods to run");
            }else if(methodName != null){
                methodToUse = methodName;
            }else{
                methodToUse = superMethodName;
            }
            
                       
            List<FrameworkMethod> testMethods= super.computeTestMethods();
            List<FrameworkMethod> finalList = new ArrayList<FrameworkMethod>();
            Iterator<FrameworkMethod> itr = testMethods.iterator();

            while(itr.hasNext()){
                FrameworkMethod method = itr.next();
                loadData(null, method);
                if(methodToUse.equals(method.getName())){
                    List<Map<String , Object>> methodData = DataContext.getData().get(methodToUse);
                    for(Map<String , Object> testData : methodData){
                        
                        EasyFrameworkMethod easyMethod = new EasyFrameworkMethod(method.getMethod());
                        easyMethod.setName(methodToUse.concat(testData.toString()));
                        finalList.add(easyMethod);
                    }                    
                    break;
                }  
            }
            if(finalList.isEmpty()){
                Assert.fail("No method exists with the given name :" + methodToUse);
            }
            frameworkMethods = finalList;
            return finalList;
        }
        
        /**
         * Validate the {@link DataPoint} filed level annotation.
         * @param errors list of any errors while validating the {@link DataPoint} field.
         */
        private void validateDataPointFields(List<Throwable> errors) {
            Field[] fields= getTestClass().getJavaClass().getDeclaredFields();
            
            for (Field each : fields)
                if (each.getAnnotation(DataPoint.class) != null && !Modifier.isStatic(each.getModifiers()))
                    errors.add(new Error("DataPoint field " + each.getName() + " must be static"));
        }
        
        /**
         *Validate that there could ever be only one constructor.
         *@param errors list of any errors while validating the Constructor
         */
        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }
        
        /**
         * Validate the test methods.
         * @param errors list of any errors while validating test method
         */
        @Override
        protected void validateTestMethods(List<Throwable> errors) {
            for (FrameworkMethod each : computeTestMethods())
                if(each.getAnnotation(Test.class) != null)
                    each.validatePublicVoid(false, errors);
                else
                    each.validatePublicVoidNoArg(false, errors);
        }
        
        /**
         * Override the methodBlock to return custom {@link ParamAnchor}
         * @param method the Framework Method
         * @return a compiled {@link Statement} object to be evaluated
         */
        @Override
        public Statement methodBlock(final FrameworkMethod method) {
               return new ParamAnchor(method, getTestClass());
        }
        
        /**
         * This method is responsible for creating a CGLIB proxy of the class that is under test currently. 
         * This is not the Test class, rather the class inside the test class which needs to be tested.
         * @param klass the test Class
         */
        //TODO: Currently not used. may be used in future. 
        protected void enhanceTestSubject(Class<?> klass){
            Field[] classFields = klass.getFields();
            for(Field field : classFields){
                TestSubject testSubjectAnnotation = field.getAnnotation(TestSubject.class);
                if(testSubjectAnnotation != null){
                    //this is the field we want to enhance.
                    Class<?> fieldClassToEnhance = field.getType();
                    Object proxy = Enhancer.create(fieldClassToEnhance, new TestDataInterceptor());
                    try {
                        field.set(null, proxy);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
                }
            }
        }
        
        
        /**
         * 
         * Static inner class to support Statement evaluation.
         *
         */
        public class ParamAnchor extends Statement {
            
            /**
             * An instance of logger associated with the test framework.
             */
            protected final Logger LOG = LoggerFactory.getLogger(EasyTestRunner.ParamAnchor.class);
            
            private int successes= 0;

            /**
             * an instance of {@link FrameworkMethod} identifying the method to be tested.
             */
            private FrameworkMethod fTestMethod;
            
            /**
             * An instance of {@link TestClass} identifying the class under test
             */
            private TestClass fTestClass;
            
            /**
             * A List of {@link Assignments}. Each member in the list
             * corresponds to a single set of test data to be passed to the test method.
             * For eg. If the user has specified the test data in the CSV file as:<br>
             * <br><B>testGetItems,LibraryId,itemType,searchText</B>
             *   <br>,4,journal,batman
             *   <br>,1,ebook,potter
             *   <br>
             *   where:
             *    <li>testGetItems is the name of the method</li>
             *    <li>LibraryId,itemType,searchText are the names of the parameters that the test method expects</li>
             *    and
             *    <li>,4,journal,batman</li>
             *    <li>,1,ebook,potter</li>
             *    are the actual test data
             *    <br>
             *    then this list will consists of TWO {@link Assignments} instances with values:
             *    <li>[[{LibraryId=4, itemType=journal, searchText=batman}]]</li>
             *    AND
             *    <li>[[{LibraryId=1, itemType=ebook, searchText=potter}]]
             *    
             */
            private List<Assignments> listOfAssignments;

            /**
             * List of Invalid parameters
             */
            private List<AssumptionViolatedException> fInvalidParameters= new ArrayList<AssumptionViolatedException>();

            /**
             * 
             * Construct a new ParamAnchor.
             * The constructor performs the following operations:<br>
             * <li> It sets the class variables method , testClass and initializes the instance of {@link #listOfAssignments}</li>
             * <li> It searches for {@link DataLoader} annotation and if it finds one, 
             * it tries to get the right {@link Loader} from the {@link LoaderFactory}.
             * If the {@link Loader} is not found, the test fails.
             * If the Loader is found, it loads the data and makes it available to the entire test Thread using {@link DataContext}
             * 
             * If the annotation {@link DataLoader} is not present, 
             * then the test assumes that the user wants to use {@link ParametersSuppliedBy} annotation and does nothing.
             * @param method the method to run the test on
             * @param testClass an instance of {@link TestClass} 
             */
            public ParamAnchor(FrameworkMethod method, TestClass testClass) {
                fTestMethod= method;
                fTestClass= testClass;
                listOfAssignments = new ArrayList<Assignments>();
                loadData(null , method);

                DataContext.setMethodName(method.getName());
            }

            private TestClass getTestClass() {
                return fTestClass;
            }

            @Override
            public void evaluate() throws Throwable {
                runWithAssignment(Assignments.allUnassigned(
                        fTestMethod.getMethod(), getTestClass()));

                if (successes == 0)
                    Assert
                            .fail("Never found parameters that satisfied method assumptions.  Violated assumptions: "
                                    + fInvalidParameters);
            }

            /**
             * This method encapsulates the actual change in behavior from the traditional JUnit Theories 
             * way of populating and supplying the test data to the test method.
             * This method creates a list of {@link Assignments} identified by {@link #listOfAssignments}
             * and then calls {@link #runWithCompleteAssignment(Assignments)} for each {@link Assignments} element in the {@link #listOfAssignments}
             * @param parameterAssignment an instance of {@link Assignments} identifying the parameters that needs to be supplied test data
             * @throws Throwable if any exception occurs.
             */
            protected void runWithAssignment(Assignments parameterAssignment)
                    throws Throwable {
                while (!parameterAssignment.isComplete()) {
                    List<PotentialAssignment> potentialAssignments = parameterAssignment.potentialsForNextUnassigned();
                    boolean isFirstSetOfArguments = listOfAssignments.isEmpty();
                    for(int i = 0 ; i < potentialAssignments.size() ; i++){
                        if(isFirstSetOfArguments){
                            Assignments assignments = Assignments.allUnassigned(fTestMethod.getMethod(), getTestClass());
                            listOfAssignments.add(assignments.assignNext(potentialAssignments.get(i)));
                        }else{
                            Assignments assignments = listOfAssignments.get(i);
                            try {
                                listOfAssignments.set(i, assignments.assignNext(potentialAssignments.get(i)));
                            } catch (IndexOutOfBoundsException e) {
                                listOfAssignments.add(assignments.assignNext(potentialAssignments.get(i)));
                            }
                        }
                        
                    }
                    parameterAssignment = parameterAssignment.assignNext(null);
                } 
                if(listOfAssignments.isEmpty()){
                    LOG.debug("The list of Assignments is null. It normally happens when the user has not supplied any parameters to the test.");
                    LOG.debug(" Creating an instance of Assignments object with all its value unassigned.");
                    listOfAssignments.add(Assignments.allUnassigned(
                        fTestMethod.getMethod(), getTestClass()));
                }
                for(Assignments assignments : listOfAssignments){
                    runWithCompleteAssignment(assignments);
                }
            }


            /**
             * Run the test data with complete Assignments
             * @param complete the {@link Assignments}
             * @throws InstantiationException if an error occurs while instantiating the method
             * @throws IllegalAccessException if an error occurs due to illegal access to the test method
             * @throws InvocationTargetException if an error occurs because the method is not invokable
             * @throws NoSuchMethodException if an error occurs because no such method with the given name exists.
             * @throws Throwable any other error
             */
            protected void runWithCompleteAssignment(final Assignments complete)
                    throws InstantiationException, IllegalAccessException,
                    InvocationTargetException, NoSuchMethodException, Throwable {
                new BlockJUnit4ClassRunner(getTestClass().getJavaClass()) {
                    @Override
                    protected void collectInitializationErrors(
                            List<Throwable> errors) {
                        // do nothing
                    }

                    @Override
                    public Statement methodBlock(FrameworkMethod method) {
                        final Statement statement= super.methodBlock(method);
                        return new Statement() {
                            @Override
                            public void evaluate() throws Throwable {
                                try {
                                    statement.evaluate();
                                    handleDataPointSuccess();
                                } catch (AssumptionViolatedException e) {
                                    handleAssumptionViolation(e);
                                } catch (Throwable e) {
                                    reportParameterizedError(e, complete
                                            .getArgumentStrings(true));
                                }
                            }

                        };
                    }

                    @Override
                    protected Statement methodInvoker(FrameworkMethod method, Object test) {
                        return methodCompletesWithParameters(method, complete, test);
                    }

                    @Override
                    public Object createTest() throws Exception {
                        return getTestClass().getOnlyConstructor().newInstance(
                                complete.getConstructorArguments(true));
                    }
                }.methodBlock(fTestMethod).evaluate();
            }

            private Statement methodCompletesWithParameters(
                    final FrameworkMethod method, final Assignments complete, final Object freshInstance) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        try {
                            final Object[] values= complete.getMethodArguments(
                                    true);
                            method.invokeExplosively(freshInstance, values);
                        } catch (CouldNotGenerateValueException e) {
                            // ignore
                        }
                    }
                };
            }

            protected void handleAssumptionViolation(AssumptionViolatedException e) {
                fInvalidParameters.add(e);
            }

            protected void reportParameterizedError(Throwable e, Object... params)
                    throws Throwable {
                if (params.length == 0)
                    throw e;
                throw new ParameterizedAssertionError(e, fTestMethod.getName(),
                        params);
            }


            protected void handleDataPointSuccess() {
                successes++;
            }
        }

    }
    
    /**
     * A simple Runner that will only run the given test methods.
     * The test methods to run are provided by the outer class, which normally is an extension of {@link Suite} Runner.
     *
     */
    private class GivenTestMethodsRunner extends BlockJUnit4ClassRunner {

        /**
         * The methods that needs to be run by this runner.
         */
        private final List<FrameworkMethod> methodsToRun;

        /**
         * 
         * Construct a new GivenTestMethodsRunner
         * 
         * @param klass
         * @throws InitializationError
         */
        public GivenTestMethodsRunner(Class<?> klass) throws InitializationError {
            super(klass);
            this.methodsToRun = unusedFrameworkMethods;
        }

        

        /**
         * @return the methodsToRun
         */
        @SuppressWarnings("unused")
        public List<FrameworkMethod> getMethodsToRun() {
            return methodsToRun;
        }


        /**
         * Validate the test methods.
         * @param errors list of any errors while validating test method
         */
        @Override
        protected void validateTestMethods(List<Throwable> errors) {
            for (FrameworkMethod each : computeTestMethods())
                if(each.getAnnotation(Test.class) != null)
                    each.validatePublicVoid(false, errors);
                else
                    each.validatePublicVoidNoArg(false, errors);
        }

        /**
         * Compute the list of {@link FrameworkMethod} that needs to be run with the given runner.
         * @return the list of {@link FrameworkMethod}
         */
        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();
            List<FrameworkMethod> methodsAvailable = super.computeTestMethods();
            for (FrameworkMethod method : unusedFrameworkMethods) {
                for (FrameworkMethod fMethod : methodsAvailable) {
                    if (fMethod.getName().equals(method.getName())) {
                        result.add(fMethod);
                        break;
                    }
                }

            }
            return result;
        }
    }
    
    /**
     * A List of {@link EasyTestRunner}s. each runner in this list corresponds to a single method in the executing test class.
     * Since EasyTest is a data driven testing framework, a single test can be run multiple times by providing multiple set of test data from outside of the test.
     * In order to give users a clear picture of the test currently in execution, each method in the test class is wrapped in their own {@link EasyTestRunner}.
     * Each {@link EasyTestRunner} will internally create a list of methods based on the number i=of input test data for the given method. For ex. if there is a method <B><I>getTestData</I></B>
     * in the test class which needs to be run with two sets of input data:
     * <ul><li>libraryId=1 , itemId=2</li>
     * <li>libraryId=34 , itemId=67</li><br><br>
     * 
     * then, for such a scenario a single {@link EasyTestRunner} instance will be created for the test method <B>getTestData</B> which will contain two test methods to run with the following name:
     * <ul><li> getTestData{libraryId=1,itemId=2}</li>
     * <li> getTestData{libraryId=34,itemId=67}</li>
     */
    private final ArrayList<Runner> runners= new ArrayList<Runner>();
    
    /**
     * The current name of the method. Normally used by the enclosed {@link EasyTestRunner} class to identify the right method name.
     */
    private String superMethodName;
    
    /**
     * List of unused {@link FrameworkMethod} that will be provided to the {@link GivenTestMethodsRunner} class.
     */
    private List<FrameworkMethod> unusedFrameworkMethods;
    
    /**
     * Get the children Runners
     * @return a list of {@link EasyTestRunner}
     */
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
    /**
     * 
     * Construct a new DataDrivenTest.
     * During construction, we will load the test data, and then we will create a list of {@link EasyTestRunner}.
     * each instance of {@link EasyTestRunner} in the list will correspond to a single method in the Test Class under test.
     * @param klass the test class
     * @throws InitializationError if an initializationError occurs
     */
    public DataDrivenTest(Class<?> klass) throws InitializationError {
        super(klass, Collections.<Runner>emptyList());
        loadData(klass, null);
        if(DataContext.getData().keySet() != null){
            Iterator<String> itr = DataContext.getData().keySet().iterator();
            while(itr.hasNext()){
                String methodName = itr.next();
                this.superMethodName = methodName;
                runners.add(new EasyTestRunner(getTestClass().getJavaClass() , superMethodName));
            }
            //Next there could be cases where the user has provided the test method and its data at the method level.
            //Handle that cases here.
            List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Test.class);
            List<FrameworkMethod> unusedMethods = new ArrayList<FrameworkMethod>();
            if(methods.size() > runners.size()){
                //This means that there are test methods for which data is not provided at the class level. Find those methods 
                boolean methodAlreadyHasRunnerAssociated = false;
                for(FrameworkMethod method : methods){
                    methodAlreadyHasRunnerAssociated = false;
                    for(Runner runner : runners){
                        if(method.getName().equals(((EasyTestRunner)runner).getMethodName())){
                            methodAlreadyHasRunnerAssociated = true;
                            break;
                        }
                    }
                    if(!methodAlreadyHasRunnerAssociated){
                        unusedMethods.add(method);           
                    }
                    
                }
            }
            if(!unusedMethods.isEmpty()){
                unusedFrameworkMethods = unusedMethods;
                runners.add(new GivenTestMethodsRunner(klass));
            }            
        }
        superMethodName = null;       
    }
}
