package org.easytest.example;

import org.easytest.converter.AbstractConverter;

import java.util.Map;

public class ItemConverter extends AbstractConverter<Item> {


    //@Override - Commented by Ravi for issue # 4 on 29-Aug-12
    public Item convert(Map<String, Object> convertFrom) {
        Item item = null;
        
        if(convertFrom != null){
            item = new Item();
            item.setDescription((String) convertFrom.get("itemDescription"));
            item.setItemId((String) convertFrom.get("itemId"));
            item.setItemType((String) convertFrom.get("itemType"));
        }
        return item;
    }

}
