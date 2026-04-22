BEGIN;

-- Dữ liệu mẫu phát triển/trình diễn cho lược đồ trong supabase/migrations/20260422054921_remote_schema.sql.
-- Yêu cầu số 1 được cố ý giữ ở trạng thái "Chờ xử lý" vì RequestProcessingView hiện đang
-- mặc định mở request_id = 1.

INSERT INTO public.role (id, name)
VALUES
    (1, 'Quản trị viên'),
    (2, 'Phòng Kinh doanh'),
    (3, 'Bộ phận Đặt hàng quốc tế'),
    (4, 'Đối tác điểm bán'),
    (5, 'Quản lý kho')
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name;

INSERT INTO public.site (id, site_code, name, description, ship_delivery_days, air_delivery_days)
VALUES
    (1, 'SGP-HUB', 'Trung tâm Singapore', 'Nhà cung cấp chính cho hàng điện tử tiêu dùng tại Đông Nam Á.', 6, 2),
    (2, 'SHA-TECH', 'Trung tâm Công nghệ Thượng Hải', 'Nhà cung cấp sản lượng lớn với tồn kho ổn định cho máy tính xách tay và phụ kiện.', 8, 3),
    (3, 'LAX-WEST', 'Khu Tây Los Angeles', 'Điểm tập kết tại Mỹ, phù hợp cho các lô hàng gấp bằng đường hàng không.', 20, 4),
    (4, 'HKG-FAST', 'Chuyển phát nhanh Hồng Kông', 'Đối tác xử lý nhanh cho các mặt hàng cao cấp và cần gấp.', 10, 2)
ON CONFLICT (id) DO UPDATE
SET site_code = EXCLUDED.site_code,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    ship_delivery_days = EXCLUDED.ship_delivery_days,
    air_delivery_days = EXCLUDED.air_delivery_days;

INSERT INTO public.merchandise (id, code, name, unit)
VALUES
    (1, 'IP15PM', 'iPhone 15 Pro Max', 'chiếc'),
    (2, 'MBA13M4', 'MacBook Air 13 M4', 'chiếc'),
    (3, 'APP2USBC', 'AirPods Pro 2 USB-C', 'chiếc'),
    (4, 'IPADAIR11', 'iPad Air 11 M2', 'chiếc'),
    (5, 'DJIOM6', 'DJI Osmo Mobile 6', 'chiếc'),
    (6, 'JBLCHG5', 'JBL Charge 5', 'chiếc')
ON CONFLICT (id) DO UPDATE
SET code = EXCLUDED.code,
    name = EXCLUDED.name,
    unit = EXCLUDED.unit;

INSERT INTO public.site_inventory (site_id, merchandise_id, stock_quantity)
VALUES
    (1, 1, 80), (1, 2, 35), (1, 3, 120), (1, 4, 40), (1, 5, 18), (1, 6, 25),
    (2, 1, 50), (2, 2, 60), (2, 3, 45), (2, 4, 20), (2, 5, 40), (2, 6, 15),
    (3, 1, 15), (3, 2, 12), (3, 3, 20), (3, 4, 8),  (3, 5, 30), (3, 6, 50),
    (4, 1, 40), (4, 2, 10), (4, 3, 80), (4, 4, 55), (4, 5, 12), (4, 6, 35)
ON CONFLICT (site_id, merchandise_id) DO UPDATE
SET stock_quantity = EXCLUDED.stock_quantity;

INSERT INTO public.account (id, username, password, full_name, status, role_id, site_id)
VALUES
    (1, 'admin', 'admin123', 'Quản trị viên hệ thống', 'ACTIVE', 1, NULL),
    (2, 'sales01', 'sales123', 'Nhân viên kinh doanh mẫu', 'ACTIVE', 2, NULL),
    (3, 'import01', 'import123', 'Điều phối nhập hàng', 'ACTIVE', 3, NULL),
    (4, 'site_sg', 'site123', 'Nhân viên vận hành Singapore', 'ACTIVE', 4, 1),
    (5, 'site_sh', 'site123', 'Điều phối viên Thượng Hải', 'ACTIVE', 4, 2),
    (6, 'warehouse01', 'warehouse123', 'Nhân viên kho mẫu', 'ACTIVE', 5, NULL)
