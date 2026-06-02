package Project1Game.ui;

import Project1Game.core.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class MinimapView extends Pane {
    private final int MINI_W = 150;
    private final int MINI_H = 150;

    // Kích thước thực tế của Map (lấy từ thông số bạn cung cấp)
    private final double WORLD_W = 3840;
    private final double WORLD_H = 2176;

    private Canvas canvas;
    private GraphicsContext gc;

    public MinimapView() {
        // 1. Tạo nền cho Minimap
        Rectangle bg = new Rectangle(MINI_W, MINI_H);
        bg.setFill(Color.rgb(0, 0, 0, 0.5)); // Đen mờ 50%
        bg.setStroke(Color.WHITE);           // Viền trắng
        bg.setStrokeWidth(1.5);
        bg.setArcWidth(10);                  // Bo góc
        bg.setArcHeight(10);

        // 2. Tạo Canvas để vẽ các chấm màu
        canvas = new Canvas(MINI_W, MINI_H);
        gc = canvas.getGraphicsContext2D();

        getChildren().addAll(bg, canvas);
    }

    public void update() {
        // Xóa hình cũ để vẽ mới
        gc.clearRect(0, 0, MINI_W, MINI_H);

        // Tỉ lệ scale
        double scaleX = MINI_W / WORLD_W;
        double scaleY = MINI_H / WORLD_H;

        // Vẽ Vật cản (Màu Xám) - Vẽ trước để nằm dưới
        drawEntities(EntityType.COLLISION, Color.GRAY, scaleX, scaleY, 2);

        // Vẽ Ô đất (Màu Nâu)
        drawEntities(EntityType.SOIL, Color.BROWN, scaleX, scaleY, 3);

        // Vẽ Player (Màu Xanh Lá) - Vẽ sau cùng để nằm trên
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player != null) {
            double px = player.getX() * scaleX;
            double py = player.getY() * scaleY;
            gc.setFill(Color.GREEN);
            // Chấm Player to hơn một chút để dễ nhìn
            gc.fillOval(px - 2, py - 2, 5, 5);
        }
    }

    private void drawEntities(EntityType type, Color color, double sx, double sy, double size) {
        List<Entity> entities = FXGL.getGameWorld().getEntitiesByType(type);
        gc.setFill(color);
        for (Entity e : entities) {
            double ex = e.getX() * sx;
            double ey = e.getY() * sy;
            gc.fillRect(ex, ey, size, size);
        }
    }
}