# JavaFX + SOLID Design Guide

## Mục tiêu
Tài liệu này giúp:
- Hiểu và áp dụng nguyên lý SOLID trong JavaFX
- Thiết kế cấu trúc folder rõ ràng, dễ maintain
- Tránh các lỗi phổ biến khi phát triển ứng dụng

---

# I. Tổng quan SOLID (áp dụng thực tế)

## 1. Single Responsibility Principle (SRP)
**Một class chỉ nên có một lý do để thay đổi**

### Áp dụng trong JavaFX
Không nhồi toàn bộ logic vào Controller.

❌ Sai:
- Controller xử lý UI + validate + business logic + database

✅ Đúng:
- Controller: xử lý UI
- Service: xử lý nghiệp vụ
- Repository: truy xuất dữ liệu
- Validator: kiểm tra dữ liệu

---

## 2. Open/Closed Principle (OCP)
**Mở để mở rộng, đóng để sửa đổi**

### Áp dụng
Tránh `if-else` theo loại logic.

❌ Sai:
```java
if(type.equals("NORMAL")) { ... }
else if(type.equals("EXPRESS")) { ... }
````

✅ Đúng:

```java
public interface ShippingStrategy {
    double calculate(Order order);
}
```

→ Thêm class mới khi cần mở rộng

---

## 3. Liskov Substitution Principle (LSP)

**Class con phải thay thế được class cha**

### Nguyên tắc

* Không override phá logic
* Không throw exception bất hợp lý

→ Tránh kế thừa sai mục đích

---

## 4. Interface Segregation Principle (ISP)

**Không ép class phụ thuộc vào thứ nó không dùng**

❌ Sai:

```java
interface AppService {
    void create();
    void update();
    void export();
    void sendEmail();
}
```

✅ Đúng:

* Tách nhỏ interface theo chức năng

---

## 5. Dependency Inversion Principle (DIP)

**Phụ thuộc abstraction, không phụ thuộc implementation**

❌ Sai:

```java
new MySQLRepository()
```

✅ Đúng:

```java
interface Repository {}
```

→ Inject dependency qua constructor

---

# II. Kiến trúc JavaFX chuẩn

## Flow chuẩn

```
FXML → Controller → Service → Repository → Database
                      ↓
                  Validator
```

---

## Vai trò từng layer

### Controller

* Nhận event từ UI
* Gọi Service
* Update UI

### Service

* Xử lý nghiệp vụ
* Điều phối logic

### Repository

* Làm việc với DB / API / file

### Model

* Entity/domain object

### DTO / ViewModel

* Dữ liệu cho UI

---

# III. Cách tổ chức folder

## 1. Layer-based (dễ học)

```
controller/
service/
repository/
model/
dto/
validator/
util/
```

### Ưu điểm

* Dễ hiểu
* Dễ triển khai

### Nhược điểm

* Khó scale khi project lớn

---

## 2. Feature-based (khuyến nghị)

```
order/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
├── validator/

customer/
product/
```

### Ưu điểm

* Gom theo nghiệp vụ
* Scale tốt
* Dễ làm việc nhóm

---

## 3. Hybrid (khuyến nghị nhất)

```
com.app
├── common/
│   ├── config/
│   ├── util/
│   └── exception/
│
├── order/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   ├── dto/
│   └── validator/
```

---

# IV. Ví dụ code chuẩn

## Repository

```java
public interface OrderRepository {
    void save(Order order);
}
```

```java
public class JdbcOrderRepository implements OrderRepository {
    public void save(Order order) {
        // JDBC
    }
}
```

---

## Validator

```java
public class OrderValidator {
    public void validate(Order order) {
        if(order.getName().isBlank()) {
            throw new IllegalArgumentException("Invalid");
        }
    }
}
```

---

## Service

```java
public class OrderService {
    private final OrderRepository repository;
    private final OrderValidator validator;

    public OrderService(OrderRepository repository, OrderValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public void create(Order order) {
        validator.validate(order);
        repository.save(order);
    }
}
```

---

## Controller

```java
public class OrderController {

    private OrderService service;

    public void setService(OrderService service) {
        this.service = service;
    }

    public void handleCreate() {
        Order order = new Order();
        service.create(order);
    }
}
```

---

# V. Anti-pattern (cần tránh)

## 1. God Controller

* Controller > 500 dòng
* Có SQL, business logic

## 2. Util class lạm dụng

* CommonUtils chứa mọi thứ

## 3. Kế thừa sai

* BaseController quá phình to

## 4. Hard dependency

* new trực tiếp repository/service

---

# VI. Checklist tự review

## Controller

* Có logic nghiệp vụ không?
* Có gọi DB không?

## Service

* Có phụ thuộc interface không?

## Folder

* Có dễ tìm file không?
* Có bị rải logic không?


