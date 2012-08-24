package org.junit.params.example;

import org.junit.params.converter.AbstractConverter;

import java.util.Map;

public class ItemConverter extends AbstractConverter<Item> {


    @Override
    public Item convert(Map<String, String> convertFrom) {
        Item item = null;
        
        if(convertFrom != null){
            item = new Item();
            item.setDescription(convertFrom.get("itemDescription"));
            item.setItemId(convertFrom.get("itemId"));
            item.setItemType(convertFrom.get("itemType"));
        }
        return item;
    }

}
