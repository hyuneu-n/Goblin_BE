// package goblin.app.Calendar.model.dto.request;
//
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import goblin.app.Calendar.model.entity.UserCalendar;
// import goblin.app.Category.model.entity.Category;
// import goblin.app.User.model.entity.User;
//
// @Getter
// @NoArgsConstructor
// public class uCalSaveRequestDto {
//
//  Long userId;
//  Long categoryId;
//
//  String title;
//  String note;
//
//  @Builder
//  public UserCalendar toEntity(User user, Category category) {
//    return UserCalendar.builder().title(title).user(user).category(category).note(note).build();
//  }
// }
