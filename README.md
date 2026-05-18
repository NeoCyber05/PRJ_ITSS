# PRJ_ITSS

Ứng dụng JavaFX quản lý quy trình đặt hàng nhập khẩu, xử lý yêu cầu, phân bổ đơn hàng và xác nhận nhập kho.

## Yêu cầu

- Java 17
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Cấu hình database trong `src/main/resources/db.properties`

## Cách chạy

### Windows

```powershell
.\mvnw.cmd clean javafx:run
```

### macOS / Linux

```bash
./mvnw clean javafx:run
```

## Ghi chú

- Dữ liệu chính được đọc từ Supabase PostgreSQL.
- Nếu dùng phần kho, cấu hình thêm các biến `WAREHOUSE_DB_*` hoặc file `warehouse-db.properties`.
- Mã nguồn giao diện nằm trong `src/main/java` và `src/main/resources`.

## Package dependency diagram - Clean Architecture

```mermaid
flowchart LR
    Config["common.config\nApplicationContext"]
    CommonData["common.data\nJDBC support"]
    Db["db\nDatabaseConnection"]

    subgraph Features["feature modules"]
        Presentation["*.presentation\nJavaFX controllers / popups"]
        Application["*.application\nuse cases / queries / view models"]
        Domain["*.domain\nentities / policies / domain services"]
        Ports["*.application.port\nrepository / gateway ports"]
        Persistence["*.infrastructure.persistence\nJDBC adapters"]
    end

    Presentation --> Application
    Application --> Domain
    Application --> Ports
    Persistence -. implements .-> Ports
    Persistence --> Domain
    Persistence --> CommonData
    CommonData --> Config
    Config --> Db
    Config -. wires .-> Application
    Config -. wires .-> Persistence
```

Quy tac kien truc hien tai:

- `domain` khong phu thuoc JavaFX, SQL, `application`, `presentation`, hoac `infrastructure`.
- `application` khong phu thuoc JavaFX, SQL, `presentation`, hoac concrete adapter trong `infrastructure`.
- `presentation` khong import truc tiep persistence adapter.
- Repository/gateway interface nam trong `*.application.port`; JDBC adapter nam trong `*.infrastructure.persistence`.
- `ApplicationContext` la composition root: tao adapter, tao use case, va wire dependency.