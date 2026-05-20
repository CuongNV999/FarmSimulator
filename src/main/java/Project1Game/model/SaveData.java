package Project1Game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public double gameTime; // THÊM DÒNG NÀY
    public double health;  // Lưu máu
    public double hunger;  // Lưu thức ăn

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
        public String type; // Wheat, Corn...
        public int stage;
    }
}