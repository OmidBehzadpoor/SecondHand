package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.model.SellerRatingRequest;
import com.example.secondhandfx.model.SellerRatingResponse;
import com.example.secondhandfx.model.SendMessageRequest;
import com.example.secondhandfx.service.ChatService;
import com.example.secondhandfx.service.ChatServiceImpl;
import com.example.secondhandfx.service.RatingService;
import com.example.secondhandfx.service.RatingServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * کنترلر صفحه‌ی چت (Chat View)
 * وظیفه: نمایش پیام‌های یک گفتگو، ارسال پیام جدید، و امتیازدهی به فروشنده
 */
public class ChatViewController {

    // ====== المان‌های گرافیکی (تزریق شده توسط FXMLLoader) ======

    @FXML
    private Label headerLabel;          // عنوان گفتگو (عنوان آگهی + نام طرف مقابل)

    @FXML
    private Button rateSellerButton;    // دکمه‌ی امتیازدهی به فروشنده (فقط برای خریدار)

    @FXML
    private ListView<MessageResponse> messageListView;  // لیست پیام‌ها

    @FXML
    private TextField messageInputField; // کادر ورودی پیام جدید


    // ====== سرویس‌ها و متغیرهای داخلی ======

    private final ChatService chatService = new ChatServiceImpl();
    private final RatingService ratingService = new RatingServiceImpl();

    // لیست قابل مشاهده برای نمایش در ListView (هر تغییری به‌صورت خودکار منعکس می‌شود)
    private final ObservableList<MessageResponse> messages = FXCollections.observableArrayList();

    private Long conversationId;    // شناسه‌ی گفتگوی فعلی
    private Long advertisementId;   // شناسه‌ی آگهی مرتبط (برای امتیازدهی)
    private boolean isBuyer;        // آیا کاربر فعلی خریدار است؟ (برای نمایش دکمه‌ی امتیاز)


    // ====== متدهای生命周期 ======

    /**
     * بعد از بارگذاری FXML اجرا می‌شود.
     * تنظیمات اولیه: اتصال ListView به ObservableList و تنظیم رویداد Enter برای کادر ورودی
     */
    @FXML
    public void initialize() {
        // تعیین نحوه‌ی نمایش هر پیام (با استفاده از MessageListCell سفارشی)
        messageListView.setCellFactory(listView -> new MessageListCell());

        // اتصال لیست داده به ListView
        messageListView.setItems(messages);

        // با فشردن کلید Enter در کادر ورودی، پیام ارسال شود
        messageInputField.setOnAction(event -> onSendButtonClick());
    }

    /**
     * این متد توسط کنترلر والد (مثلاً ConversationListController) فراخوانی می‌شود
     * تا اطلاعات گفتگوی انتخاب‌شده را به این کنترلر منتقل کند.
     *
     * @param conversation اطلاعات گفتگوی انتخاب‌شده
     */
    public void setConversation(ConversationResponse conversation) {
        // ذخیره‌ی شناسه‌ها
        this.conversationId = conversation.getId();
        this.advertisementId = conversation.getAdvertisementId();

        // تشخیص نقش کاربر فعلی (خریدار یا فروشنده)
        Long currentUserId = SessionManager.getInstance().getUserId();
        this.isBuyer = conversation.getBuyerId().equals(currentUserId);

        // ساخت عنوان: "عنوان آگهی — نام طرف مقابل"
        String otherPartyUsername = isBuyer
                ? conversation.getSellerUsername()
                : conversation.getBuyerUsername();
        headerLabel.setText(conversation.getAdvertisementTitle() + " — " + otherPartyUsername);

        // فقط خریدار می‌تواند به فروشنده امتیاز دهد
        rateSellerButton.setVisible(isBuyer);
        rateSellerButton.setManaged(isBuyer);

        // بارگذاری پیام‌های این گفتگو
        loadMessages();
    }


    // ====== عملیات مربوط به پیام‌ها ======

