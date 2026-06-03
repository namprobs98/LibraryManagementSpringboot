package com.librarymanagement.service.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.librarymanagement.entity.Member;

public interface MemberService {

    Page<Member> getAllMembers(int page, int size, String sort, boolean asc);

    Page<Member> getAllMembers(int page, int size);

    Page<Member> getFirstPage();

    List<Member> findAllForExport();

    boolean addMember(Member member);

    Optional<Member> getMemberById(String id);

    boolean updateMember(String id, String name, String email, String phone);

    boolean deleteMember(String id);
}
