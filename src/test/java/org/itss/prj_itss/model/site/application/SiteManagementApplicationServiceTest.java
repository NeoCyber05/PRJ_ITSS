package org.itss.prj_itss.model.site.application;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteAccountProvisioningPort;
import org.itss.prj_itss.model.site.application.port.SiteCommandRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.domain.Site;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SiteManagementApplicationServiceTest {

    // ---- Fakes ----

    static class FakeSiteCommandRepository implements SiteCommandRepository, SiteRepository, InventoryRepository {
        final Set<String> existingSiteCodes = new HashSet<>();
        final Map<Integer, Site> sites = new HashMap<>();
        final List<SiteDraft> createdSites = new ArrayList<>();
        int nextId = 100;

        // SiteCommandRepository
        @Override
        public int createSite(SiteDraft draft) {
            createdSites.add(draft);
            return nextId++;
        }

        @Override
        public void updateSite(int siteId, SiteDraft draft) {}

        @Override
        public boolean existsBySiteCode(String siteCode) {
            return existingSiteCodes.stream().anyMatch(c -> c.equalsIgnoreCase(siteCode));
        }

        @Override
        public boolean existsBySiteCodeExceptId(String siteCode, int siteId) {
            return false;
        }

        // SiteRepository
        @Override
        public List<Site> findAll() {
            return List.copyOf(sites.values());
        }

        @Override
        public List<Site> findAvailableForMerchandiseIds(List<Integer> ids) {
            return List.of();
        }

        @Override
        public Site findById(int id) {
            return sites.get(id);
        }

        @Override
        public Site findBySiteCode(String code) {
            return null;
        }

        @Override
        public int countAll() {
            return sites.size();
        }

        // InventoryRepository
        @Override
        public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
            return Map.of();
        }

        @Override
        public int getStockQuantity(int siteId, int merchandiseId) {
            return 0;
        }

        @Override
        public int getTotalStock(int merchandiseId) {
            return 0;
        }

        @Override
        public int countMerchandiseAtSite(int siteId) {
            return 0;
        }
    }

    static class FakeSiteAccountProvisioningPort implements SiteAccountProvisioningPort {
        int createdSiteId = -1;
        boolean usernameExistsResult = false;

        @Override
        public boolean usernameExists(String username) {
            return usernameExistsResult;
        }

        @Override
        public int createSiteAccount(SiteAccountDraft draft, int siteId) {
            this.createdSiteId = siteId;
            return 42;
        }
    }

    // ---- Helpers ----

    private SiteManagementApplicationService newService(FakeSiteCommandRepository siteRepository) {
        return newService(siteRepository, new FakeSiteAccountProvisioningPort());
    }

    private SiteManagementApplicationService newService(
            FakeSiteCommandRepository siteRepository,
            SiteAccountProvisioningPort accountPort) {
        SiteUseCase siteUseCase = new SiteUseCase(siteRepository, siteRepository);
        MerchandiseUseCase MerchandiseUseCase = new MerchandiseUseCase(stubMerchandiseRepo());
        TransactionRunner transactionRunner = callback -> callback.execute();
        return new SiteManagementApplicationService(
            siteUseCase,
            MerchandiseUseCase,
            siteRepository,
            accountPort,
            transactionRunner
        );
    }

    private MerchandiseRepository stubMerchandiseRepo() {
        return new MerchandiseRepository() {
            @Override public List<Merchandise> findAll() { return List.of(); }
            @Override public List<Merchandise> findActive() { return List.of(); }
            @Override public Merchandise findById(int id) { return null; }
            @Override public Merchandise findByCode(String code) { return null; }
            @Override public int countAll() { return 0; }
            @Override public int create(Merchandise merchandise) { return -1; }
            @Override public boolean update(Merchandise merchandise) { return false; }
            @Override public boolean setActive(int merchandiseId, boolean active) { return false; }
        };
    }

    // ---- Tests ----

    @Test
    void createSiteRejectsDuplicateSiteCode() {
        FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
        siteRepository.existingSiteCodes.add("TOKYO");
        SiteManagementApplicationService service = newService(siteRepository);

        SiteManagementResult result = service.createSite(
            new SiteDraft("TOKYO", "Tokyo Import Site", "Japan partner", 14, 3)
        );

        assertFalse(result.success());
        assertTrue(result.message().contains("Mã site đã tồn tại"));
    }

    @Test
    void createSiteSucceedsForUniqueSiteCode() {
        FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
        SiteManagementApplicationService service = newService(siteRepository);

        SiteManagementResult result = service.createSite(
            new SiteDraft("BERLIN", "Berlin Import Site", "Germany partner", 10, 2)
        );

        assertTrue(result.success());
        assertNotNull(result.siteId());
        assertEquals(1, siteRepository.createdSites.size());
        assertEquals("BERLIN", siteRepository.createdSites.get(0).siteCode());
    }

    @Test
    void createSiteRejectsBlankSiteCode() {
        FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
        SiteManagementApplicationService service = newService(siteRepository);

        SiteManagementResult result = service.createSite(
            new SiteDraft("", "Some Site", null, null, null)
        );

        assertFalse(result.success());
        assertTrue(result.message().contains("Mã site không được để trống"));
    }

    @Test
    void provisionSiteAccountRejectsMissingSite() {
        FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
        // No site with id=99 in the map
        SiteManagementApplicationService service = newService(siteRepository);

        SiteManagementResult result = service.provisionSiteAccount(99,
            new SiteAccountDraft("user1", "pass1", "User One"));

        assertFalse(result.success());
        assertTrue(result.message().contains("Site không tồn tại"));
    }

    @Test
    void provisionSiteAccountRejectsDuplicateUsername() {
        FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo Import Site", "Japan partner", 14, 3));
        FakeSiteAccountProvisioningPort accountPort = new FakeSiteAccountProvisioningPort();
        accountPort.usernameExistsResult = true;
        SiteManagementApplicationService service = newService(siteRepository, accountPort);

        SiteManagementResult result = service.provisionSiteAccount(5,
            new SiteAccountDraft("existing-user", "pass", "Existing User"));

        assertFalse(result.success());
        assertTrue(result.message().contains("Tên đăng nhập đã tồn tại"));
    }

    @Test
    void provisionSiteAccountUsesSiteId() {
        FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo Import Site", "Japan partner", 14, 3));
        FakeSiteAccountProvisioningPort accountPort = new FakeSiteAccountProvisioningPort();
        SiteManagementApplicationService service = newService(siteRepository, accountPort);

        SiteManagementResult result = service.provisionSiteAccount(5,
            new SiteAccountDraft("tokyo-site", "secret", "Tokyo Site User"));

        assertTrue(result.success());
        assertEquals(5, accountPort.createdSiteId);
        // Returned id is the fake account id (42)
        assertEquals(42, result.siteId());
    }
}
