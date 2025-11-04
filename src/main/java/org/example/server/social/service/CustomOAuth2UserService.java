package org.example.server.social.service;

import lombok.RequiredArgsConstructor;
import org.example.server.chat.entity.User;
import org.example.server.chat.respository.UserRepository;
import org.example.server.social.dto.CustomOAuth2User;
import org.example.server.social.dto.NaverResponse;
import org.example.server.social.dto.OAuth2Response;
import org.example.server.social.dto.UserDTO;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        System.out.println("========================================");
        System.out.println("=== loadUser 메서드 실행 시작!!! ===");
        System.out.println("========================================");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("oAuth2User 출력: " + oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("Provider: " + registrationId);

        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
            System.out.println("네이버 응답 파싱 완료");
            System.out.println("- Email: " + oAuth2Response.getEmail());
            System.out.println("- Name: " + oAuth2Response.getName());
            System.out.println("- Gender: " + oAuth2Response.getGender());
            System.out.println("- Nickname: " + oAuth2Response.getNickname());
        }
        else {
            System.out.println("지원하지 않는 Provider: " + registrationId);
            return null;
        }

        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        System.out.println("생성된 username: " + username);

        User existData = userRepository.findByUsername(username);
        System.out.println("데이터 조회 완료!! 기존 사용자: " + (existData != null ? "있음" : "없음"));

        if (existData == null) {
            System.out.println("=== 신규 사용자 등록 시작 ===");

            User userEntity = new User();
            userEntity.setUsername(username);
            userEntity.setEmail(oAuth2Response.getEmail());
            userEntity.setName(oAuth2Response.getName());
            userEntity.setGender(oAuth2Response.getGender());
            userEntity.setNickname(oAuth2Response.getNickname());
            userEntity.setRole("ROLE_USER");

            System.out.println("저장할 데이터:");
            System.out.println("  - username: " + userEntity.getUsername());
            System.out.println("  - email: " + userEntity.getEmail());
            System.out.println("  - name: " + userEntity.getName());
            System.out.println("  - gender: " + userEntity.getGender());
            System.out.println("  - nickname: " + userEntity.getNickname());
            System.out.println("  - role: " + userEntity.getRole());

            User savedUser = userRepository.save(userEntity);
            System.out.println("DB 저장 완료! userId: " + savedUser.getUserId());

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setUserId(savedUser.getUserId());
            userDTO.setRole("ROLE_USER");

            System.out.println("=== 신규 사용자 등록 완료 ===");
            return new CustomOAuth2User(userDTO);
        }
        else {
            System.out.println("=== 기존 사용자 정보 업데이트 시작 ===");

            existData.setEmail(oAuth2Response.getEmail());
            existData.setName(oAuth2Response.getName());
            existData.setGender(oAuth2Response.getGender());
            existData.setNickname(oAuth2Response.getNickname());

            System.out.println("업데이트할 데이터:");
            System.out.println("  - email: " + existData.getEmail());
            System.out.println("  - name: " + existData.getName());
            System.out.println("  - gender: " + existData.getGender());
            System.out.println("  - nickname: " + existData.getNickname());

            User updatedUser = userRepository.save(existData);
            System.out.println("DB 업데이트 완료!");

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.getRole());
            userDTO.setUserId(existData.getUserId());

            System.out.println("=== 기존 사용자 정보 업데이트 완료 ===");
            return new CustomOAuth2User(userDTO);
        }
    }
}