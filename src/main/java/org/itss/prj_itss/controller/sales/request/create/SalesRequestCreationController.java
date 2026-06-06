package org.itss.prj_itss.controller.sales.request.create;

import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.controller.shared.MerchandiseOptionDTO;
import org.itss.prj_itss.controller.shared.SalesRequestItemInput;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;
import org.itss.prj_itss.model.request.application.sales.create.CreateSalesRequestUseCase;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SalesRequestCreationController {

    private final SalesRequestQueryService queryService;
    private final CreateSalesRequestUseCase createUseCase;

    public SalesRequestCreationController(SalesRequestQueryService queryService, CreateSalesRequestUseCase createUseCase) {
        this.queryService = queryService;
        this.createUseCase = createUseCase;
    }

    // Cache được tải bất đồng bộ trên background thread để không block UI
    private volatile List<MerchandiseOptionDTO> merchandiseCache = null;
    private final ConcurrentHashMap<String, Integer> stockCache = new ConcurrentHashMap<>();
    private final AtomicBoolean cacheLoading = new AtomicBoolean(false);

    /**
     * Khởi động tải cache trên background thread.
     * Trả về ngay lập tức — không block JavaFX Application Thread.
     */
    public void preloadCacheAsync() {
        if (merchandiseCache != null || !cacheLoading.compareAndSet(false, true)) {
            return; // đã tải xong hoặc đang tải
        }
        Thread worker = new Thread(() -> {
            try {
                List<MerchandiseOption> options = queryService.findMerchandiseOptions();
                ConcurrentHashMap<String, Integer> newStock = new ConcurrentHashMap<>();
                for (MerchandiseOption option : options) {
                    int stock = queryService.getAvailableStock(option.code());
                    newStock.put(option.code().toLowerCase(), stock);
                }
                // Ghi vào volatile fields — ghi stockCache trước, merchandiseCache sau
                // để getMerchandiseOptionByCode không thấy cache ready khi stock chưa xong
                stockCache.clear();
                stockCache.putAll(newStock);
                merchandiseCache = options.stream().map(this::toDTO).toList();
            } catch (Exception e) {
                merchandiseCache = List.of();
            } finally {
                cacheLoading.set(false);
            }
        }, "merchandise-cache-loader");
        worker.setDaemon(true);
        worker.start();
    }

    public MerchandiseOptionDTO getMerchandiseOptionByCode(String code) {
        if (code == null) return null;
        List<MerchandiseOptionDTO> cache = merchandiseCache;
        if (cache == null) return null; // cache chưa sẵn sàng — trả về null, không block
        String trimmed = code.trim().toLowerCase();
        return cache.stream()
            .filter(m -> m.code().toLowerCase().equals(trimmed))
            .findFirst()
            .orElse(null);
    }

    public int getAvailableStock(String code) {
        if (code == null || merchandiseCache == null) return 0;
        return stockCache.getOrDefault(code.trim().toLowerCase(), 0);
    }

    public List<String> getAllMerchandiseCodes() {
        List<MerchandiseOptionDTO> cache = merchandiseCache;
        if (cache == null) return List.of();
        return cache.stream().map(MerchandiseOptionDTO::code).toList();
    }

    public List<String> suggestMerchandiseCodes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();
        List<MerchandiseOptionDTO> cache = merchandiseCache;
        if (cache == null) return List.of(); // cache chưa sẵn sàng — trả về rỗng, không block
        String lowerKeyword = keyword.toLowerCase().trim();
        return cache.stream()
            .map(MerchandiseOptionDTO::code)
            .filter(c -> c.toLowerCase().contains(lowerKeyword))
            .limit(5)
            .toList();
    }

    public ActionResult createRequest(List<SalesRequestItemInput> items) {
        if (items == null || items.isEmpty()) {
            return new ActionResult(false, "Cần ít nhất một mặt hàng để tạo yêu cầu.");
        }
        for (SalesRequestItemInput item : items) {
            if (item.quantity() == null || item.desiredDate() == null) {
                return new ActionResult(false, "Vui lòng điền đầy đủ thông tin hợp lệ.");
            }
            if (item.desiredDate().isBefore(java.time.LocalDate.now())) {
                return new ActionResult(false, "Ngày nhận không được nằm trong quá khứ.");
            }
        }
        try {
            List<SalesRequestItemSubmission> submissions = items.stream()
                .map(i -> new SalesRequestItemSubmission(i.merchandiseId(), i.quantity(), i.desiredDate()))
                .toList();
            createUseCase.createRequest(submissions, "");
            return new ActionResult(true, "Yêu cầu nhập hàng đã được gửi thành công.");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }

    private MerchandiseOptionDTO toDTO(MerchandiseOption m) {
        return new MerchandiseOptionDTO(m.id(), m.code(), m.name(), m.unit());
    }
}

