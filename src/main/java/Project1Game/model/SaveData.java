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

    // Lưu Inventory: Tên vật phẩm -> Số lượng
    public Map<String, Integer> inventoryItems = new HashMap<>();

    // Lưu danh sách các ô đất
    public List<SoilData> soils = new ArrayList<>();

    // Lưu danh sách các cây trồng
    public List<CropDataSave> crops = new ArrayList<>();

    // Lưu mức quan hệ với các Trader
    public Map<String, RelationshipLevel> traderRelationships = new HashMap<>();

    public static class SoilData implements Serializable {
        public double x, y;
        public boolean isWet;
        public boolean hasPlant;
    }

    public static class CropDataSave implements Serializable {
        public double x, y;
        public String type; // Wheat, Corn...
        public int stage;
    }
}