package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.service.ChatService;
import com.example.secondhandfx.service.ChatServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * <h2>ConversationListController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>فهرست گفت‌وگوها</b>ی کاربر جاری. لیست گفت‌وگوها را از
 * بک‌اند به‌صورت غیرهمزمان دریافت کرده و با استفاده از
 * {@link ConversationListCell} در یک {@link ListView} نمایش می‌دهد. با کلیک
 * روی هر گفت‌وگو، کاربر به صفحه‌ی چت مربوط به همان گفت‌وگو هدایت می‌شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.ChatService
 * @see com.example.secondhandfx.controller.ChatViewController
 */
public class ConversationListController implements Initializable {

    @FXML
    private ListView<ConversationResponse> conversationListView;

    private final ChatService chatService = new ChatServiceImpl();

    /**
     * مقداردهی اولیه‌ی صفحه پس از بارگذاری FXML.
     * <p>
     * سلول سفارشی {@link ConversationListCell} برای نمایش هر ردیف ثبت
     * می‌شود، رویداد کلیک روی یک گفت‌وگوی انتخاب‌شده به
     * {@link #onConversationSelected(ConversationResponse)} متصل می‌شود، و
     * در نهایت لیست گفت‌وگوها از سرور بارگذاری می‌شود.
     * </p>
     *
     * @param location  آدرس مورد استفاده برای تفکیک مسیرهای نسبی در فایل FXML (استفاده‌نشده)
     * @param resources منابع بین‌المللی‌سازی (استفاده‌نشده)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conversationListView.setCellFactory(listView -> new ConversationListCell());

        conversationListView.setOnMouseClicked(event -> {
            ConversationResponse selected = conversationListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onConversationSelected(selected);
            }
        });

        loadConversations();
    }

    /**
     * پردازش کلیک روی دکمه‌ی بازگشت، و هدایت کاربر به صفحه‌ی آگهی‌ها.
     */
    @FXML
    private void onBackClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    /**
     * بارگذاری غیرهمزمان لیست گفت‌وگوهای کاربر جاری از بک‌اند و نمایش آن‌ها
     * در {@link #conversationListView}.
     * <p>
     * در صورت بروز خطا، پشته‌ی خطا در کنسول چاپ شده و پیام مناسبی به کاربر
     * نمایش داده می‌شود.
     * </p>
     */
    private void loadConversations() {
        Task<List<ConversationResponse>> loadTask = new Task<>() {
            @Override
            protected List<ConversationResponse> call() throws Exception {
                return chatService.getMyConversations();
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<ConversationResponse> conversations = loadTask.getValue();
            conversationListView.setItems(FXCollections.observableArrayList(conversations));
        });

        loadTask.setOnFailed(event -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();
            String message = (ex instanceof ApiException)
                    ? ex.getMessage()
                    : "خطای ناشناخته‌ای در دریافت گفتگوها رخ داد.";
            AlertUtil.showError(message);
        });

        new Thread(loadTask).start();
    }

    /**
     * پردازش انتخاب یک گفت‌وگو از فهرست: هدایت کاربر به صفحه‌ی چت و مقداردهی
     * گفت‌وگوی انتخاب‌شده در کنترلر آن صفحه.
     *
     * @param conversation گفت‌وگویی که کاربر روی آن کلیک کرده است
     */
    private void onConversationSelected(ConversationResponse conversation) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/chat-view.fxml", "چت");
        ChatViewController controller = loader.getController();
        controller.setConversation(conversation);
    }
}
