package Project1Game.model;

import Project1Game.component.npc.RelationshipLevel; // Import RelationshipLevel
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public double gameTime;
    public double health;
    public double hunger;
    public int playerMoney; // Thêm trường để lưu tiền của người chơi
    
    // Lưu bản đồ và vị trí người chơi
    public String currentMap;
    public double playerX;
    public double playerY;
    public String weather;
    public String playerSkin = "Player";

    // Lưu Inventory: Tên vật phẩm -> Số lượng
    public Map<String, Integer> inventoryItems = new HashMap<>();

    // Lưu danh sách các ô đất
    public List<SoilData> soils = new ArrayList<>();

    // Lưu danh sách các cây trồng
    public List<CropDataSave> crops = new ArrayList<>();


    public static class SoilData implements Serializable {
        public double x, y;
        public boolean isWet;
        public boolean hasPlant;
    }

    public static class CropDataSave implements Serializable {
        public double x, y;
        public String type; // Wheat, Radish...
        public int stage;
    }

    // Dynamic animal serialization data list
    public List<AnimalSaveData> animals = new ArrayList<>();

    public static class AnimalSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        public double x, y;
        public String type; // CHICKEN, COW, etc.
        public int daysGrown;
    }

    // Dynamic monster serialization data list
    public List<MonsterSaveData> monsters = new ArrayList<>();

    public static class MonsterSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        public double x, y;
        public String type; // Boar, Fox, Deer, Hare
    }

    public static class QuestSave implements Serializable {
        private static final long serialVersionUID = 1L;
        public String questId;
        public String status;
        public List<Integer> objectiveProgress = new ArrayList<>();
    }

    public static class NPCSave implements Serializable {
        private static final long serialVersionUID = 1L;
        public String npcName;
        public List<QuestSave> quests = new ArrayList<>();
    }

    public List<NPCSave> npcQuests = new ArrayList<>();

    // Trader serialization fields
    public Map<String, String> traderRelationship = new HashMap<>();
    public Map<String, Integer> traderNegotiationCount = new HashMap<>();
    public Map<String, Boolean> traderNegotiatedThisSession = new HashMap<>();
    public Map<String, Integer> traderNegotiationBonusPercent = new HashMap<>();
}