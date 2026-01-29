package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;

import java.util.List;

public interface ParkingSessionService {

    List<ParkingSessionDto> getActiveSessions();

    List<ParkingSessionDto> getSessionHistory();
}
