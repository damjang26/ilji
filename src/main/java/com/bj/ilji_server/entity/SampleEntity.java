package com.bj.ilji_server.entity;

import jakarta.persistence.*; // 엔티티 관련 어노테이션 전부
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "USERS_TEST")
public class SampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ✅ 주의: Oracle Autonomous DB는 IDENTITY 전략을 완벽히 지원하지 않을 수 있음.
    //        연습 목적이면 OK, 실무에서는 SEQUENCE 권장 (아래 참고)

    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", length = 100)
    private String name;
}