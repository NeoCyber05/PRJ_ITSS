package org.itss.prj_itss.repository;

import org.itss.prj_itss.entity.WarehouseReceipt;
import org.itss.prj_itss.entity.WarehouseReceiptItem;

public interface IWarehouseReceiptRepository {
    int createReceipt(WarehouseReceipt receipt);
    boolean addReceiptItem(WarehouseReceiptItem item);
}
