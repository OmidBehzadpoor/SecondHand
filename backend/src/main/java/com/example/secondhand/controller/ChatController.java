package com.example.secondhand.controller;

import com.example.secondhand.dto.SendMessageRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.ConversationResponse;
import com.example.secondhand.dto.response.MessageResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h2>ChatController</h2>
 * <p>
 * کنترلر مسئول <b>مکالمه و پیام‌رسانی</b> بین خریدار و فروشنده، در ارتباط با
 * یک آگهی مشخص. تمام اندپوینت‌های این کنترلر تحت مسیر پایه {@code /api/chat}
 * قرار دارند و نیاز به احراز هویت دارند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.ChatService
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "مکالمه و پیام بین خریدار و فروشنده")
public class ChatController {

    private final ChatService chatService;


    /**
     * شروع یک گفت‌وگوی جدید با فروشنده‌ی یک آگهی، یا بازگرداندن گفت‌وگوی موجود.
     *
     * @param adId        شناسه آگهی‌ای که گفت‌وگو درباره‌ی آن شروع می‌شود
     * @param currentUser کاربر جاری (خریدار)
     * @return {@link ResponseEntity} حاوی {@link ConversationResponse} گفت‌وگوی جدید یا موجود
     */
    @PostMapping("/conversations/advertisement/{adId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> startOrGetConversation(
            @PathVariable Long adId,
            @AuthenticationPrincipal User currentUser) {
        ConversationResponse response = chatService.startOrGetConversation(adId, currentUser);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "CONVERSATION_READY", response));
    }


    /**
     * دریافت لیست تمام گفت‌وگوهای کاربر جاری.
     *
     * @param currentUser کاربر جاری
     * @return {@link ResponseEntity} حاوی لیست {@link ConversationResponse} مرتبط با کاربر جاری
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @AuthenticationPrincipal User currentUser) {
        List<ConversationResponse> responses = chatService.getMyConversations(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "CONVERSATIONS_RETRIEVED", responses));
    }


    /**
     * دریافت تمام پیام‌های یک گفت‌وگو.
     *
     * @param conversationId شناسه گفت‌وگویی که پیام‌های آن باید دریافت شود
     * @param currentUser    کاربر جاری (باید عضو گفت‌وگو باشد)
     * @return {@link ResponseEntity} حاوی لیست {@link MessageResponse} گفت‌وگو
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {
        List<MessageResponse> responses = chatService.getMessages(conversationId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "MESSAGES_RETRIEVED", responses));
    }


    /**
     * ارسال یک پیام جدید در یک گفت‌وگوی موجود.
     *
     * @param conversationId شناسه گفت‌وگویی که پیام باید در آن ارسال شود
     * @param request        محتوای پیام ارسالی
     * @param currentUser    کاربر جاری (باید عضو گفت‌وگو باشد)
     * @return {@link ResponseEntity} با کد وضعیت {@code 201 CREATED} و اطلاعات پیام ذخیره‌شده
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        MessageResponse response = chatService.sendMessage(conversationId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "MESSAGE_SENT", response));
    }
}
