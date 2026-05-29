package Project1Game.component;

import Project1Game.model.npc.RelationshipLevel;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TraderComponent extends Component {

    private final ObjectProperty<RelationshipLevel> relationship = new SimpleObjectProperty<>(RelationshipLevel.NEUTRAL);

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
     * Điều chỉnh giá mua/bán dựa trên mức quan hệ.
     * @param basePrice Giá gốc của vật phẩm.
     * @param isBuying True nếu là giá mua (người chơi mua từ trader), False nếu là giá bán (người chơi bán cho trader).
     * @return Giá đã điều chỉnh.
     */
    public int getAdjustedPrice(int basePrice, boolean isBuying) {
        switch (relationship.get()) {
            case GOOD:
                return isBuying ? (int) (basePrice * 0.8) : (int) (basePrice * 1.2); // Trader bán rẻ hơn, mua đắt hơn
            case BAD:
                return isBuying ? (int) (basePrice * 1.2) : (int) (basePrice * 0.8); // Trader bán đắt hơn, mua rẻ hơn
            case NEUTRAL:
            default:
                return basePrice; // Giá bình thường
        }
    }

    /**
     * Kiểm tra xem Trader có từ chối giao dịch hay không.
     * @return True nếu từ chối, False nếu chấp nhận.
     */
    public boolean willRefuseTrade() {
        // Trader có thể từ chối giao dịch nếu quan hệ xấu
        return relationship.get() == RelationshipLevel.BAD && FXGL.random(0, 100) < 30; // 30% khả năng từ chối
    }

    /**
     * Cập nhật mức quan hệ sau một giao dịch.
     * @param success True nếu giao dịch thành công, False nếu thất bại (ví dụ: không đủ tiền/vật phẩm).
     * @param isBuying True nếu người chơi mua, False nếu người chơi bán.
     */
    public void updateRelationship(boolean success, boolean isBuying) {
        if (success) {
            // Giao dịch thành công có thể cải thiện quan hệ
            if (relationship.get() == RelationshipLevel.BAD) {
                if (FXGL.random(0, 100) < 20) setRelationship(RelationshipLevel.NEUTRAL); // 20% cải thiện từ xấu lên trung bình
            } else if (relationship.get() == RelationshipLevel.NEUTRAL) {
                if (FXGL.random(0, 100) < 10) setRelationship(RelationshipLevel.GOOD); // 10% cải thiện từ trung bình lên tốt
            }
        } else {
            // Giao dịch thất bại (ví dụ: người chơi không đủ tiền) có thể làm xấu quan hệ
            if (relationship.get() == RelationshipLevel.GOOD) {
                if (FXGL.random(0, 100) < 10) setRelationship(RelationshipLevel.NEUTRAL); // 10% xấu đi từ tốt xuống trung bình
            } else if (relationship.get() == RelationshipLevel.NEUTRAL) {
                if (FXGL.random(0, 100) < 20) setRelationship(RelationshipLevel.BAD); // 20% xấu đi từ trung bình xuống xấu
            }
        }
        System.out.println("Mức quan hệ mới với Trader: " + relationship.get());
    }
}