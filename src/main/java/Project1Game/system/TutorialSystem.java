package Project1Game.system;

import Project1Game.ui.utility.GameFont;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.io.File;
import java.io.IOException;

/**
 * System that manages the onboarding tutorial for first-time players.
 */
public class TutorialSystem {
    private static TutorialSystem instance;
    public static TutorialSystem getInstance() {
        if (instance == null) {
            instance = new TutorialSystem();
        }
        return instance;
    }

    private StackPane overlay;
    private VBox card;
    private Text stepTitleText;
    private Text descText;
    private Button nextBtn;
    private Button skipBtn;
    private int currentStep = 1;
    private static final String TUTORIAL_FILE = ".tutorial_completed";

    private TutorialSystem() {}

    public boolean isFirstTime() {
        File file = new File(TUTORIAL_FILE);
        return !file.exists();
    }

    public void markTutorialCompleted() {
        try {
            File file = new File(TUTORIAL_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetTutorial() {
        try {
            File file = new File(TUTORIAL_FILE);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (overlay != null) {
            try {
                FXGL.getGameScene().removeUINode(overlay);
            } catch (Exception ignored) {}
            overlay = null;
        }
        currentStep = 1;
    }

    public void startTutorial() {
        if (overlay != null) return; // Already running

        // 1. Create full-screen translucent background overlay
        overlay = new StackPane();
        overlay.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        overlay.setStyle("-fx-background-color: rgba(10, 10, 15, 0.75);");
        overlay.setAlignment(Pos.CENTER);

        // 2. Create the onboarding dialogue card
        card = new VBox(20);
        card.setPrefSize(550, 320);
        card.setMaxSize(550, 320);
        card.setPadding(new Insets(25));
        card.setStyle(
            "-fx-background-color: rgba(25, 25, 35, 0.96); " +
            "-fx-border-color: #eccb58; " +
            "-fx-border-width: 2.5; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 0);"
        );
        card.setAlignment(Pos.TOP_CENTER);

        // Header Title
        Text headerText = new Text("HƯỚNG DẪN TÂN THỦ / FARMER'S GUIDE");
        headerText.setFont(Font.font(Project1Game.ui.utility.GameFont.GAME_FONT, FontWeight.BOLD, 22));
        headerText.setFill(Color.web("#eccb58"));
        headerText.setStroke(Color.BLACK);
        headerText.setStrokeWidth(0.5);

        // Step Title
        stepTitleText = new Text();
        stepTitleText.setFont(Font.font(Project1Game.ui.utility.GameFont.GAME_FONT, FontWeight.BOLD, 17));
        stepTitleText.setFill(Color.WHITE);

        // Step Description
        descText = new Text();
        descText.setFont(Font.font(Project1Game.ui.utility.GameFont.GAME_FONT, FontWeight.NORMAL, 14));
        descText.setFill(Color.LIGHTGRAY);
        descText.setWrappingWidth(500);

        // Buttons
        skipBtn = new Button("Bỏ qua / Skip");
        skipBtn.setPrefWidth(130);
        skipBtn.setStyle(
            "-fx-background-color: #d9383a; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 8 15;"
        );
        skipBtn.setOnAction(e -> finishTutorial(true));

        nextBtn = new Button("Tiếp tục / Next");
        nextBtn.setPrefWidth(130);
        nextBtn.setStyle(
            "-fx-background-color: #eccb58; " +
            "-fx-text-fill: #12121c; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 8 15;"
        );
        nextBtn.setOnAction(e -> goToNextStep());

        HBox btnRow = new HBox(30);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.getChildren().addAll(skipBtn, nextBtn);

        // Add components to card
        card.getChildren().addAll(headerText, stepTitleText, descText, btnRow);
        overlay.getChildren().add(card);

        // Add to game scene UI layer
        FXGL.getGameScene().addUINode(overlay);

        // Set to step 1
        currentStep = 1;
        updateStepUI();
    }

    private void goToNextStep() {
        if (currentStep < 5) {
            currentStep++;
            updateStepUI();
        } else {
            finishTutorial(false);
        }
    }

    private void updateStepUI() {
        if (nextBtn != null) {
            nextBtn.setText("Tiếp tục / Next");
        }
        switch (currentStep) {
            case 1:
                stepTitleText.setText("Bước 1: Di chuyển & Cơ bản (Movement)");
                descText.setText(
                    "Sử dụng các phím W, A, S, D hoặc các Phím Mũi Tên để di chuyển nhân vật xung quanh trang trại.\n\n" +
                    "Use W, A, S, D or Arrow Keys to walk around."
                );
                break;
            case 2:
                stepTitleText.setText("Bước 2: Chỉ số Sinh tồn (Survival Stats)");
                descText.setText(
                    "Hãy chú ý góc trên bên trái màn hình:\n" +
                    "- Các Trái Tim đại diện cho Máu (HP). Nếu máu về 0%, bạn sẽ bị kiệt sức ngất xỉu.\n" +
                    "- Thanh Thức ăn (Food/Hunger) đại diện cho độ no. Chỉ số này sẽ giảm dần theo thời gian và khi bạn làm việc. Hãy ăn hoa quả thu hoạch để hồi phục.\n\n" +
                    "Watch your top-left stats: Hearts represent Health (HP) and Food represents Hunger. Hunger drops over time or when doing work. Eat crops to restore hunger."
                );
                break;
            case 3:
                stepTitleText.setText("Bước 3: Thời gian & UI (HUD Box)");
                descText.setText(
                    "Bảng thông tin ở góc trên bên phải hiển thị:\n" +
                    "- Số ngày trôi qua (Day).\n" +
                    "- Đồng hồ thời gian thực và thời tiết hiện tại (Thời tiết tác động tới tốc độ cây trồng phát triển).\n" +
                    "- Lượng vàng (Tiền) tích lũy của bạn.\n\n" +
                    "The top-right HUD shows current day, clock, weather conditions, and accumulated gold."
                );
                break;
            case 4:
                stepTitleText.setText("Bước 4: Tương tác (Interaction)");
                descText.setText(
                    "Khi đến gần NPC, Giường ngủ (Bed), hoặc Cửa (Door), nhấn phím 'E' để thực hiện tương tác:\n" +
                    "- Ngủ trên giường giúp hồi phục 100% chỉ số HP/Thức ăn và chuyển sang ngày hôm sau.\n" +
                    "- Trò chuyện/Giao dịch với các thương nhân hoặc dân làng.\n\n" +
                    "Press 'E' to interact with doors, beds, or NPCs when nearby."
                );
                break;
            case 5:
                stepTitleText.setText("Bước 5: Vòng lặp Chăn nuôi & Giao dịch (Animal Husbandry)");
                descText.setText(
                    "Hãy làm quen với việc chăn nuôi động vật để tối đa hóa thu nhập:\n" +
                    "- Trò chuyện với Thương nhân (Trader), chọn tab \"Animals\" và mua các con thú non bằng Vàng.\n" +
                    "- Sử dụng các con thú non từ thanh công cụ để thả chúng xuống đồng cỏ ngoài trời.\n" +
                    "- Nhấn phím 'E' để kiểm tra thời gian lớn, khi chúng lớn hẳn thì nhấn phím 'E' lần nữa để thu hoạch vào túi đồ và bán lại cho Thương nhân kiếm lời.\n\n" +
                    "Talk to the Trader and navigate to the 'Animals' Tab to buy baby livestock. Use baby items from your toolbar to place them on the pasture. Press 'E' to check their growth, and harvest them when fully matured to sell back for massive profit."
                );
                if (nextBtn != null) {
                    nextBtn.setText("Hoàn thành / Finish");
                }
                break;
        }
    }

    private void finishTutorial(boolean skipped) {
        if (overlay != null) {
            FXGL.getGameScene().removeUINode(overlay);
            overlay = null;
        }
        markTutorialCompleted();
    }
}