    /**
     * بارگذاری پیام‌های گفتگوی فعلی از سرور (غیرهمزمان)
     */
    private void loadMessages() {
        System.out.println("🔄 بارگذاری پیام‌های گفتگو: " + conversationId);

        Task<List<MessageResponse>> loadTask = new Task<>() {
            @Override
            protected List<MessageResponse> call() throws Exception {
                System.out.println("📡 ارسال درخواست به سرور برای دریافت پیام‌ها");
                return chatService.getMessages(conversationId);
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<MessageResponse> messageList = loadTask.getValue();
            System.out.println("✅ تعداد پیام‌های دریافت‌شده: " + messageList.size());
            messages.setAll(messageList);
            messageListView.refresh(); // ← برای اطمینان از رندر مجدد
        });

        loadTask.setOnFailed(event -> {
            Throwable ex = loadTask.getException();
            System.err.println("❌ خطا در دریافت پیام‌ها: " + ex.getMessage());
            ex.printStackTrace();
            String userMessage = (ex instanceof ApiException)
                    ? ex.getMessage()
                    : "خطای ناشناخته‌ای در دریافت پیام‌ها رخ داد.";
            AlertUtil.showError(userMessage);
        });

        // اجرای Task در یک ترد جداگانه (غیرهمزمان)
        new Thread(loadTask).start();
    }

    /**
     * ارسال پیام جدید (با کلیک روی دکمه یا فشردن Enter)
     */
    @FXML
    private void onSendButtonClick() {
        String content = messageInputField.getText().trim();
        System.out.println("📤 ارسال پیام: " + content);

        if (content.isEmpty()) {
            return;  // پیام خالی ارسال نمی‌شود
        }

        // ساخت درخواست ارسال پیام
        SendMessageRequest request = SendMessageRequest.builder()
                .content(content)
                .build();

        Task<MessageResponse> sendTask = new Task<>() {
            @Override
            protected MessageResponse call() throws Exception {
                System.out.println("📡 ارسال درخواست به سرور برای ثبت پیام");
                return chatService.sendMessage(conversationId, request);
            }
        };

        sendTask.setOnSucceeded(event -> {
            MessageResponse sentMessage = sendTask.getValue();
            System.out.println("✅ پیام با موفقیت ارسال شد: " + sentMessage.getContent());

            messages.add(sentMessage);
            messageListView.refresh(); // ← برای نمایش فوری پیام جدید
            messageInputField.clear();
        });

        sendTask.setOnFailed(event -> {
            Throwable ex = sendTask.getException();
            System.err.println("❌ خطا در ارسال پیام: " + ex.getMessage());
            ex.printStackTrace();
            String userMessage = (ex instanceof ApiException)
                    ? ex.getMessage()
                    : "خطای ناشناخته‌ای در ارسال پیام رخ داد.";
            AlertUtil.showError(userMessage);
        });

        new Thread(sendTask).start();
    }


    // ====== دکمه‌های ناوبری ======

    /**
     * بازگشت به لیست گفتگوها
     */
    @FXML
    private void onBackClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/conversation-list.fxml", "گفتگوها");
    }


    // ====== امتیازدهی به فروشنده ======

    /**
     * نمایش دیالوگ امتیازدهی به فروشنده (فقط برای خریداران)
     */
    @FXML
    private void onRateSellerClick() {
        Dialog<SellerRatingRequest> dialog = new Dialog<>();
        dialog.setHeaderText("امتیازدهی به فروشنده");

        // انتخاب امتیاز (۱ تا ۵)
        ChoiceBox<Integer> ratingChoice = new ChoiceBox<>();
        ratingChoice.getItems().addAll(1, 2, 3, 4, 5);
        ratingChoice.setValue(5);

        // نظر اختیاری
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("نظر شما (اختیاری)");
        commentArea.setPrefRowCount(3);

        // چیدمان محتوای دیالوگ
        VBox content = new VBox(10,
                new Label("امتیاز (از ۱ تا ۵):"), ratingChoice,
                new Label("نظر:"), commentArea);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // تبدیل نتیجه‌ی دیالوگ به درخواست امتیاز
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return SellerRatingRequest.builder()
                        .rating(ratingChoice.getValue())
                        .comment(commentArea.getText())
                        .build();
            }
            return null;
        });

        // اگر کاربر OK را زد، درخواست امتیاز به سرور ارسال می‌شود
        dialog.showAndWait().ifPresent(request -> {
            Task<SellerRatingResponse> task = new Task<>() {
                @Override
                protected SellerRatingResponse call() throws Exception {
                    return ratingService.rateAdvertisement(advertisementId, request);
                }
            };

            task.setOnSucceeded(e -> AlertUtil.showSuccess("امتیاز شما ثبت شد."));

            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                ex.printStackTrace();
                String message = (ex instanceof ApiException)
                        ? ex.getMessage()
                        : "خطا در ثبت امتیاز رخ داد.";
                AlertUtil.showError(message);
            });

            new Thread(task).start();
        });
    }
}