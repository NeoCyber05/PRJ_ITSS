package org.itss.prj_itss.warehouse.application.port;

import org.itss.prj_itss.warehouse.domain.WarehouseReceipt;
import org.itss.prj_itss.warehouse.domain.WarehouseReceiptItem;

public interface WarehouseReceiptRepository {
    int createReceipt(WarehouseReceipt receipt);
    boolean addReceiptItem(WarehouseReceiptItem item);
}
