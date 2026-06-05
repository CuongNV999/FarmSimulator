package Project1Game.system;

import Project1Game.component.npc.RelationshipLevel;
import com.almasb.fxgl.dsl.FXGL;

public class NegotiationEngine {

    public static class NegotiationResult {
        public final boolean success;
        public final int bonusPercent;
        public final boolean relationshipDowngraded;

        public NegotiationResult(boolean success, int bonusPercent, boolean relationshipDowngraded) {
            this.success = success;
            this.bonusPercent = bonusPercent;
            this.relationshipDowngraded = relationshipDowngraded;
        }
    }

    public static NegotiationResult calculateNegotiation(int requestedPercent, RelationshipLevel relationship, int negotiationCount) {
        // Giới hạn phạm vi 10-30
        int finalRequestedPercent = Math.max(10, Math.min(30, requestedPercent));

        // 1. Tính tỉ lệ thành công cơ bản (nội suy tuyến tính):
        int baseSuccessChance = 80 - 3 * (finalRequestedPercent - 10);

        // 2. Điều chỉnh theo quan hệ
        int relationshipBonus = 0;
        switch (relationship) {
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

        System.out.println("[NegotiationEngine] Thương lượng " + finalRequestedPercent + "% | "
                + "Base: " + baseSuccessChance + "% | Quan hệ: " + (relationshipBonus >= 0 ? "+" : "")
                + relationshipBonus
                + "% | Phạt lần thứ " + negotiationCount + ": -" + historyPenalty
                + "% | Final: " + finalSuccessChance + "%");

        boolean success = FXGL.random(0, 100) < finalSuccessChance;
        int bonusPercent;
        boolean relationshipDowngraded = false;

        if (success) {
            bonusPercent = finalRequestedPercent;
        } else {
            int penaltyPercent = finalRequestedPercent / 2;
            bonusPercent = -penaltyPercent;

            // Xác suất giảm quan hệ khi thất bại
            int downgradeChance = 10 + 2 * (finalRequestedPercent - 10);
            downgradeChance += (negotiationCount - 1) * 5;
            downgradeChance = Math.min(80, downgradeChance); // Giới hạn tối đa 80%

            if (FXGL.random(0, 100) < downgradeChance) {
                relationshipDowngraded = true;
            }
        }

        return new NegotiationResult(success, bonusPercent, relationshipDowngraded);
    }
}
