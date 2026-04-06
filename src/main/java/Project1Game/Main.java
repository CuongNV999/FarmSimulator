package Project1Game;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.onKeyDown;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

public class Main extends GameApplication {
    private Entity player;
    private Entity selector;
    private Entity nearbyInteraction = null;
    private Inventory inventory;
    private ToolbarView toolbarView;
    private StatusBarsView statusBarsView;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1280);
        gameSettings.setHeight(720);
        gameSettings.setDeveloperMenuEnabled(true);
        gameSettings.setExperimentalTiledLargeMap(false);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().setGravity(0, 0);
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.WALL) {
            @Override
            protected void onCollisionBegin(Entity Player, Entity Wall) {
                System.out.println("On Collision wall");
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.COLLISION) {
            @Override
            protected void onCollisionBegin(Entity player, Entity collision) {
                System.out.println("Va chạm vật cản!");
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.INTERACTION) {
            @Override
            protected void onCollisionBegin(Entity player, Entity interaction) {
                // Lưu entity interaction gần nhất để dùng khi nhấn phím
                nearbyInteraction = interaction;
                System.out.println("Gần vùng tương tác!");
            }
            @Override
            protected void onCollisionEnd(Entity player, Entity interaction) {
                nearbyInteraction = null;
            }
        });
    }

    @Override
    protected void initGame() {

        inventory = new Inventory();

        FXGL.getGameWorld().addEntityFactory(new Factory());
        FXGL.setLevelFromMap("Main_level.tmx");

        // Debug: xem properties của tất cả entity không có type
        getGameWorld().getEntities().stream()
                .filter(e -> e.getType().toString().equals("0") || e.getType() == null)
                .forEach(e -> System.out.println("No-type entity props: " + e.getProperties() + " pos=" + e.getX() + "," + e.getY()));


        player = getGameWorld().getSingleton(EntityType.PLAYER);
        System.out.printf("Player spawn at x=%.0f y=%.0f%n", player.getX(), player.getY());
        selector = FXGL.spawn("Selector");

        // Giới hạn khung hình và camera đi theo player
        // Kích thước level: 120x68 ô * 32px = 3840x2176
        
        FXGL.getGameScene().getViewport().setZoom(1.0);
        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
        FXGL.getGameScene().getViewport().setBounds(0, 0, 3840, 2176);
        // Chế độ Lazy follow giúp camera mượt hơn và "mở rộng tầm nhìn" khi tiến gần rìa (deadzone)
        FXGL.getGameScene().getViewport().setLazy(true);

        // Tạo collision bodies từ tile layer có property colliable=true
        // setupTileCollisions() đã được thay bằng object layer Collisions trong TMX

        // Đảm bảo tất cả các thực thể SOIL hiện có được cập nhật texture
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
            soil.getComponent(SoilComponent.class).updateTexture();
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        if (selector != null && player != null) {
            double mouseX = FXGL.getInput().getMouseXWorld();
            double mouseY = FXGL.getInput().getMouseYWorld();

            // Làm tròn vị trí chuột về lưới 32x32
            double x = Math.floor(mouseX / 32) * 32;
            double y = Math.floor(mouseY / 32) * 32;

            selector.setPosition(x, y);

            // Cập nhật animation của Player dựa trên vận tốc từ PhysicsComponent
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            Point2D vel = new Point2D(physics.getVelocityX(), physics.getVelocityY());
            player.getComponent(PlayerComponent.class).move(vel);

            // Kiểm tra bán kính tương tác là 2 ô (64 pixels tính từ tâm)
            // Lấy tâm của player và tâm của selector
            double playerCenterX = player.getCenter().getX();
            double playerCenterY = player.getCenter().getY();
            double selectorCenterX = x + 16;
            double selectorCenterY = y + 16;

            double distance = Math.sqrt(Math.pow(playerCenterX - selectorCenterX, 2) + Math.pow(playerCenterY - selectorCenterY, 2));

            // Bán kính tương tác là 2 ô (32*2 = 64 pixels tính từ tâm của ô Player).
            // Nếu Player ở giữa 1 ô (16,16), và tâm ô thứ 2 là (80,16), khoảng cách là 64.
            // Để cho phép chọn chéo, ta có thể dùng 96 làm ngưỡng an toàn (cho phép 2 ô ngang/dọc và chéo).
            if (distance <= 96) {
                selector.getViewComponent().setOpacity(1.0);
                selector.setVisible(true);
            } else {
                selector.getViewComponent().setOpacity(0.3);
                selector.setVisible(true); // Vẫn cho thấy selector nhưng mờ để người dùng biết mình đang trỏ đi đâu
            }
        }
    }

    @Override
    protected void initUI() {
        toolbarView = new ToolbarView(inventory);

        // Đặt thanh công cụ ở giữa phía dưới màn hình
        // Sử dụng các hằng số từ ToolbarView để tính toán kích thước thực tế
        // (SLOT_SIZE = 80, SLOT_GAP = 6)
        double slotSize = 80;
        double slotGap = 6;
        double toolbarWidth = inventory.getSlots().length * (slotSize + slotGap) - slotGap;
        
        toolbarView.setLayoutX((FXGL.getAppWidth() - toolbarWidth) / 2);
        toolbarView.setLayoutY(FXGL.getAppHeight() - slotSize - 20);

        FXGL.getGameScene().addUINode(toolbarView);

        // Thanh máu và thanh thức ăn ở góc trên bên trái
        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(16);
        statusBarsView.setLayoutY(16);
        FXGL.getGameScene().addUINode(statusBarsView);
    }

    @Override
    protected void initInput() {
        int v = 200; // Tốc độ di chuyển thực tế

        Input input = FXGL.getInput();


        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(-v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(-v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.S);

        // Phím R - Tương tác với vùng gần (cửa, NPC, v.v.)
        onKeyDown(KeyCode.R, () -> {
            if (nearbyInteraction != null) {
                System.out.println("Tương tác tại: " + nearbyInteraction.getX() + ", " + nearbyInteraction.getY());
                // TODO: xử lý logic tương tác cụ thể (mở cửa, nói chuyện, v.v.)
            }
            return null;
        });

        // Phím F - Sử dụng vật phẩm đang chọn
        onKeyDown(KeyCode.F, () -> {
            ItemType selected = inventory.getSelectedItem();

            switch (selected) {
                case HOE:
                    // Cuốc đất - tạo ô đất tại vị trí player
                    useHoe();
                    break;
                case RICE_SEED:
                    // Trồng lúa - cần có hạt giống
                    plantRice();
                    break;
                case WATERING_CAN:
                    // Tưới nước (placeholder)
                    System.out.println("Đang tưới nước...");
                    break;
                default:
                    break;
            }
            return null;
        });

        // Phím E - Thu hoạch lúa chín
        onKeyDown(KeyCode.E, () -> {
            if (selector.getViewComponent().getOpacity() < 1.0) return null;

            getGameWorld().getEntitiesByType(EntityType.RICE).stream()
                    .filter(rice -> Math.abs(rice.getX() - selector.getX()) < 5 && Math.abs(rice.getY() - selector.getY()) < 5)
                    .filter(rice -> rice.getComponent(RiceComponent.class).isRipe())
                    .findFirst()
                    .ifPresent(rice -> {
                        // Tìm ô đất tương ứng và đánh dấu chưa có cây
                        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                                .filter(soil -> Math.abs(soil.getX() - rice.getX()) < 5
                                        && Math.abs(soil.getY() - rice.getY()) < 5)
                                .findFirst()
                                .ifPresent(soil -> soil.getComponent(SoilComponent.class).setHasPlant(false));

                        rice.removeFromWorld();
                        inventory.addItem(ItemType.RICE, 1);
                        System.out.println("Thu hoạch lúa! Số lúa: " + inventory.getCount(ItemType.RICE));
                    });
            return null;
        });

        // Phím số 1-9 để chọn slot trên toolbar
        onKeyDown(KeyCode.DIGIT1, () -> { selectSlot(0); return null; });
        onKeyDown(KeyCode.DIGIT2, () -> { selectSlot(1); return null; });
        onKeyDown(KeyCode.DIGIT3, () -> { selectSlot(2); return null; });
        onKeyDown(KeyCode.DIGIT4, () -> { selectSlot(3); return null; });
        onKeyDown(KeyCode.DIGIT5, () -> { selectSlot(4); return null; });
        onKeyDown(KeyCode.DIGIT6, () -> { selectSlot(5); return null; });
        onKeyDown(KeyCode.DIGIT7, () -> { selectSlot(6); return null; });
        onKeyDown(KeyCode.DIGIT8, () -> { selectSlot(7); return null; });
        onKeyDown(KeyCode.DIGIT9, () -> { selectSlot(8); return null; });

        // Phím Q/Tab để chuyển slot
        onKeyDown(KeyCode.Q, () -> {
            inventory.selectPrevious();
            if (toolbarView != null) toolbarView.updateSelection();
            System.out.println("Đã chọn: " + inventory.getSelectedItem().getDisplayName());
            return null;
        });

        onKeyDown(KeyCode.TAB, () -> {
            inventory.selectNext();
            if (toolbarView != null) toolbarView.updateSelection();
            System.out.println("Đã chọn: " + inventory.getSelectedItem().getDisplayName());
            return null;
        });
    }

    private void selectSlot(int slot) {
        inventory.setSelectedSlot(slot);
        if (toolbarView != null) toolbarView.updateSelection();
        System.out.println("Đã chọn: " + inventory.getSelectedItem().getDisplayName());
    }

    private void useHoe() {
        if (selector.getViewComponent().getOpacity() < 1.0) {
            System.out.println("Ngoài phạm vi tương tác!");
            return;
        }

        double x = selector.getX();
        double y = selector.getY();

        // Kiểm tra xem tại vị trí này đã có Soil chưa
        boolean alreadyHasSoil = getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .anyMatch(soil -> Math.abs(soil.getX() - x) < 5 && Math.abs(soil.getY() - y) < 5);

        if (!alreadyHasSoil) {
            getGameWorld().spawn("Soil", x, y);
            System.out.println("Đã tạo ô đất tại: " + x + ", " + y);

            // Chạy cập nhật texture ở frame tiếp theo để đảm bảo thực thể mới đã được thêm vào world list
            FXGL.getExecutor().startAsyncFX(() -> {
                getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
                    soil.getComponent(SoilComponent.class).updateTexture();
                });
            });
        } else {
            System.out.println("Vị trí này đã có ô đất!");
        }
    }

    private void plantRice() {
        if (inventory.getCount(ItemType.RICE_SEED) <= 0) {
            System.out.println("Không đủ hạt giống!");
            return;
        }

        if (selector.getViewComponent().getOpacity() < 1.0) {
            System.out.println("Ngoài phạm vi tương tác!");
            return;
        }

        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(soil -> Math.abs(soil.getX() - selector.getX()) < 5 && Math.abs(soil.getY() - selector.getY()) < 5)
                .filter(soil -> soil.getComponent(SoilComponent.class).canPlant())
                .findFirst()
                .ifPresent(soil -> {
                    SoilComponent config = soil.getComponent(SoilComponent.class);
                    getGameWorld().spawn("Rice", soil.getX(), soil.getY());
                    config.setHasPlant(true);
                    inventory.removeItem(ItemType.RICE_SEED, 1);
                    System.out.println("Đã trồng lúa tại: " + soil.getX() + ", " + soil.getY()
                            + " | Hạt giống còn: " + inventory.getCount(ItemType.RICE_SEED));
                });
    }

    /**
     * Đọc tile layer có property "colliable=true" và tạo static collision body
     * cho mỗi tile khác 0 trong layer đó.
     */
    private void setupTileCollisions() {
        try (InputStream is = getClass().getResourceAsStream("/assets/levels/Main_level.tmx")) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            Element map = doc.getDocumentElement();
            int tileW = Integer.parseInt(map.getAttribute("tilewidth"));
            int tileH = Integer.parseInt(map.getAttribute("tileheight"));
            int mapW  = Integer.parseInt(map.getAttribute("width"));
            int mapH  = Integer.parseInt(map.getAttribute("height"));

            NodeList layers = map.getElementsByTagName("layer");
            for (int i = 0; i < layers.getLength(); i++) {
                Element layer = (Element) layers.item(i);

                // Kiểm tra property colliable=true
                boolean colliable = false;
                NodeList props = layer.getElementsByTagName("property");
                for (int p = 0; p < props.getLength(); p++) {
                    Element prop = (Element) props.item(p);
                    if ("colliable".equals(prop.getAttribute("name"))
                            && "true".equals(prop.getAttribute("value"))) {
                        colliable = true;
                        break;
                    }
                }
                if (!colliable) continue;

                // Parse CSV data - xóa tất cả whitespace/newline trước khi split
                String csv = layer.getElementsByTagName("data").item(0)
                        .getTextContent().replaceAll("\\s+", "");
                String[] tokens = csv.split(",");
                int count = 0;
                for (int t = 0; t < tokens.length; t++) {
                    if (tokens[t].isEmpty()) continue;
                    int tileId = Integer.parseInt(tokens[t]);
                    if (tileId == 0) continue;

                    int col = t % mapW;
                    int row = t / mapW;
                    // Bỏ qua tile dummy ở góc (0,0) và (mapW-1, mapH-1) dùng để fix buffer size
                    if ((col == 0 && row == 0) || (col == mapW - 1 && row == mapH - 1)) continue;
                    double x = col * tileW;
                    double y = row * tileH;
                    if (count < 5) {
                        System.out.printf("Collision tile[%d] tileId=%d col=%d row=%d x=%.0f y=%.0f%n",
                                count, tileId, col, row, x, y);
                        count++;
                    }
                    FXGL.getGameWorld().spawn("Collisions",
                            new SpawnData(x, y)
                                    .put("width", tileW)
                                    .put("height", tileH));
                }
            }
        } catch (Exception e) {
            System.err.println("setupTileCollisions error: " + e.getMessage());
        }
    }

    private static Entity getPlayer() {
        return FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
