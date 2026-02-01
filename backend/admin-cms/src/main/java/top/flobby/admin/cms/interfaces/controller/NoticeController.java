package top.flobby.admin.cms.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.cms.application.NoticeService;
import top.flobby.admin.cms.interfaces.dto.NoticeDTO;
import top.flobby.admin.cms.interfaces.query.NoticeQuery;
import top.flobby.admin.cms.interfaces.vo.NoticeVO;
import top.flobby.admin.common.annotation.RequiresPermission;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.system.domain.repository.UserRepository;

/**
 * 公告管理控制器
 */
@Tag(name = "公告管理", description = "通知公告管理相关接口")
@RestController
@RequestMapping("/api/cms/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final UserRepository userRepository;

    @Operation(summary = "分页查询公告列表")
    @PostMapping("/list")
    @RequiresPermission("cms:notice:list")
    public Result<PageResult<NoticeVO>> listNotices(@RequestBody NoticeQuery query) {
        PageResult<NoticeVO> result = noticeService.listNotices(query);
        return Result.success(result);
    }

    @Operation(summary = "获取用户未读公告列表")
    @GetMapping("/unread")
    public Result<PageResult<NoticeVO>> listUnreadNotices(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        PageResult<NoticeVO> result = noticeService.listUnreadNotices(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取公告详情")
    @GetMapping("/{id}")
    @RequiresPermission("cms:notice:list")
    public Result<NoticeVO> getNoticeById(@PathVariable Long id) {
        NoticeVO notice = noticeService.getNoticeById(id);
        return Result.success(notice);
    }

    @Operation(summary = "新增公告")
    @PostMapping
    @RequiresPermission("cms:notice:add")
    public Result<Long> createNotice(@Valid @RequestBody NoticeDTO dto) {
        Long id = noticeService.createNotice(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    @RequiresPermission("cms:notice:edit")
    public Result<Void> updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeDTO dto) {
        noticeService.updateNotice(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    @RequiresPermission("cms:notice:delete")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }

    @Operation(summary = "发布公告")
    @PutMapping("/{id}/publish")
    @RequiresPermission("cms:notice:publish")
    public Result<Void> publishNotice(@PathVariable Long id) {
        noticeService.publishNotice(id);
        return Result.success();
    }

    @Operation(summary = "撤回公告")
    @PutMapping("/{id}/revoke")
    @RequiresPermission("cms:notice:publish")
    public Result<Void> revokeNotice(@PathVariable Long id) {
        noticeService.revokeNotice(id);
        return Result.success();
    }

    @Operation(summary = "标记公告已读")
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        noticeService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取公告已读数量")
    @GetMapping("/{id}/read-count")
    @RequiresPermission("cms:notice:list")
    public Result<Long> getReadCount(@PathVariable Long id) {
        long count = noticeService.getReadCount(id);
        return Result.success(count);
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"))
                .getId();
    }
}
