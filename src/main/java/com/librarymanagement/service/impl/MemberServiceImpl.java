package com.librarymanagement.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.MemberRepository;
import com.librarymanagement.service.services.MemberService;
import com.librarymanagement.service.services.StorageService;

@Service
public class MemberServiceImpl implements MemberService {

    /** Kích thước trang mặc định. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final MemberRepository memberRepository;
    private final StorageService storageService;

    public MemberServiceImpl(MemberRepository memberRepository, StorageService storageService) {
        this.memberRepository = memberRepository;
        this.storageService = storageService;
    }

    @Override
    public Page<Member> getAllMembers(int page, int size, String sort, boolean asc) {
        size = Math.min(size, 100);
        Sort.Direction dir = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return memberRepository.findAll(pageable);
    }

    @Override
    public Page<Member> getAllMembers(int page, int size) {
        return getAllMembers(page, size, "id", true);
    }

    @Override
    public Page<Member> getFirstPage() {
        return getAllMembers(0, DEFAULT_PAGE_SIZE, "id", true);
    }

    @Override
    public List<Member> findAllForExport() {
        return memberRepository.findAll(Sort.by("id"));
    }

    @Override
    @Transactional
    public boolean addMember(Member member) {
        if (memberRepository.existsById(member.getId())) return false;
        memberRepository.save(member);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    @Override
    public Optional<Member> getMemberById(String id) {
        return memberRepository.findById(id);
    }

    @Override
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

    @Override
    @Transactional
    public boolean deleteMember(String id) {
        if (!memberRepository.existsById(id)) return false;
        memberRepository.deleteById(id);
        storageService.persistCurrentIfNeeded();
        return true;
    }
}
