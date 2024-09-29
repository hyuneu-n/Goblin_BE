package goblin.app.Category.controller;

import goblin.app.Category.model.dto.request.categoryEditRequestDto;
import goblin.app.Category.model.dto.request.categorySaveRequestDto;
import goblin.app.Category.model.dto.response.categoryResponseDto;
import goblin.app.Category.service.CategoryService;
import goblin.app.User.model.entity.User;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController("/calendar/user/category")
@NoArgsConstructor
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @PostMapping("/save")
    public ResponseEntity<categoryResponseDto> save(@RequestBody @Valid categorySaveRequestDto requestDto, @AuthenticationPrincipal User user) {
        categoryResponseDto responseDto = categoryService.save(requestDto,user);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/edit")
    public ResponseEntity<categoryResponseDto> edit(@RequestBody @Valid categoryEditRequestDto requestDto, @AuthenticationPrincipal User user) {
        categoryResponseDto responseDto = categoryService.edit(requestDto,user);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<String> delete(@PathVariable Long categoryId, @AuthenticationPrincipal User user) {
        categoryResponseDto responseDto = categoryService.deleteById(categoryId,user);
        return ResponseEntity.status(HttpStatus.OK).body("성공적으로 삭제되었습니다");
    }

}
