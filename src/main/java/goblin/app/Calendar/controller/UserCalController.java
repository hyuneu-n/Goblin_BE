package goblin.app.Calendar.controller;


import goblin.app.Calendar.model.dto.request.uCalEditRequestDto;
import goblin.app.Calendar.model.dto.request.uCalSaveRequestDto;
import goblin.app.Calendar.model.dto.response.uCalResponseDto;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import goblin.app.Calendar.service.UserCalService;
import goblin.app.User.model.entity.User;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/calendar/user")
@NoArgsConstructor
public class UserCalController {

    @Autowired
    private UserCalService userCalService;

    // 로그인 구현 후 수정
    @PostMapping("/save")
    public ResponseEntity<uCalResponseDto> save(@RequestBody @Valid uCalSaveRequestDto requestDto, User user) {
         uCalResponseDto responseDto = userCalService.save(requestDto, user);
         return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/edit")
    public ResponseEntity<uCalResponseDto> edit(@RequestBody @Valid uCalEditRequestDto requestDto,@AuthenticationPrincipal User user) {
        uCalResponseDto responseDto = userCalService.edit(requestDto,user);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @DeleteMapping("/delete/{scheduleId}")
    public ResponseEntity<uCalResponseDto> delete(@PathVariable Long scheduleId,@AuthenticationPrincipal User user) {
        uCalResponseDto responseDto = userCalService.deleteById(scheduleId, user);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
    @GetMapping("/view-month")
    public ResponseEntity<List<uCalResponseDto>> viewByMonth(@RequestParam int year, @RequestParam int month,@AuthenticationPrincipal User user) {
        List<uCalResponseDto> calList = userCalService.viewByMonth(year, month, user);
        return ResponseEntity.status(HttpStatus.OK).body(calList);
    }

    @GetMapping("/view-day")
    public ResponseEntity<List<uCalResponseDto>> viewByDay(@RequestParam int year, @RequestParam int month,@RequestParam int day,  @AuthenticationPrincipal User user) {
        List<uCalResponseDto> calList = userCalService.viewByDay(year,month,day,user);
        return ResponseEntity.status(HttpStatus.OK).body(calList);
    }


}