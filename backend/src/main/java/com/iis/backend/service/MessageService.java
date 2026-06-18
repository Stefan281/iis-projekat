package com.iis.backend.service;

import com.iis.backend.model.Message;
import com.iis.backend.model.User;
import com.iis.backend.repository.MessageRepository;
import com.iis.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConversations(Authentication auth) {
        User currentUser = getCurrentUser(auth);
        List<Long> partnerIds = messageRepository.findConversationPartnerIds(currentUser.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long partnerId : partnerIds) {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("userId", partner.getId());
            item.put("firstName", partner.getFirstName());
            item.put("lastName", partner.getLastName());
            item.put("unreadCount", messageRepository
                    .countByRecipientIdAndSenderIdAndReadAtIsNull(currentUser.getId(), partner.getId()));
            result.add(item);
        }
        return result;
    }

    @Transactional
    public List<Map<String, Object>> getChat(Long otherUserId, Authentication auth) {
        User currentUser = getCurrentUser(auth);

        List<Message> messages = messageRepository
                .findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderBySentAtAsc(
                        currentUser.getId(), otherUserId,
                        otherUserId, currentUser.getId());

        for (Message m : messages) {
            if (m.getRecipient().getId().equals(currentUser.getId()) && m.getReadAt() == null) {
                m.setReadAt(LocalDateTime.now());
                messageRepository.save(m);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Message m : messages) {
            result.add(toMessageMap(m));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> send(Map<String, Object> body, Authentication auth) {
        User currentUser = getCurrentUser(auth);
        Long recipientId = Long.valueOf(body.get("recipientId").toString());
        String text = body.get("text").toString();

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Message msg = new Message();
        msg.setSender(currentUser);
        msg.setRecipient(recipient);
        msg.setText(text);
        msg.setSentAt(LocalDateTime.now());
        messageRepository.save(msg);

        return toMessageMap(msg);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllUsers(Authentication auth) {
        User currentUser = getCurrentUser(auth);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (u.getId().equals(currentUser.getId())) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("userId", u.getId());
            item.put("firstName", u.getFirstName());
            item.put("lastName", u.getLastName());
            item.put("role", u.getRole());
            result.add(item);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadCount(Authentication auth) {
        User currentUser = getCurrentUser(auth);
        long count = messageRepository.countByRecipientIdAndReadAtIsNull(currentUser.getId());
        return Map.of("count", count);
    }

    private Map<String, Object> toMessageMap(Message m) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", m.getId());
        item.put("senderId", m.getSender().getId());
        item.put("text", m.getText());
        item.put("sentAt", m.getSentAt());
        return item;
    }

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
