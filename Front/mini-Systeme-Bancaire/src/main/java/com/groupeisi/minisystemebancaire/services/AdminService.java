package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.AdminDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class AdminService extends ApiService {

    public List<AdminDTO> getAllAdmins() {
        HttpRequest request = createRequest("/admins").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<AdminDTO>>(){}.getType());
    }

    public AdminDTO getAdminById(Long id) {
        HttpRequest request = createRequest("/admins/" + id).GET().build();
        return sendRequest(request, AdminDTO.class);
    }

    public AdminDTO createAdmin(AdminDTO admin) {
        String json = gson.toJson(admin);
        HttpRequest request = createRequest("/admins")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, AdminDTO.class);
    }

    public AdminDTO updateAdmin(AdminDTO admin) {
        String json = gson.toJson(admin);
        HttpRequest request = createRequest("/admins/" + admin.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, AdminDTO.class);
    }

    public void deleteAdmin(Long id) {
        HttpRequest request = createRequest("/admins/" + id).DELETE().build();
        sendRequestForString(request);
    }

    public AdminDTO login(String username, String password) {
        String json = gson.toJson(new LoginRequest(username, password));
        HttpRequest request = createRequest("/admins/login")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, AdminDTO.class);
    }

    private static class LoginRequest {
        private final String username;
        private final String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