ON CONFLICT (id) DO UPDATE
SET username = EXCLUDED.username,
    password = EXCLUDED.password,
    full_name = EXCLUDED.full_name,
    status = EXCLUDED.status,
    role_id = EXCLUDED.role_id,
    site_id = EXCLUDED.site_id;

INSERT INTO public.request (id, created_at, status)
VALUES
    (1, CURRENT_TIMESTAMP - INTERVAL '1 day',  'Chờ xử lý'),
    (2, CURRENT_TIMESTAMP - INTERVAL '3 days', 'Đang xử lý'),
    (3, CURRENT_TIMESTAMP - INTERVAL '6 days', 'Đang giao'),
    (4, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Đã hoàn thành'),
    (5, CURRENT_TIMESTAMP - INTERVAL '12 days', 'Đã hủy')
ON CONFLICT (id) DO UPDATE
SET created_at = EXCLUDED.created_at,
    status = EXCLUDED.status;

INSERT INTO public.request_merchandise (request_id, merchandise_id, quantity_ordered, desired_delivery_date)
VALUES
    (1, 1, 60, CURRENT_DATE + 10),
    (1, 3, 90, CURRENT_DATE + 9),
    (1, 4, 30, CURRENT_DATE + 12),
    (2, 2, 35, CURRENT_DATE + 6),
    (2, 5, 20, CURRENT_DATE + 7),
    (3, 6, 40, CURRENT_DATE + 3),
    (3, 1, 20, CURRENT_DATE + 4),
    (4, 3, 50, CURRENT_DATE - 3),
    (4, 4, 15, CURRENT_DATE - 2),
    (5, 2, 10, CURRENT_DATE + 1),
    (5, 6, 15, CURRENT_DATE + 2)
ON CONFLICT (request_id, merchandise_id) DO UPDATE
SET quantity_ordered = EXCLUDED.quantity_ordered,
    desired_delivery_date = EXCLUDED.desired_delivery_date;

INSERT INTO public."order" (id, request_id, site_id, created_at, status)
VALUES
    (1, 2, 1, CURRENT_TIMESTAMP - INTERVAL '2 days 12 hours', 'Chờ xác nhận'),
    (2, 2, 2, CURRENT_TIMESTAMP - INTERVAL '2 days 11 hours', 'Chờ xác nhận'),
    (3, 3, 4, CURRENT_TIMESTAMP - INTERVAL '5 days',          'Đang giao'),
    (4, 4, 1, CURRENT_TIMESTAMP - INTERVAL '9 days',          'Đã hoàn thành'),
    (5, 5, 3, CURRENT_TIMESTAMP - INTERVAL '11 days',         'Đã hủy')
ON CONFLICT (id) DO UPDATE
SET request_id = EXCLUDED.request_id,
    site_id = EXCLUDED.site_id,
    created_at = EXCLUDED.created_at,
    status = EXCLUDED.status;

INSERT INTO public.order_merchandise (order_id, merchandise_id, quantity, delivery_method)
VALUES
    (1, 2, 20, 'Tàu'),
    (1, 5, 8,  'Tàu'),
    (2, 2, 15, 'Máy bay'),
    (2, 5, 12, 'Máy bay'),
    (3, 6, 40, 'Máy bay'),
    (3, 1, 20, 'Máy bay'),
    (4, 3, 50, 'Tàu'),
    (4, 4, 15, 'Tàu'),
    (5, 2, 10, 'Máy bay'),
    (5, 6, 15, 'Máy bay')
ON CONFLICT (order_id, merchandise_id) DO UPDATE
SET quantity = EXCLUDED.quantity,
    delivery_method = EXCLUDED.delivery_method;

SELECT setval('public.role_id_seq', (SELECT MAX(id) FROM public.role), true);
SELECT setval('public.site_id_seq', (SELECT MAX(id) FROM public.site), true);
SELECT setval('public.merchandise_id_seq', (SELECT MAX(id) FROM public.merchandise), true);
SELECT setval('public.account_id_seq', (SELECT MAX(id) FROM public.account), true);
SELECT setval('public.request_id_seq', (SELECT MAX(id) FROM public.request), true);
SELECT setval('public."Order_id_seq"', (SELECT MAX(id) FROM public."order"), true);

COMMIT;