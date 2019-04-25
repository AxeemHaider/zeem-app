package org.octabyte.zeem.Utils;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class StorageInformer {

    @Key
    private List<InformerItem> informerItems;

    public StorageInformer() {
    }

    public List<InformerItem> getInformerItems() {
        return informerItems;
    }

    public void setInformerItems(List<InformerItem> informerItems) {
        this.informerItems = informerItems;
    }

    public void addInformerItem(InformerItem informerItem){
        List<InformerItem> informerItems = new ArrayList<>();
        informerItems.add(informerItem);
        setInformerItems(informerItems);
    }

}
