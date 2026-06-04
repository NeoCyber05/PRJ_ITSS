INSERT INTO public.role (id, name) VALUES
    (1, 'Quản trị viên'),
    (2, 'Bộ phận bán hàng'),
    (3, 'Bộ phận đặt hàng quốc tế'),
    (4, 'Site'),
    (5, 'Bộ phận quản lý kho')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval(
    'public.role_id_seq',
    GREATEST((SELECT COALESCE(MAX(id), 1) FROM public.role), 1),
    true
);

ALTER TABLE public.account
    ALTER COLUMN status SET DEFAULT 'active';

UPDATE public.account
SET status = 'active'
WHERE status IS NULL OR TRIM(status) = '';
