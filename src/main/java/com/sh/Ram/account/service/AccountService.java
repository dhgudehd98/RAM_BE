package com.sh.Ram.account.service;

import com.sh.Ram.account.dto.AccountDto;
import com.sh.Ram.account.dto.AccountRegisterRequestDto;
import com.sh.Ram.account.dto.AccountRequestDto;
import com.sh.Ram.account.repository.AccountRepository;
import com.sh.Ram.apick.BankCode;
import com.sh.Ram.apick.repository.ApickRepository;
import com.sh.Ram.common.exception.account.AccountException;
import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Account;
import com.sh.Ram.entity.Member;
import com.sh.Ram.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ApickRepository apickRepository;
    private final MemberRepository memberRepository;



    @Transactional(readOnly = true)
    public AccountDto getAccount(Long memberId) {
        return accountRepository.findByMemberId2(memberId);
    }

    @Transactional
    public Map<String, String> chargeAccount(Long memberId, AccountRequestDto req) {

        Account account = accountRepository.findByIdAndMemberId(req.getAccountId(), memberId).orElseThrow(() -> new AccountException("해당 계좌가 존재하지 않습니다."));

        account.setAccountBalance(account.getAccountBalance() + req.getAmount());
        accountRepository.save(account);

        return Map.of(
                "result", "Y",
                "msg", "성공적으로 충전이 완료되었습니다."
        );

    }

    @Transactional
    public Map<String, String> registerAccount(Long memberId, AccountRegisterRequestDto req) {

        String accountNum = normalizedAccount(req.accountNum());

        if (!apickRepository.isVerified(memberId)) {
            throw new AccountException("계좌 인증이 필요합니다. 1원 인증을 먼저 완료해주세요.");
        }

        if (accountRepository.existsByMemberIdAndAccountNum(memberId, accountNum)) {
            throw new AccountException("이미 등록된 계좌입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("회원이 존재하지 않습니다."));

        BankCode bank = resolveBank(req.bankCode(), req.bankName());

        // account 저장
        Account account = new Account();
        account.setMember(member);
        account.setAccountNum(accountNum);
        account.setBankCode(bank.getCode());
        account.setBankName(bank.getName());
        account.setAccountBalance(1_000_000L);

        Account saved = accountRepository.save(account);

        // 회원 계좌 인증 변경 (false -> true)
        member.verifyAccount();
        member.registerAccount(saved);

        apickRepository.deleteVerified(memberId);

        return Map.of(
                "result", "Y",
                "msg", "성공적으로 계좌가 등록되었습니다."
        );
    }

    /**
     * 계좌 삭제
     * @param memberId
     * @return
     */
    @Transactional
    public Map<String, String> deleteAccount(Long memberId) {

        Account account = accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AccountException("계좌가 존재하지 않습니다."));

        // 잔액 체크
        if (account.getAccountBalance() > 0) {
            throw new AccountException("잔액이 남아있는 계좌는 삭제할 수 없습니다.");
        }

        Member member = account.getMember();
        member.deleteAccount();

        accountRepository.delete(account);

        return Map.of(
                "result", "Y",
                "msg", "성공적으로 계좌가 삭제되었습니다."
        );
    }

    /**
     * 계좌번호 형식 통일 메소드
     * @param account
     * @return 공백없는 문자열
     */
    private String normalizedAccount(String account) {
        if (account == null) return "";
        return account.replaceAll("[^0-9]", "");
    }

    private BankCode resolveBank(String bankCode, String bankName) {
        if (bankCode != null & !bankCode.isBlank()) {
            return BankCode.fromCode(bankCode);
        }
        if (bankName != null & !bankName.isBlank()) {
            return BankCode.fromName(bankName);
        }
        throw new AccountException("bankCode 또는 bankName은 필수입니다.");
    }
}
