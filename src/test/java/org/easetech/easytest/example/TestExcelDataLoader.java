package org.easetech.easytest.example;

import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.loader.LoaderType;
import org.easetech.easytest.runner.DataDrivenTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataDrivenTest.class)
@DataLoader(filePaths={"sample.xls"} , loaderType=LoaderType.EXCEL)
public class TestExcelDataLoader {

	
	@Test
	public void getTestData(@Param(name="libraryId") Float libraryId , @Param(name="itemId") Float itemId){
	    System.out.print("getTestData : ");
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	@Test
	@DataLoader(filePaths={"TestExcelDataLoader.csv"} , loaderType=LoaderType.CSV)
	public void getTestDataWithDouble(@Param(name="libraryId") Double libraryId , @Param(name="itemId") Double itemId){
	    System.out.print("getTestDataWithDouble : ");
//	    if(itemId.equals(11568.0D)){
//	        Assert.fail("ItemId is 11568 but should be 2");
//	    }
		System.out.println("LibraryId Anuj is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	@Test
	public void getTestDataWithString(@Param(name="libraryId") String libraryId , @Param(name="itemId") String itemId){
	    System.out.print("getTestDataWithString : ");
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	//This will not work unless explicit converters are written to convert from Double to Integer as Excel loader appends all numbers with .0
	@Test
	public void getTestDataNumberFormat(){
		System.out.println("This is a simple test");
	}
	
	@Test
    public void getTestError(String a){
        System.out.println("This is a simple test");
    }

}
