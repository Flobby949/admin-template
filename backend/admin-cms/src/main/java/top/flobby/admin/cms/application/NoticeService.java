package top.flobby.admin.cms.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.flobby.admin.cms.domain.entity.Notice;
import top.flobby.admin.cms.domain.entity.NoticeRead;
import top.flobby.admin.cms.domain.repository.NoticeReadRepository;
import top.flobby.admin.cms.domain.repository.NoticeRepository;
import top.flobby.admin.cms.interfaces.dto.NoticeDTO;
import top.flobby.admin.cms.interfaces.query.NoticeQuery;
import top.flobby.admin.cms.interfaces.vo.NoticeVO;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 公告服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeReadRepository noticeReadRepository;
    private final UserRepository userRepository;

    /**
     * 分页查询公告（管理端）
     */
    public PageResult<NoticeVO> listNotices(NoticeQuery query) {
        Pageable pageable = PageRequest.of(
                query.getPageNum() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Specification<Notice> spec = buildSpecification(query);
        Page<Notice> page = noticeRepository.findAll(spec, pageable);

        List<NoticeVO> list = page.getContent().stream()
                .map(this::toNoticeVO)
                .toList();

        return new PageResult<>(list, page.getTotalElements(), (long) query.getPageNum(), (long) query.getPageSize());
    }

    /**
     * 获取用户未读公告列表
     */
    public PageResult<NoticeVO> listUnreadNotices(Long userId, int pageNum, int pageSize) {
        Set<Long> readNoticeIds = noticeReadRepository.findReadNoticeIdsByUserId(userId);

        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "publishTime")
        );

        Specification<Notice> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), Notice.STATUS_PUBLISHED));
            if (!readNoticeIds.isEmpty()) {
                predicates.add(cb.not(root.get("id").in(readNoticeIds)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Notice> page = noticeRepository.findAll(spec, pageable);

        List<NoticeVO> list = page.getContent().stream()
                .map(n -> {
                    NoticeVO vo = toNoticeVO(n);
                    vo.setRead(false);
                    return vo;
                })
                .toList();

        return new PageResult<>(list, page.getTotalElements(), (long) pageNum, (long) pageSize);
    }

    /**
     * 获取公告详情
     */
    public NoticeVO getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        return toNoticeVO(notice);
    }

    /**
     * 创建公告
     */
    @Transactional
    public Long createNotice(NoticeDTO dto) {
        User currentUser = getCurrentUser();

        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setStatus(Notice.STATUS_DRAFT);
        notice.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        notice.setPublisherId(currentUser.getId());
        notice.setDeptId(dto.getDeptId());
        notice.setDeleted(0);
        notice.setCreateBy(currentUser.getUsername());

        Notice saved = noticeRepository.save(notice);
        log.info("创建公告成功: id={}, title=", saved.getId(), saved.getTitle());
        return saved.getId();
    }

    /**
     * 更新公告
     */
    @Transactional
    public void updateNotice(Long id, NoticeDTO dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        if (dto.getSortOrder() != null) {
            notice.setSortOrder(dto.getSortOrder());
        }
        if (dto.getDeptId() != null) {
            notice.setDeptId(dto.getDeptId());
        }
        notice.setUpdateBy(getCurrentUsername());

        noticeRepository.save(notice);
        log.info("更新公告成功: id={}", id);
    }

    /**
     * 删除公告
     */
    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        noticeRepository.deleteById(id);
        log.info("删除公告成功: id={}", id);
    }

    /**
     * 发布公告
     */
    @Transactional
    public void publishNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        notice.publish();
        noticeRepository.save(notice);
        log.info("公告发布成功: id={}", id);
    }

    /**
     * 撤回公告
     */
    @Transactional
    public void revokeNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        notice.revoke();
        noticeRepository.save(notice);
        log.info("公告撤回: id={}", id);
    }

    /**
     * 标记已读
     */
    @Transactional
    public void markAsRead(Long noticeId, Long userId) {
        if (noticeReadRepository.existsByNoticeIdAndUserId(noticeId, userId)) {
            return;
        }

        NoticeRead noticeRead = new NoticeRead();
        noticeRead.setNoticeId(noticeId);
        noticeRead.setUserId(userId);
        noticeRead.setReadTime(LocalDateTime.now());
        noticeReadRepository.save(noticeRead);
        log.info("标记公告已读: noticeId={}, userId={}", noticeId, userId);
    }

    /**
     * 获取公告已读数量
     */
    public long getReadCount(Long noticeId) {
        return noticeReadRepository.countByNoticeId(noticeId);
    }

    private Specification<Notice> buildSpecification(NoticeQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(query.getTitle())) {
                predicates.add(cb.like(root.get("title"), "%" + query.getTitle() + "%"));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private NoticeVO toNoticeVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setStatus(notice.getStatus());
        vo.setPublishTime(notice.getPublishTime());
        vo.setRevokeTime(notice.getRevokeTime());
        vo.setDeptId(notice.getDeptId());
        vo.setPublisherId(notice.getPublisherId());
        vo.setSortOrder(notice.getSortOrder());
        vo.setCreateTime(notice.getCreateTime());
        vo.setCreateBy(notice.getCreateBy());
        vo.setReadCount(noticeReadRepository.countByNoticeId(notice.getId()));
        return vo;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
