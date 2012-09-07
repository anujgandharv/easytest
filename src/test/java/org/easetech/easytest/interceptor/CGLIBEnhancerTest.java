package org.easetech.easytest.interceptor;

import org.easetech.easytest.annotation.TestSubject;
import org.easetech.easytest.example.ItemId;
import org.easetech.easytest.example.LibraryId;
import org.easetech.easytest.example.RealItemService;
import org.easetech.easytest.runner.EasyTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyTestRunner.class)
public class CGLIBEnhancerTest {

	@TestSubject() public static RealItemService itemService = new RealItemService();
	
	@Test
    public void testGetItemEnh() {
        itemService.findItem(new LibraryId(1L), new ItemId(2L));

    }
}
