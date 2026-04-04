package com.sh.Ram.entity;

import com.sh.Ram.auth.dto.LoginRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private String email;

    private String password;

    private String nickname;

    private String phone;

    private String address;

    private boolean isAccount;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    @OneToMany(mappedBy = "member")
    private List<WishList> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Product> product = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Bid> bid = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Notification> notifications = new ArrayList<>();

    // 여기는 나중에 암호화해서 저장
    public Member(LoginRequestDto loginRequestDto, String password) {
        this.name = loginRequestDto.getName();
        this.email = loginRequestDto.getEmail();
        this.nickname = loginRequestDto.getNickName();
        this.phone = loginRequestDto.getPhone();
        this.password = password;
    }

    public void verifyAccount() {
        this.isAccount = true;
    }

    public void registerAccount(Account account) {
        this.account = account;
    }
}
