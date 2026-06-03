package com.librarymanagement.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librarymanagement.dto.ApiResponse;
import com.librarymanagement.dto.MemberRequest;
import com.librarymanagement.dto.PageResponse;
import com.librarymanagement.entity.Member;
import com.librarymanagement.service.services.MemberService;

import jakarta.validation.Valid;

/**
 * MemberController — REST API cho quản lý thành viên.
 *
 * Base URL: /api/members
 *
 * Tất cả các endpoint trả danh sách đều hỗ trợ phân trang qua query params:
 *   ?page=0&size=20&sort=name&asc=true
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * GET /api/members              - Danh sách thành viên (phân trang)
     * GET /api/members?page=1&size=50&sort=name&asc=false
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Member>>> getAllMembers(
            @RequestParam(defaultValue = "0")    int     page,
            @RequestParam(defaultValue = "20")   int     size,
            @RequestParam(defaultValue = "id")   String  sort,
            @RequestParam(defaultValue = "true") boolean asc) {

        Page<Member> memberPage = memberService.getAllMembers(page, size, sort, asc);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(memberPage)));
    }

    /**
     * GET /api/members/{id} - Lấy thành viên theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Member>> getMemberById(@PathVariable String id) {
        Optional<Member> member = memberService.getMemberById(id);
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Member not found: " + id));
        }
        return ResponseEntity.ok(ApiResponse.ok(member.get()));
    }

    /**
     * POST /api/members - Thêm thành viên mới
     * Body: { "id":"M004", "name":"...", "email":"...", "phone":"..." }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Member>> addMember(@Valid @RequestBody MemberRequest req) {
        Member member = new Member(req.getId(), req.getName(), req.getEmail(), req.getPhone());
        boolean success = memberService.addMember(member);
        if (!success) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Member ID already exists: " + req.getId()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Member added successfully!", member));
    }

    /**
     * PUT /api/members/{id} - Cập nhật thành viên
     * Body: { "name":"...", "email":"...", "phone":"..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            @PathVariable String id,
            @RequestBody MemberRequest req) {
        boolean success = memberService.updateMember(id, req.getName(), req.getEmail(), req.getPhone());
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Member not found: " + id));
        }
        return ResponseEntity.ok(ApiResponse.ok("Member updated successfully!", null));
    }

    /**
     * DELETE /api/members/{id} - Xóa thành viên
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable String id) {
        boolean success = memberService.deleteMember(id);
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Member not found: " + id));
        }
        return ResponseEntity.ok(ApiResponse.ok("Member deleted successfully!", null));
    }
}
