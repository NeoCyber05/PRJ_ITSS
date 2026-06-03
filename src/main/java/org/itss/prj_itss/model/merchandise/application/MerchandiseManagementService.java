package org.itss.prj_itss.model.merchandise.application;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;

import java.util.List;
import java.util.Locale;

public final class MerchandiseManagementService {

    private final MerchandiseRepository merchandiseRepository;

    public MerchandiseManagementService(MerchandiseRepository merchandiseRepository) {
        this.merchandiseRepository = merchandiseRepository;
    }

    public List<Merchandise> findAll() {
        return merchandiseRepository.findAll();
    }

    public List<Merchandise> findActive() {
        return merchandiseRepository.findActive();
    }

    public Merchandise findById(int id) {
        return merchandiseRepository.findById(id);
    }

    public MerchandiseManagementResult create(MerchandiseDraft draft) {
        String code = normalizeCode(draft.code());
        String name = normalizeText(draft.name());
        String unit = normalizeText(draft.unit());

        if (code.isEmpty()) {
            return MerchandiseManagementResult.failure("Mã hàng không được để trống.");
        }
        if (name.isEmpty()) {
            return MerchandiseManagementResult.failure("Tên mặt hàng không được để trống.");
        }
        if (unit.isEmpty()) {
            return MerchandiseManagementResult.failure("Đơn vị không được để trống.");
        }

        Merchandise existing = merchandiseRepository.findByCode(code);
        if (existing != null) {
            return MerchandiseManagementResult.failure("Mã hàng đã tồn tại.");
        }

        Merchandise merchandise = new Merchandise();
        merchandise.setCode(code);
        merchandise.setName(name);
        merchandise.setUnit(unit);
        merchandise.setActive(true);

        int id = merchandiseRepository.create(merchandise);
        if (id <= 0) {
            return MerchandiseManagementResult.failure("Không thể tạo mặt hàng.");
        }

        return MerchandiseManagementResult.success("Tạo mặt hàng thành công.", id);
    }

    public MerchandiseManagementResult update(int id, MerchandiseDraft draft) {
        Merchandise existing = merchandiseRepository.findById(id);
        if (existing == null) {
            return MerchandiseManagementResult.failure("Mặt hàng không tồn tại.");
        }

        String code = normalizeCode(draft.code());
        String name = normalizeText(draft.name());
        String unit = normalizeText(draft.unit());

        if (code.isEmpty()) {
            return MerchandiseManagementResult.failure("Mã hàng không được để trống.");
        }
        if (name.isEmpty()) {
            return MerchandiseManagementResult.failure("Tên mặt hàng không được để trống.");
        }
        if (unit.isEmpty()) {
            return MerchandiseManagementResult.failure("Đơn vị không được để trống.");
        }

        Merchandise byCode = merchandiseRepository.findByCode(code);
        if (byCode != null && byCode.getId() != id) {
            return MerchandiseManagementResult.failure("Mã hàng đã được sử dụng bởi mặt hàng khác.");
        }

        existing.setCode(code);
        existing.setName(name);
        existing.setUnit(unit);

        boolean updated = merchandiseRepository.update(existing);
        if (!updated) {
            return MerchandiseManagementResult.failure("Không thể cập nhật mặt hàng.");
        }

        return MerchandiseManagementResult.success("Cập nhật mặt hàng thành công.", id);
    }

    public MerchandiseManagementResult deactivate(int id) {
        Merchandise existing = merchandiseRepository.findById(id);
        if (existing == null) {
            return MerchandiseManagementResult.failure("Mặt hàng không tồn tại.");
        }
        if (!existing.isActive()) {
            return MerchandiseManagementResult.failure("Mặt hàng đã bị vô hiệu hóa.");
        }
        boolean updated = merchandiseRepository.setActive(id, false);
        if (!updated) {
            return MerchandiseManagementResult.failure("Không thể vô hiệu hóa mặt hàng.");
        }
        return MerchandiseManagementResult.success("Đã vô hiệu hóa mặt hàng.", id);
    }

    public MerchandiseManagementResult restore(int id) {
        Merchandise existing = merchandiseRepository.findById(id);
        if (existing == null) {
            return MerchandiseManagementResult.failure("Mặt hàng không tồn tại.");
        }
        if (existing.isActive()) {
            return MerchandiseManagementResult.failure("Mặt hàng đang hoạt động.");
        }
        boolean updated = merchandiseRepository.setActive(id, true);
        if (!updated) {
            return MerchandiseManagementResult.failure("Không thể khôi phục mặt hàng.");
        }
        return MerchandiseManagementResult.success("Đã khôi phục mặt hàng.", id);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim();
    }
}
