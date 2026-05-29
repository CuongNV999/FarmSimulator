package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class TimeSystem {
    private double gameTime = 360; // Mặc định khởi đầu là 6:00 AM (360 phút)
    private int hour = 6;
    private int minute = 0;

    public double getGameTime() { return gameTime; }

    private final Rectangle nightOverlay;
    private final Text clockText;

    // Định nghĩa các hằng số mốc thời gian để dễ quản lý, bảo trì
    private static final int START_DUSK = 18; // 6:00 PM bắt đầu tối dần
    private static final int FULL_DARK  = 22; // 10:00 PM tối hẳn
    private static final int START_DAWN = 5;  // 5:00 AM bắt đầu sáng dần
    private static final int FULL_LIGHT = 6;  // 6:00 AM sáng hẳn

    private static final double MAX_OPACITY = 0.7; // Độ tối tối đa của đêm

    public void setGameTime(double time) {
        this.gameTime = time;
        // Cập nhật ngay lập tức UI khi load
        onUpdate(0);
    }
    public TimeSystem(Rectangle nightOverlay, Text clockText) {
        this.nightOverlay = nightOverlay;
        this.clockText = clockText;
    }

    public void onUpdate(double tpf) {
        // Tốc độ dòng chảy thời gian (tpf * 10)
        gameTime += tpf * 10;
        if (gameTime >= 1440) {
            gameTime = 0;
        }

        WeatherSystem.getInstance().updateTime(gameTime);

        int oldHour = hour;
        hour = (int) (gameTime / 60);
        minute = (int) (gameTime % 60);

        updateUI();
        updateVisuals();

        // Kiểm tra chuyển giao giữa Ngày và Đêm dựa trên chính hàm isDayTime() để phát Event đồng bộ
        boolean WAS_DAY = (oldHour >= START_DAWN && oldHour < START_DUSK);
        boolean IS_DAY = isDayTime();

        if (WAS_DAY && !IS_DAY) {
            // Vừa bước sang khung giờ tối (18:00)
            FXGL.getEventBus().fireEvent(new DayNightEvent(DayNightEvent.SET_NIGHT));
        } else if (!WAS_DAY && IS_DAY) {
            // Vừa bước sang khung giờ sáng (05:00)
            FXGL.getEventBus().fireEvent(new DayNightEvent(DayNightEvent.SET_DAY));
        }
    }

    private void updateUI() {
        if (clockText != null) {
            // Định nghĩa hiển thị 12h AM/PM chuẩn chỉnh
            int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            String amPm = hour >= 12 ? "PM" : "AM";

            clockText.setText(String.format("%02d:%02d %s", displayHour, minute, amPm));

            // Đổi màu chữ đồng hồ: Vàng ấm áp cho ban ngày, Xanh neon cho ban đêm
            clockText.setFill(isDayTime() ? Color.GOLD : Color.CYAN);
        }
    }

    private void updateVisuals() {
        if (nightOverlay == null) return;

        double opacity = 0;

        // Lập trình tuyến tính mượt mà hiệu ứng ánh sáng
        if (hour >= START_DUSK && hour < FULL_DARK) {
            // Hoàng hôn: Tối dần từ 18h đến 22h (Thời gian kéo dài 4 tiếng = 240 phút)
            double minutesPassed = gameTime - (START_DUSK * 60);
            opacity = (minutesPassed / 240.0) * MAX_OPACITY;
        }
        else if (hour >= FULL_DARK || hour < START_DAWN) {
            // Đêm muộn: Từ 22h đêm đến 5h sáng hôm sau -> Giữ nguyên độ tối đỉnh điểm
            opacity = MAX_OPACITY;
        }
        else if (hour >= START_DAWN && hour < FULL_LIGHT) {
            // Bình minh: Sáng dần từ 5h đến 6h sáng (Thời gian kéo dài 1 tiếng = 60 phút)
            double minutesPassed = gameTime - (START_DAWN * 60);
            opacity = MAX_OPACITY * (1.0 - (minutesPassed / 60.0));
        }
        else {
            // Ban ngày: Từ 6h sáng đến 18h tối -> Trời sáng hoàn toàn (Kính lọc trong suốt)
            opacity = 0.0;
        }

        nightOverlay.setOpacity(opacity);
    }

    /**
     * Trả về trạng thái ngày hay đêm.
     * Từ 5h sáng đến trước 18h tối được coi là ban ngày.
     */
    public boolean isDayTime() {
        return hour >= START_DAWN && hour < START_DUSK;
    }

    public int getHour() { return hour; }
    public int getMinute() { return minute; }

    /**
     * Tiến thời gian đến sáng hôm sau (ví dụ: 6:00 AM).
     */
    public void advanceToNextDay() {
        gameTime = FULL_LIGHT * 60; // Đặt lại về 6:00 AM
        hour = FULL_LIGHT;
        minute = 0;
        updateUI(); // Cập nhật UI ngay lập tức
        updateVisuals(); // Cập nhật hiệu ứng hình ảnh
        FXGL.getEventBus().fireEvent(new DayNightEvent(DayNightEvent.SET_DAY)); // Kích hoạt sự kiện ngày mới
        System.out.println("Thời gian đã được đặt lại về 6:00 AM.");
    }
}
