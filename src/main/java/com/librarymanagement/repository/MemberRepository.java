package com.librarymanagement.repository;

import com.librarymanagement.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    // JpaRepository cung cấp sẵn: findAll, findById, save, deleteById, existsById, count
}
