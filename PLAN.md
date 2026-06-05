# Revised Plan: Shared Suggest Engine + Objective Strategy

## Summary
- “Site ưu tiên” được hiểu lại là “chỉ chọn các site đó”, không phải rank cao hơn.
- Dùng `AllocationObjective` làm Strategy cho tiêu chí tốt-xấu.
- Dùng chung một suggest engine để tránh `OptimalSuggest` và `AllSuggest` drift logic.
- Rename bỏ hậu tố `Algo`.

## Key Changes
- Rename semantic (CHỐT tên: `selectedSiteIds`, không dùng `includedSiteIds` — khớp UI “chọn”):
  - `prioritySiteIds` -> `selectedSiteIds` ở mọi tầng: `SiteFilterModel`, `SiteFilterController`, `SiteFilterView` (`getPrioritySiteIds`), `RequestProcessingLayoutView:146`, `RequestProcessingSession`, `AllocationControl`, `AllSuggestAlgo`.
  - UI text đổi từ “Site ưu tiên sẽ được sắp xếp lên trên” sang nghĩa “Chỉ phân bổ từ site đã chọn”
  - Đổi luôn các chuỗi UI còn lại, không chỉ một chỗ: `SiteFilterView:165` “Chưa chọn site ưu tiên”, badge `★`, summary `:211` “ưu tiên”.
- Thêm `SiteSelectionScope`:
  - nhận `allSites`, `excludedSiteIds`, `selectedSiteIds`
  - nếu `selectedSiteIds` không rỗng: chỉ giữ site nằm trong selected và không excluded
  - nếu rỗng: giữ tất cả site không excluded
  - **Guard bắt buộc**: filter phải check `selectedSiteIds.isEmpty()` TRƯỚC. Viết kiểu `selectedSiteIds.contains(id)` trần (không guard) sẽ cho 0 candidate khi user không chọn → `buildItemVariants` trả `List.of()` → `collectVariantsForAllItems` trả null → `buildSuggestedPlans` trả `List.of()` → UI rỗng im lặng, không báo lỗi. Đây là case “không bấm site nào”.
- Thêm `AllocationObjective` Strategy:
  - `pickTransport(...)`
  - `siteComparator(...)`
  - `itemVariantComparator(...)`
  - `planComparator(...)`
- Đổi `AllocationPolicy` thành `FastDeliveryObjective`.
- Rename:
  - `OptimalSuggestAlgo` -> `OptimalSuggest`
  - `AllSuggestAlgo` -> `AllSuggest`
- Thêm `AllocationSuggestEngine` làm engine chung:
  - nhận `SiteSelectionScope`, `AllocationObjective`
  - sinh candidate sites, item variants, plan combinations
  - rank bằng objective
  - expose `suggestMany(limit, maxItemVariants)`
- `AllSuggest` gọi `engine.suggestMany(limit, maxItemVariants)`.
- `OptimalSuggest` gọi cùng engine nhưng lấy top 1 rồi trả `allocationsByItem`; nếu cần giữ hiệu năng greedy hiện tại, `OptimalSuggest` vẫn có thể dùng greedy path riêng nhưng phải dùng chung `SiteSelectionScope` và `AllocationObjective`.

- **Đường optimal hiện KHÔNG nhận selected** — phải sửa cross-cut, không ẩn dưới “dùng chung scope”. Touch points để optimal tôn trọng `selectedSiteIds`:
  - `AllocationSuggester.buildOptimalDrafts(...)` — thêm tham số `selectedSiteIds`.
  - `DefaultAllocationSuggester.buildOptimalDrafts:28` — truyền xuống.
  - `OptimalSuggestAlgo` ctor + `buildCandidateSites:42` — filter qua `SiteSelectionScope` thay vì chỉ `excludedSiteIds`.
  - `AllocationControl` — đang giữ `prioritySiteIds`, đổi field + truyền vào optimal path.
  - `RequestProcessingSession.createAllocationControl:340` + call site optimize — truyền `selectedSiteIds`.
  - Nếu bỏ qua bước này: optimal lờ selection trong khi all-suggest tôn trọng → kết quả lệch nhau, vi phạm test line “cả optimal và all-suggest chỉ dùng site đã chọn”.

- **`prioritySiteCount` thành dead/wrong sau khi đổi nghĩa.** Flow: `AllSuggestAlgo.assemblePlan:381` -> `SuggestedPlan:14` -> `SuggestedPlanView:8` -> `RequestProcessingSession:186` -> UI. Sau flip “include only”, mọi site trong plan đều là selected nên `prioritySiteCount == siteCount` (hoặc 0 khi rỗng) → vô nghĩa. Quyết định: **bỏ field** xuyên chuỗi `SuggestedPlan`/`SuggestedPlanView`/session:186, hoặc định nghĩa lại. Không để lửng.

- **UI cảnh báo site bị loại khi có selection.** `SiteFilterModel.sortedSites:107` đẩy priority lên đầu list — vô nghĩa với nghĩa mới, bỏ hoặc đổi. Site “visible nhưng không selected” giờ bị loại khỏi phân bổ khi có ≥1 selected, nhưng vẫn hiện bình thường trong list → bẫy UX. Thêm dấu hiệu “chỉ N site đã chọn được dùng” / mờ site chưa chọn.

- **Selected + excluded chồng vai khi có selection.** Khi selected không rỗng, excluded thừa (site không-selected đã bị loại). Cân nhắc ẩn UI exclude lúc selection đang bật để tránh rối.

## Design Decisions
- Không dùng registry vì chưa có UI/runtime chọn thuật toán.
- `AllocationObjective` là Strategy thật sự sau refactor; hiện tại `AllocationPolicy` mới chỉ là policy một phần.
- Nếu ưu tiên giữ hiệu năng, giữ `OptimalSuggest` greedy riêng; nếu ưu tiên consistency, lấy top 1 từ engine chung. Plan mặc định chọn consistency vì mục tiêu của bạn là dễ mở rộng.

## Test Plan
- **Test empty selection (case “không bấm site nào”) — BẮT BUỘC**: `selectedSiteIds` rỗng → cả optimal lẫn all-suggest cho kết quả y hệt hiện tại (mọi site không excluded). Chống regression Gap-2 (kết quả rỗng im lặng).
- Test selected site: khi `selectedSiteIds` có giá trị, cả optimal và all-suggest chỉ dùng các site đó.
- Test optimal + all-suggest đồng nhất tập site khi cùng `selectedSiteIds` (chống lệch do optimal quên nhận selected).
- Test excluded site vẫn loại bỏ kể cả khi selected.
- Test `AllSuggest` limit top N.
- Test `OptimalSuggest` lấy cùng top plan đầu tiên từ engine trong case đơn giản.
- Test fake `AllocationObjective` đổi thứ tự ranking mà không sửa `AllSuggest`/`OptimalSuggest`.
- Chạy `.\mvnw.cmd -q test`; `MvcDependencyTest` phải xanh.

## Assumptions
- Không thêm cost.
- Không thêm UI chọn thuật toán mới.
- Giữ MVC: thuật toán nằm trong Model, controller/view chỉ truyền selected/excluded site ids.
