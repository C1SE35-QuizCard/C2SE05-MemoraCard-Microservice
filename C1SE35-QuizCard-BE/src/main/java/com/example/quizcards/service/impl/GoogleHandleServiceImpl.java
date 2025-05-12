package com.example.quizcards.service.impl;

import com.example.quizcards.client.GoogleOAuth2Client;
import com.example.quizcards.client.GoogleUserInfoClient;
import com.example.quizcards.dto.GoogleInfoUser;
import com.example.quizcards.dto.request.GoogleLoginRequest;
import com.example.quizcards.dto.response.GoogleAccessTokenResponse;
import com.example.quizcards.dto.response.GoogleUserInfoResponse;
import com.example.quizcards.service.IGoogleHandleService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class GoogleHandleServiceImpl implements IGoogleHandleService {
    private final ClientRegistration clientRegistration;
    private final GoogleOAuth2Client googleOAuth2Client;
    private final GoogleUserInfoClient googleUserInfoClient;

    public GoogleHandleServiceImpl(ClientRegistrationRepository clientRegistrationRepository,
                                   GoogleOAuth2Client googleOAuth2Client,
                                   GoogleUserInfoClient googleUserInfoClient) {
        this.googleOAuth2Client = googleOAuth2Client;
        this.googleUserInfoClient = googleUserInfoClient;
        this.clientRegistration = clientRegistrationRepository.findByRegistrationId("google");
    }

    private MultiValueMap<String, String> getGoogleOAuth2Params(String code) {
        MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientRegistration.getClientId());
        params.add("client_secret", clientRegistration.getClientSecret());

        params.add("redirect_uri", "postmessage");
        params.add("grant_type", "authorization_code");
        return params;
    }

    private String getAccessTokenFromCode(String code) {
        GoogleAccessTokenResponse tokenResponse = googleOAuth2Client.getAccessToken(
                code,
                clientRegistration.getClientId(),
                clientRegistration.getClientSecret(),
                "postmessage",
                "authorization_code"
        );
        return tokenResponse.getAccess_token();
    }

    private GoogleUserInfoResponse getInfoUserFromAccessToken(String accessToken) {
        return googleUserInfoClient.getUserInfo(accessToken);
    }

    @Override
    public GoogleInfoUser extractDataFromCode(GoogleLoginRequest request) throws Exception {
        if (clientRegistration == null) {
            throw new Exception("ERROR: Cannot find Google service data.");
        }

        String code = request.getCode();
        GoogleUserInfoResponse userResponse = getInfoUserFromAccessToken(getAccessTokenFromCode(code));

        GoogleInfoUser userInfo = GoogleInfoUser
                .builder()
                .email(userResponse.getEmail())
                .avatarUrl(userResponse.getPicture())
                .firstName(userResponse.getGiven_name())
                .lastName(userResponse.getFamily_name())
                .sub(userResponse.getSub())
                .build();

        int atIndex = userResponse.getEmail().indexOf("@");

        if (atIndex != -1) {
            userInfo.setUserName(userResponse.getEmail().substring(0, atIndex));
        } else {
            userInfo.setUserName(userResponse.getEmail());
        }

        userInfo.setEnabled(userResponse.isEmail_verified());

        return userInfo;
    }
}
