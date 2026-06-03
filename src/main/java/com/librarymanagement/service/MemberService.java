package com.librarymanagement.service;

import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    /** Kích thước trang mặc định. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final MemberRepository memberRepository;
    private final StorageService storageService;

    public MemberService(MemberRepository memberRepository, StorageService storageService) {
        this.memberRepository = memberRepository;
        this.storageService = storageService;
    }

    // ── Phân trang — dùng cho REST API ───────────────────────────────────────

    /**
     * Lấy danh sách thành viên CÓ PHÂN TRANG.
     *
     * @param page trang hiện tại (0-indexed)
     * @param size số bản ghi mỗi trang (tối đa 100)
     * @param sort tên cột sắp xếp (id | name | email | phone)
     * @param asc  true = tăng dần, false = giảm dần
     */
    public Page<Member> getAllMembers(int page, int size, String sort, boolean asc) {
        size = Math.min(size, 100);
        Sort.Direction dir = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return memberRepository.findAll(pageable);
    }

    /**
     * Shorthand với default sort theo id tăng dần.
     */
    public Page<Member> getAllMembers(int page, int size) {
        return getAllMembers(page, size, "id", true);
    }

    // ── Console helper ────────────────────────────────────────────────────────

    /**
     * Dùng cho ConsoleRunner: chỉ lấy trang đầu tiên.
     */
    public Page<Member> getFirstPage() {
        return getAllMembers(0, DEFAULT_PAGE_SIZE, "id", true);
    }

    // ── Export — CHỈ dùng nội bộ (StorageService) ────────────────────────────

    /**
     * ⚠️ INTERNAL USE ONLY — chỉ gọi từ StorageService khi export.
     */
    public List<Member> findAllForExport() {
        return memberRepository.findAll(Sort.by("id"));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Transactional
    public boolean addMember(Member member) {
        if (memberRepository.existsById(member.getId())) return false;
        memberRepository.save(member);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    public Optional<Member> getMemberById(String id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public boolean updateMember(String id, String name, String email, String phone) {
        Optional<Member> found = memberRepository.findById(id);
        if (found.isEmpty()) return false;
        Member member = found.get();
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        memberRepository.save(member);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    @Transactional
    public boolean deleteMember(String id) {
        if (!memberRepository.existsById(id)) return false;
        memberRepository.deleteById(id);
        storageService.persistCurrentIfNeeded();
        return true;
    }
}
