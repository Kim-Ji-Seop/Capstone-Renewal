package com.umc.jaetteoli.domain.web.sms.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "Sms")
public class Sms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long smsIdx;

    @Column(name = "phone", nullable = false, length = 100)
    private String phone;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "uid", nullable = false, length = 100)
    private String uid;

    @Column(name = "certification_num",nullable = true, length = 100)
    private String certificationNum;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime updated;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;
    public enum Status {
        SIGN_UP("S"),
        ID_SEARCH("I"), // 판매자 - ID찾기
        PW_SEARCH("P"), // 판매자 - PW찾기
        ID_CUSTOMER_SEARCH("IC"), // 소비자 - ID찾기
        PW_CUSTOMER_SEARCH("PC"), // 소비자 - PW찾기
        DONE_OR_EXPIRED("D"); // 찾기완료 혹은 인증번호 만료

        private String code;

        Status(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    @Override
    public String toString() {
        return "Sms{" +
                "smsIdx=" + smsIdx +
                ", phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", uid='" + uid + '\'' +
                ", certificationNum='" + certificationNum + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", status=" + status +
                '}';
    }
}
