package goblin.app.Calendar.controller;


import goblin.app.Calendar.model.dto.request.uCalEditRequestDto;
import goblin.app.Calendar.model.dto.request.uCalSaveRequestDto;
import goblin.app.Calendar.model.dto.response.uCalResponseDto;
import goblin.app.Calendar.model.entity.UserCalendar;
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
    public ResponseEntity<uCalResponseDto> save(@RequestBody uCalSaveRequestDto requestDto, User user) {
         uCalResponseDto responseDto = userCalService.save(requestDto, user);
         return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/edit")
    public ResponseEntity<uCalResponseDto> edit(@RequestBody uCalEditRequestDto requestDto, User user) {
        uCalResponseDto responseDto = userCalService.edit(requestDto,user);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /*
    @DeleteMapping("/delete/{scheduleId}")
    public ResponseEntity<uCalResponseDto> delete(@PathVariable Long scheduleId, User user) {

    }
    @GetMapping("/view-month")
    public ResponseEntity<List<uCalResponseDto>> viewByMonth(@RequestParam int year, @RequestParam int month, User user) {
        List<uCalResponseDto> calList = userCalService.viewByMonth(year, month, user);
    }

    @GetMapping("/view-day")
    public ResponseEntity<uCalResponseDto> viewByDay(@RequestParam int year, @RequestParam int month) {

    }
    */


}
