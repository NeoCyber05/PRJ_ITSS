package org.itss.prj_itss.auth.workspace;

import org.itss.prj_itss.auth.AuthenticatedUser;
import org.itss.prj_itss.auth.role.RoleType;

import java.util.List;
import java.util.Locale;

public final class RoleWorkspaceContentFactory {

    private RoleWorkspaceContentFactory() {
    }

    public static RoleWorkspaceContent create(AuthenticatedUser user) {
        RoleType roleType = RoleType.from(user);
        return switch (roleType) {
            case ADMIN -> new RoleWorkspaceContent(
                "Tổng quan quyền",
                "Không gian làm việc theo phân quyền của quản trị viên",
                "Không gian quản trị",
                "Tài khoản đã được xác thực đúng role. Module nghiệp vụ riêng cho quản trị viên chưa được triển khai trong phiên bản này.",
                "Quyền chính: quản lý tài khoản người dùng nội bộ.",
                "Chưa triển khai",
                List.of(
                    "Tạo, sửa, vô hiệu hóa hoặc hủy tài khoản nội bộ",
                    "Theo dõi phạm vi quyền truy cập theo role",
                    "Không được truy cập module đặt hàng quốc tế nếu không đúng quyền"
                )
            );
            case SALES -> new RoleWorkspaceContent(
                "Tổng quan quyền",
                "Không gian làm việc theo phân quyền của bộ phận bán hàng",
                "Không gian bộ phận bán hàng",
                "Tài khoản đã được xác thực đúng role. Module nghiệp vụ riêng cho bộ phận bán hàng chưa được triển khai trong phiên bản này.",
                "Quyền chính: tạo và theo dõi yêu cầu đặt hàng.",
                "Chưa triển khai",
                List.of(
                    "Tạo và cập nhật yêu cầu đặt hàng",
                    "Theo dõi trạng thái yêu cầu nhập hàng",
                    "Quản lý danh mục mặt hàng"
                )
            );
            case ORDERING -> new RoleWorkspaceContent(
                "Trang chủ",
                "Không gian làm việc của bộ phận đặt hàng quốc tế",
                "Không gian bộ phận đặt hàng quốc tế",
                "Tài khoản này được phép dùng các màn hình đã triển khai cho luồng đặt hàng quốc tế.",
                "Quyền chính: xử lý yêu cầu, quản lý site và đơn hàng.",
                "Đã triển khai",
                List.of(
                    "Quản lý site nhập khẩu",
                    "Xử lý yêu cầu đã nhận và phân bổ đơn hàng",
                    "Theo dõi danh sách đơn hàng đã tạo"
                )
            );
            case SITE -> new RoleWorkspaceContent(
                "Tổng quan quyền",
                "Không gian làm việc theo phân quyền của site",
                "Không gian site",
                "Tài khoản đã được xác thực đúng role. Module nghiệp vụ riêng cho site chưa được triển khai trong phiên bản này.",
                "Quyền chính: cập nhật thông tin site, tồn kho và phản hồi đơn hàng.",
                "Chưa triển khai",
                List.of(
                    "Cập nhật thông tin site và thời gian vận chuyển",
                    "Quản lý mặt hàng kinh doanh và tồn kho",
                    "Xem và phản hồi đơn hàng được gửi tới site"
                )
            );
            case WAREHOUSE -> new RoleWorkspaceContent(
                "Tổng quan quyền",
                "Không gian làm việc theo phân quyền của bộ phận quản lý kho",
                "Không gian quản lý kho",
                "Tài khoản đã được xác thực đúng role. Module nghiệp vụ riêng cho bộ phận quản lý kho chưa được triển khai trong phiên bản này.",
                "Quyền chính: tra cứu và xác nhận đơn giao tới kho.",
                "Chưa triển khai",
                List.of(
                    "Tra cứu các đơn hàng chuẩn bị cập bến",
                    "Đối chiếu chi tiết đơn giao tới kho",
                    "Xác nhận đơn hàng đã nhận thực tế"
                )
            );
            case UNKNOWN -> new RoleWorkspaceContent(
                "Tổng quan quyền",
                "Không gian làm việc theo phân quyền của " + user.roleName().toLowerCase(Locale.ROOT),
                user.roleName(),
                "Tài khoản đã đăng nhập nhưng role chưa được map rõ trong ứng dụng hiện tại.",
                "Quyền được quyết định theo cấu hình role của tài khoản.",
                "Chưa triển khai",
                List.of(
                    "Đăng nhập theo tài khoản được cấp",
                    "Chỉ vào các màn hình phù hợp với role",
                    "Không có quyền truy cập chéo sang module khác"
                )
            );
        };
    }
}
