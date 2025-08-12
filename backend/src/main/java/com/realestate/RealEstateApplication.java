package com.realestate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 不動産管理システムのメインアプリケーションクラス
 * 
 * このクラスは不動産管理システムのエントリーポイントです。
 * Spring Bootアプリケーションとして起動し、不動産の物件、顧客、契約、取引の管理機能を提供します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@SpringBootApplication
@EnableScheduling
public class RealEstateApplication {

    /**
     * アプリケーションのメインメソッド
     * 
     * Spring Bootアプリケーションを起動し、不動産管理システムのサービスを開始します。
     * 
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(RealEstateApplication.class, args);
    }
}
