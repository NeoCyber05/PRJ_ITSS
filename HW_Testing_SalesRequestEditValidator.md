# Ke hoach kiem thu SalesRequestEditValidator

## 1. Mo ta module duoc kiem thu

### 1.1. Module

Module duoc chon la `SalesRequestEditValidator` trong chuc nang Update Sales Request.

Pham vi kiem thu tap trung vao hai phuong thuc:

- `validate(SalesRequestEditDraft draft, LocalDate today)`
- `validateForSubmission(SalesRequestEditDraft draft, LocalDate today)`

### 1.2. Input

Input chinh la mot `SalesRequestEditDraft`, trong do co:

- `requestId`: ma dinh danh cua sales request.
- `requestCode`: ma hien thi cua sales request.
- `items`: danh sach cac dong hang can cap nhat.

Moi dong hang la mot `SalesRequestEditItemDraft`, gom:

- `lineId`: ma dong hang tren form.
- `merchandise`: mat hang duoc chon, kieu `MerchandiseOption`.
- `quantity`: so luong can yeu cau, kieu `BigDecimal`.
- `desiredDate`: ngay mong muon nhan hang, kieu `LocalDate`.

Tham so `today` duoc dung lam moc ngay hien tai de kiem tra `desiredDate`.

### 1.3. Output

Voi `validate(...)`, output la `SalesRequestEditValidationResult`, trong do co danh sach `SalesRequestEditFieldViolation`.

Voi `validateForSubmission(...)`:

- Neu draft hop le: tra ve `ValidatedSalesRequestEditDraft`.
- Neu draft khong hop le: nem `SalesRequestEditValidationException`.

### 1.4. Quy tac nghiep vu

Validator hien tai ap dung cac quy tac sau:

- Danh sach mat hang khong duoc rong.
- Moi dong hang phai chon `merchandise`.
- Khong duoc chon trung mot mat hang tren nhieu dong.
- `quantity` khong duoc `null` va phai lon hon `0`.
- `desiredDate` khong duoc `null` va khong duoc truoc ngay `today`.

Luu y: code hien tai khong co gioi han tren cho `quantity`, vi vay tai lieu nay khong tao test case cho bien tren nhu `1000` hay `1001`.

## 2. Thiet ke test case theo hop den

### 2.1. Co so thiet ke

Kiem thu hop den khong dua vao cau truc `if/else` ben trong code, ma dua tren dac ta dau vao va dau ra mong doi.

Hai ky thuat duoc ap dung:

- Phan vung tuong duong: chia input thanh cac vung hop le va khong hop le.
- Phan tich gia tri bien: tap trung vao cac gia tri sat ranh gioi, vi day la noi de xay ra loi.

Du lieu mau dung trong bang:

- `today = 2026-05-25`
- `M1 = MerchandiseOption(10, "MH-001", "Item 1", "box")`
- `M2 = MerchandiseOption(11, "MH-002", "Item 2", "box")`

### 2.2. Phan vung tuong duong

| Dau vao | Vung hop le | Vung khong hop le |
|---|---|---|
| `items` | Co it nhat 1 dong hang | Danh sach rong |
| `merchandise` | Khac `null` va khong bi trung | `null` hoac bi trung voi dong khac |
| `quantity` | Khac `null` va `> 0` | `null`, `< 0`, `= 0` |
| `desiredDate` | Bang `today` hoac sau `today` | `null` hoac truoc `today` |
| `validateForSubmission` | Draft hop le | Draft co it nhat 1 loi validate |

### 2.3. Bang test case hop den

| Test Case ID | Input Data | Expected Output | Ky thuat ap dung |
|---|---|---|---|
| BB-01 | 1 item: `merchandise=M1`, `quantity=1`, `desiredDate=2026-05-25` | `validForm() = true`, khong co violation | Vung hop le |
| BB-02 | `items=[]` | Co violation field `items`: phai co it nhat 1 mat hang | Vung khong hop le cua danh sach |
| BB-03 | 1 item: `merchandise=null`, `quantity=1`, `desiredDate=2026-05-25` | Co violation field `merchandise` | Vung khong hop le cua merchandise |
| BB-04 | 2 item cung `merchandise=M1`, `quantity=1`, `desiredDate=2026-05-25` | Co violation field `merchandise` do chon trung mat hang | Phan vung trung lap merchandise |
| BB-05 | 1 item: `merchandise=M1`, `quantity=null`, `desiredDate=2026-05-25` | Co violation field `quantity` | Vung khong hop le: quantity null |
| BB-06 | 1 item: `merchandise=M1`, `quantity=-1`, `desiredDate=2026-05-25` | Co violation field `quantity` | Gia tri bien duoi khong hop le |
| BB-07 | 1 item: `merchandise=M1`, `quantity=0`, `desiredDate=2026-05-25` | Co violation field `quantity` | Gia tri bien tai ranh gioi khong hop le |
| BB-08 | 1 item: `merchandise=M1`, `quantity=0.01`, `desiredDate=2026-05-25` | Khong co violation `quantity` | Gia tri bien duoi hop le gan 0 |
| BB-09 | 1 item: `merchandise=M1`, `quantity=1`, `desiredDate=null` | Co violation field `desiredDate` | Vung khong hop le: date null |
| BB-10 | 1 item: `merchandise=M1`, `quantity=1`, `desiredDate=2026-05-24` | Co violation field `desiredDate` | Gia tri bien truoc ngay hien tai |
| BB-11 | 1 item: `merchandise=M1`, `quantity=1`, `desiredDate=2026-05-25` | Khong co violation `desiredDate` | Gia tri bien hop le tai `today` |
| BB-12 | 1 item: `merchandise=M1`, `quantity=1`, `desiredDate=2026-05-26` | Khong co violation `desiredDate` | Vung hop le sau `today` |
| BB-13 | 1 item: `merchandise=null`, `quantity=0`, `desiredDate=2026-05-24` | Co 3 violation: `merchandise`, `quantity`, `desiredDate` | Ket hop nhieu vung khong hop le |
| BB-14 | Goi `validateForSubmission` voi draft cua BB-01 | Tra ve `ValidatedSalesRequestEditDraft` | Vung hop le cua submission |
| BB-15 | Goi `validateForSubmission` voi draft cua BB-13 | Nem `SalesRequestEditValidationException` | Vung khong hop le cua submission |

