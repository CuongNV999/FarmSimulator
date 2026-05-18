package Project1Game.quest;

/**
 * Trạng thái vòng đời của một Quest.
 */
public enum QuestStatus {
    /** Chưa nhận – NPC chưa giao hoặc người chơi chưa chấp nhận */
    NOT_STARTED,
    /** Đã nhận, đang thực hiện */
    IN_PROGRESS,
    /** Đã hoàn thành điều kiện, chờ nộp cho NPC */
    COMPLETED,
    /** Đã nộp và nhận thưởng */
    REWARDED
}
