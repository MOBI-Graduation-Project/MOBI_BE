package com.mobi.mobi.mydata.repository;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.mydata.entity.MyData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyDataRepository extends JpaRepository<MyData, Long> {

    // 특정 사용자의 모든 마이데이터 조회
    List<MyData> findAllByMember(Member member);

    // 사용자와 주식 코드로 데이터 존재 여부 확인 (중복 등록 방지)
    boolean existsByMemberAndStockCode(Member member, String stockCode);

    // 마이데이터 ID와 사용자 ID로 데이터 조회 (수정/삭제 시 소유권 확인)
    Optional<MyData> findByIdAndMemberId(Long id, Long memberId);
}
