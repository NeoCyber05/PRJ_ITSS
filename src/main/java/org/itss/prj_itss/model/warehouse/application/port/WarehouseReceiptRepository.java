package org.itss.prj_itss.model.warehouse.application.port;

import org.itss.prj_itss.model.warehouse.domain.WarehouseReceipt;
import org.itss.prj_itss.model.warehouse.domain.WarehouseReceiptItem;

public interface WarehouseReceiptRepository {
    int createReceipt(WarehouseReceipt receipt);
    boolean addReceiptItem(WarehouseReceiptItem item);
}
