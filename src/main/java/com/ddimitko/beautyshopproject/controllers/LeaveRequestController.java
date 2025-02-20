package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.calendar.LeaveRequestDto;
import com.ddimitko.beautyshopproject.entities.calendar.LeaveRequest;
import com.ddimitko.beautyshopproject.services.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveService leaveService;

    public LeaveRequestController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }


    @PostMapping("/request")
    public ResponseEntity<Void> requestLeave(@RequestBody LeaveRequestDto leaveRequestDto) {
        leaveService.requestLeave(leaveRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto>> getLeaveRequestsByEmployee(@PathVariable long employeeId) {
        List<LeaveRequestDto> leaveRequests = leaveService.getLeaveRequestsFromRedis(employeeId);
        return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
    }

    @PutMapping("/approve")
    public ResponseEntity<LeaveRequest> approveLeave(@RequestParam String leaveKey) {
        leaveService.approveLeave(leaveKey);
        return ResponseEntity.ok().build();
    }
}