## 3. Thiet ke test case theo hop trang C1

### 3.1. Phan tich cau truc nhanh

Kiem thu hop trang C1 yeu cau moi nhanh dieu kien True va False phai duoc chay qua it nhat mot lan.

Trong `SalesRequestEditValidator`, cac nhanh chinh gom:

| Ma nhanh | Dieu kien | Huong can bao phu |
|---|---|---|
| B1 | `draft.items().isEmpty()` | True va False |
| B2 | `merchandise == null` | True va False |
| B3 | `selectionPolicy.isDuplicateSelection(...)` | True va False |
| B4a | `quantity == null` | True va False |
| B4b | `quantity.compareTo(BigDecimal.ZERO) <= 0` | True va False, khi `quantity != null` |
| B5a | `desiredDate == null` | True va False |
| B5b | `desiredDate.isBefore(today)` | True va False, khi `desiredDate != null` |
| B6 | `!validationResult.validForm()` trong `validateForSubmission` | True va False |

Ghi chu: B4 va B5 la dieu kien ket hop bang toan tu `||`, nen co tinh chat short-circuit. Neu ve trai la True thi ve phai khong duoc thuc thi. Vi vay, de bao phu C1 chat che, can co ca test case `null` va test case non-null nhung sai gia tri.

### 3.2. Tap test case toi uu cho C1

| Test Case ID | Input Data | Nhanh di qua | Ket qua tra ve |
|---|---|---|---|
| WB-01 | `items=[]` | B1=True | `validate(...)` tra ve violation field `items` va return som |
| WB-02 | 1 item hop le: `M1`, `quantity=1`, `desiredDate=2026-05-25`; goi them `validateForSubmission(...)` | B1=False, B2=False, B3=False, B4a=False, B4b=False, B5a=False, B5b=False, B6=False | `validate(...)` hop le; `validateForSubmission(...)` tra ve `ValidatedSalesRequestEditDraft` |
| WB-03 | 1 item: `merchandise=null`, `quantity=null`, `desiredDate=null`; goi them `validateForSubmission(...)` | B1=False, B2=True, B4a=True, B5a=True, B6=True | Co violation `merchandise`, `quantity`, `desiredDate`; `validateForSubmission(...)` nem `SalesRequestEditValidationException` |
| WB-04 | 2 item cung `M1`, trong do item can check co `quantity=0`, `desiredDate=2026-05-24` | B1=False, B2=False, B3=True, B4a=False, B4b=True, B5a=False, B5b=True | Co violation trung merchandise, quantity khong hop le, desiredDate truoc `today` |

### 3.3. Ket luan ve C1

Bon test case WB-01 den WB-04 bao phu du cac nhanh True va False cua cac dieu kien quan trong trong validator:

- B1 duoc bao phu True o WB-01 va False o WB-02/WB-03/WB-04.
- B2 duoc bao phu True o WB-03 va False o WB-02/WB-04.
- B3 duoc bao phu True o WB-04 va False o WB-02.
- B4a duoc bao phu True o WB-03 va False o WB-02/WB-04.
- B4b duoc bao phu True o WB-04 va False o WB-02.
- B5a duoc bao phu True o WB-03 va False o WB-02/WB-04.
- B5b duoc bao phu True o WB-04 va False o WB-02.
- B6 duoc bao phu True o WB-03 va False o WB-02.

Tap test case hop den co so luong lon hon vi muc tieu cua hop den la kiem tra cac vung du lieu va gia tri bien. Tap test case hop trang duoc rut gon hon vi muc tieu la dat do bao phu nhanh C1.
