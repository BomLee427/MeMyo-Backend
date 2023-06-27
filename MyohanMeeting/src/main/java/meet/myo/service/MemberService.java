package meet.myo.service;

import lombok.RequiredArgsConstructor;
import meet.myo.domain.EmailCertification;
import meet.myo.domain.Member;
import meet.myo.domain.Oauth;
import meet.myo.domain.OauthType;
import meet.myo.dto.request.member.*;
import meet.myo.dto.response.member.EmailUpdateResponseDto;
import meet.myo.dto.response.member.MemberResponseDto;
import meet.myo.dto.response.member.MemberUpdateResponseDto;
import meet.myo.dto.response.member.OauthUpdateResponseDto;
import meet.myo.exception.NotFoundException;
import meet.myo.repository.EmailCertificationRepository;
import meet.myo.repository.MemberRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailCertificationRepository emailCertificationRepository;

    /**
     * 회원정보 조회
     */
    @Transactional(readOnly = true)
    public MemberResponseDto getMemberById(Long id) {
        return MemberResponseDto.fromEntity(
                memberRepository.findByIdAndDeletedAtNull(id).orElseThrow(
                        () -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다.")));
    }

    /**
     * 회원가입(직접)
     */
    public Long directJoin(MemberDirectCreateRequestDto dto) {
        validateEmailDuplication(dto.getEmail());
        validateNickNameDuplication(dto.getNickName());

        String encoded = passwordEncoder.encode(dto.getPassword());
        Member member = Member.directJoinBuilder()
                .email(dto.getEmail())
                .password(encoded)
                .nickName(dto.getNickName())
                .phoneNumber(dto.getPhoneNumber())
                .build();

        return memberRepository.save(member).getId();
    }

    /**
     * 회원가입(SNS)
     */
    public Long oauthJoin(MemberOauthCreateRequestDto dto) {

        validateEmailDuplication(dto.getEmail());

        Member.OauthJoinMemberBuilder memberBuilder = Member.oauthJoinBuilder()
                .oauthType(dto.getOauthType() != null ? OauthType.valueOf(dto.getOauthType()) : null)
                .oauthId(dto.getOauthId())
                .email(dto.getEmail());
        if (dto.getNickName() != null) {
            validateNickNameDuplication(dto.getNickName());
            memberBuilder.nickName(dto.getNickName());
        } else {
            String randomNickname = createRandomNickname();
            validateNickNameDuplication(randomNickname);
            memberBuilder.nickName(randomNickname);
        }
        Member member = memberBuilder.build();

        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 이메일 중복체크
     */
    public void emailDuplicationCheck(EmailDuplicationCheckRequestDto dto) {
        validateEmailDuplication(dto.getEmail());
    }

    /**
     * 닉네임 중복체크
     */
    public void nickNameDuplicationCheck(NickNameDuplicationCheckRequestDto dto) {
        validateNickNameDuplication(dto.getNickName());
    }

    /**
     * 이메일 인증용 메일발송 및 UUID 저장
     */
    public void sendCertificationEmail(Long memberId) { // TODO: 리턴항목 생각
        Member member = memberRepository.findByIdAndDeletedAtNull(memberId)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다."));
        // 메일은 이곳에서 발송
        EmailCertification emailCertification = EmailCertification.createEmailCertification(member);
        emailCertificationRepository.save(emailCertification);
    }

    /**
     * 이메일 인증 UUID 비교
     */
    public void verifyCertificationEmail(Long memberId, CertifyEmailRequestDto dto) {
        EmailCertification latestCertification =
                emailCertificationRepository.findLatestByMemberIdAndUUIDAndDeletedAtNull(memberId, dto.getUUID())
                .orElseThrow(() -> new NotFoundException("해당하는 이메일을 찾을 수 없습니다."));

        if (latestCertification.isExpired()) {
            throw new RuntimeException("UUID가 만료되었습니다.");
        }

    }

    /**
     * 이메일 수정
     */
    @Transactional
    public EmailUpdateResponseDto updateEmail(Long memberId, EmailUpdateRequestDto dto) {
        String newEmail = dto.getNewEmail();
        validateEmailDuplication(newEmail); // 이메일 중복체크 추가

        Member member = memberRepository.findByIdAndDeletedAtNull(memberId)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다."));
        member.updateEmail(newEmail);

        return EmailUpdateResponseDto.fromEntity(member);
    }


    /**
     * Oauth 수정
     */
    public OauthUpdateResponseDto updateOauth(Long memberId, OauthUpdateRequestDto dto) {
        Member member = memberRepository.findByIdAndDeletedAtNull(memberId)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다."));

        Oauth updatedOauth = Oauth.createOauth(OauthType.valueOf(dto.getOauthType()), dto.getOauthId()); //TODO: null check
        member.updateOauth(updatedOauth);

        return OauthUpdateResponseDto.fromEntity(member);
    }

    /**
     * Oauth 삭제
     */
    public void deleteOauth(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtNull(memberId)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다."));

        member.updateOauth(null);
    }


    /**
     * 비밀번호 수정
     */
    public void updatePassword(Long memberId, PasswordUpdateRequestDto dto) {

        Member member = memberRepository.findByIdAndDeletedAtNull(memberId)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다."));

        String currentPassword = dto.getCurrentPassword();
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        member.updatePassword(encodedPassword);
    }


    /**
     * 개인정보 수정
     */
    public MemberUpdateResponseDto updateMember(Long memberId, MemberUpdateRequestDto dto) {
        Member member = memberRepository.findByIdAndDeletedAtNull(memberId)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다."));

        member.updateNickName(dto.getNickName());
        member.updatePhoneNumber(dto.getPhoneNumber());

        return MemberUpdateResponseDto.fromEntity(member);
    }


    /**
     * 탈퇴
     */
    public Long resign(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtNull(memberId)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 회원을 찾을 수 없습니다."));
        member.delete();
        
        return member.getId();
    }

    /**
     * 중복체크 유틸
     */

    private void validateEmailDuplication(String email) throws DuplicateKeyException {
        memberRepository.findByEmailAndDeletedAtNull(email)
                .ifPresent(m -> {
                    throw new DuplicateKeyException("이미 같은 이메일이 존재합니다.");
                });
    }

    private void validateNickNameDuplication(String nickName) throws DuplicateKeyException {
        memberRepository.findByNickNameAndDeletedAtNull(nickName)
                .ifPresent(m -> {
                    throw new DuplicateKeyException("이미 같은 닉네임이 존재합니다.");
                });
    }

    private String createRandomNickname() {
        return "nickname" + UUID.randomUUID();
    }

}