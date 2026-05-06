package org.itss.prj_itss.repository;

import org.itss.prj_itss.common.config.IConnectionProvider;
import org.itss.prj_itss.entity.WarehouseReceipt;
import org.itss.prj_itss.entity.WarehouseReceiptItem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class WarehouseReceiptRepository extends RepositorySupport implements IWarehouseReceiptRepository {

    public WarehouseReceiptRepository(IConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public int createReceipt(WarehouseReceipt receipt) {
        String sql = """
            INSERT INTO warehouse_receipts
                (source_order_id, source_order_code, source_request_code, site_code, site_name,
                 result_status, has_discrepancy, discrepancy_note, overall_note, confirmed_at,
                 confirmed_by_account_id, confirmed_by_username)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, receipt.getSourceOrderId());
            ps.setString(2, receipt.getSourceOrderCode());
            ps.setString(3, receipt.getSourceRequestCode());
            ps.setString(4, receipt.getSiteCode());
            ps.setString(5, receipt.getSiteName());
            ps.setString(6, receipt.getResultStatus());
            ps.setBoolean(7, receipt.isHasDiscrepancy());
            ps.setString(8, receipt.getDiscrepancyNote());
            ps.setString(9, receipt.getOverallNote());
            ps.setTimestamp(10, Timestamp.valueOf(receipt.getConfirmedAt()));
            if (receipt.getConfirmedByAccountId() == null) {
                ps.setNull(11, Types.INTEGER);
            } else {
                ps.setInt(11, receipt.getConfirmedByAccountId());
            }
            ps.setString(12, receipt.getConfirmedByUsername());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException exception) {
            System.err.println("WarehouseReceiptRepository.createReceipt: " + exception.getMessage());
        }
        return -1;
    }

    @Override
    public boolean addReceiptItem(WarehouseReceiptItem item) {
        String sql = """
            INSERT INTO warehouse_receipt_items
                (receipt_id, source_order_item_id, merchandise_code, merchandise_name, ordered_quantity,
                 received_quantity, unit, transport_method, inspection_result, item_note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, item.getReceiptId());
            if (item.getSourceOrderItemId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, item.getSourceOrderItemId());
            }
            ps.setString(3, item.getMerchandiseCode());
            ps.setString(4, item.getMerchandiseName());
            ps.setBigDecimal(5, item.getOrderedQuantity());
            ps.setBigDecimal(6, item.getReceivedQuantity());
            ps.setString(7, item.getUnit());
            ps.setString(8, item.getTransportMethod());
            ps.setString(9, item.getInspectionResult());
            ps.setString(10, item.getItemNote());
            return ps.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.err.println("WarehouseReceiptRepository.addReceiptItem: " + exception.getMessage());
        }
        return false;
    }
}
