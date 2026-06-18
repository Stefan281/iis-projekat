package com.iis.backend.repository;

import com.iis.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientIdOrderBySentAtDesc(Long recipientId);
    List<Message> findBySenderIdOrderBySentAtDesc(Long senderId);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.recipient.id ELSE m.sender.id END " +
           "FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    List<Long> findConversationPartnerIds(@Param("userId") Long userId);

    List<Message> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderBySentAtAsc(
            Long s1, Long r1, Long s2, Long r2);

    long countByRecipientIdAndSenderIdAndReadAtIsNull(Long recipientId, Long senderId);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);
}
