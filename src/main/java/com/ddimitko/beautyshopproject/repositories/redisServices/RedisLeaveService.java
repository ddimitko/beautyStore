package com.ddimitko.beautyshopproject.repositories.redisServices;

import com.ddimitko.beautyshopproject.Dto.calendar.LeaveRequestDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class RedisLeaveService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration ttl = Duration.ofDays(1);

    public RedisLeaveService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveLeaveRequest(String leaveKey, LeaveRequestDto leaveRequestDto) {

        redisTemplate.opsForValue().set(leaveKey, leaveRequestDto, ttl);
    }

    public LeaveRequestDto getLeaveRequest(String leaveKey) {
        return (LeaveRequestDto) redisTemplate.opsForValue().get(leaveKey);
    }

    public void addToEmployeeIndex(Long employeeId, String leaveKey){
        String employeeIndexKey = "employee:" + employeeId + ":leaveKeys";
        redisTemplate.opsForSet().add(employeeIndexKey, leaveKey);
    }

    public void removeFromEmployeeIndex(Long employeeId, String leaveKey){
        String employeeIndexKey = "employee:" + employeeId + ":leaveKeys";
        redisTemplate.opsForSet().remove(employeeIndexKey, leaveKey);
    }

    public Set<Object> getEmployeeLeaveKeys(Long employeeId) {
        String employeeIndexKey = "employee:" + employeeId + ":leaveKeys";
        return redisTemplate.opsForSet().members(employeeIndexKey);
    }

    public void deleteLeaveRequest(String leaveKey) {
        redisTemplate.delete(leaveKey);
    }
}
