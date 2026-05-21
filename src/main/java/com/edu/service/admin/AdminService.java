package com.edu.service.admin;

import com.edu.domain.dto.request.admin.AdminRequest;
import com.edu.domain.dto.response.admin.AdminResponse;

public interface AdminService {

    AdminResponse.UserDetail createUser(AdminRequest.CreateUser request);

    AdminResponse.UserList getAllUsers();

    AdminResponse.UserDetail getUserById(Long id);

    AdminResponse.UserDetail promoteToAdmin(Long id);

    AdminResponse.UserDetail disableUser(Long id);

    AdminResponse.UserDetail enableUser(Long id);

    void deleteUser(Long id);

    AdminResponse.RoleList getAllRoles();
}