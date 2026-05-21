package com.edu.service.worker;


import com.edu.domain.dto.request.worker.WorkerRequest;
import com.edu.domain.dto.response.worker.WorkerResponse;

public interface WorkerService {

    WorkerResponse.Profile getProfile(String username);

    WorkerResponse.Profile updateProfile(String username, WorkerRequest.UpdateProfile request);

    void changePassword(String username, WorkerRequest.ChangePassword request);
}
