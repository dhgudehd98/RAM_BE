package com.sh.Ram.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ProductIndexFailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "index_fail_id")
    private Long id;
    private Long productId;
    private String originMessageId;
    private String action;
    private String failReason;
    @CreatedDate
    private LocalDateTime createdAt;

    public ProductIndexFailLog(Long productId, String originMessageId, String action, String failReason) {
        this.productId = productId;
        this.originMessageId = originMessageId;
        this.action = action;
        this.failReason = failReason;
    }
}