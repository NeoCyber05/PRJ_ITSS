package org.itss.prj_itss.model.merchandise.application;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MerchandiseManagementServiceTest {

    @Test
    void createRejectsEmptyCode() {
        FakeRepository repo = new FakeRepository();
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.create(new MerchandiseDraft("  ", "Tea", "box"));

        assertFalse(result.success());
        assertEquals("Mã hàng không được để trống.", result.message());
    }

    @Test
    void createRejectsEmptyName() {
        FakeRepository repo = new FakeRepository();
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.create(new MerchandiseDraft("M-01", "  ", "box"));

        assertFalse(result.success());
        assertEquals("Tên mặt hàng không được để trống.", result.message());
    }

    @Test
    void createRejectsEmptyUnit() {
        FakeRepository repo = new FakeRepository();
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.create(new MerchandiseDraft("M-01", "Tea", "  "));

        assertFalse(result.success());
        assertEquals("Đơn vị không được để trống.", result.message());
    }

    @Test
    void createRejectsDuplicateCode() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box"));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.create(new MerchandiseDraft("m-01", "Coffee", "bag"));

        assertFalse(result.success());
        assertEquals("Mã hàng đã tồn tại.", result.message());
    }

    @Test
    void createNormalizesCodeToUpperCase() {
        FakeRepository repo = new FakeRepository();
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.create(new MerchandiseDraft("m-02", "Coffee", "bag"));

        assertTrue(result.success());
        assertEquals("M-02", repo.findById(result.merchandiseId()).getCode());
    }

    @Test
    void updateRejectsMissingId() {
        FakeRepository repo = new FakeRepository();
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.update(99, new MerchandiseDraft("M-99", "Tea", "box"));

        assertFalse(result.success());
        assertEquals("Mặt hàng không tồn tại.", result.message());
    }

    @Test
    void updateRejectsDuplicateCode() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box"));
        repo.add(new Merchandise(2, "M-02", "Coffee", "bag"));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.update(1, new MerchandiseDraft("m-02", "Green Tea", "box"));

        assertFalse(result.success());
        assertEquals("Mã hàng đã được sử dụng bởi mặt hàng khác.", result.message());
    }

    @Test
    void updateAllowsSameCode() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box"));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.update(1, new MerchandiseDraft("m-01", "Green Tea", "bag"));

        assertTrue(result.success());
        assertEquals("Green Tea", repo.findById(1).getName());
        assertEquals("bag", repo.findById(1).getUnit());
    }

    @Test
    void deactivateSetsActiveToFalse() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box", true));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.deactivate(1);

        assertTrue(result.success());
        assertFalse(repo.findById(1).isActive());
    }

    @Test
    void deactivateRejectsAlreadyInactive() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box", false));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.deactivate(1);

        assertFalse(result.success());
        assertEquals("Mặt hàng đã bị vô hiệu hóa.", result.message());
    }

    @Test
    void restoreSetsActiveToTrue() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box", false));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.restore(1);

        assertTrue(result.success());
        assertTrue(repo.findById(1).isActive());
    }

    @Test
    void restoreRejectsAlreadyActive() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box", true));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        MerchandiseManagementResult result = service.restore(1);

        assertFalse(result.success());
        assertEquals("Mặt hàng đang hoạt động.", result.message());
    }

    @Test
    void findActiveReturnsOnlyActiveRows() {
        FakeRepository repo = new FakeRepository();
        repo.add(new Merchandise(1, "M-01", "Tea", "box", true));
        repo.add(new Merchandise(2, "M-02", "Coffee", "bag", false));
        MerchandiseManagementService service = new MerchandiseManagementService(repo);

        List<Merchandise> active = service.findActive();

        assertEquals(1, active.size());
        assertEquals(1, active.get(0).getId());
    }

    static final class FakeRepository implements MerchandiseRepository {
        final Map<Integer, Merchandise> merchandise = new LinkedHashMap<>();
        final AtomicInteger nextId = new AtomicInteger(1);

        void add(Merchandise m) {
            merchandise.put(m.getId(), m);
        }

        @Override
        public List<Merchandise> findAll() {
            return new ArrayList<>(merchandise.values());
        }

        @Override
        public List<Merchandise> findActive() {
            return merchandise.values().stream()
                .filter(Merchandise::isActive)
                .toList();
        }

        @Override
        public Merchandise findById(int id) {
            return merchandise.get(id);
        }

        @Override
        public Merchandise findByCode(String code) {
            String normalized = code == null ? "" : code.trim().toUpperCase(java.util.Locale.ROOT);
            return merchandise.values().stream()
                .filter(m -> m.getCode().equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
        }

        @Override
        public int countAll() {
            return merchandise.size();
        }

        @Override
        public int create(Merchandise m) {
            int id = nextId.getAndIncrement();
            m.setId(id);
            merchandise.put(id, m);
            return id;
        }

        @Override
        public boolean update(Merchandise m) {
            if (!merchandise.containsKey(m.getId())) {
                return false;
            }
            merchandise.put(m.getId(), m);
            return true;
        }

        @Override
        public boolean setActive(int merchandiseId, boolean active) {
            Merchandise m = merchandise.get(merchandiseId);
            if (m == null) return false;
            m.setActive(active);
            return true;
        }
    }
}
