package Project1Game.quest;

/**
 * Đại diện cho <b>một</b> điều kiện (objective) trong Quest.
 * Mỗi Quest có thể chứa nhiều objective; tất cả phải DONE thì Quest mới COMPLETED.
 */
public abstract class QuestObjective {

    private final String description;
    private final int    required;      // số lượng cần đạt
    private int          current;       // tiến độ hiện tại

    protected QuestObjective(String description, int required) {
        if (required <= 0) throw new IllegalArgumentException("required phải > 0");
        this.description = description;
        this.required    = required;
        this.current     = 0;
    }

    /**
     * Được gọi mỗi khi có sự kiện game.
     * Lớp con gọi {@link #addProgress(int)} nếu sự kiện phù hợp.
     *
     * @param ctx snapshot trạng thái game tại thời điểm sự kiện
     */
    public abstract void checkProgress(QuestContext ctx);

    protected void addProgress(int amount) {
        current = Math.min(current + amount, required);
    }

    public boolean isDone()   { return current >= required; }
    public int getCurrent()   { return current; }
    public int getRequired()  { return required; }
    public String getDescription() { return description; }

    public String getProgressText() {
        return description + " (" + current + "/" + required + ")";
    }
}
