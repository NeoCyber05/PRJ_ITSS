sed -i '' 's/package org.itss.prj_itss.sales.request.update;/package org.itss.prj_itss.sales.request.view;/g' src/main/java/org/itss/prj_itss/sales/request/view/ViewOrderRequestController.java
sed -i '' 's/public class UpdateOrderRequestController/public class ViewOrderRequestController/g' src/main/java/org/itss/prj_itss/sales/request/view/ViewOrderRequestController.java
sed -i '' 's/package org.itss.prj_itss.sales.request.update;/package org.itss.prj_itss.sales.request.view;/g' src/main/java/org/itss/prj_itss/sales/request/view/ViewOrderRequestPopup.java
sed -i '' 's/public class UpdateOrderRequestPopup/public class ViewOrderRequestPopup/g' src/main/java/org/itss/prj_itss/sales/request/view/ViewOrderRequestPopup.java
sed -i '' 's/UpdateOrderRequestController/ViewOrderRequestController/g' src/main/java/org/itss/prj_itss/sales/request/view/ViewOrderRequestPopup.java
sed -i '' 's/update-order-request-view.fxml/view-order-request-view.fxml/g' src/main/java/org/itss/prj_itss/sales/request/view/ViewOrderRequestPopup.java
