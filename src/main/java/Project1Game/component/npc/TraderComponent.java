package Project1Game.component.npc;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TraderComponent extends Component {

    // SỬA LỖI: Bổ sung khởi tạo giá trị NEUTRAL mặc định cho Property và đóng ngoặc
    private final ObjectProperty<RelationshipLevel> relationship = new SimpleObjectProperty<>(
            RelationshipLevel.NEUTRAL);

    // --- BIẾN QUẢN LÝ THƯƠNG LƯỢNG ---
    public boolean hasNegotiatedThisSession = false; // Đảm bảo chỉ được mặc cả 1 lần mỗi khi mở shop
    private int negotiationBonusPercent = 0; // % giá trị thay đổi sau khi mặc cả
    private int negotiationCount = 0; // Đếm số lần thương lượng tổng cộng (để tăng độ khó)

    public TraderComponent() {
        // Mức quan hệ ban đầu là NEUTRAL
    }

    public RelationshipLevel getRelationship() {
        return relationship.get();
    }

    public ObjectProperty<RelationshipLevel> relationshipProperty() {
        return relationship;
    }

    public void setRelationship(RelationshipLevel relationship) {
        this.relationship.set(relationship);
    }

    /**
     * Hàm này được gọi từ TradingView mỗi khi người chơi mở cửa hàng.
     * Tác dụng: Reset lại quyền mặc cả cho phiên mới.
     */
    public void resetSession() {
        hasNegotiatedThisSession = false;
        negotiationBonusPercent = 0;
        System.out.println("[TraderComponent] Đã reset trạng thái cửa hàng.");
    }

    /**
     * Hàm xử lý thương lượng dựa trên thanh trượt.
     *
     * @param requestedPercent % giảm giá mà người chơi yêu cầu (10 - 30)
     * @return true nếu thương lượng thành công, false nếu thất bại
     *
     *         Logic:
     *         - Tỉ lệ thành công cơ bản giảm dần theo % yêu cầu:
     *         + 10% → 80% thành công cơ bản
     *         + 20% → 50% thành công cơ bản
     *         + 30% → 20% thành công cơ bản
     *         - Tỉ lệ thành công được điều chỉnh bởi quan hệ:
     *         + GOOD: +15% cơ hội
     *         + NEUTRAL: ±0%
     *         + BAD: -15% cơ hội
     *         - Thương lượng càng nhiều lần → tỉ lệ thành công giảm thêm (mỗi lần
     *         trước -5%)
     *         - Thất bại với % cao → xác suất giảm quan hệ cao hơn
     */
    public boolean tryNegotiateWithSlider(int requestedPercent) {
        if (hasNegotiatedThisSession) {
            return false;
        }
        hasNegotiatedThisSession = true;
        negotiationCount++; // Tăng tổng số lần đã thương lượng

        // Giới hạn phạm vi 10-30
        requestedPercent = Math.max(10, Math.min(30, requestedPercent));

        // 1. Tính tỉ lệ thành công cơ bản (nội suy tuyến tính):
        int baseSuccessChance = 80 - 3 * (requestedPercent - 10);

        // 2. Điều chỉnh theo quan hệ
        int relationshipBonus = 0;
        switch (relationship.get()) {
            case GOOD:
                relationshipBonus = 15;
                break;
            case NEUTRAL:
                relationshipBonus = 0;
                break;
            case BAD:
                relationshipBonus = -15;
                break;
        }

        // 3. Phạt theo số lần thương lượng trước đó (-5% mỗi lần trước)
        int historyPenalty = (negotiationCount - 1) * 5;

        int finalSuccessChance = Math.max(5, Math.min(95, baseSuccessChance + relationshipBonus - historyPenalty));

        System.out.println("[TraderComponent] Thương lượng " + requestedPercent + "% | "
                + "Base: " + baseSuccessChance + "% | Quan hệ: " + (relationshipBonus >= 0 ? "+" : "")
                + relationshipBonus
                + "% | Phạt lần thứ " + negotiationCount + ": -" + historyPenalty
                + "% | Final: " + finalSuccessChance + "%");

        // 4. Quay số
        if (FXGL.random(0, 100) < finalSuccessChance) {
            // Thành công: Người chơi được giảm giá/tăng giá bán
            negotiationBonusPercent = requestedPercent;
            System.out.println("[TraderComponent] Thương lượng THÀNH CÔNG! Bonus: +" + requestedPercent + "%");
            return true;
        } else {
            // Thất bại: Bị phạt ngược (trader tăng giá mua / giảm giá bán)
            int penaltyPercent = requestedPercent / 2;
            negotiationBonusPercent = -penaltyPercent;
            System.out.println("[TraderComponent] Thương lượng THẤT BẠI! Phạt: -" + penaltyPercent + "%");

            // 5. Xác suất giảm quan hệ khi thất bại
            int downgradeChance = 10 + 2 * (requestedPercent - 10);
            downgradeChance += (negotiationCount - 1) * 5;
            downgradeChance = Math.min(80, downgradeChance); // Giới hạn tối đa 80%

            if (FXGL.random(0, 100) < downgradeChance) {
                if (relationship.get() == RelationshipLevel.GOOD) {
                    setRelationship(RelationshipLevel.NEUTRAL);
                    System.out.println("[TraderComponent] Quan hệ bị giảm: GOOD → NEUTRAL");
                } else if (relationship.get() == RelationshipLevel.NEUTRAL) {
                    setRelationship(RelationshipLevel.BAD);
                    System.out.println("[TraderComponent] Quan hệ bị giảm: NEUTRAL → BAD");
                }
            }
            return false;
        }
    }

    /**
     * Hàm xử lý tỷ lệ thành công khi bấm nút Thương lượng (legacy - giữ lại cho
     * tương thích).
     */
    public boolean tryNegotiate() {
        return tryNegotiateWithSlider(15); // Mặc định 15% cho hệ thống cũ
    }

    /**
     * Lấy % bonus hiện tại từ kết quả thương lượng
     */
    public int getNegotiationBonusPercent() {
        return negotiationBonusPercent;
    }

    /**
     * Lấy số lần thương lượng đã thực hiện
     */
    public int getNegotiationCount() {
        return negotiationCount;
    }

    /**
     * Hàm tính giá mới nhất (Bao gồm quan hệ + Kết quả thương lượng)
     */
    public int getAdjustedPrice(int basePrice, boolean isBuying) {
        double priceAfterRelationship = basePrice;

        // 1. Chỉnh giá theo độ thân thiết
        switch (relationship.get()) {
            case GOOD:
                priceAfterRelationship = isBuying ? (basePrice * 0.8) : (basePrice * 1.2);
                break;
            case BAD:
                priceAfterRelationship = isBuying ? (basePrice * 1.2) : (basePrice * 0.8);
                break;
            case NEUTRAL:
            default:
                priceAfterRelationship = basePrice;
                break;
        }

        // 2. Chỉnh giá theo kết quả mặc cả (Nếu có)
        if (isBuying) {
            // Mua đồ: Có lợi (+%) thì giá giảm, Bất lợi (-%) thì giá tăng
            priceAfterRelationship = priceAfterRelationship * (1.0 - (negotiationBonusPercent / 100.0));
        } else {
            // SỬA LỖI: Điền công thức tính toán và đóng ngoặc cho logic BÁN ĐỒ
            // Bán đồ: Có lợi (+%) thì bán được nhiều tiền, Bất lợi (-%) thì bán được ít
            // tiền
            priceAfterRelationship = priceAfterRelationship * (1.0 + (negotiationBonusPercent / 100.0));
        }

        return Math.max(1, (int) Math.round(priceAfterRelationship)); // Đảm bảo giá không bao giờ rơi xuống dưới 1 G
    }

    public boolean willRefuseTrade() {
        return relationship.get() == RelationshipLevel.BAD && FXGL.random(0, 100) < 30;
    }

    // SỬA LỖI: Tái cấu trúc hoàn chỉnh các nhánh điều kiện nâng/hạ quan hệ dựa trên
    // chuỗi văn bản bị đứt đoạn
    public void updateRelationship(boolean success, boolean isBuying) {
        if (success) {
            if (relationship.get() == RelationshipLevel.BAD) {
                if (FXGL.random(0, 100) < 20) {
                    setRelationship(RelationshipLevel.NEUTRAL);
                }
            } else if (relationship.get() == RelationshipLevel.NEUTRAL) {
                if (FXGL.random(0, 100) < 10) {
                    setRelationship(RelationshipLevel.GOOD);
                }
            }
        } else {
            if (relationship.get() == RelationshipLevel.GOOD) {
                if (FXGL.random(0, 100) < 10) {
                    setRelationship(RelationshipLevel.NEUTRAL);
                }
            } else if (relationship.get() == RelationshipLevel.NEUTRAL) {
                if (FXGL.random(0, 100) < 20) {
                    setRelationship(RelationshipLevel.BAD);
                }
            }
        }
    }

    public boolean hasNegotiatedThisSession() {
        return hasNegotiatedThisSession;
    }
}
