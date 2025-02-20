package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.calendar.LeaveRequestDto;
import com.ddimitko.beautyshopproject.entities.Employee;
import com.ddimitko.beautyshopproject.entities.calendar.LeaveRequest;
import com.ddimitko.beautyshopproject.repositories.EmployeeRepository;
import com.ddimitko.beautyshopproject.repositories.LeaveRequestRepository;
import com.ddimitko.beautyshopproject.repositories.redisServices.RedisLeaveService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class LeaveService {

    private final RedisLeaveService redisLeaveService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(RedisLeaveService redisLeaveService, LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
        this.redisLeaveService = redisLeaveService;
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    public void requestLeave(LeaveRequestDto leaveRequestDto) {

        if(employeeRepository.findById(leaveRequestDto.getEmployeeId()).isEmpty()){
            throw new RuntimeException("Employee not found");
        }

        if(leaveRequestDto.getStartDate().isAfter(leaveRequestDto.getEndDate()) ||
                leaveRequestDto.getStartDate() == null || leaveRequestDto.getEndDate() == null) {
            throw new RuntimeException("leave date is empty");
        }

        String leaveKey = "leave:" + leaveRequestDto.getEmployeeId() + ":" + leaveRequestDto.getStartDate();
        leaveRequestDto.setStatus(LeaveRequest.LeaveStatus.PENDING.name());
        redisLeaveService.saveLeaveRequest(leaveKey, leaveRequestDto);
        redisLeaveService.addToEmployeeIndex(leaveRequestDto.getEmployeeId(), leaveKey);

    }

    public List<LeaveRequestDto> getLeaveRequestsFromRedis(long employeeId) {

        if(employeeRepository.findById(employeeId).isEmpty()){
            throw new RuntimeException("Employee not found");
        }

        Set<Object> leaveKeys = redisLeaveService.getEmployeeLeaveKeys(employeeId);
        List<LeaveRequestDto> leaveRequests = new ArrayList<>();

        if(!leaveKeys.isEmpty()){
            for(Object leaveKey : leaveKeys){
                LeaveRequestDto leaveRequestDto = redisLeaveService.getLeaveRequest(leaveKey.toString());
                leaveRequests.add(leaveRequestDto);
            }
        }
        return leaveRequests;
    }

    @Transactional
    public void approveLeave(String leaveKey){

        //Retrieve Leave request from Redis
        LeaveRequestDto leaveRequestDto = redisLeaveService.getLeaveRequest(leaveKey);

        if(leaveRequestDto == null){
            throw new RuntimeException("Leave request not found (Redis)");
        }

        Employee employee = employeeRepository.findById(leaveRequestDto.getEmployeeId()).orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveRequest approvedRequest = new LeaveRequest();
        approvedRequest.setEmployee(employee);
        approvedRequest.setStartDate(leaveRequestDto.getStartDate());
        approvedRequest.setEndDate(leaveRequestDto.getEndDate());
        approvedRequest.setReason(leaveRequestDto.getReason());
        approvedRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);

        leaveRequestRepository.save(approvedRequest);
        redisLeaveService.deleteLeaveRequest(leaveKey);
        redisLeaveService.removeFromEmployeeIndex(approvedRequest.getEmployee().getId(), leaveKey);
    }

    public LeaveRequest getLeaveRequest(Long leaveRequestId) {
        return leaveRequestRepository.findById(leaveRequestId).orElseThrow(() -> new RuntimeException("Leave not found."));
    }

    public List<LeaveRequest> getApprovedLeavesByEmployee(Long userId, int year) {

        return leaveRequestRepository.findAllByYearAndEmployee(year, userId);

    }
}
